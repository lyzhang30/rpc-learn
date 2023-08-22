package cn.zly.rpcstudy.consumer;

import cn.zly.rpcstudy.annotation.RpcReference;
import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.filter.FilterConfig;
import cn.zly.rpcstudy.protocol.serialization.SerializationFactory;
import cn.zly.rpcstudy.register.RegisterFactory;
import cn.zly.rpcstudy.router.LoadBalancerFactory;
import cn.zly.rpcstudy.utils.PropertiesUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @author zhanglianyong
 * 2023-08-19 9:57
 */
public class ConsumerPostProcessor implements BeanPostProcessor, EnvironmentAware, InitializingBean {

    RpcProperties rpcProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        SerializationFactory.init();
        RegisterFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initClientFilter();
    }

    @Override
    public void setEnvironment(Environment environment) {
        RpcProperties properties = RpcProperties.getInstance();
        PropertiesUtils.init(properties, environment);
        this.rpcProperties = properties;
        System.out.println("加载配置文件成功");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        final Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcReference.class)) {
                final RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                final Class<?> fieldTypeClass = field.getType();
                field.setAccessible(true);
                Object object = null;
                try {
                    // proxy
                    object = Proxy.newProxyInstance(
                            fieldTypeClass.getClassLoader(),
                            new Class<?>[]{fieldTypeClass},
                            new RpcInvokerProxy(
                                    rpcReference.serviceVersion(),
                                    rpcReference.timeout(),
                                    rpcReference.faultTolerant(),
                                    rpcReference.loadBalancer(),
                                    rpcReference.retryCount()

                            ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    field.set(bean, object);
                    field.setAccessible(false);
                    System.out.println(beanName + " field: " + field.getName() + "注入成功");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    System.out.println(beanName + " field: " + field.getName() + "注入失败");
                }
            }
        }
        return bean;
    }
}
