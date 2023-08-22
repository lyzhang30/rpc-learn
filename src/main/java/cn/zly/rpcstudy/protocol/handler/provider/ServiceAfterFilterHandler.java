package cn.zly.rpcstudy.protocol.handler.provider;

import cn.zly.rpcstudy.common.RpcResponse;
import cn.zly.rpcstudy.common.constants.MsgStatus;
import cn.zly.rpcstudy.filter.FilterConfig;
import cn.zly.rpcstudy.filter.FilterData;
import cn.zly.rpcstudy.protocol.MsgHeader;
import cn.zly.rpcstudy.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @description:
 * @Author: Xhy
 * @gitee: https://gitee.com/XhyQAQ
 * @copyright: B站: https://space.bilibili.com/152686439
 * @CreateTime: 2023-08-08 22:52
 */
@Slf4j
public class ServiceAfterFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) {
        final FilterData filterData = new FilterData();
        filterData.setData(protocol.getBody());
        RpcResponse response = new RpcResponse();
        MsgHeader header = protocol.getHeader();
        try {
            FilterConfig.getServiceAfterFilterChain().doFilter(filterData);
        } catch (Throwable throwable) {
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setThrowable(throwable);
            log.error("after process request {} error", header.getRequestId(), throwable);
        }
        ctx.writeAndFlush(protocol);
    }
}
