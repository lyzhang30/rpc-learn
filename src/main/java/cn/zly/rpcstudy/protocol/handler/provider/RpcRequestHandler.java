package cn.zly.rpcstudy.protocol.handler.provider;

import cn.zly.rpcstudy.common.RpcRequest;
import cn.zly.rpcstudy.common.RpcResponse;
import cn.zly.rpcstudy.common.RpcServiceNameBuilder;
import cn.zly.rpcstudy.common.constants.MsgStatus;
import cn.zly.rpcstudy.common.constants.MsgType;
import cn.zly.rpcstudy.protocol.MsgHeader;
import cn.zly.rpcstudy.protocol.RpcProtocol;
import cn.zly.rpcstudy.protocol.handler.RpcRequestProcessor;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;
import java.util.Objects;

/**
 * @author zhanglianyong
 * 2023-08-19 10:17
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    // 缓存服务
    private final Map<String, Object> rpcServiceMap;

    public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        System.out.println("RpcRequestHandler.channelRead0, line:34, 结果：");
        RpcRequestProcessor.submitRequest(() -> {
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            RpcResponse response = new RpcResponse();
            MsgHeader header = protocol.getHeader();
            header.setMsgType((byte) MsgType.RESPONSE.ordinal());
            final RpcRequest request = protocol.getBody();
            try {
                Object result = handle(request);
                response.setData(result);
                header.setStatus((byte) MsgStatus.SUCCESS.ordinal());
            } catch (Throwable throwable) {
                // 执行失败异常返回
                header.setStatus((byte) MsgStatus.FAILED.ordinal());
                response.setThrowable(throwable);
                System.out.println("process request error" + header.getRequestId());
            }

            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            System.out.printf("执行成功：%s#%s:%s 返回值：%s%n ",
                    request.getClassName(), request.getMethodName(),request.getServiceVersion(), JSON.toJSONString(response));
            ctx.fireChannelRead(resProtocol);
        });
    }

    private Object handle(RpcRequest request) throws Throwable {
        String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        // 获取服务信息
        Object serviceBean = rpcServiceMap.get(serviceKey);
        if (Objects.isNull(serviceBean)) {
            throw new RuntimeException(String.format("service not exist: %s:%s",
                    request.getClassName(), request.getMethodName()));
        }
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParams();

        FastClass fastClass = FastClass.create(serviceBeanClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }
 }
