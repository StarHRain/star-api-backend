package star.api.chart.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static star.api.chart.constant.MqConstant.BI_EXCHANGE_NAME;
import static star.api.chart.constant.MqConstant.BI_ROUTING_KEY;

@Component
public class BiMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param message
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BI_EXCHANGE_NAME, BI_ROUTING_KEY, message);
    }

}
