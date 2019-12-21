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
import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.remoting.protocol.LongSequenceHelper;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;
import com.sky.meteor.rpc.future.DefaultInvokeFuture;
import com.sky.meteor.rpc.future.InvokeFuture;
import com.sky.meteor.serialization.ObjectSerializer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author
 */
@Slf4j
public abstract class ClientInvokerTemplate implements Invoker {

    protected ObjectSerializer serializer;

    protected LoadBalance loadBalance;

    @Override
    public <T> T invoke(Invocation invocation) throws RpcException {
        long id = LongSequenceHelper.getId();
        Request request = new Request(id);
        byte[] serialize = serializer.serialize(invocation);
        request.bytes(serializer.getSchema(), serialize);
        RegisterMeta.Address address = getAddress(invocation);
        if (address == null) {
            log.error("the client invoker address not found:{}");
            response(id, Status.SERVICE_NOT_FOUND);
            return null;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address.getHost(), address.getPort());
        return (T) doInvoke(id, inetSocketAddress, request, invocation);
    }

    protected RegisterMeta.Address getAddress(Invocation invocation) {
        String group = invocation.getAttachment(CommonConstants.GROUP);
        String version = invocation.getAttachment(CommonConstants.VERSION, "1.0.0");
        String providerName = invocation.getAttachment(CommonConstants.PROVIDER_NAME, invocation.getClazzName());
        RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta(group, providerName, version);
        return loadBalance.select(serviceMeta);
    }


    protected void response(Long id, Status status) {
        Response response = new Response(id);
        response.setStatus(status.getKey());
        DefaultInvokeFuture.fakeReceived(response);
    }

    /**
     * 远程调用
     *
     * @param id
     * @param address
     * @param request
     * @param invocation
     * @return
     */
    public abstract InvokeFuture doInvoke(Long id, InetSocketAddress address, Request request, Invocation invocation);
}
