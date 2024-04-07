package star.api.gateway.filter;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import jodd.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import star.api.common.ErrorCode;
import star.api.exception.BusinessException;
import star.api.gateway.utils.LogUtils;
import star.api.model.entity.InterfaceInfo;
import star.api.model.entity.User;
import star.api.sdk.utils.SignUtils;
import star.api.service.InnerInterfaceInfoService;
import star.api.service.InnerUserInterfaceInfoService;
import star.api.service.InnerUserService;
import star.api.utils.ThrowUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static star.api.gateway.constant.GatewayConstant.*;

/**
 * @author 千树星雨
 * @date 2024 年 04 月 06 日
 */
@Component
@Data
@Slf4j
public class SignGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 1. 请求日志打印
                ServerHttpRequest request = exchange.getRequest();
                LogUtils.logPrint(request);

                //2. 访问控制 - 黑白名单
                String IP_ADDRESS = Objects.requireNonNull(request.getLocalAddress()).getHostString();
//                if (!IP_WHITE_LIST.contains(IP_ADDRESS)) {
//                    return handleNoAuth(exchange.getResponse());
//                }

                //3. 用户鉴权
                HttpHeaders headers = request.getHeaders();
                String accessKey = headers.getFirst("accessKey");
                String timestamp = headers.getFirst("timestamp");
                String nonce = headers.getFirst("nonce");
                String sign = headers.getFirst("sign");
                String body = URLUtil.decode(headers.getFirst("body"), CharsetUtil.CHARSET_UTF_8);
                String method = headers.getFirst("method");

//                if (StringUtil.isEmpty(nonce)
//                        || StringUtil.isEmpty(sign)
//                        || StringUtil.isEmpty(timestamp)
//                        || StringUtil.isEmpty(method)) {
//                    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求头参数不完整！");
//                }

                //通过 accessKey 查询用户是否存在
                User invokeUser = innerUserService.getInvokeUser(accessKey);
                if (invokeUser == null) {
                    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "accessKey 不合法！");
                }
                //锁 同一个用户只能串行调用接口
                Long userId = invokeUser.getId();
                RLock lock = redissonClient.getLock(LOCK_GATEWAY_USER + userId);
                try {
                    if (lock.tryLock(0, LOCK_REGAIN_MAX_TIME_SEC, TimeUnit.MILLISECONDS)) {
                        //判断随机数是否存在 防止重放攻击
                        String nonceCache = (String) redisTemplate.opsForValue().get(nonce);
                        if (StringUtil.isNotBlank(nonceCache)) {
                            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求重复！");
                        }
                        redisTemplate.opsForValue().set(nonce, 1, 3, TimeUnit.MILLISECONDS);
                        //时间戳和当前时间不超过五分钟（ 300000毫秒
                        long currentTimeMillis = System.currentTimeMillis() / 1000;
                        long difference = currentTimeMillis - Long.parseLong(timestamp);
                        if (Math.abs(difference) > 300000) {
                            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求超时！");
                        }

                        //签名认证
                        String serverSign = SignUtils.getSign(body, invokeUser.getSecretKey());
                        ThrowUtils.throwIf(!sign.equals(serverSign), ErrorCode.OPERATION_ERROR, "签名认证失败");

                        //查询接口是否存在
                        String path = request.getPath().value();
                        InterfaceInfo interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
                        if (interfaceInfo == null) {
                            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "接口不存在！");
                        }

                        //请求转发,调用接口
                        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
                    }
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求超时");
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
                log.info("打印");
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请稍后再试");
            }
        }, 1);
    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            // 从交换机拿到原始response
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓冲区工厂 拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到状态码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        // 对象是响应式的
                        if (body instanceof Flux) {
                            // 我们拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里面写数据
                            // 拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // 调用成功，接口调用次数 + 1
                                try {
                                    innerUserInterfaceInfoService.invokeCount(userId, interfaceInfoId);
                                } catch (Exception e) {
                                    log.error("invokeInterfaceCount error", e);
                                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新接口调用次数失败");
                                }
                                // data从这个content中读取
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);// 释放掉内存
                                // 6.构建日志
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);// data
                                rspArgs.add(data);
                                log.info("<--- status:{} data:{}"// data
                                        , rspArgs.toArray());// log.info("<-- {} {}", originalResponse.getStatusCode(), data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8.调用失败返回错误状态码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //流量染色
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header(DYE_DATA_HEADER, DYE_DATA_VALUE)
                        .build();

                ServerWebExchange serverWebExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .response(decoratedResponse)
                        .build();

                log.info("{}", exchange.getRequest().getURI());

                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            // 降级处理返回数据
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }

    }

    /**
     * 无权限异常处理
     * @param response
     * @return
     */
    private Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    /**
     * 调用异常处理
     * @param response
     * @return
     */
    private Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

}
