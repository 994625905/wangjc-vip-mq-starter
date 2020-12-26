package vip.wangjc.mq.auto.properties;

import vip.wangjc.mq.entity.RabbitmqProjectType;

/**
 * rabbitmq的交换机配置文件对应，改用注解设定
 * @author wangjc
 * @title: RabbitmqProperties
 * @projectName wangjc-vip
 * @date 2020/12/24 - 15:31
 */
public class RabbitmqAutoProperties {

    /**
     * mq项目的类型
     */
    private RabbitmqProjectType type;

    public RabbitmqProjectType getType() {
        return type;
    }

    public void setType(RabbitmqProjectType type) {
        this.type = type;
    }
}
