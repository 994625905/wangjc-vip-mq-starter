package vip.wangjc.mq.register;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import vip.wangjc.mq.annotation.EnableRabbitMq;
import vip.wangjc.mq.annotation.RabbitConsumer;
import vip.wangjc.mq.consumer.abstracts.AbstractRabbitConsumerHandler;
import vip.wangjc.mq.entity.RabbitmqProjectType;
import vip.wangjc.mq.pool.RabbitConsumerPool;

import java.util.Set;

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

        /**
         * 消费者初始化
         */
        Enum<?> type = attributes.getEnum("type");
        if(!type.equals(RabbitmqProjectType.producer)){
            String[] packages = attributes.getStringArray("packages");
            if(packages == null || packages.length == 0){
                this.initRabbitConsumerPool(new Reflections("")); // 全路径扫描，从根路径开始
            }else{
                for(String pack:packages){
                    this.initRabbitConsumerPool(new Reflections(pack));
                }
            }
        }
    }


    /**
     * 初始化消费者缓存池
     * @param reflections
     */
    private void initRabbitConsumerPool(Reflections reflections){
        Set<Class<?>> consumerSet = reflections.getTypesAnnotatedWith(RabbitConsumer.class);
        for(Class<?> clazz:consumerSet){
            try {
                RabbitConsumer consumer = clazz.getAnnotation(RabbitConsumer.class);
                RabbitConsumerPool.set(consumer.queue(), (AbstractRabbitConsumerHandler) clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("init consumer pool error,reason:[{}]",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取mq项目的类型
     * @return
     */
    public static RabbitmqProjectType getProjectType(){
        return PROJECT_TYPE;
    }
}
