package cn.zly.rpcstudy.router;

import cn.zly.rpcstudy.common.ServiceMeta;
import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.register.IRegisterService;
import cn.zly.rpcstudy.spi.ExtensionLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhanglianyong
 * 2023-08-19 11:27
 */
public class RoundLoadBalancer implements LoadBalancer {

    private static AtomicInteger roundRobinId = new AtomicInteger(0);

    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {
        // 获取注册中心
        IRegisterService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());
        List<ServiceMeta> discoveries = registryService.discoveries(serviceName);
        // 1.获取所有服务
        int size = discoveries.size();

        // 2.根据当前轮询ID取余服务长度得到具体服务
        roundRobinId.addAndGet(1);
        if (roundRobinId.get() == Integer.MAX_VALUE){
            roundRobinId.set(0);
        }
        if (size == 0) {
            throw new RuntimeException("no available node");
        }

        return ServiceMetaRes.build(discoveries.get(roundRobinId.get() % size),discoveries);
    }
}
