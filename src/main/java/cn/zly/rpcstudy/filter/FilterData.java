package cn.zly.rpcstudy.filter;

import cn.zly.rpcstudy.common.RpcRequest;
import cn.zly.rpcstudy.common.RpcResponse;
import lombok.Data;

import java.util.Arrays;
import java.util.Map;

/**
 * @author zhanglianyong
 * 2023-08-21 19:38
 */
@Data
public class FilterData {

    private String serviceVersion;
    private long timeout;
    private long retryCount;
    private String className;
    private String methodName;
    private Object[] args;
    private Map<String, Object> serviceAttachments;
    private Map<String, Object> clientAttachments;
    private RpcResponse data;

    public FilterData(RpcRequest rpcRequest) {
        this.className = rpcRequest.getClassName();
        this.methodName = rpcRequest.getMethodName();
        this.args = rpcRequest.getParams();
        this.serviceVersion = rpcRequest.getServiceVersion();
        this.serviceAttachments = rpcRequest.getServiceAttachments();
        this.clientAttachments = rpcRequest.getClientAttachments();
    }

    public FilterData(){

    }

    @Override
    public String toString() {
        return "FilterData{" +
                "serviceVersion='" + serviceVersion + '\'' +
                ", timeout=" + timeout +
                ", retryCount=" + retryCount +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", serviceAttachments=" + serviceAttachments +
                ", clientAttachments=" + clientAttachments +
                ", data=" + data +
                '}';
    }


}
