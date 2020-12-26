package vip.wangjc.mq.auto.configure;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vip.wangjc.mq.auto.init.RabbitmqAutoInit;
import vip.wangjc.mq.auto.properties.RabbitmqAutoExchangeProperties;
import vip.wangjc.mq.auto.properties.RabbitmqAutoQueueProperties;
import vip.wangjc.mq.callback.MsgSendConfirmCallBack;
import vip.wangjc.mq.callback.MsgSendReturnCallBack;
import vip.wangjc.mq.producer.service.ProducerService;
import vip.wangjc.mq.producer.service.impl.ProducerServiceImpl;
import vip.wangjc.mq.producer.template.DefinedRabbitTemplate;

/**
 * rabbitmq的自动配置
 * @author wangjc
 * @title: RabbitmqAutoConfigure
 * @projectName wangjc-vip
 * @date 2020/12/24 - 14:31
 */
@Configuration
@ConditionalOnClass(ConnectionFactory.class)
@EnableConfigurationProperties({RabbitmqAutoExchangeProperties.class, RabbitmqAutoQueueProperties.class})
public class RabbitmqAutoConfiguration {

    private final RabbitmqAutoExchangeProperties exchangeProperties;

    private final RabbitmqAutoQueueProperties queueProperties;

    public RabbitmqAutoConfiguration(RabbitmqAutoExchangeProperties exchangeProperties, RabbitmqAutoQueueProperties queueProperties){
        this.exchangeProperties = exchangeProperties;
        this.queueProperties = queueProperties;
    }

    /**
     * 定义rabbitAdmin，创建交换机/队列，相互绑定
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * 添加参数rabbitTemplate用于数据的接收和发送
     * @param rabbitTemplate：模板
     * @param confirmCallback：消息发送的确认
     * @param returnCallback：消息发送的返回
     * @return
     */
    @Bean
    @ConditionalOnBean(value = {MsgSendConfirmCallBack.class,MsgSendReturnCallBack.class})
    public DefinedRabbitTemplate definedRabbitTemplate(RabbitTemplate rabbitTemplate, MsgSendConfirmCallBack confirmCallback, MsgSendReturnCallBack returnCallback){

        /**
         * 使用confirm-callback：
         * 配置文件：spring.rabbitmq.publisher-confirms=true已经过时，需改为：spring.rabbitmq.publisher-confirm-type=correlated，（默认为none）
         *
         * 使用return-callback：
         * 配置文件：spring.rabbitmq.publisher-returns=true，
         */
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnsCallback(returnCallback);

        /**
         * return-callback：额外配置，或者配置文件中：spring.rabbitmq.template.mandatory=true
         * 表示开启强制消息投递（mandatory为设置为true），但消息未被路由至任何一个队列时，则回退一条消息到RabbitTemplate.ReturnCallback中的returnedMessage方法中，即return-callback生效
         * 强制型，优先级高于spring.rabbitmq.publisher-returns
         */
        rabbitTemplate.setMandatory(true);
        return new DefinedRabbitTemplate(rabbitTemplate);
    }

    /**
     * 注册生产者bean
     * @param rabbitAdmin
     * @param definedRabbitTemplate
     * @return
     */
    @Bean
    @ConditionalOnBean({RabbitAdmin.class,DefinedRabbitTemplate.class})
    public ProducerService producerService(RabbitAdmin rabbitAdmin, DefinedRabbitTemplate definedRabbitTemplate){
        return new ProducerServiceImpl(rabbitAdmin, definedRabbitTemplate);
    }

    /**
     * 自动初始化
     * @param producerService
     * @return
     */
    @Bean
    @ConditionalOnBean(ProducerService.class)
    public RabbitmqAutoInit rabbitmqAutoInit(ConnectionFactory connectionFactory, ProducerService producerService){
        return new RabbitmqAutoInit(connectionFactory, producerService, this.exchangeProperties, this.queueProperties);
    }
}
