package cn.zly.rpcstudy.protocol.handler.provider;

import cn.zly.rpcstudy.common.RpcRequest;
import cn.zly.rpcstudy.common.RpcResponse;
import cn.zly.rpcstudy.common.constants.MsgStatus;
import cn.zly.rpcstudy.filter.FilterConfig;
import cn.zly.rpcstudy.filter.FilterData;
import cn.zly.rpcstudy.protocol.MsgHeader;
import cn.zly.rpcstudy.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhanglianyong
 * 2023-08-21 20:07
 */
@Slf4j
public class ServiceBeforeFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        log.info("ServiceBeforeFilterHandler.channelRead0, line:21");
        RpcRequest rpcRequest = protocol.getBody();
        FilterData filterData = new FilterData(rpcRequest);
        RpcResponse rpcResponse = new RpcResponse();
        MsgHeader header = protocol.getHeader();
        try {
            FilterConfig.getServiceBeforeFilterChain().doFilter(filterData);
        } catch (Throwable throwable) {
            RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            rpcResponse.setThrowable(throwable);
            responseRpcProtocol.setHeader(header);
            responseRpcProtocol.setBody(rpcResponse);
            ctx.writeAndFlush(responseRpcProtocol);
            return;
        }
        ctx.fireChannelRead(protocol);
    }
}
