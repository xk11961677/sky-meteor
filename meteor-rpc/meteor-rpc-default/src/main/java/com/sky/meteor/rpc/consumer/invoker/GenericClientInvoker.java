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
package com.sky.meteor.rpc.consumer.invoker;

import com.sky.meteor.cluster.loadbalance.LoadBalance;
import com.sky.meteor.common.constant.CommonConstants;
import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.remoting.netty.client.NettyClient;
import com.sky.meteor.remoting.netty.client.pool.commons.ChannelGenericPool;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.RpcContext;
import com.sky.meteor.rpc.future.DefaultInvokeFuture;
import com.sky.meteor.rpc.future.InvokeFuture;
import com.sky.meteor.serialization.ObjectSerializer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author
 */
@Slf4j
public class GenericClientInvoker extends ClientInvokerTemplate {

    public GenericClientInvoker(ObjectSerializer serializer, LoadBalance loadBalance) {
        super.serializer = serializer;
        super.loadBalance = loadBalance;
    }

    @Override
    public InvokeFuture doInvoke(Long id, InetSocketAddress address, Request request, Invocation invocation) {
        long timeout = Long.parseLong(invocation.getAttachment(CommonConstants.TIMEOUT, "0"));
        InvokeFuture invokeFuture = DefaultInvokeFuture.with(id, timeout, RpcContext.getContext().get().getReturnType());
        try {
            ChannelGenericPool channelGenericPool = (ChannelGenericPool) NettyClient.getInstance().getChannelPoolFactory().get(address);
            Channel channel = channelGenericPool.acquire();
            try {
                channel.writeAndFlush(request);
            } finally {
                channelGenericPool.release(channel);
            }
        } catch (Exception e) {
            log.error("the client invoker failed:{}", e.getMessage());
            response(id, Status.CLIENT_ERROR);
        }
        return invokeFuture;
    }

}