package cn.zly.rpcstudy.provider;

import cn.zly.rpcstudy.annotation.RpcService;
import cn.zly.rpcstudy.common.RpcServiceNameBuilder;
import cn.zly.rpcstudy.common.ServiceMeta;
import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.filter.FilterConfig;
import cn.zly.rpcstudy.protocol.codec.Decoder;
import cn.zly.rpcstudy.protocol.codec.Encoder;
import cn.zly.rpcstudy.protocol.handler.provider.RpcRequestHandler;
import cn.zly.rpcstudy.protocol.handler.provider.ServiceAfterFilterHandler;
import cn.zly.rpcstudy.protocol.handler.provider.ServiceBeforeFilterHandler;
import cn.zly.rpcstudy.protocol.serialization.SerializationFactory;
import cn.zly.rpcstudy.register.IRegisterService;
import cn.zly.rpcstudy.register.RegisterFactory;
import cn.zly.rpcstudy.router.LoadBalancerFactory;
import cn.zly.rpcstudy.utils.PropertiesUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhanglianyong
 * 2023-08-19 10:08
 */
public class ProviderPostProcessor implements EnvironmentAware, InitializingBean, BeanPostProcessor {

    RpcProperties rpcProperties;

    private static String serverAddress;

    static {
        try {
            serverAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private final Map<String, Object> rpcServiceMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startRpcServer();
                } catch (Exception e) {
                    System.out.println("strt rpc server error." + e);
                }
            }
        });
        // 守护线程
        t.setDaemon(true);
        t.start();
        SerializationFactory.init();
        RegisterFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initServiceFilter();
    }

    private void startRpcServer() throws InterruptedException {
        int serverPort = rpcProperties.getPort();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new Encoder())
                                    .addLast(new Decoder())
                                    .addLast(new ServiceBeforeFilterHandler())
                                    .addLast(new RpcRequestHandler(rpcServiceMap))
                                    .addLast(new ServiceAfterFilterHandler());

                        }
                    }).childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.bind(serverAddress, serverPort)
                    .sync();
            System.out.printf("server addr start %s start on port %s%n", serverAddress, serverPort);
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        RpcProperties properties = RpcProperties.getInstance();
        PropertiesUtils.init(properties,environment);
        rpcProperties = properties;
        System.out.println("读取配置文件成功");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (Objects.nonNull(rpcService)) {
            String serviceName = beanClass.getInterfaces()[0].getName();
            if (!Objects.equals(rpcService.serviceInterface(), void.class)) {
                serviceName = rpcService.serviceInterface().getName();
            }
            String serviceversion = rpcService.serviceVersion();
            try {
                int serverPort = rpcProperties.getPort();
                IRegisterService registerService = RegisterFactory.get(rpcProperties.getRegisterType());
                ServiceMeta serviceMeta = new ServiceMeta();
                serviceMeta.setServiceAddr(serverAddress);
                serviceMeta.setServicePort(serverPort);
                serviceMeta.setServiceVersion(serviceversion);
                serviceMeta.setServiceName(serviceName);
                registerService.register(serviceMeta);
                rpcServiceMap.put(RpcServiceNameBuilder.buildServiceKey(
                        serviceMeta.getServiceName(), serviceMeta.getServiceVersion()
                ), bean);
                System.out.printf("register server %s version %s", serviceName, serviceversion);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("fail to register server %s version %s", serviceName, serviceversion);
            }
        }
        return bean;
    }
}
