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


import com.sky.meteor.registry.Register;
import com.sky.meteor.registry.Registry;
import com.sky.meteor.registry.RegistryService;
import com.sky.meteor.remoting.AbstractBootstrap;
import com.sky.meteor.remoting.netty.protocol.ProtocolDecoder;
import com.sky.meteor.remoting.netty.protocol.ProtocolEncoder;
import com.sky.meteor.rpc.InternalHandler;
import com.sky.meteor.rpc.Processor;
import com.sky.meteor.common.spi.SpiLoader;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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


    public NettyClient() {
        client = this;
    }

    @Override
    public void startup() {
        super.startup();
        this.init();
    }


    @Override
    public void stop() {
        if (status()) {
            super.stop();
            if (group != null) {
                group.shutdownGracefully();
            }
        } else {
            log.info(" the client has been shutdown !");
        }
    }

    @Override
    public void init() {
        /*LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);*/
        ClientChannelHandler clientChannelHandler = new ClientChannelHandler(processor);

        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
//                        p.addLast("loggingHandler", loggingHandler);
                        p.addLast(new ClientIdleStateTrigger());
                        p.addLast(new HeartbeatChannelHandler());
                        p.addLast("protocolEncoder", new ProtocolEncoder());
                        p.addLast("protocolDecoder", new ProtocolDecoder());
//                        p.addLast("loggingHandler", loggingHandler);
                        p.addLast("clientChannelHandler", clientChannelHandler);
                    }
                });
    }

    @Override
    public void connectToRegistryServer(Register register) {
        registryService = SpiLoader.loadName(RegistryService.class, register.getName());
        registryService.addNotifyListener(new PoolNotifyListener());
        registryService.connectToRegistryServer(register);
    }

    @Override
    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    /**
     * 获取channel
     *
     * @param key
     * @return
     */
    public Channel getChannel(String key) {
        String[] split = key.split(":");
        return getChannel(split[0], Integer.parseInt(split[1]));
    }

    /**
     * 获取client对象
     *
     * @return
     */
    public static NettyClient getInstance() {
        return client;
    }

    /**
     * 根据address port 获取channel
     *
     * @param address
     * @param port
     * @return
     */
    private Channel getChannel(String address, int port) {
        try {
            ChannelFuture f = bootstrap.connect(address, port).sync();
            Channel channel = f.channel();
            return channel;
        } catch (Exception e) {
            log.error("the client get channel failed! :{}", e.getMessage());
        }
        return null;
    }
}
