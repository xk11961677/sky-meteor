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

import com.sky.meteor.remoting.netty.client.ChannelInitializerHandler;
import com.sky.meteor.remoting.netty.client.pool.ChannelPoolFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.SimpleChannelPool;

import java.net.InetSocketAddress;

/**
 * @author
 */
public class FixedChannelPoolFactory implements ChannelPoolFactory {

    private ChannelPoolMap<InetSocketAddress, SimpleChannelPool> pools = null;

    private Bootstrap bootstrap;

    private ChannelInitializerHandler channelInitializerHandler;

    public FixedChannelPoolFactory(Bootstrap bootstrap, ChannelInitializerHandler channelInitializerHandler) {
        this.bootstrap = bootstrap;
        this.channelInitializerHandler = channelInitializerHandler;
    }

    @Override
    public Object get(InetSocketAddress address) {
        return pools.get(address);
    }

    @Override
    public void init() {
        pools = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                return new CustomFixedChannelPool(bootstrap.remoteAddress(key), channelInitializerHandler);
            }
        };
    }

    @Override
    public void close() {
        ((AbstractChannelPoolMap) pools).close();
    }
}
