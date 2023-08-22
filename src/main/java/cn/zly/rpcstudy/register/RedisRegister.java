package cn.zly.rpcstudy.register;

import cn.zly.rpcstudy.common.RpcServiceNameBuilder;
import cn.zly.rpcstudy.common.ServiceMeta;
import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.utils.LocalDateTimeUtils;
import com.alibaba.fastjson.JSON;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhanglianyong
 * 2023-08-17 22:10
 */
public class RedisRegister implements IRegisterService {

    private JedisPool jedisPool;

    private String UUID;

    private static final int TTL = 100 * 1000;

    private Set<String> serviceSet = new HashSet<>();


    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    public RedisRegister() {
        RpcProperties properties = RpcProperties.getInstance();
        String[] split = properties.getRegisterAddr().split(":");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        jedisPool = new JedisPool(poolConfig, split[0], Integer.valueOf(split[1]));
        this.UUID = java.util.UUID.randomUUID().toString();
        heartbeat();
    }

    private void heartbeat(){
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            for (String key : serviceSet) {
                List<ServiceMeta> serviceNodes = listServices(key);
                Iterator<ServiceMeta> iterator = serviceNodes.iterator();
                while (iterator.hasNext()) {
                    ServiceMeta node = iterator.next();
                    // 1. 如果过期
                    if (node.getEndTime() < LocalDateTimeUtils.getEpochMilli()) {
                        iterator.remove();
                    }
                    // 2.续签
                    if (node.getUUID().equals(this.UUID)) {
                        node.setEndTime(node.getEndTime() + TTL / 2);
                    }
                }
                if (!ObjectUtils.isEmpty(serviceNodes)) {
                    loadService(key, serviceNodes);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void loadService(String key,List<ServiceMeta> serviceMetas){
        String script = "redis.call('DEL', KEYS[1])\n" +
                "for i = 1, #ARGV do\n" +
                "   redis.call('RPUSH', KEYS[1], ARGV[i])\n" +
                "end \n"+
                "redis.call('EXPIRE', KEYS[1],KEYS[2])";
        List<String> keys = new ArrayList<>();
        keys.add(key);
        keys.add(String.valueOf(10));
        List<String> values = serviceMetas.stream().map(o -> JSON.toJSONString(o)).collect(Collectors.toList());
        Jedis jedis = getJedis();
        jedis.eval(script,keys,values);
        jedis.close();
    }

    private Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        RpcProperties properties = RpcProperties.getInstance();
        jedis.auth(properties.getRegisterPsw());
        return jedis;
    }

    @Override
    public void register(ServiceMeta serviceMeta) {
        String key = RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion());
        if (!serviceSet.contains(key)) {
            serviceSet.add(key);
        }

        serviceMeta.setUUID(this.UUID);
        serviceMeta.setEndTime(LocalDateTimeUtils.getEpochMilli() + TTL);
        Jedis jedis = getJedis();
        String script = "redis.call('RPUSH', KEYS[1], ARGV[1])\n" +
                "redis.call('EXPIRE', KEYS[1], ARGV[2])";
        List<String> value = new ArrayList<>();
        value.add(JSON.toJSONString(serviceMeta));
        value.add(String.valueOf(10));
        jedis.eval(script, Collections.singletonList(key), value);
        jedis.close();
    }

    @Override
    public void unregister(ServiceMeta serviceMeta) {

    }

    @Override
    public List<ServiceMeta> discoveries(String serviceName) {
        return listServices(serviceName);
    }

    private List<ServiceMeta> listServices(String key) {
        Jedis jedis = getJedis();
        List<String> list = jedis.lrange(key, 0, -1);
        jedis.close();
        return list.stream().map(o -> JSON.parseObject(o, ServiceMeta.class))
                .collect(Collectors.toList());
    }

    @Override
    public void destory() {

    }
}
