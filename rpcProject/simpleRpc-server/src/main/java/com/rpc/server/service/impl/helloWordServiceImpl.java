package com.rpc.server.service.impl;

import com.rpc.server.service.helloWorldService;

public class helloWordServiceImpl implements helloWorldService {
    @Override
    public String sayHello(String name) {
        return name+",Hi";
    }
}
