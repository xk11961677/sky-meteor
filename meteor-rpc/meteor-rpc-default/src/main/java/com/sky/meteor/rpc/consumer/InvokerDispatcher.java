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
import com.sky.meteor.common.enums.SideEnum;
import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.common.spi.SpiLoader;
import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.remoting.netty.client.pool.ChannelGenericPool;
import com.sky.meteor.remoting.netty.client.pool.ChannelGenericPoolFactory;
import com.sky.meteor.remoting.protocol.LongSequenceHelper;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;
import com.sky.meteor.rpc.RpcContext;
import com.sky.meteor.rpc.filter.Filter;
import com.sky.meteor.rpc.filter.FilterBuilder;
import com.sky.meteor.rpc.future.DefaultInvokeFuture;
import com.sky.meteor.rpc.future.InvokeFuture;
import com.sky.meteor.serialization.ObjectSerializer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
        ClientInvoker invokerWrapper = new ClientInvoker();
        Invoker last = new Invoker() {
            @Override
            public <T> T invoke(Invocation invocation) throws RpcException {
                return clusterInvoker.invoke(invokerWrapper, invocation);
            }
        };
        chain = FilterBuilder.build(last, SideEnum.CONSUMER);
        serializer = SpiExtensionHolder.getInstance().get(ObjectSerializer.class);
        loadBalance = SpiExtensionHolder.getInstance().get(LoadBalance.class);
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


    /**
     * invoker尾节点,进行远程调用
     */
    private class ClientInvoker implements Invoker {
        @Override
        public <T> T invoke(Invocation invocation) throws RpcException {
            long id = LongSequenceHelper.getId();
            Request request = new Request(id);
            byte[] serialize = serializer.serialize(invocation);
            request.bytes(serializer.getSchema(), serialize);
            return (T) doInvoke(id, request, invocation);
        }

        private RegisterMeta.Address getAddress(Invocation invocation) {
            String group = invocation.getAttachment(CommonConstants.GROUP);
            String version = invocation.getAttachment(CommonConstants.VERSION, "1.0.0");
            String providerName = invocation.getAttachment(CommonConstants.PROVIDER_NAME, invocation.getClazzName());
            RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta(group, providerName, version);
            return loadBalance.select(serviceMeta);
        }

        private InvokeFuture doInvoke(Long id, Request request, Invocation invocation) {
            RegisterMeta.Address address = getAddress(invocation);
            long timeout = Long.parseLong(invocation.getAttachment(CommonConstants.TIMEOUT, "0"));

            //todo 对象池每次请求需要一个channel不太好, 性能待优化
            ChannelGenericPool channelGenericPool = ChannelGenericPoolFactory.getPools().get(address);
            InvokeFuture invokeFuture = null;
            Channel channel = null;
            try {
                channel = channelGenericPool.getConnection();
                try {
                    invokeFuture = DefaultInvokeFuture.with(id, timeout, RpcContext.getContext().get().getReturnType());
                    channel.writeAndFlush(request);
                } catch (Exception e) {
                    log.error("the client invoke failed:{}", e);
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
