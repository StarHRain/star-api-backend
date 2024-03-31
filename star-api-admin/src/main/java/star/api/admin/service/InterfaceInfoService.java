package star.api.admin.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestBody;
import star.api.common.DeleteRequest;
import star.api.common.IdRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import star.api.model.entity.InterfaceInfo;
import star.api.model.vo.InterfaceInfoVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 千树星雨
 * @description 针对表【interface_info(接口信息)】的数据库操作Service
 * @createDate 2023-11-03 14:07:08
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 添加接口服务
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    Long addInterfaceInfo(InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request);

    /**
     * 删除接口服务
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    Boolean deleteInterfaceInfo(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 更新接口服务
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    Boolean updateInterfaceInfo(InterfaceInfoUpdateRequest interfaceInfoUpdateRequest, HttpServletRequest request);

    /**
     * 获取接口（ VO
     * @param interfaceInfo
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo);

    /**
     * 通过 ID 获取接口（ VO
     *
     * @param id
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVOById(long id);

    /**
     * 查询接口列表（仅管理员
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    List<InterfaceInfoVO> listInterfaceInfoVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    Page<InterfaceInfo> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 获取接口（分页，VO
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> listInterfaceInfoVOByPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 上线接口
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    Boolean onlineInterfaceInfo(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request);

    /**
     * 下线接口
     * @param idRequest
     * @return
     */
    Boolean offlineInterfaceInfo(IdRequest idRequest);

    /**
     * 调用接口
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    Object invokeInterfaceInfo(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request);

    /**
     * 接口校验服务
     *
     * @param interfaceInfo
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo);

    /**
     * 获取当前用户接口列表
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> listInterfaceInfoVOByCurrentId(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request);
}
