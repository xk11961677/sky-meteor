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

import com.sky.meteor.remoting.netty.client.pool.ChannelPoolFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author
 */
public class GenericChannelPoolFactory implements ChannelPoolFactory<ChannelGenericPool> {

    private ConcurrentHashMap<InetSocketAddress, ChannelGenericPool> pools = new ConcurrentHashMap<>();

    private final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public ChannelGenericPool get(InetSocketAddress address) {
        ChannelGenericPool pool = pools.get(address);
        if (pool != null) {
            return pool;
        }
        LOCK.lock();
        try {
            pool = pools.get(address);
            if (pool == null) {
                pools.putIfAbsent(address, new ChannelGenericPool(address));
            }
        } finally {
            LOCK.unlock();
        }
        return pools.get(address);
    }

    /**
     * 创建channel对象池
     */
    @Override
    public void init() {
    }

    /**
     * 销毁所有channel对象池
     */
    @Override
    public void close() {
        pools.values().forEach(pool -> pool.close());
    }
}
