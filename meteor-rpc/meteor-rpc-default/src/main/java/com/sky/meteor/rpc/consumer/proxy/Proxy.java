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
package com.sky.meteor.rpc.consumer.proxy;


import com.sky.meteor.cluster.ClusterInvoker;
import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.registry.meta.RegisterMeta;
import com.sky.meteor.remoting.Request;
import com.sky.meteor.rpc.RpcInvocation;
import com.sky.meteor.rpc.annotation.Reference;
import com.sky.meteor.rpc.consumer.Dispatcher;
import com.sky.meteor.rpc.consumer.InvokerDispatcher;
import com.sky.meteor.serialization.ObjectSerializer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author
 */
@Slf4j
public class Proxy {

    private static Dispatcher dispatcher = new InvokerDispatcher();

    private Class<?> interfaceClass;

    private Reference reference;

    public Proxy(Class<?> interfaceClass, Reference reference) {
        this.interfaceClass = interfaceClass;
        this.reference = reference;
    }

    public Object remoteCall(Method method, Object[] args) {
        //todo 将信息抽取context 方便filter和interceptor
        RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta();
        serviceMeta.setGroup(reference.group());
        serviceMeta.setServiceProviderName(interfaceClass.getName());
        serviceMeta.setVersion(reference.version());

        Request request = new Request();
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setClazzName(interfaceClass.getName());
        rpcInvocation.setMethodName(method.getName());
        rpcInvocation.setParameterTypes(method.getParameterTypes());
        rpcInvocation.setArguments(args);

        ObjectSerializer serializer = SpiExtensionHolder.getInstance().get(ObjectSerializer.class);
        byte[] serialize = serializer.serialize(rpcInvocation);
        request.bytes(serializer.getSchema(), serialize);

        ClusterInvoker invoker = SpiExtensionHolder.getInstance().get(ClusterInvoker.class);
        Object result = invoker.invoke(dispatcher, request, serviceMeta, method.getReturnType());

        return result;
    }


}
