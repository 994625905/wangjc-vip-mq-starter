package vip.wangjc.mq.annotation;

import org.springframework.context.annotation.Import;
import vip.wangjc.mq.callback.MsgSendConfirmCallBack;
import vip.wangjc.mq.callback.MsgSendReturnCallBack;
import vip.wangjc.mq.entity.RabbitmqProjectType;
import vip.wangjc.mq.register.EnableRabbitmqRegister;
import vip.wangjc.mq.register.RabbitmqApplicationSelector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：开启wangjc-vip-mq
 * @author wangjc
 * @title: EnableRabbitMq
 * @projectName wangjc-vip-mq
 * @date 2020/12/24 - 14:01
 */
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
     * 扫描的包路径（针对消费者使用）
     * @return
     */
    String[] packages() default {};
}
