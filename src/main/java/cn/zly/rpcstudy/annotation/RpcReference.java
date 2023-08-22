package cn.zly.rpcstudy.annotation;

import cn.zly.rpcstudy.common.constants.FaultTolerantRules;
import cn.zly.rpcstudy.common.constants.LoadBalancerRules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 服务调用方注解
 * @Author: Xhy
 * @gitee: https://gitee.com/XhyQAQ
 * @copyright: B站: https://space.bilibili.com/152686439
 * @CreateTime: 2023-04-24 23:32
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcReference {


    /**
     * 版本
     * @return
     */
    String serviceVersion() default "1.0";

    /**
     * 超时时间
     * @return
     */
    long timeout() default 5000;

    /**
     * 可选的负载均衡:consistentHash,roundRobin...
     *
     * @return
     */
    String loadBalancer() default LoadBalancerRules.Random;

    /**可选的容错策略:failover,failFast,failsafe...
     *
     * @return
     */
    String faultTolerant() default FaultTolerantRules.FailFast;

    /**
     * 重试次数
     * @return
     */
    long retryCount() default 3;
}
