package com.rpc.client;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.RpcProtocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import utils.Constant;

public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CuratorFramework curatorClient;

    //会话超时时间
    private static final int SESSION_TIMEOUT = 30000;

    //连接超时时间
    private static final int CONNECTION_TIMEOUT = 5000;

    private static final RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    private List<String> pathList=new ArrayList<>();

    public void watchPathChildrenNode(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorClient, path, true);
        //BUILD_INITIAL_CACHE 代表使用同步的方式进行缓存初始化。
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    public ServiceDiscovery(String remotoAdress) throws Exception {
        // 连接zk
        curatorClient = CuratorFrameworkFactory.newClient(remotoAdress, SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);
        curatorClient.start();
        System.out.println("连接成功！");
        // 获取服务地址，并连接
        discoveryService();
        watchPathChildrenNode(Constant.ZK_REGISTRY_PATH, new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                ChildData childData = pathChildrenCacheEvent.getData();
                switch (type) {
                    case CONNECTION_RECONNECTED:
                        logger.info("Reconnected to zk, try to get latest service list");
                        discoveryService();
                        break;
                    case CHILD_ADDED:
                        discoveryService(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                    case CHILD_UPDATED:
                        discoveryService(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                    case CHILD_REMOVED:
                        discoveryService(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                        break;
                }
            }
        });
    }
    private void discoveryService(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        String data = new String(childData.getData(), StandardCharsets.UTF_8);
        System.out.println(String.format("Child data updated, path:%s,type:%s,data:%s", path, type, data));
        RpcProtocol rpcProtocol =  RpcProtocol.fromJson(data);
        ConnectService.getInstance().connectNode(rpcProtocol);
    }

    public void discoveryService() throws Exception {

        List<String> nodeList = curatorClient.getChildren().forPath(Constant.ZK_REGISTRY_PATH);
        for (String node : nodeList) {
            logger.debug("Service node: " + node);
            byte[] bytes = curatorClient.getData().forPath(Constant.ZK_REGISTRY_PATH + "/" + node);
            String json = new String(bytes);
            RpcProtocol rpcProtocol = RpcProtocol.fromJson(json);
            ConnectService.getInstance().connectNode(rpcProtocol);
        }

    }

}
