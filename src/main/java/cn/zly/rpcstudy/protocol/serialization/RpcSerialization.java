package cn.zly.rpcstudy.protocol.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author zhanglianyong
 * 2023-08-19 9:38
 */
public interface RpcSerialization {


    <T> byte[] serialize(T obj) throws Exception;


    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;
}
