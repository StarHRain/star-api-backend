package star.api.interfaceInfo.es;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import star.api.interfaceInfo.es.dao.InterfaceInfoEsDao;
import star.api.interfaceInfo.es.dto.InterfaceInfoEsDTO;
import star.api.interfaceInfo.job.once.FullSyncInterfaceInfoToEs;
import star.api.interfaceInfo.service.InterfaceInfoService;
import star.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import star.api.model.entity.InterfaceInfo;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 帖子 ES 操作测试
 *
 */
@SpringBootTest
public class InterfaceInfoEsDaoTest {

    @Resource
    private InterfaceInfoEsDao interfaceInfoEsDao;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Test
    void testFullSync(){
        FullSyncInterfaceInfoToEs fullSyncInterfaceInfoToEs = new FullSyncInterfaceInfoToEs();
    }


    @Test
    void test() {
        InterfaceInfoQueryRequest interfaceInfoQueryRequest = new InterfaceInfoQueryRequest();
        interfaceInfoQueryRequest.setCurrent(1);
        interfaceInfoQueryRequest.setPageSize(3);
        interfaceInfoQueryRequest.setSearchText("t t");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.searchFromEs(interfaceInfoQueryRequest);
        System.out.println(interfaceInfoPage);
    }

    @Test
    void testSelect() {
        System.out.println(interfaceInfoEsDao.count());
        Page<InterfaceInfoEsDTO> InterfaceInfoPage = interfaceInfoEsDao.findAll(
                PageRequest.of(0, 5, Sort.by("createTime")));
        List<InterfaceInfoEsDTO> interfaceInfoList = InterfaceInfoPage.getContent();
        System.out.println(interfaceInfoList);
    }

    @Test
    void testAdd() {
        InterfaceInfoEsDTO interfaceInfoEsDTO = new InterfaceInfoEsDTO();
        interfaceInfoEsDTO.setId(10L);
        interfaceInfoEsDTO.setName("test");
        interfaceInfoEsDTO.setDescription("test");
        interfaceInfoEsDTO.setUrl("test");
        interfaceInfoEsDTO.setHost("test");
        interfaceInfoEsDTO.setRequestParams("test");
        interfaceInfoEsDTO.setRequestParamsRemark("test");
        interfaceInfoEsDTO.setResponseParamsRemark("test");
        interfaceInfoEsDTO.setRequestHeader("test");
        interfaceInfoEsDTO.setResponseHeader("test");
        interfaceInfoEsDTO.setStatus(0);
        interfaceInfoEsDTO.setMethod("test");
        interfaceInfoEsDTO.setUserId(1L);
        interfaceInfoEsDTO.setCreateTime(new Date());
        interfaceInfoEsDTO.setUpdateTime(new Date());
        interfaceInfoEsDTO.setIsDelete(0);

        interfaceInfoEsDao.save(interfaceInfoEsDTO);
        System.out.println(interfaceInfoEsDTO.getId());
    }

    @Test
    void testFindById() {
        Optional<InterfaceInfoEsDTO> interfaceInfoEsDTO = interfaceInfoEsDao.findById(1L);
        System.out.println(interfaceInfoEsDTO);
    }

    @Test
    void testCount() {
        System.out.println(interfaceInfoEsDao.count());
    }

    @Test
    void testFindByCategory() {
        List<InterfaceInfoEsDTO> interfaceInfoEsDaoTestList = interfaceInfoEsDao.findByUserId(1L);
        System.out.println(interfaceInfoEsDaoTestList);
    }
}
