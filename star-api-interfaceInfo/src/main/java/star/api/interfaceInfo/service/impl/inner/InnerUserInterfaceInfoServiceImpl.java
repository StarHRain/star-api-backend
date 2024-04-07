package star.api.interfaceInfo.service.impl.inner;

import org.apache.dubbo.config.annotation.DubboService;
import star.api.common.ErrorCode;
import star.api.exception.BusinessException;
import star.api.interfaceInfo.service.UserInterfaceInfoService;
import star.api.model.entity.UserInterfaceInfo;
import star.api.service.InnerUserInterfaceInfoService;
import star.api.utils.ThrowUtils;

import javax.annotation.Resource;

@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    @Override
    public Boolean hasLeftNum(Long userId, Long interfaceInfoId) {
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .one();
        ThrowUtils.throwIf(userInterfaceInfo==null,ErrorCode.NOT_FOUND_ERROR);
        Integer leftNum = userInterfaceInfo.getLeftNum();
        return leftNum > 0;
    }

    @Override
    public Boolean invokeCount(long userId, long interfaceInfoId) {
        // 查询接口是否存在
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .one();
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口不存在");
        }
        // 修改调用次数
        return userInterfaceInfoService.lambdaUpdate()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .set(UserInterfaceInfo::getTotalNum, userInterfaceInfo.getTotalNum() + 1)
                .set(UserInterfaceInfo::getLeftNum, userInterfaceInfo.getLeftNum() - 1)
                .update();
    }

    @Override
    public Boolean addDefaultUserInterfaceInfo(Long interfaceId, Long userId) {
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(interfaceId);
        userInterfaceInfo.setLeftNum(99999999);

        return userInterfaceInfoService.save(userInterfaceInfo);
    }

    @Override
    public UserInterfaceInfo checkUserHasInterface(long interfaceId, long userId) {
        if (interfaceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getUserId, userId)
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceId)
                .one();
    }

}
