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
package com.sky.meteor.rpc.filter;

import com.sky.meteor.common.enums.SideEnum;
import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.common.spi.SpiLoader;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;

import java.util.List;

/**
 * @author
 */
public class FilterBuilder {

    /**
     * 创建责任链
     *
     * @param last
     * @return
     */
    public static Invoker build(Invoker last, SideEnum side) {
        List<Filter> filters = SpiLoader.loadAllPriorityAndSide(Filter.class, side);
        Invoker next = last;
        for (Filter filter : filters) {
            next = getNode(filter, next);
        }
        return next;
    }

    /**
     * 获取节点
     *
     * @param filter
     * @param next
     * @return
     */
    private static Invoker getNode(Filter filter, Invoker next) {
        Invoker invoker = new Invoker() {
            @Override
            public <T> T invoke(Invocation invocation) throws RpcException {
                return filter.invoke(next, invocation);
            }
        };
        return invoker;
    }
}
