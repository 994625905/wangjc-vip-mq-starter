package vip.wangjc.mq.auto.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * rabbitmq的交换机配置文件对应
 * @author wangjc
 * @title: RabbitmqProperties
 * @projectName wangjc-vip-mq
 * @date 2020/12/24 - 15:31
 */
@ConfigurationProperties(prefix = "vip.wangjc.mq.queue")
public class RabbitmqAutoQueueProperties {

    /**
     * 队列名称
     * vip.wangjc.mq.queue.{exchange}.name=
     *
     * 队列的持久化，true/false，默认true
     * vip.wangjc.mq.queue.{exchange}.durable=
     *
     * 队列没有在使用时将被自动删除，true/false，默认false
     * vip.wangjc.mq.queue.{exchange}.autoDelete=
     *
     * 队列是否只在当前connection生效，true/false，默认false
     * vip.wangjc.mq.queue.{exchange}.exclusive=
     *
     * 队列绑定到交换机的路由key
     * vip.wangjc.mq.queue.{exchange}.routingKey=
     */

    /**
     * 与交换机对应的队列
     * key-value：交换机name-队列list
     */
    private Map<String, List<Object>> bindExchange = new HashMap<>();

    public Map<String, List<Object>> getBindExchange() {
        return bindExchange;
    }

    public void setBindExchange(Map<String, List<Object>> bindExchange) {
        this.bindExchange = bindExchange;
    }
}
