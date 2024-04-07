package star.api.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import star.api.admin.annotation.AuthCheck;
import star.api.admin.service.ChartService;
import star.api.common.BaseResponse;
import star.api.common.DeleteRequest;
import star.api.common.ResultUtils;
import star.api.constant.UserConstant;
import star.api.model.dto.chart.*;
import star.api.model.entity.Chart;
import star.api.model.vo.AiResultVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图表信息
 *
 * @author 千树星雨
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {
    @Resource
    private ChartService chartService;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        return ResultUtils.success(chartService.addChart(chartAddRequest, request));
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return ResultUtils.success(chartService.deleteChart(deleteRequest, request));
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        return ResultUtils.success(chartService.updateChart(chartUpdateRequest));
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        return ResultUtils.success(chartService.getChartById(id,request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        return ResultUtils.success(chartService.listChartByPage(chartQueryRequest, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        return ResultUtils.success(chartService.listMyChartByPage(chartQueryRequest,request));
    }



    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        return ResultUtils.success(chartService.editChart(chartEditRequest,request));
    }

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<AiResultVO> genChartByAi(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        return ResultUtils.success(chartService.genChartByAi(multipartFile,genChartByAiRequest,request));
    }

    /**
     * 智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<AiResultVO> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        return ResultUtils.success(chartService.genChartByAiAsyncMq(multipartFile, genChartByAiRequest,request));
    }
}
