package com.rpc.server;


import com.rpc.server.handle.SocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class rpcServer {
    private static final Logger logger = LoggerFactory.getLogger(rpcServer.class);
    private serviceRegister register;
    private Map<String,Object> serviceMap=new HashMap<>();
    private String localAddress;
    private Thread thread;

    /**
     * 初始化注册器
     * @param address
     */
    public rpcServer(String address,String localAddress){
         this.register = new serviceRegister(address);
         this.localAddress=localAddress;
    }

    /**
     * 启动注册
     */
    public void start(){
        // netty的使用
        //启动服务端
        thread = new Thread(new Runnable() {

            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8,
                600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

        @Override
        public void run() {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup wokerGroup = new NioEventLoopGroup();
            String host=localAddress.split(":")[0];
            int port=Integer.parseInt(localAddress.split(":")[1]);
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                //在服务器端的handler()方法表示对bossGroup起作用，而childHandler表示对wokerGroup起作用
                serverBootstrap.group(bossGroup, wokerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(new SocketServerInitializer(serviceMap, threadPoolExecutor));
                ChannelFuture channelFuture = serverBootstrap.bind(host,port).sync();
                register.startRegister(host,port,serviceMap);
                channelFuture.channel().closeFuture().sync();
            }
            catch(Exception e){

            }
            finally {
                try {
                    register.unregisterService();
                    wokerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
            }
        });
        thread.start();
    }

    /**
     * 添加服务接口信息
     * @param serviceName
     * @param version
     * @param serviceBean
     */
    public void addService(String serviceName,String version,Object serviceBean){
        // 根据服务名和版本获取编号
        String key=serviceName+":"+version;
        serviceMap.put(key,serviceBean);

    }
}
