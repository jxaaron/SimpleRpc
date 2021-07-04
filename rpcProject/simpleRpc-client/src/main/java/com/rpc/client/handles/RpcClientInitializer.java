package com.rpc.client.handles;

import entity.RpcRequest;
import entity.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import kryo.KryoSerializer;
import kryo.Serializer;
import utils.Beta;
import utils.RpcDecoder;
import utils.RpcEncoder;

import java.util.concurrent.TimeUnit;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        Serializer serializer = KryoSerializer.class.newInstance();
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new IdleStateHandler(0, 0, Beta.BEAT_INTERVAL, TimeUnit.SECONDS));
        cp.addLast(new RpcEncoder(RpcRequest.class, serializer));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcResponse.class, serializer));
        cp.addLast(new RpcClientHandler());
    }
}
