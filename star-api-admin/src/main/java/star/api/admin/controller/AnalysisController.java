package star.api.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import star.api.admin.annotation.AuthCheck;
import star.api.admin.service.impl.AnalysisServiceImpl;
import star.api.common.BaseResponse;
import star.api.common.ResultUtils;
import star.api.model.vo.InterfaceInfoVO;

import javax.annotation.Resource;
import java.util.List;

import static star.api.constant.UserConstant.ADMIN_ROLE;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 12 日
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private AnalysisServiceImpl analysisServiceImpl;

    private static final int TOP_LIMIT = 3;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo() {
        return ResultUtils.success(analysisServiceImpl.listTopInvokeInterfaceInfo(TOP_LIMIT));
    }
}
