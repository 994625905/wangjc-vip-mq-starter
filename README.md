# wangjc-vip-mq-starter
Rabbitmq的Java快速集成方案

# 一、简介

wangjc-vip-mq-starter告诉你，关于集成rabbitmq，几乎所有的准备工作它都已经完成，初始化，投递成功，投递失败，消费确认……不用再去在乎配置项，只需专注于业务场景的使用和消息分路由投放即可。



一切功能包括但不限于交换机，队列的创建、绑定、删除；直连投递，主题订阅，广播发送，延时，死信队列……，对外提供生产者消息推送的接口和消费者适配接收的抽象类，在应用该组件时只需要在配置注解中指明该应用的类型（生产者，消费者，双存）即可。



PS：零代码，极少数配置集成该组件，便可使用全量的功能；交换机，队列采用属性配置文件的方式完成初始化。



另外：如果想了解初始化/消息投递/消费……等详细信息，可以将日志层级改为debug：

```properties
#vip.wangjc下的所有组件都开启debug，如果只想开启哪一个，细化到包名
logging.level.vip.wangjc=debug
```

------------

# 二、功能简介

## 集成步骤

### 1.提供属性配置文件完成交换机的初始化

多个交换机就以逗号隔开的方式；有默认属性的可以不用填（填就必须填完整	），

```java
    @ConfigurationProperties(prefix = "vip.wangjc.mq.exchange")
    public class RabbitmqAutoExchangeProperties {
	/**
	 * 交换机名称集合（必填项）
	 */
	private List<String> names;

	/**
	 * 交换机的持久化：true/false，（默认true）
	 */
	private List<Boolean> durables;

	/**
	 * 自动删除：当所有队列在完成使用此exchange时，是否删除。true/false，（默认false）
	 */
	private List<Boolean> autoDeletes;

	/**
	 * 交换机类型（必填项：直连，主题，广播，延时……）
	 */
	private List<RabbitmqExchangeType> types;

	/**
	 * 消息监听的确认模式：
	 * AcknowledgeMode.NONE：无ack模式，server默认推送的所有消息都已经被消费成功，自己不会暂存，会不断地向消费端推送消息
	 * AcknowledgeMode.AUTO：有ack模式，自动确认，由spring-rabbit依据消息处理逻辑是否抛出异常自动发送ack（无异常）或nack（异常）到server端，无异常自动ack
	 * AcknowledgeMode.MANUAL：有ack模式，手动确认，需要人为地获取到channel之后调用方法向server发送ack（或消费失败时的nack）信息
	 *
	 * server端行为（以有ack模式为例）：
	 * rabbitmq server推送给每个channel的消息数量有限制，会保证每个channel没有收到ack的消息数量不会超过{prefetchCount}
	 * server端会暂存没有收到ack的消息，等消费端ack后才会丢掉；如果收到消费端的nack（消费失败的标识）或connection断开没收到反馈，会将消息放回到原队列头部。
	 *
	 * 如上所示：
	 * 无ack模式：效率高，server不断推送，推送完就丢掉，存在丢失大量消息的风险。
	 * 有ack模式：效率低，server需要接收反馈，但数据准确，不会丢消息。
	 *
	 * {prefetchCount}
	 * spring.rabbitmq.listener.direct.prefetch= 每个消费者可最大处理的nack消息数量.
	 * spring.rabbitmq.listener.simple.prefetch= 一个消费者最多可处理的nack消息数量，如果有事务的话，必须大于等于transaction数量.
	 *
	 * （默认为AcknowledgeMode.NONE）
	 */
	private List<AcknowledgeMode> ackList;
	…………省略setting/getting
    }
```

eg：对应的配置项为例（创建三台交换机）

```properties
vip.wangjc.mq.exchange.names=wangjc-direct,wangjc-topic,wangjc-delay
vip.wangjc.mq.exchange.types=direct,topic,delay
vip.wangjc.mq.exchange.ackList=MANUAL,AUTO,AUTO
```

### 2.提供属性配置文件完成具体交换机上应该绑定的队列初始化

跟上文创建的交换机相对应，采用预设值匹配的方式来获取，构建的方式与交换机一致：多个以逗号隔开；有默认属性的可以不用填（填就必须填完整	）

```java
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
	 * 与交换机对应的队列：bind-exchange
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
```

