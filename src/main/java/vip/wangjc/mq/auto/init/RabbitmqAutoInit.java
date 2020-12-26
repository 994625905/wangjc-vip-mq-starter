package vip.wangjc.mq.auto.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import vip.wangjc.mq.auto.properties.RabbitmqAutoExchangeProperties;
import vip.wangjc.mq.auto.properties.RabbitmqAutoQueueProperties;
import vip.wangjc.mq.consumer.AbstractConsumerHandler;
import vip.wangjc.mq.entity.RabbitmqExchangeType;
import vip.wangjc.mq.entity.RabbitmqProjectType;
import vip.wangjc.mq.producer.service.ProducerService;
import vip.wangjc.mq.register.EnableRabbitmqRegister;
import vip.wangjc.mq.register.RabbitmqApplicationSelector;
import vip.wangjc.mq.util.RabbitmqUtil;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * rabbitmq初始化
 * @author wangjc
 * @title: RabbitmqAutoInit
 * @projectName wangjc-vip
 * @date 2020/12/24 - 15:25
 */
public class RabbitmqAutoInit {

    private static final Logger logger = LoggerFactory.getLogger(RabbitmqAutoInit.class);

    private final ConnectionFactory connectionFactory;

    private final ProducerService producerService;

    private final RabbitmqAutoExchangeProperties exchangeProperties;

    private final RabbitmqAutoQueueProperties queueProperties;

    private final RabbitmqProjectType projectType = EnableRabbitmqRegister.getProjectType();

    public RabbitmqAutoInit(ConnectionFactory connectionFactory, ProducerService producerService, RabbitmqAutoExchangeProperties exchangeProperties, RabbitmqAutoQueueProperties queueProperties){
        this.connectionFactory = connectionFactory;
        this.producerService = producerService;
        this.exchangeProperties = exchangeProperties;
        this.queueProperties = queueProperties;
    }

    /**
     * 自动初始化
     */
    @PostConstruct
    public void autoInit(){

        /** 交换机配置 */
        if(this.exchangeProperties.getNames() == null || this.exchangeProperties.getNames().size() == 0){
            logger.warn("RabbitMQ auto init,but exchange list is null");
            return;
        }

        for(int i=0;i<this.exchangeProperties.getNames().size();i++){

            String exchangeName = this.exchangeProperties.getNames().get(i);
            Boolean exchangeDurables = (this.exchangeProperties.getDurables() == null || this.exchangeProperties.getDurables().size() == 0)?true:this.exchangeProperties.getDurables().get(i);
            Boolean exchangeAutoDeletes = (this.exchangeProperties.getAutoDeletes() == null || this.exchangeProperties.getAutoDeletes().size() == 0)?false:this.exchangeProperties.getAutoDeletes().get(i);
            RabbitmqExchangeType exchangeType = this.exchangeProperties.getTypes().get(i);
            AcknowledgeMode ack = (this.exchangeProperties.getAckList() == null || this.exchangeProperties.getAckList().size() == 0)?AcknowledgeMode.NONE:this.exchangeProperties.getAckList().get(i);

            /** 直连交换机 */
            if(RabbitmqExchangeType.direct.equals(exchangeType)){
                DirectExchange directExchange = null;

                if(RabbitmqProjectType.producer.equals(this.projectType) || RabbitmqProjectType.all.equals(this.projectType)){
                    directExchange = this.producerService.createDirectExchange(exchangeName, exchangeDurables, exchangeAutoDeletes);
                    logger.info("RabbitMQ初始化创建直连交换机完毕：名称[{}]，持久化[{}]，自动删除[{}]",exchangeName,exchangeDurables,exchangeAutoDeletes);
                }

                /** 初始化队列：创建，绑定到对应的交换机，设置监听 */
                this.initQueue(exchangeType, directExchange, ack);
            }

            /** 主题交换机 */
            if(RabbitmqExchangeType.topic.equals(exchangeType)){
                TopicExchange topicExchange = null;

                if(RabbitmqProjectType.producer.equals(this.projectType) || RabbitmqProjectType.all.equals(this.projectType)){
                    topicExchange = this.producerService.createTopicExchange(exchangeName, exchangeDurables, exchangeAutoDeletes);
                    logger.info("RabbitMQ初始化创建主题交换机完毕：名称[{}]，持久化[{}]，自动删除[{}]",exchangeName,exchangeDurables,exchangeAutoDeletes);
                }

                /** 初始化队列：创建，绑定到对应的交换机，设置监听 */
                this.initQueue(exchangeType, topicExchange, ack);
            }

            /** 广播交换机 */
            if(RabbitmqExchangeType.fanout.equals(exchangeType)){
                FanoutExchange fanoutExchange = null;

                if(RabbitmqProjectType.producer.equals(this.projectType) || RabbitmqProjectType.all.equals(this.projectType)){
                    fanoutExchange = this.producerService.createFanoutExchange(exchangeName, exchangeDurables, exchangeAutoDeletes);
                    logger.info("RabbitMQ初始化创建广播交换机完毕：名称[{}]，持久化[{}]，自动删除[{}]",exchangeName,exchangeDurables,exchangeAutoDeletes);
                }

                /** 初始化队列：创建，绑定到对应的交换机，设置监听 */
                this.initQueue(exchangeType, fanoutExchange, ack);
            }

            /** 延时交换机，采用直连替代 */
            if(RabbitmqExchangeType.delay.equals(exchangeType)){
                CustomExchange delayExchange = null;

                if(RabbitmqProjectType.producer.equals(this.projectType) || RabbitmqProjectType.all.equals(this.projectType)){
                    delayExchange = this.producerService.createDelayExchange(exchangeName, exchangeDurables, exchangeAutoDeletes);
                    logger.info("RabbitMQ初始化创建延时交换机完毕：名称[{}] ",exchangeName);
                }

                /** 初始化队列：创建，绑定到对应的交换机，设置监听 */
                this.initQueue(exchangeType, delayExchange, ack);
            }
        }
    }

