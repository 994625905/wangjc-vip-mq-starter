package vip.wangjc.mq.entity;

/**
 * 交换机类型
 * @author wangjc
 * @title: RabbitmqExchangeType
 * @projectName wangjc-vip-mq
 * @date 2020/12/24 - 15:50
 */
public enum RabbitmqExchangeType {

    /**
     * 直连，主题，广播，延时
     */
    direct,
    topic,
    fanout,
    delay;

    private RabbitmqExchangeType(){

    }
}
