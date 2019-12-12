/*
 * The MIT License (MIT)
 * Copyright © 2019-2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.meteor.example.benchmark;

import com.sky.meteor.example.api.ExampleApi;
import com.sky.meteor.registry.Register;
import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.registry.zookeeper.ZookeeperRegister;
import com.sky.meteor.remoting.netty.Processor;
import com.sky.meteor.remoting.netty.client.NettyClient;
import com.sky.meteor.rpc.annotation.Reference;
import com.sky.meteor.rpc.consumer.ConsumerProcessorHandler;
import com.sky.meteor.rpc.consumer.proxy.ProxyFactory;
import com.sky.meteor.rpc.consumer.proxy.javassist.JavassistProxyFactory;
import com.taobao.stresstester.StressTestUtils;
import com.taobao.stresstester.core.StressTask;

import java.lang.annotation.Annotation;

/**
 * 测试步骤:
 *
 * @author
 */
@SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
public class QPSApplication {

    private Reference reference;

    private NettyClient nettyClient;

    private String content;

    private ExampleApi exampleApi;

    /**
     * 准备
     */
    public void prepare() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 1024; i++) {
            sb.append("a");
        }
        content = sb.toString();

        Processor processor = new ConsumerProcessorHandler();
        Register register = new ZookeeperRegister();
        register.setConnect("127.0.0.1:2181");
        register.setName("zookeeper");
        register.setGroup("example");

        nettyClient = new NettyClient();
        nettyClient.connectToRegistryServer(register);
        nettyClient.setProcessor(processor);
        nettyClient.start();

        reference = new Reference() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Reference.class;
            }

            @Override
            public String name() {
                return "com.sky.meteor.example.api.ExampleApi";
            }

            @Override
            public String group() {
                return "example";
            }

            @Override
            public String version() {
                return "1.0.0";
            }
        };
        RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta();
        serviceMeta.setGroup(reference.group());
        serviceMeta.setServiceProviderName(ExampleApi.class.getName());
        serviceMeta.setVersion(reference.version());
        nettyClient.getRegistryService().subscribe(serviceMeta);

        ProxyFactory factory = new JavassistProxyFactory();
//        ProxyFactory factory = new JdkProxyFactory();
//        ProxyFactory factory = new ByteBuddyProxyFactory();
        exampleApi = factory.newInstance(ExampleApi.class, reference);
    }

    /**
     * 预热
     *
     * @param count
     */
    public void warmup(int count) {
        for (int i = 0; i < count; i++) {
            send();
        }
    }

    /**
     * 发送信息
     */
    public void send() {
        String hello = exampleApi.hello(content);
        System.out.println("=====result hello:{}");
    }

    /**
     * 关闭
     */
    public void shutdown() {
        nettyClient.stop();
        System.out.println("=============shutdown=============");
    }

    public static void main(String[] args) throws Exception {
        QPSApplication application = new QPSApplication();
        application.prepare();
        application.warmup(10);
        StressTestUtils.testAndPrint(100, 1000000, new StressTask() {
            @Override
            public Object doTask() throws Exception {
                application.send();
                return null;
            }
        });
        application.shutdown();
    }
}