eg：对应的配置项为例（三台交换机下的队列）

```properties
#直连交换机--队列
vip.wangjc.mq.queue.bind-exchange.wangjc-direct.name=direct-queue1,direct-queue2
vip.wangjc.mq.queue.bind-exchange.wangjc-direct.routingKey=direct-routing1,direct-routing2

#主题交换机--队列
vip.wangjc.mq.queue.bind-exchange.wangjc-topic.name=topic-queue
vip.wangjc.mq.queue.bind-exchange.wangjc-topic.routingKey=topic-routing

#延时交换机--队列
vip.wangjc.mq.queue.bind-exchange.wangjc-delay.name=delay-queue
vip.wangjc.mq.queue.bind-exchange.wangjc-delay.routingKey=delay-routing
vip.wangjc.mq.queue.bind-exchange.wangjc-delay.durable=true
vip.wangjc.mq.queue.bind-exchange.wangjc-delay.autoDelete=false
vip.wangjc.mq.queue.bind-exchange.wangjc-delay.exclusive=false
```

### 3.添加注解，开启wangjc-vip-mq

只需要在主类上，或者是任何配置类上添加该注解，然后根据情况选择设置参数即可开启，其中RabbitmqProjectType比较重要，表明该应用是生产者还是消费者，默认为双存的（生产者/消费者位于同一应用下，只是不同线程来处理），如果是生产者，则只提供对应的消息发送投递功能；如果是消费者，则只拥有消息接收和确认的功能。MsgSendConfirmCallBack和MsgSendReturnCallBack都可以自定义子类，重写方法在其中完成定制化业务。

```java
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Import({EnableRabbitmqRegister.class, RabbitmqApplicationSelector.class})
    public @interface EnableRabbitMq {
	/**
	 * mq项目端的类型，默认为双存（生产者和消费者并存）
	 * @return
	 */
	RabbitmqProjectType type() default RabbitmqProjectType.all;

	/**
	 * 消息发送到交换机的确认机制，成功回调
	 * @return
	 */
	Class<? extends MsgSendConfirmCallBack> confirm() default MsgSendConfirmCallBack.class;

	/**
	 * 消息发送到交换机的返回机制，失败回调
	 * @return
	 */
	Class<? extends MsgSendReturnCallBack> returns() default MsgSendReturnCallBack.class;
        
    /**
     * 扫描的消费者包路径（针对消费者使用）
     * @return
     */
    String[] packages() default {};
    }
```

## 生产者

自动注册了ProducerService的bean，作为应用项目，只需要关注后三个发送消息的方法即可，至于前面提供的功能，都是vip-wangjc-mq-starter在完成初始化时用到的。

```java
    public interface RabbitProducerService {
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
	Binding bindQueueToDirectExchange(DirectExchange directExchange,Queue queue,String routingKey);

	/**
	 * 将队列绑定到主题交换机
	 * @param topicExchange
	 * @param queue
	 * @param routingKey
	 */
	Binding bindQueueToTopicExchange(TopicExchange topicExchange,Queue queue,String routingKey);

	/**
	 * 将队列绑定到广播交换机
	 * @param fanoutExchange
	 * @param queue
	 */
	Binding bindQueueToFanoutExchange(FanoutExchange fanoutExchange,Queue queue);

	/**
	 * 绑定延时队列到交换机上
	 * @param exchange
	 * @param queue
	 * @param routingKey
	 * @return
	 */
	Binding bindDelayQueueToExchange(Exchange exchange,Queue queue,String routingKey);

	/**
	 * 绑定死信队列到交换机上，跟延时队列并用
	 * @param exchange
	 * @param queue
	 * @param routingKey
	 * @return
	 */
	Binding bindDeadQueueToExchange(Exchange exchange,Queue queue,String routingKey);

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
	void senMessage(String exchangeName,String msg);

	/**
	 * 发送延时消息到交换机上
	 * @param exchangeName
	 * @param routingKey
	 * @param msg
	 * @param delayTime
	 */
	void sendDelayMessage(String exchangeName, String routingKey, String msg, Integer delayTime);
    }
```

## 消费者

为了秉承零代码的极简化模式，采用监听器适配器的方式来接收消息，抽象类如下：



