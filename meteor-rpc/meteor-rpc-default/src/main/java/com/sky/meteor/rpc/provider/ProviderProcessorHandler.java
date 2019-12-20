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
package com.sky.meteor.rpc.provider;


import com.sky.meteor.common.constant.CommonConstants;
import com.sky.meteor.common.enums.SideEnum;
import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.common.threadpool.DefaultAsynchronousHandler;
import com.sky.meteor.common.threadpool.ThreadPoolHelper;
import com.sky.meteor.common.util.ReflectAsmUtils;
import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.remoting.netty.AbstractProcessor;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;
import com.sky.meteor.rpc.RpcContext;
import com.sky.meteor.rpc.RpcInvocation;
import com.sky.meteor.rpc.filter.FilterBuilder;
import com.sky.meteor.serialization.ObjectSerializer;
import com.sky.meteor.serialization.SerializerHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author
 */
@Slf4j
public class ProviderProcessorHandler extends AbstractProcessor {

    /**
     * 服务端责任链
     */
    private Invoker chain;

    public ProviderProcessorHandler() {
        super.executors();
        chain = FilterBuilder.build(new ServerInvoker(), SideEnum.PROVIDER);
    }

    @Override
    public void handler(ChannelHandlerContext ctx, Request request) {
        ThreadPoolHelper.execute(new DefaultAsynchronousHandler() {
            @Override
            public Object call() throws Exception {
                Response response = new Response(request.getId());
                response.setStatus(Status.OK.getKey());
                response.setSerializerCode(request.getSerializerCode());
                try {
                    RpcContext.getServerContext().get().setAttachment(CommonConstants.SIDE, SideEnum.PROVIDER.getKey());
                    ObjectSerializer serializer = SerializerHolder.getInstance().getSerializer(request.getSerializerCode());
                    RpcInvocation invocation = serializer.deSerialize(request.getBytes(), RpcInvocation.class);
                    Object result = chain.invoke(invocation);
                    response.setBytes(serializer.serialize(result));
                } catch (Exception e) {
                    response.setStatus(Status.SERVER_ERROR.getKey());
                    log.error("the server exception :{}", e);
                } finally {
                    RpcContext.getServerContext().remove();
                    Channel channel = ctx.channel();
                    if (channel.isActive() && channel.isWritable()) {
                        channel.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                            //log.info("the server response completed:{}");
                        });
                    }
                }
                return null;
            }
        });
    }

    /**
     * 服务端调用业务类
     */
    private class ServerInvoker implements Invoker {
        @Override
        public <T> T invoke(Invocation invocation) throws RpcException {
            Object result = ReflectAsmUtils.invoke(invocation.getClazzName(),
                    invocation.getMethodName(),
                    invocation.getParameterTypes(), invocation.getArguments());
            return (T) result;
        }
    }
}
