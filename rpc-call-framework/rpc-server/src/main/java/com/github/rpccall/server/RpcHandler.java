package com.github.rpccall.server;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rpccall.common.RpcRequest;
import com.github.rpccall.common.RpcResponse;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理具体的业务调用 通过构造时传入的“业务接口及实现”handlerMap，来调用客户端所请求的业务方法
 * 并将业务方法返回值封装成response对象写入下一个handler（即编码handler-RpcEncoder）
 **/
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 接收消息，处理消息，返回结果
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            // 根据request来处理具体的业务调用
            Object result = handle(request); // 处理消息request
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t);
        }
        // 写入outbundle(即RpcEncoder)进行下一步处理（即编码）后发送到channel中给客户端
        // 注意，addListener(ChannelFutureListener.CLOSE)如果不加，客户端会一直没有响应;
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();

        // 拿到实现类对象
        Object serviceBean = handlerMap.get(className);

        // 拿到要调用的方法名、参数类型、参数值
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // 拿到接口类
        Class<?> forName = Class.forName(className);

        // 调用实现类对象的指定方法并返回结果
        Method method = forName.getMethod(methodName, parameterTypes);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
