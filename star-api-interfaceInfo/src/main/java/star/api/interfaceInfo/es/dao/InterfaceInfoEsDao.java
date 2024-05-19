package star.api.interfaceInfo.es.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import star.api.interfaceInfo.es.dto.InterfaceInfoEsDTO;

import java.util.List;

/**
 * 帖子 ES 操作
 *
 */
public interface InterfaceInfoEsDao extends ElasticsearchRepository<InterfaceInfoEsDTO, Long> {

    List<InterfaceInfoEsDTO> findByUserId(Long userId);
}