package cn.zly.rpcstudy.consumer;

import cn.zly.rpcstudy.common.*;
import cn.zly.rpcstudy.common.constants.MsgType;
import cn.zly.rpcstudy.common.constants.ProtocolConstants;
import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.protocol.MsgHeader;
import cn.zly.rpcstudy.protocol.RpcProtocol;
import cn.zly.rpcstudy.register.RegisterFactory;
import cn.zly.rpcstudy.router.LoadBalancer;
import cn.zly.rpcstudy.router.LoadBalancerFactory;
import cn.zly.rpcstudy.router.ServiceMetaRes;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static cn.zly.rpcstudy.common.constants.FaultTolerantRules.*;

/**
 * @author zhanglianyong
 * 2023-08-17 20:30
 */
@Slf4j
public class RpcInvokerProxy implements InvocationHandler {

    private String serviceVersion;
    private long timeout;
    private String loadBalancerType;
    private String faultTolerantType;
    private long retryCount;

    public RpcInvokerProxy(String serviceVersion, long timeout,String faultTolerantType,String loadBalancerType,long retryCount) throws Exception {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.loadBalancerType = loadBalancerType;
        this.faultTolerantType = faultTolerantType;
        this.retryCount = retryCount;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        // 填充协议头部
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        fillHeader(protocol, requestId);

        RpcRequest request = new RpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(ObjectUtils.isEmpty(args) ? new Object[0] : args);
        request.setServiceAttachments(RpcProperties.getInstance().getServiceAttachments());
        request.setClientAttachments(RpcProperties.getInstance().getClientAttachments());
        protocol.setBody(request);

        RpcConsumer rpcConsumer = new RpcConsumer();
        String serviceName = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object[] params = request.getParams();

        RpcResponse rpcResponse = null;
        LoadBalancer loadBalancer = LoadBalancerFactory.get(loadBalancerType);
        final ServiceMetaRes serviceMetaRes = loadBalancer.select(
                params, serviceName
        );
        ServiceMeta curServiceMeta = serviceMetaRes.getCurServiceMeta();
        final Collection<ServiceMeta> otherServiceMeta = serviceMetaRes.getOtherServiceMeta();
        long count = 0L;
        while (count < retryCount) {
            RpcFuture<RpcResponse> rpcFuture = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
            RpcRequestHolder.REQUEST_MAP.put(requestId, rpcFuture);
            try {
                rpcConsumer.sendRequest(protocol, serviceMetaRes.getCurServiceMeta());
                rpcResponse = rpcFuture.getPromise().get(
                        rpcFuture.getTimeout(),
                        TimeUnit.MILLISECONDS
                );
                if (rpcResponse.getThrowable() != null && otherServiceMeta.size() == 0) {
                    throw rpcResponse.getThrowable();
                }
                if (rpcResponse.getThrowable() != null) {
                    throw rpcResponse.getThrowable();
                }
                System.out.println("rpc 调用成功， serviceName " + serviceName);
                return rpcResponse.getData();
            } catch (Throwable e) {
                // TODO 容错扩展
                String errorMsg = e.getMessage() == null ? e.getCause().getMessage() : e.getMessage();
                switch (faultTolerantType) {
                    // 故障转移
                    case Failover:
                        log.error("故障转移， 第{}次重试", count);
                        count++;
                        if (!ObjectUtils.isEmpty(otherServiceMeta)) {
                            final ServiceMeta next = serviceMetaRes.getOtherServiceMeta().iterator().next();
                            curServiceMeta = next;
                            otherServiceMeta.remove(next);
                        } else {
                            final String msg = String.format("rpc 调用失败,无服务可用 serviceName: {%s}, 异常信息: {%s}", serviceName, errorMsg);
                            log.warn(msg);
                            throw new RuntimeException(msg);
                        }
                        break;
                    // 快速失败
                    case FailFast:
                        log.error("快速失败");
                        return !Objects.isNull(rpcResponse)? rpcResponse.getThrowable() : e;
                    // 安全失败
                    case Failsafe:
                        log.error("安全失败");
                        return null;

                }
            }
        }
        throw new RuntimeException("超过最大重试次数 " + retryCount + ", 请稍后重试");
    }

    public void fillHeader(RpcProtocol<RpcRequest> protocol, long requestId) {
        MsgHeader header = new MsgHeader();

        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        final byte[] serialization = RpcProperties.getInstance().getSerialization().getBytes();
        header.setSerializationLen(serialization.length);
        header.setSerializations(serialization);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);
    }

}
