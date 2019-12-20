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
package com.sky.meteor.rpc;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 */
public class RpcContext {

    private static final ThreadLocal<RpcContext> context = ThreadLocal.withInitial(() -> new RpcContext());

    private static final ThreadLocal<RpcContext> serverContext = ThreadLocal.withInitial(() -> new RpcContext());

    public static ThreadLocal<RpcContext> getContext() {
        return context;
    }

    public static ThreadLocal<RpcContext> getServerContext() {
        return serverContext;
    }

    public static void remove() {
        context.remove();
    }

    public static void removeServer() {
        serverContext.remove();
    }

    @Getter
    @Setter
    private Invoker invoker;

    @Getter
    @Setter
    private Invocation invocation;

    @Getter
    @Setter
    private Class<?> returnType;

    private Map<String, String> attachments;


    public void setAttachment(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<>();
        }
        attachments.put(key, value);
    }

    public void setAttachmentIfAbsent(String key, String value) {
        if (attachments.get(key) == null) {
            setAttachment(key, value);
        }
    }

    public String getAttachment(String key) {
        return attachments.get(key);
    }

    public String getAttachment(String key, String defaultValue) {
        String value = attachments.get(key);
        return (value == null || "".equals(value)) ? defaultValue : value;
    }
}
