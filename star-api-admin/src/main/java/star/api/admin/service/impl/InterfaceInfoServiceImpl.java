package star.api.admin.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import star.api.admin.config.GatewayConfig;
import star.api.admin.config.ThreadPoolConfig;
import star.api.admin.exception.BusinessException;
import star.api.admin.exception.ThrowUtils;
import star.api.admin.factory.ApiClientFactory;
import star.api.admin.mapper.InterfaceInfoMapper;
import star.api.admin.service.InterfaceInfoService;
import star.api.admin.service.UserInterfaceInfoService;
import star.api.admin.service.UserService;
import star.api.admin.utils.SqlUtils;
import star.api.common.DeleteRequest;
import star.api.common.ErrorCode;
import star.api.common.IdRequest;
import star.api.common.RedisData;
import star.api.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import star.api.model.entity.InterfaceInfo;
import star.api.model.entity.User;
import star.api.model.entity.UserInterfaceInfo;
import star.api.model.vo.InterfaceInfoVO;
import star.api.model.vo.RequestParamsRemarkVO;
import star.api.model.vo.ResponseParamsRemarkVO;
import star.api.model.vo.UserVO;
import star.api.sdk.client.StarApiClient;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static star.api.admin.constant.RedisConstant.*;
import static star.api.constant.CommonConstant.SORT_ORDER_ASC;
import static star.api.constant.CommonConstant.SORT_ORDER_DESC;
import static star.api.model.enums.InterfaceInfoStatusEnum.OFFLINE;
import static star.api.model.enums.InterfaceInfoStatusEnum.ONLINE;

