package cn.zly.rpcstudy.protocol.codec;

import cn.zly.rpcstudy.protocol.MsgHeader;
import cn.zly.rpcstudy.protocol.RpcProtocol;
import cn.zly.rpcstudy.protocol.serialization.JsonSerialization;
import cn.zly.rpcstudy.protocol.serialization.RpcSerialization;
import cn.zly.rpcstudy.protocol.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zhanglianyong
 * 2023-08-17 20:53
 */
public class Encoder extends MessageToByteEncoder<RpcProtocol<Object>> {


    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        MsgHeader header = msg.getHeader();

        byteBuf.writeShort(header.getMagic());

        byteBuf.writeByte(header.getVersion());

        byteBuf.writeByte(header.getMsgType());

        byteBuf.writeByte(header.getStatus());

        byteBuf.writeLong(header.getRequestId());

        byteBuf.writeInt(header.getSerializationLen());

        final byte[] ser = header.getSerializations();

        final String serialization = new String(ser);

        byteBuf.writeBytes(ser);
        RpcSerialization rpcSerialization = SerializationFactory.getSerialization(serialization);
        byte[] data = rpcSerialization.serialize(msg.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);

    }
}
