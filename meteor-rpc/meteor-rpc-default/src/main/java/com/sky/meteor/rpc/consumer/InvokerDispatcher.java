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
package com.sky.meteor.rpc.consumer;

import com.sky.meteor.cluster.ClusterInvoker;
import com.sky.meteor.cluster.loadbalance.LoadBalance;
import com.sky.meteor.common.config.ConfigManager;
import com.sky.meteor.common.constant.CommonConstants;
import com.sky.meteor.common.enums.SideEnum;
import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;
import com.sky.meteor.rpc.RpcContext;
import com.sky.meteor.rpc.consumer.invoker.FixedClientInvoker;
import com.sky.meteor.rpc.consumer.invoker.GenericClientInvoker;
import com.sky.meteor.rpc.filter.FilterBuilder;
import com.sky.meteor.serialization.ObjectSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
 * @author
 */
@Slf4j
public class InvokerDispatcher implements Dispatcher {

    private Invoker chain;

    private ObjectSerializer serializer;

    private LoadBalance loadBalance;

    public InvokerDispatcher() {
        ClusterInvoker clusterInvoker = SpiExtensionHolder.getInstance().get(ClusterInvoker.class);
        serializer = SpiExtensionHolder.getInstance().get(ObjectSerializer.class);
        loadBalance = SpiExtensionHolder.getInstance().get(LoadBalance.class);
        Invoker invokerWrapper = ConfigManager.nettyChannelPool() ? new FixedClientInvoker(serializer, loadBalance) :
                new GenericClientInvoker(serializer, loadBalance);
        Invoker last = new Invoker() {
            @Override
            public <T> T invoke(Invocation invocation) throws RpcException {
                return clusterInvoker.invoke(invokerWrapper, invocation);
            }
        };
        chain = FilterBuilder.build(last, SideEnum.CONSUMER);
    }

    @Override
    public Object dispatch(Invocation invocation, Class<?> returnType) {
        Object result;
        try {
            RpcContext.getContext().get().setAttachment(CommonConstants.SIDE, SideEnum.CONSUMER.getKey());
            RpcContext.getContext().get().setReturnType(returnType);
            Map<String, String> attachments = invocation.getAttachments();
            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                RpcContext.getContext().get().setAttachment(entry.getKey(), entry.getValue());
            }
            result = chain.invoke(invocation);
        } finally {
            RpcContext.getContext().remove();
        }
        return result;
    }
}
