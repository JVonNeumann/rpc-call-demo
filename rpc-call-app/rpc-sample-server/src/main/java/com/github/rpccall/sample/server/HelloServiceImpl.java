package com.github.rpccall.sample.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rpccall.sample.client.HelloService;
import com.github.rpccall.sample.client.Person;
import com.github.rpccall.server.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    public String hello(String name) {
        logger.info("已经调用服务端接口实现，业务处理结果为：Hello! {}", name);
        return "Hello! " + name;
    }

    public String hello(Person person) {
        logger.info("已经调用服务端接口实现，业务处理为：Hello! {} {}", person.getFirstName(), person.getLastName());
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
