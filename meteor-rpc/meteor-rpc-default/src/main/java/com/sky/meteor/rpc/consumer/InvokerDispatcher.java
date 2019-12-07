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

import com.sky.meteor.cluster.loadbalance.LoadBalance;
import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.remoting.netty.client.pool.ChannelGenericPool;
import com.sky.meteor.remoting.netty.client.pool.ChannelGenericPoolFactory;
import com.sky.meteor.remoting.protocol.LongSequenceUtils;
import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.rpc.future.DefaultInvokeFuture;
import com.sky.meteor.common.exception.RpcException;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


/**
 * @author
 */
@Slf4j
public class InvokerDispatcher implements Dispatcher {


    @Override
    public DefaultInvokeFuture dispatch(Request request, RegisterMeta.ServiceMeta serviceMeta, Class<?> returnType) {
        LoadBalance instance = SpiExtensionHolder.getInstance().get(LoadBalance.class);
        RegisterMeta.Address select = instance.select(serviceMeta);

        ChannelGenericPool channelGenericPool = ChannelGenericPoolFactory.getClientPoolMap().get(select);
        Channel channel = null;
        DefaultInvokeFuture invokeFuture = null;
        try {
            channel = channelGenericPool.getConnection();
            invokeFuture = $invoke(channel, request, returnType);
        } catch (Exception e) {
            log.error("dispatcher exception:{}", e);
            throw new RpcException(Status.CLIENT_ERROR.value(), Status.CLIENT_ERROR.description(), e);
        } finally {
            channelGenericPool.releaseConnection(channel);
        }
        return invokeFuture;
    }


    /**
     * doInvoke
     *
     * @return
     * @throws Exception
     */
    private DefaultInvokeFuture $invoke(Channel channel, Request request, Class<?> returnType) throws Exception {
        DefaultInvokeFuture<?> invokeFuture = null;
        long id = LongSequenceUtils.getId();
        try {
            request.setId(id);
            invokeFuture = DefaultInvokeFuture.with(id, 0, returnType);
            channel.writeAndFlush(request);
        } catch (Exception e) {
            log.error("the client proxy invoke failed:{}", e.getMessage());
            Response response = new Response(id);
            response.setStatus(Status.CLIENT_ERROR.value());
            DefaultInvokeFuture.fakeReceived(response);
        }
        return invokeFuture;
    }
}