```java
    public abstract class AbstractRabbitConsumerHandler implements ChannelAwareMessageListener {
	private static final Logger logger = LoggerFactory.getLogger(AbstractRabbitConsumerHandler.class);

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
```

消费端只需要继承该抽象类，添加注解RabbitConsumer，重写handleMessage方法来执行根据消息具体的业务逻辑即可。

## 消费者声明注解

为了将业务逻辑充分解耦，强制将消费者分类处理，采用注解来声明消费者：一个队列可以有多个消费者（至少一个，否则消息将在队列中堆积），多线程支持，默认负载均衡的轮训策略。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RabbitConsumer {

    /**
     * 监听的队列名：必填项
     * @return
     */
    String queue();
}
```



------------

# 三、代码结构说明

包结构前缀统一为：vip.wangjc.mq。然后下面根据职责细分：注解，自动配置，自动初始化，自动加载属性文件值，回调函数，实体类，消费者，生产者，注册器，工具……其中，回调函数可以被继承完成定制化操作，然后在注解中指定。

ProducerService对外提供自动注入的bean，做消息投递；AbstractConsumerHandler对外提供有待重写的方法，只操作具体的业务逻辑。

```java
├─src
	│  ├─main
	│  │  ├─java
	│  │  │  └─vip
	│  │  │      └─wangjc
	│  │  │          └─mq
	│  │  │              ├─annotation
	│  │  │              │      EnableRabbitMq.java （注解：开启wangjc-vip-mq组件）
    │  │  │              │      RabbitConsumer.java （注解：声明消费者）
	│  │  │              │
	│  │  │              ├─auto
	│  │  │              │  ├─configure
	│  │  │              │  │      RabbitmqAutoConfiguration.java （自动注入）
	│  │  │              │  │
	│  │  │              │  ├─init
	│  │  │              │  │      RabbitmqAutoInit.java （读取配置信息完成初始化工作）
	│  │  │              │  │
	│  │  │              │  └─properties
	│  │  │              │          RabbitmqAutoExchangeProperties.java （属性配置文件对应的值--交换机）
	│  │  │              │          RabbitmqAutoProperties.java （决定当前应用的类型）
	│  │  │              │          RabbitmqAutoQueueProperties.java （属性配置文件对应的值--队列）
	│  │  │              │
	│  │  │              ├─callback
	│  │  │              │      MsgSendConfirmCallBack.java （confirm回调函数）
	│  │  │              │      MsgSendReturnCallBack.java （returns回调函数）
	│  │  │              │
	│  │  │              ├─consumer
	│  │  │              │      AbstractRabbitConsumerHandler.java （消费者的抽象类）
	│  │  │              │
	│  │  │              ├─entity
	│  │  │              │      RabbitmqExchangeType.java （枚举：交换机类型）
	│  │  │              │      RabbitmqProjectType.java （枚举：当前应用类型）
	│  │  │              │
    │  │  │              ├─pool
	│  │  │              │      RabbitConsumerPool.java （消费者的缓存池）
	│  │  │              │
	│  │  │              ├─producer
	│  │  │              │  ├─service
	│  │  │              │  │  │  RabbitProducerService.java （接口：生产者功能）
	│  │  │              │  │  │
	│  │  │              │  │  └─impl
	│  │  │              │  │          RabbitProducerServiceImpl.java （生产者功能的实现）
	│  │  │              │  │
	│  │  │              │  └─template
	│  │  │              │          DefinedRabbitTemplate.java （自定义的RabbitTemplate，添加一些set项）
	│  │  │              │
	│  │  │              ├─register
	│  │  │              │      EnableRabbitmqRegister.java （注册器，在初始化之前完成）
	│  │  │              │      RabbitmqApplicationSelector.java （Spring上下文内容选择器）
	│  │  │              │
	│  │  │              └─util
	│  │  │                      RabbitmqUtil.java （工具类：格式化命名）
	│  │  │
	│  │  └─resources
	│  │      └─META-INF
	│  │              spring.factories （springboot的自动化配置支持）
	│
```



**UML图如下：**
![wangjc-vip-mq-starter类图](http://www.wangjc.vip/group1/M00/00/01/rBAAD1_p1OqAKpuWAAK3Ko6wfQs902.png "wangjc-vip-mq-starter类图")

------------

# 四、使用方式

## 1.添加依赖

```xml
		<!-- springboot集成rabbitmq依赖 -->
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-amqp</artifactId>
  		</dependency>

  		<dependency>
  			<groupId>vip.wangjc</groupId>
  			<artifactId>wangjc-vip-mq-starter</artifactId>
  			<version>查看maven中央仓库release最新版本</version>
  		</dependency>
