package vip.wangjc.mq.util;

/**
 * @author wangjc
 * @title: RabbitmqUtil
 * @projectName wangjc-vip-mq
 * @date 2020/12/25 - 16:51
 */
public class RabbitmqUtil {

    private static final String dead_prefix = "dead-";

    /**
     * 获取死信交换机名称
     * @param exchange
     * @return
     */
    public static final String getDeadExchangeName(String exchange){
        return dead_prefix+"exchange-"+exchange;
    }

    /**
     * 获取死信队列名称
     * @param queueName
     * @return
     */
    public static final String getDeadQueueName(String queueName){
        return dead_prefix+"queue-"+queueName;
    }

    /**
     * 死信队列绑定的路由
     * @param routingKey
     * @return
     */
    public static final String getDeadRoutingKey(String routingKey){
        return dead_prefix+"routingKey-"+routingKey;
    }
}
