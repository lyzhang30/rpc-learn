package cn.zly.rpcstudy.router;

import cn.zly.rpcstudy.spi.ExtensionLoader;

/**
 * @author zhanglianyong
 * 2023-08-20 16:12
 */
public class LoadBalancerFactory {

    public static LoadBalancer get(String loadBalancerName) {
        return ExtensionLoader.getInstance().get(loadBalancerName);
    }


    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(LoadBalancer.class);
    }
}
