package star.api.admin.mq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import star.api.admin.exception.BusinessException;
import star.api.admin.service.ChartService;
import star.api.admin.service.impl.AiService;
import star.api.common.ErrorCode;
import star.api.model.entity.Chart;
import star.api.model.vo.AiResultVO;

import javax.annotation.Resource;

import static star.api.admin.constant.ChartConstant.*;
import static star.api.admin.constant.MqConstant.BI_QUEUE_NAME;
import static star.api.constant.CommonConstant.BI_MODEL_ID;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 26 日
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiService aiService;

    /**
     * 监听消息处理
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("收到消息：{}", message);
        if (StringUtils.isBlank(message)){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart==null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表不存在");
        }
        // 修改图表任务状态
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus(STATUS_RUNNING);
        boolean isSuccessful = chartService.updateById(updateChart);
        if (!isSuccessful){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图表执行中状态失败");
            return;
        }
        // AI调用
        String question = chartService.constructUserInput(chartId);
        AiResultVO aiResultVO = aiService.genChart(chartId, question);

        // 更新
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(aiResultVO.getGenChart());
        updateChartResult.setGenResult(aiResultVO.getGenResult());
        updateChartResult.setStatus(STATUS_SUCCESS);
        boolean b = chartService.updateById(updateChartResult);
        if (!b){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图表成功状态失败");
            return;
        }
        // 消息确认
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 构建用户输入
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    private void handleChartUpdateError(long chartId, String execMessage){
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus(STATUS_FAILED);
        chart.setExecMessage(execMessage);
        boolean isSuccessful = chartService.updateById(chart);
        if (!isSuccessful){
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
