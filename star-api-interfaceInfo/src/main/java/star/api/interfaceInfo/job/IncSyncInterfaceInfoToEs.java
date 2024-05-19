package star.api.interfaceInfo.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import star.api.interfaceInfo.es.dao.InterfaceInfoEsDao;
import star.api.interfaceInfo.es.dto.InterfaceInfoEsDTO;
import star.api.interfaceInfo.mapper.InterfaceInfoMapper;
import star.api.model.entity.InterfaceInfo;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步帖子到 es
 *
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class IncSyncInterfaceInfoToEs {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private InterfaceInfoEsDao interfaceInfoEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoMapper.listInterfaceInfoWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(interfaceInfoList)) {
            return;
        }
        List<InterfaceInfoEsDTO> interfaceInfoEsDTOList = interfaceInfoList.stream()
                .map(InterfaceInfoEsDTO::objToDto)
                .collect(Collectors.toList());
        final int pageSize = 500;
        int total = interfaceInfoEsDTOList.size();
        log.info("----------增量同步开始 接口信息 To ES-----------");
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            interfaceInfoEsDao.saveAll(interfaceInfoEsDTOList.subList(i, end));
        }
        log.info("----------增量同步结束----------");
    }
}
