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


import com.sky.meteor.common.constant.CommonConstants;
import com.sky.meteor.rpc.RpcInvocation;
import com.sky.meteor.rpc.annotation.Reference;
import com.sky.meteor.rpc.consumer.Dispatcher;
import com.sky.meteor.rpc.consumer.InvokerDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author
 */
@Slf4j
public class Proxy {

    private static final Dispatcher dispatcher = new InvokerDispatcher();

    private Class<?> interfaceClass;

    private Reference reference;

    public Proxy(Class<?> interfaceClass, Reference reference) {
        this.interfaceClass = interfaceClass;
        this.reference = reference;
    }

    public Object remoteCall(Method method, Object[] args) {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setClazzName(interfaceClass.getName());
        invocation.setMethodName(method.getName());
        invocation.setParameterTypes(method.getParameterTypes());
        invocation.setArguments(args);
        /**
         * 以下参数均有默认值,可不填写
         */
        invocation.setAttachment(CommonConstants.GROUP, reference.group());
        invocation.setAttachment(CommonConstants.VERSION, reference.version());
        invocation.setAttachment(CommonConstants.PROVIDER_NAME, reference.name());

        Object result = dispatcher.dispatch(invocation, method.getReturnType());
        return result;
    }


}
