package star.api.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import star.api.admin.annotation.AuthCheck;
import star.api.admin.service.UserInterfaceInfoService;
import star.api.common.BaseResponse;
import star.api.common.DeleteRequest;
import star.api.common.ResultUtils;
import star.api.constant.UserConstant;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoAddRequest;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import star.api.model.entity.UserInterfaceInfo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * API信息接口
 *
 * @author 千树星雨
 */
@RestController
@RequestMapping("/userInterfaceInfo")
@Slf4j
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    /**
     * 创建
     *
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        return ResultUtils.success(userInterfaceInfoService.addUserInterfaceInfo(userInterfaceInfoAddRequest, request));
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return ResultUtils.success(userInterfaceInfoService.deleteUserInterfaceInfo(deleteRequest, request));
    }

    /**
     * 更新
     *
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                         HttpServletRequest request) {
        return ResultUtils.success(userInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoUpdateRequest, request));
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfoById(long id) {
        return ResultUtils.success(userInterfaceInfoService.getUserInterfaceInfoById(id));
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<UserInterfaceInfo>> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        return ResultUtils.success(userInterfaceInfoService.listUserInterfaceInfo(userInterfaceInfoQueryRequest));
    }

    /**
     * 分页获取列表
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(userInterfaceInfoService.listUserInterfaceInfoByPage(userInterfaceInfoQueryRequest, request));
    }
}
