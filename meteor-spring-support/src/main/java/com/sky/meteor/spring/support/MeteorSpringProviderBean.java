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


import com.sky.meteor.common.util.ReflectAsmUtils;
import com.sky.meteor.registry.Register;
import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.registry.zookeeper.ZookeeperRegister;
import com.sky.meteor.remoting.netty.Processor;
import com.sky.meteor.remoting.netty.server.NettyServer;
import com.sky.meteor.rpc.provider.ProviderProcessorHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.*;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author
 */
@Slf4j
public class MeteorSpringProviderBean implements InitializingBean, ApplicationContextAware {
    @Getter
    @Setter
    private MeteorSpringServer server;
    @Getter
    @Setter
    private Object providerImpl;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String group;
    @Getter
    @Setter
    private String version;


    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) applicationContext).addApplicationListener(new MeteorApplicationListener());
        }
    }

    private void init() throws Exception {

    }

    private final class MeteorApplicationListener implements ApplicationListener {

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent) {
                if(name ==null) {
                    name = providerImpl.getClass().getName();
                }
                RegisterMeta registerMeta = new RegisterMeta();
                registerMeta.setPort(server.getPort());
                registerMeta.setGroup(group == null ? "group" : group);
                registerMeta.setServiceProviderName(name);
                registerMeta.setVersion(version == null ? "1.0.0" : version);
                ReflectAsmUtils.add(name, providerImpl);
                server.getNettyServer().getRegistryService().register(registerMeta);
                log.info("#publish service: {}.", registerMeta);
            }
        }
    }
}
