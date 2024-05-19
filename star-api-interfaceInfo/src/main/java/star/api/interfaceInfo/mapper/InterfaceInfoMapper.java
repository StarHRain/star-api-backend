package star.api.interfaceInfo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import star.api.model.entity.InterfaceInfo;

import java.util.Date;
import java.util.List;

/**
* @author 千树星雨
* @description 针对表【interface_info(接口信息)】的数据库操作Mapper
* @createDate 2023-11-03 14:07:08
* @Entity star.api.admin.model.entity.InterfaceInfo
*/
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {
    /**
     * 查询接口信息列表（包括已被删除的数据）
     */
    List<InterfaceInfo> listInterfaceInfoWithDelete (Date minUpdateTime);
}




