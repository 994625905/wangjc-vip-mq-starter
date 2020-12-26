package vip.wangjc.mq.register;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * bean的查找器
 * @author wangjc
 * @title: RabbitmqBeanSelector
 * @projectName wangjc-vip
 * @date 2020/12/24 - 19:45
 */
public class RabbitmqApplicationSelector implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    /**
     * 查找bean
     * @param beanName
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName,Class<T> clazz){
        return applicationContext.getBean(beanName,clazz);
    }

    /**
     * 查找bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取属性文件的值
     * @param key
     * @return
     */
    public static String getProperties(String key){
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 获取属性文件的配置项
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getProperties(String key,Class<T> clazz){
        return applicationContext.getEnvironment().getProperty(key,clazz);
    }
}
