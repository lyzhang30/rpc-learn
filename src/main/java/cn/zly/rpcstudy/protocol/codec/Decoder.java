package cn.zly.rpcstudy.protocol.codec;

import cn.zly.rpcstudy.common.RpcRequest;
import cn.zly.rpcstudy.common.RpcResponse;
import cn.zly.rpcstudy.common.constants.MsgType;
import cn.zly.rpcstudy.common.constants.ProtocolConstants;
import cn.zly.rpcstudy.protocol.MsgHeader;
import cn.zly.rpcstudy.protocol.RpcProtocol;
import cn.zly.rpcstudy.protocol.serialization.RpcSerialization;
import cn.zly.rpcstudy.protocol.serialization.SerializationFactory;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.Objects;

/**
 * @author zhanglianyong
 * 2023-08-19 9:26
 */
public class Decoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // 协议头
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
            return ;
        }
        // 标记当前读取位置
        in.markReaderIndex();

        short magic = in.readShort();
        System.out.println(magic);
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is ill");
        }
        byte version = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();
        int serializationLen = in.readInt();
        if (in.readableBytes() < serializationLen) {
            in.resetReaderIndex();
            return;
        }
        final byte[] bytes = new byte[serializationLen];
        in.readBytes(bytes);
        final String serialization = new String(bytes);
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (Objects.isNull(msgTypeEnum)) {
            return;
        }
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setSerializations(bytes);
        header.setSerializationLen(serializationLen);
        header.setMsgLen(dataLength);

        RpcSerialization rpcSerialization = SerializationFactory.getSerialization(serialization);
        System.out.println("serialization:" + serialization);
        switch (msgTypeEnum) {
            // 请求消息
            case REQUEST:
                RpcRequest request = rpcSerialization.deserialize(data, RpcRequest.class);
                if (request != null) {
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    System.out.println("Decoder.decode, line:81, protocol" + JSON.toJSONString(protocol));
                    out.add(protocol);
                }
                break;
            // 响应消息
            case RESPONSE:
                RpcResponse response = rpcSerialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    System.out.println("Decoder.decode, line:93, protocol" + JSON.toJSONString(protocol));
                    out.add(protocol);
                }
                break;
        }
    }
}
