package com.rpc.client;


import com.rpc.client.proxy.ObjectProxy;

import java.lang.reflect.Proxy;

public class RpcClient {

    private ServiceDiscovery serviceDiscovery;
    public RpcClient(String zkAddress) throws Exception {
        this.serviceDiscovery=new ServiceDiscovery(zkAddress);

    }
    @SuppressWarnings("unchecked")
    public static <T, P> T createService(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T, P>(interfaceClass, version)
        );
    }
}
