package vip.wangjc.mq.entity;

/**
 * 项目类型，生产者还是服务者，或者是双者双存
 * @author wangjc
 * @title: RabbitmqExchangeType
 * @projectName wangjc-vip
 * @date 2020/12/24 - 15:50
 */
public enum RabbitmqProjectType {

    /**
     * 生产者，消费者，双者双存（默认）
     */

    producer,
    consumer,
    all;

    RabbitmqProjectType(){

    }
}
