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
package com.sky.meteor.remoting.netty.client.pool.fixed;

import com.sky.meteor.common.config.ConfigManager;
import com.sky.meteor.remoting.netty.client.ChannelInitializerHandler;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author
 */
@Slf4j
public class CustomChannelPoolHandler implements ChannelPoolHandler {

    private ChannelInitializerHandler initializerHandler;

    public CustomChannelPoolHandler(ChannelInitializerHandler initializerHandler) {
        this.initializerHandler = initializerHandler;
    }

    @Override
    public void channelReleased(Channel channel) throws Exception {
        log.info("channelPoolChandler released :{}", channel.id());
    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {
        log.info("channelPoolChandler acquired :{}", channel.id());
    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        log.info("channelPoolChandler created :{}", channel.id());
        SocketChannel ch = (SocketChannel) channel;
        ch.config().setTcpNoDelay(ConfigManager.tcpNodelay());
        ch.pipeline().addLast(initializerHandler);
    }
}
