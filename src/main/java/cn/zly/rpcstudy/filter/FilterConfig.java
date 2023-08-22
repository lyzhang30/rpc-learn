package cn.zly.rpcstudy.filter;

import cn.zly.rpcstudy.spi.ExtensionLoader;
import lombok.SneakyThrows;

/**
 * @author zhanglianyong
 * 2023-08-21 19:55
 */
public class FilterConfig {

    private static FilterChain serviceBeforeFilterChain = new FilterChain();
    private static FilterChain serviceAfterFilterChain = new FilterChain();
    private static FilterChain clientBeforeFilterChain = new FilterChain();
    private static FilterChain clientAfterFilterChain = new FilterChain();


    @SneakyThrows
    public static void initServiceFilter() {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ServiceBeforeFilter.class);
        extensionLoader.loadExtension(ServiceAfterFilter.class);
        serviceBeforeFilterChain.addFilter(extensionLoader.gets(ServiceBeforeFilter.class));
        serviceAfterFilterChain.addFilter(extensionLoader.gets(ServiceAfterFilter.class));
    }

    @SneakyThrows
    public static void initClientFilter() {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ClientBeforeFilter.class);
        extensionLoader.loadExtension(ClientAfterFilter.class);
        clientBeforeFilterChain.addFilter(extensionLoader.gets(ClientBeforeFilter.class));
        clientAfterFilterChain.addFilter(extensionLoader.gets(ClientAfterFilter.class));
    }

    public static FilterChain getServiceBeforeFilterChain() {
        return serviceBeforeFilterChain;
    }

    public static FilterChain getServiceAfterFilterChain() {
        return serviceAfterFilterChain;
    }

    public static FilterChain getClientBeforeFilterChain() {
        return clientBeforeFilterChain;
    }

    public static FilterChain getClientAfterFilterChain() {
        return clientAfterFilterChain;
    }
}
