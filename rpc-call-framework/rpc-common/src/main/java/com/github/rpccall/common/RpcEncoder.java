package com.github.rpccall.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器
 **/
@SuppressWarnings("rawtypes")
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    // 构造函数传入序列化的class
    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf out) throws Exception {
        // 对象o序列化成二进制
        if (genericClass.isInstance(o)) {
            byte[] data = SerializationUtil.serialize(o);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
