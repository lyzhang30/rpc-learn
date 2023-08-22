package cn.zly.rpcstudy.protocol.serialization;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author zhanglianyong
 * 2023-08-22 22:26
 */
public class HessianSerialization implements RpcSerialization {

    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        Hessian2Output ho = null;
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            ho = new Hessian2Output(baos);
            ho.writeObject(obj);
            ho.flush();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new Exception("Hessian.serialize.异常.", ex);
        } finally {
            if (null != ho) {
                ho.close();
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        T javaBean = null;
        Hessian2Input hi = null;
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(data);
            hi = new Hessian2Input(bais);
            javaBean = (T) hi.readObject();
            return javaBean;
        } catch (Exception ex) {
            throw new Exception("Hessian.deserialize.异常.", ex);
        } finally {
            if (null != hi) {
                hi.close();
            }
        }
    }
}
