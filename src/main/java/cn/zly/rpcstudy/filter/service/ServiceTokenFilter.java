package cn.zly.rpcstudy.filter.service;

import cn.zly.rpcstudy.config.RpcProperties;
import cn.zly.rpcstudy.filter.FilterData;
import cn.zly.rpcstudy.filter.ServiceBeforeFilter;
import cn.zly.rpcstudy.filter.client.ClientLogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @description: token拦截器
 * @Author: Xhy
 * @gitee: https://gitee.com/XhyQAQ
 * @copyright: B站: https://space.bilibili.com/152686439
 * @CreateTime: 2023-08-03 12:07
 */
public class ServiceTokenFilter implements ServiceBeforeFilter {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);
    @Override
    public void doFilter(FilterData filterData) {
        final Map<String, Object> attachments = filterData.getClientAttachments();
        final Map<String, Object> serviceAttachments = RpcProperties.getInstance().getServiceAttachments();
        if (!attachments.getOrDefault("token","").equals(serviceAttachments.getOrDefault("token",""))){
            logger.error("token不匹配：{}", attachments.getOrDefault("token", ""));
            throw new IllegalArgumentException("token不正确");
        }
    }

}
