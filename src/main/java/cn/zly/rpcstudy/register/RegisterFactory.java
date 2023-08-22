package cn.zly.rpcstudy.register;

import cn.zly.rpcstudy.spi.ExtensionLoader;

import java.io.IOException;

/**
 * @author zhanglianyong
 * 2023-08-17 22:42
 */
public class RegisterFactory {

    public static IRegisterService get(String registerServiceName) throws Exception {
        return ExtensionLoader.getInstance().get(registerServiceName);
    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(IRegisterService.class);
    }

}
