package cn.zly.rpcstudy.register;

import cn.zly.rpcstudy.common.RpcServiceNameBuilder;
import cn.zly.rpcstudy.common.ServiceMeta;
import cn.zly.rpcstudy.config.RpcProperties;
import com.google.common.collect.Lists;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhanglianyong
 * 2023-08-22 21:18
 */
public class ZookeeperRegister implements IRegisterService {


    private static final String ZK_BASE_PATH = "/myrpc";

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;


    private final ServiceDiscovery<ServiceMeta> serviceDiscovery;

    public ZookeeperRegister() {
        String registerAddress = RpcProperties.getInstance().getRegisterAddr();
        CuratorFramework client = CuratorFrameworkFactory.newClient(registerAddress, new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES));
        client.start();
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client).serializer(serializer).basePath(ZK_BASE_PATH).build();
    }


    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance.<ServiceMeta>builder()
                .name(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        this.serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unregister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public List<ServiceMeta> discoveries(String serviceName) {
        try {
            return listService(serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Lists.newArrayList();
    }

    private List<ServiceMeta> listService(String serviceName) throws Exception {
        return this.serviceDiscovery.queryForInstances(serviceName).stream().map(ServiceInstance::getPayload).collect(Collectors.toList());
    }

    @Override
    public void destory() {
        try {
            this.serviceDiscovery.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
