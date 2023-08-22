package cn.zly.rpcstudy.register;

import cn.zly.rpcstudy.common.ServiceMeta;

import java.util.List;

/**
 * @author zhanglianyong
 * 2023-08-17 22:09
 */
public interface IRegisterService {

    void register(ServiceMeta serviceMeta);

    void unregister(ServiceMeta serviceMeta);

    List<ServiceMeta> discoveries(String serviceName);

    void destory();
}
