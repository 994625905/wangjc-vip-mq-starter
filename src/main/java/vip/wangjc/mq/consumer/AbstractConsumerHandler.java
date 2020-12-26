package vip.wangjc.mq.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消费者的抽象类消费器
 * @author wangjc
 * @title: ConsumerHandler
 * @projectName wangjc-vip-mq
 * @date 2020/12/24 - 11:18
 */
public abstract class AbstractConsumerHandler implements ChannelAwareMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractConsumerHandler.class);

    /**
     * 存储需要手动确认消息的队列
     */
    private List<String> manualQueueList = new CopyOnWriteArrayList<>();

    /**
     * 添加需要手动确认消息的队列
     * @param queueName
     */
    public final void addManualQueue(String queueName){
        this.manualQueueList.add(queueName);
    }

    /**
     * 抽象方法，给子类重写的执行业务
     * @param msg
     * @param channel
     * @param queue
     * @return
     */
    public abstract Boolean handleMessage(String msg, Channel channel, String queue);

    /**
     * 接收消息
     * @param message
     * @param channel
     */
    @Override
    public void onMessage(Message message, Channel channel){
        logger.debug("===============消费者接收到RabbitMQ消息========================");
        try {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            String queue = message.getMessageProperties().getConsumerQueue();
            String msg = new String(message.getBody());
            logger.debug("队列：[{}]，内容：[{}]",queue,msg);

            Boolean result = this.handleMessage(msg, channel, queue);
            if(result){
                this.ackMessage(channel,queue,deliveryTag);
            }else{
                logger.warn("=========================消费异常，handleMessage return false ============================");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 确认消息
     * @param channel
     * @param queue
     * @param deliveryTag:RabbitMQ 向该 Channel 投递这条消息的唯一标识 ID，单调递增的正整数
     */
    private void ackMessage(Channel channel,String queue,Long deliveryTag){
        try {
            if(this.manualQueueList.contains(queue)){

                /** 当该参数为 true 时，则可以一次性确认 deliveryTag 小于等于传入值的所有消息 */
                channel.basicAck(deliveryTag,true);
                logger.debug("=================已消费,手动确认成功=========================");
            }else{
                logger.debug("=================已消费,自动确认成功=========================");
            }
        }catch (Exception e){
            logger.error("=========================确认消息异常============================");
            e.printStackTrace();
        }
    }
}
