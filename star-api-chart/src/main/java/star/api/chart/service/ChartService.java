package star.api.chart.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import star.api.common.DeleteRequest;
import star.api.model.dto.chart.*;
import star.api.model.entity.Chart;
import star.api.model.vo.AiResultVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 图表服务
 */
public interface ChartService extends IService<Chart> {

    /**
     * 添加
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    Long addChart(ChartAddRequest chartAddRequest, HttpServletRequest request);

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    Boolean deleteChart(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    Boolean updateChart(ChartUpdateRequest chartUpdateRequest);

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    Chart getChartById(long id, HttpServletRequest request);

    /**
     * 获取图表（分页
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    Page<Chart> listChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request);

    /**
     * 获取用户图表（分页
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    Page<Chart> listMyChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request);

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    Boolean editChart(ChartEditRequest chartEditRequest, HttpServletRequest request);

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    AiResultVO genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    /**
     * 智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    AiResultVO genChartByAiAsyncMq(MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    /**
     * 构造问题
     * @param chartId
     * @return
     */
    String constructUserInput(long chartId);
}
