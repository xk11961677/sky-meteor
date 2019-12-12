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
import com.sky.meteor.common.constant.CommonConstants;
import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.remoting.netty.client.pool.ChannelGenericPool;
import com.sky.meteor.remoting.netty.client.pool.ChannelGenericPoolFactory;
import com.sky.meteor.remoting.protocol.LongSequenceHelper;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;
import com.sky.meteor.rpc.future.DefaultInvokeFuture;
import com.sky.meteor.serialization.ObjectSerializer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


/**
 * @author
 */
@Slf4j
public class InvokerDispatcher implements Dispatcher {

    @Override
    public Object dispatch(Invocation invocation, Class<?> returnType) {

        Invoker invoker = new InvokerWrapper(invocation, returnType);
        ClusterInvoker clusterInvoker = SpiExtensionHolder.getInstance().get(ClusterInvoker.class);
        Object result = clusterInvoker.invoke(invoker, invocation);
        return result;
    }

    class InvokerWrapper implements Invoker {

        Invocation invocation;

        Class<?> returnType;

        Request request;

        long id;

        InvokerWrapper(Invocation invocation, Class<?> returnType) {
            this.invocation = invocation;
            this.returnType = returnType;
            this.id = LongSequenceHelper.getId();
        }


        @Override
        public <T> T invoke(Invocation invocation) throws RpcException {
            request = new Request(id);
            ObjectSerializer serializer = SpiExtensionHolder.getInstance().get(ObjectSerializer.class);
            byte[] serialize = serializer.serialize(invocation);
            request.bytes(serializer.getSchema(), serialize);
            return (T) doInvoke();
        }

        private RegisterMeta.Address getAddress() {
            String group = invocation.getAttachment(CommonConstants.GROUP);
            String version = invocation.getAttachment(CommonConstants.VERSION, "1.0.0");
            String providerName = invocation.getAttachment(CommonConstants.PROVIDER_NAME, invocation.getClazzName());

            RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta(group, providerName, version);
            LoadBalance instance = SpiExtensionHolder.getInstance().get(LoadBalance.class);
            return instance.select(serviceMeta);
        }

        private DefaultInvokeFuture doInvoke() {
            RegisterMeta.Address address = getAddress();
            long timeout = Long.parseLong(invocation.getAttachment(CommonConstants.TIMEOUT, "0"));

            //todo 对象池每次请求需要一个channel不太好, 性能待优化
            ChannelGenericPool channelGenericPool = ChannelGenericPoolFactory.getPools().get(address);
            DefaultInvokeFuture invokeFuture = null;
            Channel channel = null;
            try {
                channel = channelGenericPool.getConnection();
                try {
                    invokeFuture = DefaultInvokeFuture.with(id, timeout, returnType);
                    channel.writeAndFlush(request);
                } catch (Exception e) {
                    log.error("the client invoke failed:{}", e.getMessage());
                    Response response = new Response(id);
                    response.setStatus(Status.CLIENT_ERROR.getKey());
                    DefaultInvokeFuture.fakeReceived(response);
                }
            } catch (Exception e) {
                log.error("InvokerWrapper exception:{}", e);
                throw new RpcException(Status.CLIENT_ERROR.getKey(), Status.CLIENT_ERROR.getValue(), e);
            } finally {
                channelGenericPool.releaseConnection(channel);
            }
            return invokeFuture;
        }
    }
}
