package vip.wangjc.mq.producer.service.impl;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import vip.wangjc.mq.producer.service.ProducerService;
import vip.wangjc.mq.producer.template.DefinedRabbitTemplate;
import vip.wangjc.mq.util.RabbitmqUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangjc
 * @title: ProduceServiceImpl
 * @projectName wangjc-vip-mq
 * @date 2020/12/23 - 19:40
 */
public class ProducerServiceImpl implements ProducerService {


    private final RabbitAdmin rabbitAdmin;

    private final DefinedRabbitTemplate definedRabbitTemplate;

    private final RabbitTemplate rabbitTemplate;

    public ProducerServiceImpl(RabbitAdmin rabbitAdmin, DefinedRabbitTemplate definedRabbitTemplate){
        this.rabbitAdmin = rabbitAdmin;
        this.definedRabbitTemplate = definedRabbitTemplate;
        this.rabbitTemplate = definedRabbitTemplate.getRabbitTemplate();
    }

    @Override
    public RabbitAdmin getRabbitAdmin() {
        return this.rabbitAdmin;
    }

    @Override
    public RabbitTemplate getRabbitTemplate() {
        return this.rabbitTemplate;
    }

    /**
     * direct交换器相对来说比较简单，匹配规则为：如果路由键匹配，消息就被投送到相关的队列
     * @param exchangeName
     * @return
     */
    @Override
    public DirectExchange createDirectExchange(String exchangeName, Boolean durable, Boolean autoDelete) {
        /**
         * 1.durable=true ，交换机的持久化！rabbitmq重启的时候不需要创建新的交换机，
         */
        DirectExchange directExchange = new DirectExchange(exchangeName,durable,autoDelete);
        this.rabbitAdmin.declareExchange(directExchange);
        return directExchange;
    }

    /**
     * topic交换机采用模糊匹配路由键的原则进行转发消息到队列中，发布订阅者模式，
     * @param exchangeName
     * @return
     */
    @Override
    public TopicExchange createTopicExchange(String exchangeName, Boolean durable, Boolean autoDelete) {
        TopicExchange topicExchange = new TopicExchange(exchangeName,durable,autoDelete);
        this.rabbitAdmin.declareExchange(topicExchange);
        return topicExchange;
    }

    /**
     * 广播交换机，发送消息无需路由键
     * @param exchangeName
     * @param durable
     * @param autoDelete
     * @return
     */
    @Override
    public FanoutExchange createFanoutExchange(String exchangeName, Boolean durable, Boolean autoDelete) {
        FanoutExchange fanoutExchange = new FanoutExchange(exchangeName,durable,autoDelete);
        this.rabbitAdmin.declareExchange(fanoutExchange);
        return fanoutExchange;
    }

    @Override
    public CustomExchange createDelayExchange(String exchangeName, Boolean durable, Boolean autoDelete) {
        Map<String,Object> args = new HashMap<>(1);
        args.put("x-delayed-type", "direct");
        CustomExchange customExchange = new CustomExchange(exchangeName, "x-delayed-message", durable, autoDelete, args);
        this.rabbitAdmin.declareExchange(customExchange);
        return customExchange;
    }

    @Override
    public Queue createQueue(String queueName, Boolean durable, Boolean exclusive, Boolean autoDelete) {
        /**
         * exclusive：排他性，独有
         */
        Queue queue = new Queue(queueName, durable, exclusive, autoDelete);
        this.rabbitAdmin.declareQueue(queue);
        return queue;
    }

    @Override
    public Queue createDelayQueue(String queueName, Boolean durable, Boolean exclusive, Boolean autoDelete) {

        /** 延时参数配置 */
        Map<String,Object> args = new HashMap<>(1);
        args.put("x-dead-letter-exchange", RabbitmqUtil.getDeadExchangeName(queueName));

        Queue queue = new Queue(queueName, durable,exclusive,autoDelete,args);
        this.rabbitAdmin.declareQueue(queue);
        return queue;
    }

    @Override
    public Binding bindQueueToDirectExchange(DirectExchange directExchange, Queue queue, String routingKey) {
        Binding binding = BindingBuilder.bind(queue).to(directExchange).with(routingKey);
        this.rabbitAdmin.declareBinding(binding);
        return binding;
    }

    @Override
    public Binding bindQueueToTopicExchange(TopicExchange topicExchange, Queue queue, String routingKey) {
        Binding binding = BindingBuilder.bind(queue).to(topicExchange).with(routingKey);
        this.rabbitAdmin.declareBinding(binding);
        return binding;
    }

    @Override
    public Binding bindQueueToFanoutExchange(FanoutExchange fanoutExchange, Queue queue) {
        Binding binding = BindingBuilder.bind(queue).to(fanoutExchange);
        this.rabbitAdmin.declareBinding(binding);
        return binding;
    }

    @Override
    public Binding bindDelayQueueToExchange(Exchange exchange, Queue queue, String routingKey) {
        Binding bind = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
        this.rabbitAdmin.declareBinding(bind);
        return bind;
    }

    @Override
    public Binding bindDeadQueueToExchange(Exchange exchange, Queue queue, String routingKey) {
        Binding bind = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
        this.rabbitAdmin.declareBinding(bind);
        return bind;
    }


    /**
     * 发送普通消息，直连交换机好理解，如果是主题交换机的话，routingKey带有正则表达式的匹配特性
     * @param exchangeName
     * @param routeKey：上述的交换机与队列绑定默认是按照队列名称绑定的。
     * @param msg：消息
     */
    @Override
    public void sendMessage(String exchangeName, String routeKey, String msg) {
        this.rabbitTemplate.send(exchangeName,routeKey,new Message(msg.getBytes(),new MessageProperties()));
    }

    /**
     * 无路由的投递
     * @param exchangeName
     * @param msg
     */
    @Override
    public void senMessage(String exchangeName, String msg) {
        this.rabbitTemplate.send(exchangeName,null,new Message(msg.getBytes(),new MessageProperties()));
    }

    /**
     * 延时消息投递
     * @param exchangeName
     * @param routingKey
     * @param msg
     * @param delayTime
     */
    @Override
    public void sendDelayMessage(String exchangeName, String routingKey, String msg, Integer delayTime) {
        this.rabbitTemplate.setExchange(exchangeName);
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // 设置延时
                message.getMessageProperties().setDelay(delayTime);
                return message;
            }
        };
        this.rabbitTemplate.convertAndSend(exchangeName,routingKey,msg,messagePostProcessor);
    }
}
