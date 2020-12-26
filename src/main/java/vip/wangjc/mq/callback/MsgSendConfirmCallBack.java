package vip.wangjc.mq.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 消息发送到交换机的确认机制
 * @author wangjc
 * @title: MsgSendConfirmCallBack
 * @projectName wangjc-vip
 * @date 2020/12/23 - 19:28
 */
public class MsgSendConfirmCallBack implements RabbitTemplate.ConfirmCallback{

    private static final Logger logger = LoggerFactory.getLogger(MsgSendConfirmCallBack.class);

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        logger.debug("MsgSendConfirmCallBack[消息发送到交换机成功]");
        if(ack){
            logger.debug("消息发送成功");
        }else{
            logger.debug("消息发送失败，原因[{}]",cause);
        }
    }
}