```



## 2.配置交换机和队列

```properties
  #rabbitMQ消息队列）
  spring.rabbitmq.host=127.0.0.1
  spring.rabbitmq.port=5672
  spring.rabbitmq.username=guest
  spring.rabbitmq.password=guest
  spring.rabbitmq.publisher-returns=true
  spring.rabbitmq.publisher-confirm-type=correlated
  vip.wangjc.mq.exchange.names=wangjc-direct,wangjc-topic,wangjc-delay
  vip.wangjc.mq.exchange.types=direct,topic,delay
  vip.wangjc.mq.exchange.ackList=MANUAL,AUTO,AUTO
  #直连交换机--队列
  vip.wangjc.mq.queue.bind-exchange.wangjc-direct.name=direct-queue1,direct-queue2
  vip.wangjc.mq.queue.bind-exchange.wangjc-direct.routingKey=direct-routing1,direct-routing2
  #主题交换机--队列
  vip.wangjc.mq.queue.bind-exchange.wangjc-topic.name=topic-queue
  vip.wangjc.mq.queue.bind-exchange.wangjc-topic.routingKey=topic-routing
  #延时交换机--队列
  vip.wangjc.mq.queue.bind-exchange.wangjc-delay.name=delay-queue
  vip.wangjc.mq.queue.bind-exchange.wangjc-delay.routingKey=delay-routing
  vip.wangjc.mq.queue.bind-exchange.wangjc-delay.durable=true
  vip.wangjc.mq.queue.bind-exchange.wangjc-delay.autoDelete=false
  vip.wangjc.mq.queue.bind-exchange.wangjc-delay.exclusive=false
```

## 3.添加核心注解

当然，注解参数都可以采用默认配置的。

type如果注明all或者consumer的话，可以顺手设置packages，当然不设置也可以，只不过扫描范围将被扩大到从根目录开始，项目大的话比较耗时。

```java
@EnableRabbitMq(type = RabbitmqProjectType.all,confirm = SendConfirmCallBack.class,returns = SendReturnCallBack.class,packages = {"vip.wangjc.test.mq.consumer"})
```

## 4.生产者--消息投递

```java
    @RestController
    @RequestMapping(value = "mq")
    public class ProducerController {
	@Autowired
	private RabbitProducerService producerService;

	@RequestMapping(value = "sendDirect")
	public Object sendDirect(){
		User user = new User();
		this.producerService.sendMessage("wangjc-direct","direct-routing1", JSON.toJSONString(user));
		return user;
	}

	@RequestMapping(value = "sendTopic")
	public Object sendTopic(){
		User user = new User();
		this.producerService.sendMessage("wangjc-topic","topic-routing", JSON.toJSONString(user));
		return user;
	}

	@RequestMapping(value = "sendDelay")
	public Object sendDelay(){
		User user = new User();
		this.producerService.sendDelayMessage("wangjc-delay","delay-routing", JSON.toJSONString(user),20000);
		return user;
	}
    }
```

## 5.消费者--消息处理

多个消费者测试负载均衡

```java
@RabbitConsumer(queue = "direct-queue1")
public class ConsumerHandler extends AbstractConsumerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    @Override
    public Boolean handleMessage(String msg, Channel channel, String queue) {

        logger.info(msg);
        logger.info(queue);

        return true;
    }
}
```

```java
@RabbitConsumer(queue = "direct-queue1")
public class ConsumerHandlerMore extends AbstractConsumerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandlerMore.class);

    @Override
    public Boolean handleMessage(String msg, Channel channel, String queue) {

        logger.info(msg);
        logger.info(queue);

        return true;
    }
}
```

```java
@RabbitConsumer(queue = "direct-queue1")
public class ConsumerHandlerThree extends AbstractConsumerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandlerThree.class);

    @Override
    public Boolean handleMessage(String msg, Channel channel, String queue) {

        logger.info(msg);
        logger.info(queue);

        return true;
    }
}
```

更多的业务场景，我就不一一做演示了。
详情代码信息请前往开源平台，然后顺手给个star呗！