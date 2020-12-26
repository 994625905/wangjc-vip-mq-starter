package vip.wangjc.mq.producer.template;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 自定义的rabbitTemplate，与原rabbitTemplate不冲突
 * @author wangjc
 * @title: DefinedRabbitTemplate
 * @projectName wangjc-vip-mq
 * @date 2020/12/25 - 15:10
 */
public class DefinedRabbitTemplate {

    private final RabbitTemplate rabbitTemplate;

    public DefinedRabbitTemplate(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }
}
