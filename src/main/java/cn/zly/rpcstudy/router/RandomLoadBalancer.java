package cn.zly.rpcstudy.router;

import cn.zly.rpcstudy.common.ServiceMeta;
import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.register.IRegisterService;
import cn.zly.rpcstudy.spi.ExtensionLoader;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 * @author zhanglianyong
 * 2023-08-20 16:21
 */
public class RandomLoadBalancer implements LoadBalancer {

    private static Random random;

    static {
        random = new Random();
    }

    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {

        // 获取注册中心
        IRegisterService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());
        List<ServiceMeta> discoveries = registryService.discoveries(serviceName);
        // 1.获取所有服务
        int size = discoveries.size();

        if (size == 0) {
            throw new RuntimeException("no available node");
        }
        // 获取一个随机数
        int index = random.nextInt(size);
        return ServiceMetaRes.build(discoveries.get(index), discoveries);
    }
}