/**
 * @author 千树星雨
 * @description 针对表【interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2023-11-03 14:07:08
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {

    @Resource
    private UserService userService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private GatewayConfig gatewayConfig;

    private volatile boolean isCacheRebuilding = false;

    /**
     * 添加接口
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @Override
    public Long addInterfaceInfo(InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(interfaceInfoAddRequest == null, ErrorCode.PARAMS_ERROR);
        //填充信息
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        this.validInterfaceInfo(interfaceInfo);
        interfaceInfo.setUserId(userService.getLoginUser(request).getId());
        interfaceInfo.setRequestParams(JSONUtil.toJsonStr(interfaceInfoAddRequest.getRequestParams()));
        interfaceInfo.setResponseHeader(JSONUtil.toJsonStr(interfaceInfoAddRequest.getResponseParamsRemark()));
        interfaceInfo.setStatus(0);
        //存放数据
        boolean save = this.save(interfaceInfo);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);

        return interfaceInfo.getId();
    }

    /**
     * 删除接口
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteInterfaceInfo(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 参数校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(deleteRequest.getId());
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        User user = userService.getLoginUser(request);
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return this.removeById(deleteRequest.getId());
    }

    /**
     * 更新接口
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateInterfaceInfo(InterfaceInfoUpdateRequest interfaceInfoUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(interfaceInfoUpdateRequest.getId());
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        //填充
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        interfaceInfo.setRequestParamsRemark(JSONUtil.toJsonStr(interfaceInfoUpdateRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(JSONUtil.toJsonStr(interfaceInfoUpdateRequest.getResponseParamsRemark()));

        //校验
        this.validInterfaceInfo(interfaceInfo);
        // 仅本人或管理员可修改
        User user = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request), ErrorCode.NO_AUTH_ERROR);
        return this.updateById(interfaceInfo);
    }

    /**
     * 查询接口
     *
     * @param id
     * @return
     */
    @Override
    public InterfaceInfoVO getInterfaceInfoVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        return this.getInterfaceInfoVO(this.getById(id));
    }

    /**
     * 获取接口（ VO
     *
     * @param interfaceInfo
     * @return
     */
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo) {
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.PARAMS_ERROR);
        InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);

        //获取和填充
        UserVO userVO = userService.getUserVOById(interfaceInfo.getUserId());
        List<RequestParamsRemarkVO> requestParamsRemarkVOList = this.getRequestParamsRemarkVOList(interfaceInfo.getRequestParamsRemark());
        List<ResponseParamsRemarkVO> responseParamsRemarkVOList = this.getResponseParamsRemarkVOList(interfaceInfo.getResponseParamsRemark());

        interfaceInfoVO.setUser(userVO);
        interfaceInfoVO.setRequestParamsRemark(requestParamsRemarkVOList);
        interfaceInfoVO.setResponseParamsRemark(responseParamsRemarkVOList);

        return interfaceInfoVO;
    }

    /**
     * 获取接口列表（ VO
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public List<InterfaceInfoVO> listInterfaceInfoVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        // 参数校验并转换
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        // 查询
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = this.list(queryWrapper);
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return interfaceInfoVOList;
    }

    /**
     * 获取接口列表（分页
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<InterfaceInfo> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            return null;
        }
        //查询是否有缓存数据
        long current = interfaceInfoQueryRequest.getCurrent();
        String cacheKey = INTERFACE_QUERY_KEY + current;
        String cache = (String) redisTemplate.opsForValue().get(cacheKey);
        //为空 代表没有访问过数据库（访问过数据库没有数据会缓存 “” 空值
        //获取互斥锁
        String lockKey = LOCK_INTERFACE_PAGE + current;
        RLock lock = redissonClient.getLock(lockKey);
        if (cache == null) {
            if (lock.tryLock()) {
                try {
                    //双重检测
                    cache = (String) redisTemplate.opsForValue().get(cacheKey);
                    if (cache == null) {
                        //缓存数据
                        Page<InterfaceInfo> interfaceInfoPage = this.cacheInterfaceInfo(interfaceInfoQueryRequest, INTERFACE_QUERY_TTL);
                        return interfaceInfoPage;
                    }
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "缓存失败");
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            return null;
        }
        //数据库有数据且已缓存
        RedisData<Page<InterfaceInfo>> redisData = JSONUtil.toBean(cache, new TypeReference<RedisData<Page<InterfaceInfo>>>() {
        }, false);
        Page<InterfaceInfo> interfaceInfoPage = redisData.getData();
        LocalDateTime expireTime = redisData.getExpireTime();
        //缓存数据未过期，返回
        if (expireTime.isAfter(LocalDateTime.now())) {
            return interfaceInfoPage;
        }
        //缓存过期，缓存重建
        // 1. 上锁
        if (lock.tryLock()) {
            try {
                //设立标志位，缓存过期且没有其他线程正在重建缓存
                // 则重建缓存（防止因异步执行（缓存重建前锁被提前释放）导致的多次重建
                if (!isCacheRebuilding) {
                    //双重检测（防止重建后锁一释放缓存被多次重建
                    cache = (String) redisTemplate.opsForValue().get(cacheKey);
                    redisData = JSONUtil.toBean(cache, new TypeReference<RedisData<Page<InterfaceInfo>>>() {
                    }, false);
                    expireTime = redisData.getExpireTime();
                    if (expireTime.isAfter(LocalDateTime.now())) {
                        return redisData.getData();
                    }
                    // 异步执行，重建缓存
                    isCacheRebuilding = true;
                    threadPoolExecutor.execute(() -> {
                        this.cacheInterfaceInfo(interfaceInfoQueryRequest, INTERFACE_QUERY_TTL);
                        isCacheRebuilding = false; // 缓存重建完成，清除标志位
                    });
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        }
        // 2. 返回旧数据
        return interfaceInfoPage;

    }

    @Transactional
    public Page<InterfaceInfo> cacheInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest, Long expireSeconds) {
        //查询数据
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("createTime");
        interfaceInfoQueryRequest.setSortOrder(SORT_ORDER_DESC);

        Page<InterfaceInfo> interfaceInfoPage = this.page(new Page<>(current, size), this.getQueryWrapper(interfaceInfoQueryRequest));

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //如果数据库无该数据 缓存空值 防止缓存穿透
        RedisData<Page<InterfaceInfo>> redisData=null;
        if (interfaceInfoPage == null) {
            //数据库没有数据缓存空值
            redisData = new RedisData<>(null, LocalDateTime.now().plusSeconds(1L));
        } else {
            //缓存数据,采用逻辑过期的方式，Key主键为页数
            redisData = new RedisData<>(interfaceInfoPage, LocalDateTime.now().plusSeconds(1L));
        }
        redisTemplate.opsForValue().set(INTERFACE_QUERY_KEY + current, JSONUtil.toJsonStr(redisData));

        return interfaceInfoPage;

    }

    /**
     * 获取接口列表（分页，VO
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    @Override
    public Page<InterfaceInfoVO> listInterfaceInfoVOByPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        //判空
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        if (CollectionUtils.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }

        //1.关联查询用户信息
        Set<Long> userIdSet = interfaceInfoList.stream()
                .map(InterfaceInfo::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //填充信息
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream()
                .map(interfaceInfo -> {
                    InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
                    //判断该接口是否属于该用户
                    UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                            .eq(UserInterfaceInfo::getUserId, loginUser.getId())
                            .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfo.getId())
                            .one();
                    //填充调用总数，剩余次数，接口是否为当前用户拥有
                    if (userInterfaceInfo != null) {
                        interfaceInfoVO.setIsOwnerByCurrentUser(true);
                        interfaceInfoVO.setTotalNum(userInterfaceInfo.getTotalNum());
                        interfaceInfoVO.setLeftNum(userInterfaceInfo.getLeftNum());
                    } else {
                        interfaceInfoVO.setIsOwnerByCurrentUser(false);
                    }

                    Long userId = interfaceInfo.getId();
                    //填充接口创建用户
                    User user = userIdUserListMap.getOrDefault(userId, Collections.emptyList())
                            .stream().findFirst().orElse(null);
                    interfaceInfoVO.setUser(userService.getUserVO(user));

                    //填充请求参数说明和响应参数说明
                    List<RequestParamsRemarkVO> requestParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getRequestParamsRemark()), RequestParamsRemarkVO.class);
                    List<ResponseParamsRemarkVO> responseParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getResponseParamsRemark()), ResponseParamsRemarkVO.class);
                    interfaceInfoVO.setRequestParamsRemark(requestParamsRemarkVOList);
                    interfaceInfoVO.setResponseParamsRemark(responseParamsRemarkVOList);

                    return interfaceInfoVO;
                }).collect(Collectors.toList());

        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;

    }

    /**
     * 接口发布
     *
     * @return
     */
    @Override
    public Boolean onlineInterfaceInfo(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        //判断接口是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(interfaceInfoInvokeRequest.getId());
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        //判断是否可以调用
        this.invokeInterfaceInfo(interfaceInfoInvokeRequest, request);

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(interfaceInfoInvokeRequest.getId());
        interfaceInfo.setStatus(ONLINE.getValue());

        return this.updateById(interfaceInfo);
    }

    /**
     * 接口下线
     *
     * @param idRequest 携带id
     * @return
     */
    @Override
    public Boolean offlineInterfaceInfo(IdRequest idRequest) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        //判断接口是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(idRequest.getId());
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //更新数据库
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(idRequest.getId());
        interfaceInfo.setStatus(OFFLINE.getValue());

        return this.updateById(interfaceInfo);
    }

    /**
     * 接口调用
     *
     * @return
     */
    @Override
    public Object invokeInterfaceInfo(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        //参数检验
        ThrowUtils.throwIf(interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        //判断接口是否存在
        InterfaceInfo interfaceInfo = this.getById(interfaceInfoInvokeRequest.getId());
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        //接口状态是否开放
        ThrowUtils.throwIf(!interfaceInfo.getStatus().equals(ONLINE.getValue()), ErrorCode.OPERATION_ERROR);

        //获取SDK客户端
        StarApiClient appClient = new ApiClientFactory().getApiClient(userService.getLoginUser(request));
        //设置网关地址
        appClient.setGatwayHost(gatewayConfig.getHost());

        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String requestParams = interfaceInfo.getRequestParams();

        String response = null;
        try {
            response = appClient.invokeInterface(requestParams, url, method);
            ThrowUtils.throwIf(StringUtils.isBlank(response), ErrorCode.OPERATION_ERROR);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
        }
        return response;
    }

    /**
     * 获取查询接口类
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return interfaceInfoQueryWrapper;
        }

        String searchText = interfaceInfoQueryRequest.getSearchText();
        Long id = interfaceInfoQueryRequest.getId();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String method = interfaceInfoQueryRequest.getMethod();
        Long userId = interfaceInfoQueryRequest.getUserId();
        Date createTime = interfaceInfoQueryRequest.getCreateTime();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();

        if (StringUtils.isNotBlank(searchText)) {
            interfaceInfoQueryWrapper.like("name", searchText).or().like("description", searchText);
        }
        interfaceInfoQueryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        interfaceInfoQueryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        interfaceInfoQueryWrapper.like(StringUtils.isNotBlank(method), "method", method);
        interfaceInfoQueryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        interfaceInfoQueryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        interfaceInfoQueryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        interfaceInfoQueryWrapper.eq("isDelete", false);
        interfaceInfoQueryWrapper.gt(ObjectUtils.isNotEmpty(createTime), "createTime", createTime);
        interfaceInfoQueryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(SORT_ORDER_ASC)
                , sortField);
        return interfaceInfoQueryWrapper;
    }

    /**
     * 校验接口
     *
     * @param interfaceInfo
     */
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestParams = interfaceInfo.getRequestParams();
        String host = interfaceInfo.getHost();
        String method = interfaceInfo.getMethod();


        // 由于前端页面修改和添加没有传status属性 所以在此让其默认为
        // 创建时，所有参数必须非空
        if (StringUtils.isAnyBlank(name, description, url, host, requestParams, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(name) && name.length() > 40) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名字过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
    }

    //    todo：根据用户id查询UserInfo里的接口 返回该用户只开通的接口
    @Override
    public Page<InterfaceInfoVO> listInterfaceInfoVOByCurrentId(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        Page<InterfaceInfo> interfaceInfoPage = this.listInterfaceInfoByPage(interfaceInfoQueryRequest, request);
        return this.listInterfaceInfoVOByPage(interfaceInfoPage, request);
    }

    /**
     * 获取请求参数详情 VO
     *
     * @param requestParamsRemark
     * @return
     */
    public List<RequestParamsRemarkVO> getRequestParamsRemarkVOList(String requestParamsRemark) {
        List<RequestParamsRemarkVO> requestParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(requestParamsRemark), RequestParamsRemarkVO.class);
        return requestParamsRemarkVOList;
    }

    /**
     * 获取响应参数详情 VO
     *
     * @param responseParamsRemark
     * @return
     */
    public List<ResponseParamsRemarkVO> getResponseParamsRemarkVOList(String responseParamsRemark) {
        List<ResponseParamsRemarkVO> responseParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(responseParamsRemark), ResponseParamsRemarkVO.class);
        return responseParamsRemarkVOList;
    }

}




