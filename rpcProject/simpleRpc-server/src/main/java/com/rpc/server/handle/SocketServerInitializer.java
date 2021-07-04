package com.rpc.server.handle;

import entity.RpcRequest;
import entity.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import kryo.KryoSerializer;
import kryo.Serializer;
import utils.Beta;
import utils.RpcDecoder;
import utils.RpcEncoder;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String,Object> serviceMap;
    private ThreadPoolExecutor threadPoolExecutor;
    public SocketServerInitializer(Map<String,Object> serviceMap,ThreadPoolExecutor threadPoolExecutor){
        this.serviceMap=serviceMap;
        this.threadPoolExecutor=threadPoolExecutor;
    }
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        Serializer serializer = KryoSerializer.class.newInstance();
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(new IdleStateHandler(0, 0, Beta.BEAT_TIMEOUT, TimeUnit.SECONDS));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcRequest.class, serializer));
        cp.addLast(new RpcEncoder(RpcResponse.class, serializer));
        cp.addLast(new SocketServerHandler(serviceMap, threadPoolExecutor));
    }
}

