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
package com.sky.meteor.remoting.netty.client.pool.commons;

import com.sky.meteor.remoting.netty.client.NettyClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.BasePoolableObjectFactory;

import java.net.InetSocketAddress;

/**
 * @author
 */
@Slf4j
public class BaseConnectionFactory extends BasePoolableObjectFactory<Channel> {

    private NettyClient client;

    private InetSocketAddress address;

    public BaseConnectionFactory(NettyClient client, InetSocketAddress address) {
        this.client = client;
        this.address = address;
    }

    @Override
    public Channel makeObject() throws Exception {
        return client.getChannel(address);
    }

    @Override
    public void destroyObject(Channel channel) throws Exception {
        channel.close().addListener((ChannelFutureListener) channelFuture -> log.info("baseConnectionFactory close channel complete !"));
    }

    @Override
    public boolean validateObject(Channel channel) {
        return channel.isOpen() || channel.isActive() || channel.isWritable();
    }
}