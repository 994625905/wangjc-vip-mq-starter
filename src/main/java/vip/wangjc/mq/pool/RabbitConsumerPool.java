package vip.wangjc.mq.pool;

import vip.wangjc.mq.consumer.AbstractRabbitConsumerHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费者缓存池
 * @author wangjc
 * @title: RabbitConsumerPool
 * @projectName wangjc-vip
 * @date 2020/12/28 - 17:50
 */
public class RabbitConsumerPool {

    /**
     * 缓存池：
     * 第一个string：queueName，
     * 第二个string：className，
     * 可能就是同一个队列名下有多个不同的监听器
     */
    private static Map<String, Map<String, AbstractRabbitConsumerHandler>> CONSUMER_POOL = new ConcurrentHashMap<>();

    /**
     * 缓存池中设置消费者，全类名+队列名
     * @param queueName
     * @param consumerHandler
     */
    public static void set(String queueName,AbstractRabbitConsumerHandler consumerHandler){
        String className = consumerHandler.getClass().getName();
        synchronized (CONSUMER_POOL){

            if(CONSUMER_POOL.containsKey(queueName)){
                CONSUMER_POOL.get(queueName).put(className,consumerHandler);
            }else{
                Map<String,AbstractRabbitConsumerHandler> consumerMap = new HashMap<>(1);
                consumerMap.put(className,consumerHandler);
                CONSUMER_POOL.put(queueName,consumerMap);
            }
        }
    }

    /**
     * 从缓存池中获取队列的消费者列表
     * @param queueName
     */
    public static List<AbstractRabbitConsumerHandler> get(String queueName){
        if(!CONSUMER_POOL.containsKey(queueName)){
            return null;
        }
        synchronized (CONSUMER_POOL){
            Set<String> keySet = CONSUMER_POOL.get(queueName).keySet();
            List<AbstractRabbitConsumerHandler> list = new ArrayList<>();
            for(String key:keySet){
                list.add(CONSUMER_POOL.get(queueName).get(key));
            }
            return list;
        }
    }
}
