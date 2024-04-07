package star.api.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import star.api.admin.manager.RedisLimiterManager;
import star.api.admin.mapper.ChartMapper;
import star.api.admin.mq.BiMessageProducer;
import star.api.admin.service.ChartService;
import star.api.admin.service.UserService;
import star.api.admin.utils.ExcelUtils;
import star.api.common.DeleteRequest;
import star.api.common.ErrorCode;
import star.api.constant.CommonConstant;
import star.api.exception.BusinessException;
import star.api.exception.ThrowUtils;
import star.api.model.dto.chart.*;
import star.api.model.entity.Chart;
import star.api.model.entity.User;
import star.api.model.vo.AiResultVO;
import star.api.utils.SqlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static star.api.constant.ChartConstant.STATUS_FAILED;
import static star.api.constant.ChartConstant.STATUS_SUCCESS;


/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    @Resource
    private UserService userService;

    @Resource
    private AiService aiService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private BiMessageProducer biMessageProducer;


    @Override
    public Long addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(chartAddRequest == null, ErrorCode.PARAMS_ERROR);
        //填充查询
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = this.save(chart);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return chart.getId();
    }

    @Override
    public Boolean deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public Boolean updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        ThrowUtils.throwIf(chartUpdateRequest == null || chartUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        return this.updateById(chart);
    }

    @Override
    public Chart getChartById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        Chart chart = this.getById(id);
        ThrowUtils.throwIf(chart == null, ErrorCode.NOT_FOUND_ERROR);

        return chart;
    }

    @Override
    public Page<Chart> listChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = this.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return chartPage;
    }

    @Override
    public Page<Chart> listMyChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = this.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return chartPage;
    }

    @Override
    public Boolean editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(chartEditRequest == null || chartEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        ThrowUtils.throwIf(!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.FORBIDDEN_ERROR);

        return this.updateById(chart);
    }

    @Override
    public AiResultVO genChartByAi(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        // 参数校验
        verifyGenChart(multipartFile, genChartByAiRequest);

        // 限流判断，每个用户一个限流器
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAi_" + userService.getLoginUser(request).getId());

        // 构造用户输入
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        String userInput = constructUserInput(csvData, genChartByAiRequest);

        // AI生成图像数据和数据分析
        AiResultVO aiResultVO = aiService.genChart(RandomUtils.nextInt(), userInput);
        String genChart = aiResultVO.getGenChart();
        String genResult = aiResultVO.getGenResult();

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(genChartByAiRequest.getName());
        chart.setGoal(genChartByAiRequest.getGoal());
        chart.setChartData(csvData);
        chart.setChartType(genChartByAiRequest.getChartType());
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        if (genResult.equals("生成失败")){
            chart.setStatus(STATUS_FAILED);
        }else{
            chart.setStatus(STATUS_SUCCESS);
        }
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        aiResultVO.setChartId(chart.getId());
        return aiResultVO;
    }

    @Override
    public AiResultVO genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        verifyGenChart(multipartFile, genChartByAiRequest);

        // 限流判断，每个用户一个限流器
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        // 构造用户输入
        String userInput = constructUserInput(csvData, genChartByAiRequest);

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(genChartByAiRequest.getName());
        chart.setGoal(genChartByAiRequest.getGoal());
        chart.setChartData(csvData);
        chart.setChartType(genChartByAiRequest.getChartType());
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        long newChartId = chart.getId();
        biMessageProducer.sendMessage(String.valueOf(newChartId));

        AiResultVO aiResultVO = new AiResultVO();
        aiResultVO.setChartId(newChartId);
        return aiResultVO;
    }

    @Override
    public String constructUserInput(long chartId) {
        Chart chart = this.getById(chartId);
        ThrowUtils.throwIf(chart==null, ErrorCode.NOT_FOUND_ERROR);

        //获取参数
        String goal = chart.getGoal();
        String chartData = chart.getChartData();
        String chartType = chart.getChartType();

        //根据用户上传的数据，压缩ai提问语
        StringBuffer res = new StringBuffer();
        res.append("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：");
        res.append("\n").append("分析需求：").append("\n").append("{").append(goal).append("}").append("\n");

        res.append("原始数据:").append("\n").append(chartData);
        res.append("请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n【【【【【\n先输出上面原始数据的分析结果：\n然后输出【【【【【\n{前端 Echarts V5 的 option 配置对象JSON代码，生成");
        res.append(chartType);
        res.append("合理地将数据进行可视化，不要生成任何多余的内容，不要注释}");
        return res.toString();
    }


    /**
     * 验证 genChartByAiRequest 是否合法
     * @param multipartFile
     * @param genChartByAiRequest
     */
    private void verifyGenChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() >= 64, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR);
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(StringUtils.isBlank(suffix), ErrorCode.PARAMS_ERROR, "文件名异常");
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
    }

    /**
     * 构造问题
     * @param csvData
     * @param genChartByAiRequest
     * @return
     */
    private String constructUserInput(String csvData, GenChartByAiRequest genChartByAiRequest) {
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 构造用户输入
        StringBuffer res = new StringBuffer();
        res.append("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：");
        res.append("\n").append("分析需求：").append("\n").append("{").append(goal).append("}").append("\n");

        res.append("原始数据:").append("\n").append(csvData);
        res.append("请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n【【【【【\n先输出上面原始数据的分析结果：\n然后输出【【【【【\n{前端 Echarts V5 的 option 配置对象JSON代码JSON代码JSON代码，生成");
        res.append(chartType);
        res.append("合理地将数据进行可视化，不要生成任何多余的内容，不要注释}");
        return res.toString();
    }

    /**
     * 异步操作失败更新表状态
     *
     * @param chartId
     * @param execMessage
     */
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage("execMessage");
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




