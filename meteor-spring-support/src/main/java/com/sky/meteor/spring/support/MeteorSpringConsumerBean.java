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
package com.sky.meteor.spring.support;

import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.rpc.annotation.Reference;
import com.sky.meteor.rpc.consumer.proxy.ProxyFactory;
import com.sky.meteor.rpc.consumer.proxy.javassist.JavassistProxyFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.annotation.Annotation;


/**
 * @author
 */
public class MeteorSpringConsumerBean<T> implements FactoryBean<T>, InitializingBean {
    /**
     * 服务接口类型
     */
    @Getter
    @Setter
    private Class<T> interfaceClass;
    /**
     * 服务版本号, 通常在接口不兼容时版本号才需要升级
     */
    @Getter
    @Setter
    private String version;
    /**
     * consumer代理对象
     */
    private transient T proxy;
    /**
     * 调用超时时间设置
     */
    @Getter
    @Setter
    private long timeoutMillis;
    /**
     * provider地址列表, 逗号分隔(IP直连)
     */
    @Getter
    @Setter
    private String providerAddresses;
    /**
     * failover重试次数(只对ClusterInvoker.Strategy.FAIL_OVER有效)
     */
    @Getter
    @Setter
    private int failoverRetries;
    @Getter
    @Setter
    private String group;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private MeteorSpringClient client;

    @Override
    public T getObject() throws Exception {
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    private void init() {
        ProxyFactory factory = new JavassistProxyFactory();
        if (name == null) {
            name = interfaceClass.getName();
        }
        group = group == null ? "group" : group;
        version = version == null ? "1.0.0" : version;
        RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta();
        serviceMeta.setGroup(group);
        serviceMeta.setServiceProviderName(name);
        serviceMeta.setVersion(version);
        client.getNettyClient().getRegistryService().subscribe(serviceMeta);
        proxy = factory.newInstance(interfaceClass, new Reference() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Reference.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String group() {
                return group;
            }

            @Override
            public String version() {
                return version;
            }
        });
    }

}
