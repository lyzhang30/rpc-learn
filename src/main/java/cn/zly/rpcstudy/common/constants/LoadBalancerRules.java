package cn.zly.rpcstudy.common.constants;

/**
 * @description: 负债均衡
 * @Author: Xhy
 * @gitee: https://gitee.com/XhyQAQ
 * @copyright: B站: https://space.bilibili.com/152686439
 * @CreateTime: 2023-07-28 22:13
 */
public interface LoadBalancerRules {

    String ConsistentHash = "consistentHash";
    String RoundRobin = "roundRobin";
    String Random = "random";
}
