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
package com.sky.meteor.remoting.netty.client;


import com.sky.meteor.common.config.ConfigManager;
import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.common.threadpool.ThreadPoolHelper;
import com.sky.meteor.registry.Register;
import com.sky.meteor.registry.Registry;
import com.sky.meteor.registry.RegistryService;
import com.sky.meteor.remoting.AbstractBootstrap;
import com.sky.meteor.remoting.netty.InternalHandler;
import com.sky.meteor.remoting.netty.Processor;
import com.sky.meteor.remoting.netty.client.pool.ChannelPoolFactory;
import com.sky.meteor.remoting.netty.client.pool.commons.GenericChannelPoolFactory;
import com.sky.meteor.remoting.netty.client.pool.fixed.FixedChannelPoolFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author
 */
@Slf4j
public class NettyClient extends AbstractBootstrap implements Registry, InternalHandler {
    /**
     * client对象
     */
    private static NettyClient client = null;
    /**
     * 事件组
     */
    private EventLoopGroup group = new NioEventLoopGroup();
    /**
     * 启动项
     */
    private Bootstrap bootstrap;
    /**
     * 内部处理器
     */
    private Processor processor;
    /**
     * 注册中心
     */
    @Getter
    private RegistryService registryService;

    /**
     *
     */
    @Getter
    private ChannelPoolFactory channelPoolFactory;


    public NettyClient() {
        client = this;
    }

    @Override
    public void startup() {
        super.startup();
        this.init();
    }


    @Override
    public void shutdown() {
        if (status()) {
            super.shutdown();
            try {
                channelPoolFactory.close();
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
            try {
                ThreadPoolHelper.shutdown();
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
            try {
                if (group != null) {
                    group.shutdownGracefully();
                }
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            log.info(" the remoting client has been shutdown !");
        }
    }

    @Override
    public void init() {
        ClientChannelHandler clientChannelHandler = new ClientChannelHandler(processor);
        ChannelInitializerHandler channelInitializerHandler = new ChannelInitializerHandler(clientChannelHandler);

        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, ConfigManager.tcpNodelay())
                .handler(channelInitializerHandler);
        /**
         * 是否使用netty fixedChannelPool
         */
        channelPoolFactory = ConfigManager.nettyChannelPool() ? new FixedChannelPoolFactory(bootstrap, channelInitializerHandler) :
                new GenericChannelPoolFactory();
        channelPoolFactory.init();
    }

    @Override
    public void connectToRegistryServer(Register register) {
        registryService = SpiExtensionHolder.getInstance().loadSpiExtension(RegistryService.class, register.getName());
        registryService.addNotifyListener(new RemotingNotifyListener());
        registryService.connectToRegistryServer(register);
    }

    @Override
    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    /**
     * 获取channel
     *
     * @return
     */
    public Channel getChannel(InetSocketAddress address) {
        try {
            ChannelFuture f = bootstrap.connect(address).sync();
            Channel channel = f.channel();
            return channel;
        } catch (Exception e) {
            log.error("the remoting client get channel failed! :{}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取client对象
     *
     * @return
     */
    public static NettyClient getInstance() {
        return client;
    }
}
