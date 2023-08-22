package cn.zly.rpcstudy.consumer;

import cn.zly.rpcstudy.common.RpcRequest;
import cn.zly.rpcstudy.common.ServiceMeta;
import cn.zly.rpcstudy.protocol.RpcProtocol;
import cn.zly.rpcstudy.protocol.codec.Decoder;
import cn.zly.rpcstudy.protocol.codec.Encoder;
import cn.zly.rpcstudy.protocol.handler.consumer.RpcResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author zhanglianyong
 * 2023-08-17 20:49
 */
public class RpcConsumer {


    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;


    public RpcConsumer() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new Encoder())
                                .addLast(new Decoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }


    public void sendRequest(RpcProtocol<RpcRequest> protocol, ServiceMeta serviceMetadata) throws Exception {
        if (serviceMetadata != null) {
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(),
                    serviceMetadata.getServicePort()).sync();
            future.addListener((ChannelFutureListener) channelFuture -> {
                if (future.isSuccess()) {
                    System.out.println("连接 rpc server " + serviceMetadata.getServiceAddr()
                            + " 端口 " + serviceMetadata.getServicePort());
                } else {
                    System.out.println("连接 rpc server " + serviceMetadata.getServiceAddr()
                            +" 端口 " +  serviceMetadata.getServicePort() +" 失败.");
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            future.channel().writeAndFlush(protocol);
        }
    }
}
