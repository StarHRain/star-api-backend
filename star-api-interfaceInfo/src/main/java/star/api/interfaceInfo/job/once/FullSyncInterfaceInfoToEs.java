package star.api.interfaceInfo.job.once;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import star.api.interfaceInfo.es.dao.InterfaceInfoEsDao;
import star.api.interfaceInfo.es.dto.InterfaceInfoEsDTO;
import star.api.interfaceInfo.service.InterfaceInfoService;
import star.api.model.entity.InterfaceInfo;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步帖子到 es
 *
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FullSyncInterfaceInfoToEs implements CommandLineRunner {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private InterfaceInfoEsDao interfaceInfoEsDao;

    @Override
    public void run(String... args) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list();
        if (CollectionUtils.isEmpty(interfaceInfoList)) {
            return;
        }
        List<InterfaceInfoEsDTO> interfaceInfoEsDTOList = interfaceInfoList.stream().map(InterfaceInfoEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500;
        int total = interfaceInfoEsDTOList.size();
        log.info("----------全量同步开始 接口信息 To ES----------");
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            interfaceInfoEsDao.saveAll(interfaceInfoEsDTOList.subList(i, end));
        }
        log.info("----------全量同步结束----------");
    }
}
