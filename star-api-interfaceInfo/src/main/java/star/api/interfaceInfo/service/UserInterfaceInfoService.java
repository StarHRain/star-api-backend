package star.api.interfaceInfo.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import star.api.common.DeleteRequest;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoAddRequest;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import star.api.model.entity.UserInterfaceInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 千树星雨
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Service
* @createDate 2023-11-09 17:04:22
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 创建
     *
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    Long addUserInterfaceInfo(UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request);

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    Boolean deleteUserInterfaceInfo(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 更新
     *
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    Boolean updateUserInterfaceInfo( UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                         HttpServletRequest request);

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    UserInterfaceInfo getUserInterfaceInfoById(long id);

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    List<UserInterfaceInfo> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest);

    /**
     * 分页获取列表
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
    Page<UserInterfaceInfo> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request);


    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo);

    boolean invokeInterfaceCount(long userId, long interfaceInfoId);
}
