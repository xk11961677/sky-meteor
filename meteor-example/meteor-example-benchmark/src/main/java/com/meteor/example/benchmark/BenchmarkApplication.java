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
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

/**
 * 测试步骤:
 *
 * @author
 */
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.SECONDS)
//@Measurement(iterations = 5, time = 60, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@State(Scope.Benchmark)
@Threads(10)
//@GroupThreads(10)
public class BenchmarkApplication {

    private Reference reference;

    private NettyClient nettyClient;

    private String content;

    private ExampleApi exampleApi;

    @Setup
    public void prepare() {
        System.out.println("=============prepare=============");
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
        nettyClient.startup();

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

    @TearDown
    public void shutdown() {
        nettyClient.shutdown();
        System.out.println("=============shutdown=============");
    }


    @Benchmark
    public void client() {
        String hello = exampleApi.hello(content);
        System.out.println("=====result hello:{}");
    }

    /**
     * ============================== HOW TO RUN THIS TEST: ====================================
     * <p>
     * You can see measureRight() yields the result, and measureWrong() fires
     * the assert at the end of first iteration! This will not generate the results
     * for measureWrong(). You can also prevent JMH for proceeding further by
     * requiring "fail on error".
     * <p>
     * You can run this test:
     * <p>
     * a) Via the command line:
     * $ mvn clean install
     * $ java -jar target/benchmarks.jar ReachFrameworkBenchmark
     * (we requested single fork; there are also other options, see -h)
     * <p>
     * You can optionally supply -foe to fail the complete run.
     * <p>
     * b) Via the Java API:
     * (see the JMH homepage for possible caveats when running from IDE:
     * http://openjdk.java.net/projects/code-tools/jmh/)
     * <p>
     * 通过下面地址可将result.json文件上传查看图
     * http://deepoove.com/jmh-visual-chart/
     */
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(BenchmarkApplication.class.getSimpleName())
                .forks(1)
                .result("result.json")
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opt).run();
    }
}
