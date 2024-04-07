package star.api.interfaceInfo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import star.api.common.DeleteRequest;
import star.api.common.ErrorCode;
import star.api.constant.CommonConstant;
import star.api.exception.BusinessException;
import star.api.interfaceInfo.mapper.UserInterfaceInfoMapper;
import star.api.interfaceInfo.service.UserInterfaceInfoService;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoAddRequest;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import star.api.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import star.api.model.entity.UserInterfaceInfo;
import star.api.service.InnerUserService;
import star.api.utils.SqlUtils;
import star.api.utils.ThrowUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 千树星雨
 * @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Service实现
 * @createDate 2023-11-09 17:04:22
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @DubboReference
    private InnerUserService innerUserService;


    public QueryWrapper getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {

        Long id = userInterfaceInfoQueryRequest.getId();
        String searchText = userInterfaceInfoQueryRequest.getSearchText();
        Long userId = userInterfaceInfoQueryRequest.getUserId();
        Long interfaceInfoId = userInterfaceInfoQueryRequest.getInterfaceInfoId();
        Integer totalNum = userInterfaceInfoQueryRequest.getTotalNum();
        Integer leftNum = userInterfaceInfoQueryRequest.getLeftNum();
        Integer status = userInterfaceInfoQueryRequest.getStatus();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();

        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        //拼接查询条件
        if(StringUtils.isNotBlank(searchText)){
            queryWrapper.like("name",searchText);
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(totalNum), "totalNum", totalNum);
        queryWrapper.eq(ObjectUtils.isNotEmpty(leftNum), "leftNum", leftNum);
        queryWrapper.eq(ObjectUtils.isNotEmpty(interfaceInfoId), "interfaceInfoId", interfaceInfoId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        return queryWrapper;

    }


    /**
     * 添加用户接口
     *
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    @Override
    public Long addUserInterfaceInfo(UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userInterfaceInfoAddRequest == null, ErrorCode.PARAMS_ERROR);

        //填充
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);

        // 获取token
        String token = request.getHeader("Authorization");
        userInterfaceInfo.setUserId(innerUserService.getLoginUser(token).getId());
        userInterfaceInfo.setLeftNum(9999999);

        // 校验
        this.validUserInterfaceInfo(userInterfaceInfo);
        boolean result = this.save(userInterfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return userInterfaceInfo.getId();
    }

    /**
     * 删除用户接口
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteUserInterfaceInfo(DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        //判断接口是否存在
        UserInterfaceInfo userInterfaceInfo = this.getById(deleteRequest.getId());
        ThrowUtils.throwIf(userInterfaceInfo == null, ErrorCode.PARAMS_ERROR);

        //只有本人或管理员可删
        String token = request.getHeader("Authorization");
        Long id = innerUserService.getLoginUser(token).getId();
        ThrowUtils.throwIf(userInterfaceInfo.getUserId().equals(id) && !innerUserService.isAdmin(token), ErrorCode.OPERATION_ERROR);

        return this.removeById(deleteRequest.getId());
    }

    /**
     * 更新用户接口
     *
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateUserInterfaceInfo(UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        //填充
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);

        this.validUserInterfaceInfo(userInterfaceInfo);
        //是否存在
        UserInterfaceInfo userInterfaceInfo1 = this.getById(userInterfaceInfoUpdateRequest.getId());
        ThrowUtils.throwIf(userInterfaceInfo1 == null, ErrorCode.PARAMS_ERROR);

        return this.updateById(userInterfaceInfo);
    }

    /**
     * 通过id获取用户接口
     *
     * @param id
     * @return
     */
    @Override
    public UserInterfaceInfo getUserInterfaceInfoById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        UserInterfaceInfo userInterfaceInfo = this.getById(id);
        ThrowUtils.throwIf(userInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        return userInterfaceInfo;
    }

    /**
     * 获得用户接口列表
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @Override
    public List<UserInterfaceInfo> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        if (userInterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        List<UserInterfaceInfo> userInterfaceInfoList = this.list(queryWrapper);
        return userInterfaceInfoList;
    }

    /**
     * 获取用户接口列表（分页
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<UserInterfaceInfo> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userInterfaceInfoQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = userInterfaceInfoQueryRequest.getCurrent();
        long size = userInterfaceInfoQueryRequest.getPageSize();

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //查询
        QueryWrapper queryWrapper = this.getQueryWrapper(userInterfaceInfoQueryRequest);
        Page<UserInterfaceInfo> userInterfaceInfoPage = this.page(new Page<>(current, size), queryWrapper);

        return userInterfaceInfoPage;
    }

    /**
     * 检验用户接口
     *
     * @param userInterfaceInfo
     */
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo) {

        // 参数校验
        ThrowUtils.throwIf(userInterfaceInfo == null, ErrorCode.PARAMS_ERROR);

        Long userId = userInterfaceInfo.getUserId();
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        Integer totalNum = userInterfaceInfo.getTotalNum();
        Integer leftNum = userInterfaceInfo.getLeftNum();

        ThrowUtils.throwIf(userId == null || interfaceInfoId == null, ErrorCode.PARAMS_ERROR);

        List<UserInterfaceInfo> list = this.lambdaQuery()
                .eq(UserInterfaceInfo::getUserId, userId)
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .list();
        ThrowUtils.throwIf(!list.isEmpty(), ErrorCode.OPERATION_ERROR, "用户已有该接口");

    }

    /**
     * 用户接口调用次数减一
     *
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    @Override
    public boolean invokeInterfaceCount(long userId, long interfaceInfoId) {
        if (userId <= 0 || interfaceInfoId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.gt("leftNum", 0);
        updateWrapper.setSql("leftNum = leftNum -1, totalNum = totalNum + 1");

        return update(updateWrapper);
    }
}




