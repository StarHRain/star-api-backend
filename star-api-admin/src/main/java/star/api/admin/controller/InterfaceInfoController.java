package star.api.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import star.api.admin.annotation.AuthCheck;
import star.api.admin.service.InterfaceInfoService;
import star.api.common.BaseResponse;
import star.api.common.DeleteRequest;
import star.api.common.IdRequest;
import star.api.common.ResultUtils;
import star.api.constant.UserConstant;
import star.api.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import star.api.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import star.api.model.entity.InterfaceInfo;
import star.api.model.vo.InterfaceInfoVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * API信息接口
 *
 * @author 千树星雨
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        return ResultUtils.success(interfaceInfoService.addInterfaceInfo(interfaceInfoAddRequest, request));
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return ResultUtils.success(interfaceInfoService.deleteInterfaceInfo(deleteRequest, request));
    }

    /**
     * 更新（仅管理员
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        return ResultUtils.success(interfaceInfoService.updateInterfaceInfo(interfaceInfoUpdateRequest, request));
    }

    /**
     * 获取接口（ VO
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(long id) {
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOById(id));
    }

    /**
     * 获取接口列表（分页，VO
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.listInterfaceInfoByPage(interfaceInfoQueryRequest);
        return ResultUtils.success(interfaceInfoService.listInterfaceInfoVOByPage(interfaceInfoPage, request));
    }

//    @PostMapping("/list/page")
//    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
//        return ResultUtils.success(interfaceInfoService.listInterfaceInfoByPage(interfaceInfoQueryRequest, request));
//    }

    /**
     * 获取当前用户接口列表（分页，VO
     *
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByUserId(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                           HttpServletRequest request) {
    return ResultUtils.success(interfaceInfoService.listInterfaceInfoVOByCurrentId(interfaceInfoQueryRequest,request));
    }

    /**
     * 上线接口
     *
     * @return 是否上线成功
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) throws UnsupportedEncodingException {
        return ResultUtils.success(interfaceInfoService.onlineInterfaceInfo(interfaceInfoInvokeRequest, request));
    }

    /**
     * 下线接口
     *
     * @param idRequest 携带id
     * @return 是否下线成功
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) throws UnsupportedEncodingException {
        return ResultUtils.success(interfaceInfoService.offlineInterfaceInfo(idRequest));
    }

    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest invokeInterfaceRequest, HttpServletRequest request) throws UnsupportedEncodingException {
        return ResultUtils.success(interfaceInfoService.invokeInterfaceInfo(invokeInterfaceRequest, request));
    }


}
