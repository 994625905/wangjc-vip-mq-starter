package vip.wangjc.mq.auto.properties;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import vip.wangjc.mq.entity.RabbitmqExchangeType;

import java.util.List;

/**
 * rabbitmq的交换机配置文件对应
 * @author wangjc
 * @title: RabbitmqProperties
 * @projectName wangjc-vip-mq
 * @date 2020/12/24 - 15:31
 */
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
     * 交换机类型（必填项）
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

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<Boolean> getDurables() {
        return durables;
    }

    public void setDurables(List<Boolean> durables) {
        this.durables = durables;
    }

    public List<Boolean> getAutoDeletes() {
        return autoDeletes;
    }

    public void setAutoDeletes(List<Boolean> autoDeletes) {
        this.autoDeletes = autoDeletes;
    }

    public List<RabbitmqExchangeType> getTypes() {
        return types;
    }

    public void setTypes(List<RabbitmqExchangeType> types) {
        this.types = types;
    }

    public List<AcknowledgeMode> getAckList() {
        return ackList;
    }

    public void setAckList(List<AcknowledgeMode> ackList) {
        this.ackList = ackList;
    }
}
