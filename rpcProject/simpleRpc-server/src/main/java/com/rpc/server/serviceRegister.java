package com.rpc.server;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.RpcProtocol;
import protocol.RpcServiceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import utils.Constant;

public class serviceRegister {
    private static final Logger logger = LoggerFactory.getLogger(serviceRegister.class);

    private CuratorFramework curatorClient;

    //会话超时时间
    private static final int SESSION_TIMEOUT = 30000;

    //连接超时时间
    private static final int CONNECTION_TIMEOUT = 5000;

    private static final RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    private List<String> pathList=new ArrayList<>();
    public serviceRegister(String remotoAdress){
        curatorClient = CuratorFrameworkFactory.newClient(remotoAdress, SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);
        curatorClient.start();
        System.out.println("连接成功！");
    }

    /**
     * 开始注册
     * @param host
     * @param port
     * @param serviceMap
     */
    public void startRegister(String host,int port, Map<String,Object> serviceMap){


        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for (String key : serviceMap.keySet()) {
            String[] serviceInfo = key.split(":");
            if (serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                if (serviceInfo.length == 2) {
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                } else {
                    rpcServiceInfo.setVersion("");
                }
                System.out.println(String.format("Register new service: %s",key));
                serviceInfoList.add(rpcServiceInfo);
            } else {
                System.out.println(String.format("Can not get service name and version: {} : %s",key));
            }
        }
        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfoList(serviceInfoList);
            String serviceData = rpcProtocol.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = Constant.ZK_DATA_PATH + "-" + rpcProtocol.hashCode();
            // 创建目录并存取数据，目录为 对象数据的hashcode拼接出来。
            curatorClient.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(path, bytes);
            pathList.add(path);
            System.out.println(String.format("Register %s new service, host: %s, port: %s", serviceInfoList.size(), host, port));
        } catch (Exception e) {
            System.out.println(String.format("Register service fail, exception: %s", e.getMessage()));
        }
        //重连
        curatorClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if (connectionState == ConnectionState.RECONNECTED) {
                    logger.info("Connection state: {}, register service after reconnected", connectionState);
                    startRegister(host, port, serviceMap);
                }
            }
        });

    }
    void unregisterService(){
        logger.info("Unregister all service");
        for (String path : pathList) {
            try {
                this.curatorClient.delete().forPath(path);
            } catch (Exception ex) {
                logger.error("Delete service path error: " + ex.getMessage());
            }
        }
        this.curatorClient.close();
    }
}
