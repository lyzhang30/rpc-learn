package cn.zly.rpcstudy.router;

import cn.zly.rpcstudy.common.ServiceMeta;
import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.register.IRegisterService;
import cn.zly.rpcstudy.spi.ExtensionLoader;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author zhanglianyong
 * 2023-08-20 22:25
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPILT = "$";

    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {
        IRegisterService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());
        List<ServiceMeta> serviceMetas = registryService.discoveries(serviceName);
        ServiceMeta serviceMeta = allocateNode(makeConsistentHashRing(serviceMetas), params[0].hashCode());
        return ServiceMetaRes.build(serviceMeta, serviceMetas);
    }


    private ServiceMeta allocateNode(TreeMap<Integer, ServiceMeta> map, int hashCode) {
        Map.Entry<Integer, ServiceMeta> entry = map.ceilingEntry(hashCode);
        if (Objects.isNull(entry)) {
            entry = map.firstEntry();
        }
        return entry.getValue();
    }

    private TreeMap<Integer, ServiceMeta> makeConsistentHashRing(List<ServiceMeta> serviceMetas) {
        TreeMap<Integer, ServiceMeta> map = new TreeMap<>();
        for (ServiceMeta server : serviceMetas) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                map.put((buildServiceInstanceKey(server) + VIRTUAL_NODE_SPILT + i).hashCode(), server);
            }
        }
        return map;
    }

    /**
     * 根据服务实例信息构建缓存键
     * @param serviceMeta
     * @return
     */
    private String buildServiceInstanceKey(ServiceMeta serviceMeta) {

        return String.join(":", serviceMeta.getServiceAddr(), String.valueOf(serviceMeta.getServicePort()));
    }
}
