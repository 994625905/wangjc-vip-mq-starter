package vip.wangjc.mq.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 消息发送到交换机返回机制，失败完成回调
 * @author wangjc
 * @title: MsgSendReturnCallBack
 * @projectName wangjc-vip
 * @date 2020/12/23 - 19:31
 */
public class MsgSendReturnCallBack implements RabbitTemplate.ReturnsCallback {

    private static final Logger logger = LoggerFactory.getLogger(MsgSendReturnCallBack.class);

    /**
     * 当消息从交换机到队列失败时，该方法被调用。（若成功，则不调用）
     * 需要注意的是：该方法调用后，{@link MsgSendConfirmCallBack}中的confirm方法也会被调用，且ack = true
     * @param returnedMessage
     */
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {

        /** 由于rabbitmq在延时队列使用插件的投递过程中，可能会触发returnedMessage，但最终也能被消费成功，所以建议子类在此处手动过滤 */

        logger.info("MsgSendReturnCallBack[消息从交换机到队列失败]:[{}]",returnedMessage.getMessage().toString());
        logger.info("replyCode:[{}]",returnedMessage.getReplyCode());
        logger.info("replyText:[{}]",returnedMessage.getReplyText());
        logger.info("exchange:[{}]",returnedMessage.getExchange());
        logger.info("routingKey:[{}]",returnedMessage.getRoutingKey());
    }

}
