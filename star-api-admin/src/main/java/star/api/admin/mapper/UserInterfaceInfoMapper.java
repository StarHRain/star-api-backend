package star.api.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import star.api.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author 千树星雨
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Mapper
* @createDate 2023-11-09 17:04:22
* @Entity star.api.admin.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    /**
     * 获取调用次数最多的 N 个接口
     * @param limit
     * @return
     */
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




