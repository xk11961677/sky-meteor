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
package com.sky.meteor.cluster.loadbalance;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.sky.meteor.common.spi.SpiMetadata;
import com.sky.meteor.registry.AbstractRegistryService;
import com.sky.meteor.registry.meta.RegisterMeta;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 简单轮训负载均衡方式
 *
 * @author
 */
@SpiMetadata(name = "roundrobin")
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    /**
     * snapshot
     */
    private CopyOnWriteArrayList<RegisterMeta.Address> cList = new CopyOnWriteArrayList();

    private static final AtomicIntegerFieldUpdater<RoundRobinLoadBalance> indexUpdater =
            AtomicIntegerFieldUpdater.newUpdater(RoundRobinLoadBalance.class, "index");

    private volatile int index = 0;

    @Override
    public <T> T doSelect(RegisterMeta.ServiceMeta serviceMeta) {
        ConcurrentHashSet<RegisterMeta.Address> addresses = AbstractRegistryService.metaAddressMap.get(serviceMeta);
        if (addresses == null) {
            return null;
        }
        this.snapshot(addresses);
        int rrIndex = indexUpdater.getAndIncrement(this) & Integer.MAX_VALUE;
        return (T) cList.get(rrIndex % cList.size());
    }

    /**
     * @param addresses
     */
    private void snapshot(ConcurrentHashSet<RegisterMeta.Address> addresses) {
        if (cList.size() != addresses.size()) {
            cList.addAll(addresses);
        }
    }
}
