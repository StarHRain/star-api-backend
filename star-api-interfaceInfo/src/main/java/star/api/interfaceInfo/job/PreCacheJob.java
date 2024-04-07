package star.api.interfaceInfo.job;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 17 日
 */

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import star.api.interfaceInfo.service.InterfaceInfoService;
import star.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static star.api.constant.RedisConstant.LOCK_CACHE_PREHEAT;

/**
 * 缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    //缓存预热接口页数
    private List<Long> cachePageNum=Arrays.asList(1L,2L);


    //每天执行，预热首页接口列表
    @Scheduled(cron = "0 27 1 * * *")
    public void preCacheInterfaceInfoPage() {
        RLock lock = redissonClient.getLock(LOCK_CACHE_PREHEAT);
        try{
            if (lock.tryLock()){
                for (Long cachePageNum : cachePageNum){
                    InterfaceInfoQueryRequest interfaceInfoQueryRequest = new InterfaceInfoQueryRequest();
                    interfaceInfoQueryRequest.setCurrent(cachePageNum);
                    interfaceInfoQueryRequest.setPageSize(10L);
                    interfaceInfoQueryRequest.setSearchText("");

                    interfaceInfoService.listInterfaceInfoByPage(interfaceInfoQueryRequest);
                };
            }
        }finally {
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
