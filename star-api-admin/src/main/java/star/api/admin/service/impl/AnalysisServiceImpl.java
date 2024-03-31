package star.api.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import star.api.admin.exception.ThrowUtils;
import star.api.admin.mapper.UserInterfaceInfoMapper;
import star.api.admin.service.InterfaceInfoService;
import star.api.common.ErrorCode;
import star.api.model.entity.InterfaceInfo;
import star.api.model.entity.UserInterfaceInfo;
import star.api.model.vo.InterfaceInfoVO;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 千树星雨
 */
@Service
public class AnalysisServiceImpl {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    public List<InterfaceInfoVO> listTopInvokeInterfaceInfo(int limit) {
        ThrowUtils.throwIf(limit<=0,ErrorCode.PARAMS_ERROR);

        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(limit);
        ThrowUtils.throwIf(userInterfaceInfoList==null,ErrorCode.NOT_FOUND_ERROR,"接口信息不存在");

        //流处理然后查询
        Set<Long> interfaceInfoIdSet = userInterfaceInfoList.stream().map(userInterfaceInfo -> {
            return userInterfaceInfo.getInterfaceInfoId();
        }).collect(Collectors.toSet());
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdSet);
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        ThrowUtils.throwIf(list==null,ErrorCode.NOT_FOUND_ERROR);

        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = interfaceInfoService.getInterfaceInfoVO(interfaceInfo);
            return interfaceInfoVO;
        }).collect(Collectors.toList());

        return interfaceInfoVOList;
    }

}




