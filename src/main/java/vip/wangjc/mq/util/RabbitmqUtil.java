package vip.wangjc.mq.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author wangjc
 * @title: RabbitmqUtil
 * @projectName wangjc-vip-mq
 * @date 2020/12/25 - 16:51
 */
public class RabbitmqUtil {

    private static final String dead_prefix = "dead-";

    /**
     * 获取死信交换机名称
     * @param exchange
     * @return
     */
    public static final String getDeadExchangeName(String exchange){
        return dead_prefix+"exchange-"+exchange;
    }

    /**
     * 获取死信队列名称
     * @param queueName
     * @return
     */
    public static final String getDeadQueueName(String queueName){
        return dead_prefix+"queue-"+queueName;
    }

    /**
     * 死信队列绑定的路由
     * @param routingKey
     * @return
     */
    public static final String getDeadRoutingKey(String routingKey){
        return dead_prefix+"routingKey-"+routingKey;
    }

    /*******************************************获取指定包路径下，特定注解声明的类*******************************************/

    /**
     * 获取指定包路径下，特定注解声明的类
     * @param packageName：指定包路径
     * @param annotationClass：特定注解声明的类
     * @param <A>
     * @return
     * @throws Exception
     */
    public static <A extends Annotation> Set<Class<?>> getAnnotationClasses(String packageName, Class<A> annotationClass){
        Set<Class<?>> res = new HashSet<>();

        Set<Class<?>> clsList = getClasses(packageName);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                if (cls.getAnnotation(annotationClass) != null) {
                    res.add(cls);
                }
            }
        }
        return res;
    }

    /**
     * 获取指定包路径下，所有的类
     * @param packageName
     * @return
     * @throws Exception
     */
    private static Set<Class<?>> getClasses(String packageName){
        Set<Class<?>> res = new HashSet<>();

        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    addClass(res, filePath, packageName);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            res.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static void addClass(Set<Class<?>> classes, String filePath, String packageName){
        File[] files = new File(filePath).listFiles(file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
        assert files != null;
        for (File file : files) {
            String fileName = file.getName();
            if (file.isFile()) {
                String classsName = fileName.substring(0, fileName.lastIndexOf("."));
                if (!packageName.isEmpty()) {
                    classsName = packageName + "." + classsName;
                }
                doAddClass(classes, classsName);
            }

        }
    }

    private static void doAddClass(Set<Class<?>> classes, final String classsName){
        ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return super.loadClass(name);
            }
        };
        try {
            classes.add(classLoader.loadClass(classsName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
