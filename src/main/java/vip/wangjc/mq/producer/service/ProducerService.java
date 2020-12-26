package vip.wangjc.mq.producer.service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 消息生产者提供的接口
 * @author wangjc
 * @title: ProduceService
 * @projectName wangjc-vip-mq
 * @date 2020/12/23 - 19:39
 */
public interface ProducerService {

    /**
     * 获取RabbitAdmin
     * @return
     */
    RabbitAdmin getRabbitAdmin();

    /**
     * 获取RabbitTemplate
     * @return
     */
    RabbitTemplate getRabbitTemplate();

    /**
     * 创建直连交换机(已存在的话则不重复创建，同下)
     * @param exchangeName
     * @param durable：持久化（同下）
     * @param autoDelete：自动删除（同下）
     * @return
     */
    DirectExchange createDirectExchange(String exchangeName, Boolean durable, Boolean autoDelete);

    /**
     * 创建主题交换机
     * @param exchangeName
     * @param durable
     * @param autoDelete
     * @return
     */
    TopicExchange createTopicExchange(String exchangeName, Boolean durable, Boolean autoDelete);

    /**
     * 创建广播交换机
     * @param exchangeName
     * @param durable
     * @param autoDelete
     * @return
     */
    FanoutExchange createFanoutExchange(String exchangeName, Boolean durable, Boolean autoDelete);

    /**
     * 创建延时交换机
     * @param exchangeName
     * @param durable
     * @param autoDelete
     * @return
     */
    CustomExchange createDelayExchange(String exchangeName, Boolean durable, Boolean autoDelete);

    /**
     * 创建队列
     * @param queueName
     * @param durable
     * @param exclusive：排他性（同下）
     * @param autoDelete
     * @return
     */
    Queue createQueue(String queueName, Boolean durable, Boolean exclusive, Boolean autoDelete);

    /**
     * 创建延时队列
     * @param queueName
     * @param durable
     * @param exclusive
     * @param autoDelete
     * @return
     */
    Queue createDelayQueue(String queueName, Boolean durable, Boolean exclusive, Boolean autoDelete);

    /**
     * 将队列绑定到直连交换机上
     * @param directExchange
     * @param queue
     * @param routingKey
     */
    Binding bindQueueToDirectExchange(DirectExchange directExchange, Queue queue, String routingKey);

    /**
     * 将队列绑定到主题交换机
     * @param topicExchange
     * @param queue
     * @param routingKey
     */
    Binding bindQueueToTopicExchange(TopicExchange topicExchange, Queue queue, String routingKey);

    /**
     * 将队列绑定到广播交换机
     * @param fanoutExchange
     * @param queue
     */
    Binding bindQueueToFanoutExchange(FanoutExchange fanoutExchange, Queue queue);

    /**
     * 绑定延时队列到交换机上
     * @param exchange
     * @param queue
     * @param routingKey
     * @return
     */
    Binding bindDelayQueueToExchange(Exchange exchange, Queue queue, String routingKey);

    /**
     * 绑定死信队列到交换机上，跟延时队列并用
     * @param exchange
     * @param queue
     * @param routingKey
     * @return
     */
    Binding bindDeadQueueToExchange(Exchange exchange, Queue queue, String routingKey);

    /**
     * 发送消息到交换机上，发送普通消息，直连交换机好理解，如果是主题交换机的话，routingKey带有正则表达式的匹配特性
     * @param exchangeName
     * @param routeKey
     * @param msg
     */
    void sendMessage(String exchangeName, String routeKey, String msg);

    /**
     * 发送消息到交换机上，广播交换机，没有路由
     * @param exchangeName
     * @param msg
     */
    void senMessage(String exchangeName, String msg);

    /**
     * 发送延时消息到交换机上
     * @param exchangeName
     * @param routingKey
     * @param msg
     * @param delayTime
     */
    void sendDelayMessage(String exchangeName, String routingKey, String msg, Integer delayTime);
}
