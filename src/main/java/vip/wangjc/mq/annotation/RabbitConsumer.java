package vip.wangjc.mq.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明消费者：可以一个队列多个消费者：默认负载均衡的轮训策略
 * @author wangjc
 * @title: RabbitConsumer
 * @projectName wangjc-vip-mq
 * @date 2020/12/28 - 17:22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RabbitConsumer {

    /**
     * 监听的队列名：必填项
     * @return
     */
    String queue();

}
