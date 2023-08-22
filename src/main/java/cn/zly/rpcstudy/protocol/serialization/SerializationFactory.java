package cn.zly.rpcstudy.protocol.serialization;

import cn.zly.rpcstudy.spi.ExtensionLoader;

import java.io.IOException;

/**
 * @author zhanglianyong
 * 2023-08-19 9:38
 */
public class SerializationFactory {

    public static RpcSerialization getSerialization(String serializationName) {
        return ExtensionLoader.getInstance().get(serializationName);
    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(RpcSerialization.class);
    }
}
