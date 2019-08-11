package com.github.rpccall.sample.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.rpccall.client.RpcProxy;
import com.github.rpccall.sample.client.HelloService;
import com.github.rpccall.sample.client.Person;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application.xml")
public class HelloServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceTest.class);

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest1() {
        // 调用代理的create方法，代理HelloService接口
        HelloService helloService = rpcProxy.create(HelloService.class);

        // 调用代理的方法，执行invoke
        String result = helloService.hello("World");
        logger.info("服务端返回结果：{}", result);
    }

    @Test
    public void helloTest2() {
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello(new Person("Hello", "World"));
        logger.info("服务端返回结果：{}", result);
    }
}