    /**
     * 初始化队列：创建，绑定到对应的交换机，设置监听
     * @param exchangeType
     * @param exchange
     * @param ack
     */
    private void initQueue(RabbitmqExchangeType exchangeType, Exchange exchange, AcknowledgeMode ack){

        List<Object> queueNameList = this.queueProperties.getBindExchange().get(exchange.getName() + ".name");
        List<Object> queueDurableList = this.queueProperties.getBindExchange().get(exchange.getName() + ".durable");
        List<Object> queueAutoDeleteList = this.queueProperties.getBindExchange().get(exchange.getName() + ".autoDelete");
        List<Object> queueExclusiveList = this.queueProperties.getBindExchange().get(exchange.getName() + ".exclusive");
        List<Object> queueRoutingKeyList = this.queueProperties.getBindExchange().get(exchange.getName() + ".routingKey");

        if(queueNameList == null || queueNameList.size() == 0){
            logger.warn("there is no queue under the exchange[{}]",exchange.getName());
            return;
        }
        for(int j=0;j<queueNameList.size();j++){

            String queueName = (String) queueNameList.get(j);

            /** producer和 all需要创建绑定队列 */
            if(RabbitmqProjectType.producer.equals(this.projectType) || RabbitmqProjectType.all.equals(this.projectType)){

                Boolean queueDurable = (queueDurableList == null || queueDurableList.size() == 0)?true: Boolean.parseBoolean(queueDurableList.get(j).toString());
                Boolean queueAutoDelete = (queueAutoDeleteList == null || queueAutoDeleteList.size() == 0)?false: Boolean.parseBoolean(queueAutoDeleteList.get(j).toString());
                Boolean queueExclusive = (queueExclusiveList == null || queueExclusiveList.size() == 0)?false: Boolean.parseBoolean(queueExclusiveList.get(j).toString());
                String queueRoutingKey = (String) queueRoutingKeyList.get(j);

                /** 延时队列的独特创建 */
                Queue queue = null;
                if(exchangeType == RabbitmqExchangeType.delay){
                    queue = this.producerService.createDelayQueue(queueName, queueDurable, queueExclusive, queueAutoDelete);
                }else{
                    queue = this.producerService.createQueue(queueName, queueDurable, queueExclusive, queueAutoDelete);
                }

                if(RabbitmqExchangeType.direct.equals(exchangeType)){
                    this.producerService.bindQueueToDirectExchange((DirectExchange) exchange, queue, queueRoutingKey);
                }
                if(RabbitmqExchangeType.topic.equals(exchangeType)){
                    this.producerService.bindQueueToTopicExchange((TopicExchange) exchange, queue, queueRoutingKey);
                }
                if(RabbitmqExchangeType.fanout.equals(exchangeType)){
                    this.producerService.bindQueueToFanoutExchange((FanoutExchange) exchange, queue);
                }
                if(RabbitmqExchangeType.delay.equals(exchangeType)){
                    this.producerService.bindDelayQueueToExchange( exchange, queue, queueRoutingKey);

                    /** 延时队列的后置-->准备一个死信交换机（直连替代），死信队列 */
                    Exchange deadExchange = this.producerService.createDirectExchange(RabbitmqUtil.getDeadExchangeName(queueName),exchange.isDurable(),exchange.isAutoDelete());
                    Queue deadQueue = this.producerService.createQueue(RabbitmqUtil.getDeadQueueName(queueName), queueDurable, queueExclusive, queueAutoDelete);
                    this.producerService.bindDeadQueueToExchange(deadExchange,deadQueue,RabbitmqUtil.getDeadRoutingKey(queueName));
                }

                if(!RabbitmqExchangeType.fanout.equals(exchangeType)){
                    logger.info("RabbitMQ初始化创建队列完毕，名称[{}]，持久化[{}]，排他性[{}],自动删除[{}]，绑定交换机[{}]，路由key[{}]",queueName,queueDurable,queueExclusive,queueAutoDelete,exchange.getName(),queueRoutingKey);
                }else{
                    logger.info("RabbitMQ初始化创建队列完毕，名称[{}]，持久化[{}]，排他性[{}],自动删除[{}]，绑定交换机[{}]",queueName,queueDurable,queueExclusive,queueAutoDelete,exchange.getName());
                }
            }
            /** consumer和 all需要设置监听 */
            if(RabbitmqProjectType.consumer.equals(this.projectType) || RabbitmqProjectType.all.equals(this.projectType)){
                this.addMessageListener(queueName, ack);

                /** 是否存在死信队列的监听，有延时就有死信 */
                if(RabbitmqExchangeType.delay.equals(exchangeType)){
                    this.addMessageListener(RabbitmqUtil.getDeadQueueName(queueName),ack);
                }
            }
        }
    }

    /**
     * 添加消息监听器，（手动cache异常）
     * @param queueName
     * @param ack
     */
    private void addMessageListener(String queueName, AcknowledgeMode ack){
        try {
            AbstractConsumerHandler consumerHandler = RabbitmqApplicationSelector.getBean(AbstractConsumerHandler.class);
            if(consumerHandler != null){

                SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
                container.setConnectionFactory(this.connectionFactory);
                container.setQueueNames(queueName);
                container.setAcknowledgeMode(ack);

                if(ack == AcknowledgeMode.MANUAL){ // 人为手动确认消息被消费的队列
                    consumerHandler.addManualQueue(queueName);
                }

                /** 消息监听器适配器，已适配的方式来接收消息 */
                MessageListenerAdapter adapter = new MessageListenerAdapter(consumerHandler);
                container.setMessageListener(adapter);
                container.start();
                logger.info("已成功开始监听RabbitMQ异步消息：queue[{}] ",queueName);
            }else{
                logger.info("======================= 消息监听器缺少对应的处理类 ======================");
            }
        }catch (Exception e){
            logger.error(e.getMessage());
            logger.error("该应用下缺乏对应的消息监听器");
        }
    }
}
