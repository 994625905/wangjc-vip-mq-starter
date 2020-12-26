package vip.wangjc.mq.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import vip.wangjc.mq.annotation.EnableRabbitMq;
import vip.wangjc.mq.entity.RabbitmqProjectType;

/**
 * rabbitmq的注册器
 * @author wangjc
 * @title: EnableRabbitmqRegister
 * @projectName wangjc-vip-mq
 * @date 2020/12/24 - 14:05
 */
public class EnableRabbitmqRegister implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(EnableRabbitmqRegister.class);

    private static RabbitmqProjectType PROJECT_TYPE;

    /**
     * 注册bean定义
     * @param importingClassMetadata：导入类的元数据
     * @param registry：bean注册器
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRabbitMq.class.getName()));

        Class<?> confirmCallback = attributes.getClass("confirm");
        Class<?> returnCallback = attributes.getClass("returns");
        PROJECT_TYPE = attributes.getEnum("type");

        /**
         * 将confirmCallback和returnCallback注册到spring容器，将枚举也注册到spring容器
         * 核心类为：RootBeanDefinition
         */
        RootBeanDefinition confirmCallbackBeanDefinition = new RootBeanDefinition(confirmCallback);
        RootBeanDefinition returnCallbackBeanDefinition = new RootBeanDefinition(returnCallback);

        registry.registerBeanDefinition("confirmCallback",confirmCallbackBeanDefinition);
        registry.registerBeanDefinition("returnCallback",returnCallbackBeanDefinition);

        logger.info("[{}] has been registered to the bean container",confirmCallback.getName());
        logger.info("[{}] has been registered to the bean container",returnCallback.getName());
    }

    /**
     * 获取mq项目的类型
     * @return
     */
    public static RabbitmqProjectType getProjectType(){
        return PROJECT_TYPE;
    }
}
