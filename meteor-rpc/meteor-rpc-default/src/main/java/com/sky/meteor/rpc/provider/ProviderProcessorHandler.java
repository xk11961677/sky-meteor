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


import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.Status;
import com.sky.meteor.rpc.AbstractProcessor;
import com.sky.meteor.rpc.RpcInvocation;
import com.sky.meteor.serialization.ObjectSerializer;
import com.sky.meteor.serialization.SerializerHolder;
import com.sky.meteor.common.threadpool.CommonThreadPool;
import com.sky.meteor.common.threadpool.DefaultAsynchronousHandler;
import com.sky.meteor.common.threadpool.ThreadPoolProperties;
import com.sky.meteor.common.util.ReflectAsmUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * todo 修改线程池
 *
 * @author
 */
@Slf4j
public class ProviderProcessorHandler extends AbstractProcessor {

    public ProviderProcessorHandler() {
        ThreadPoolProperties properties = new ThreadPoolProperties();
        CommonThreadPool.initThreadPool(properties);
    }

    @Override
    public void handler(ChannelHandlerContext ctx, Request request) {
        CommonThreadPool.execute(new DefaultAsynchronousHandler() {

            @Override
            public Object call() throws Exception {
                Response response = new Response(request.getId());
                response.setStatus(Status.OK.getKey());
                response.setSerializerCode(request.getSerializerCode());
                try {
                    ObjectSerializer serializer = SerializerHolder.getInstance().getSerializer(request.getSerializerCode());

                    byte[] bytes = request.getBytes();
                    RpcInvocation rpcInvocation = serializer.deSerialize(bytes, RpcInvocation.class);

                    Object result = ReflectAsmUtils.invoke(rpcInvocation.getClazzName(),
                            rpcInvocation.getMethodName(),
                            rpcInvocation.getParameterTypes(), rpcInvocation.getArguments());

                    byte[] body = serializer.serialize(result);
                    response.setBytes(body);

                } catch (Exception e) {
                    response.setStatus(Status.SERVER_ERROR.getKey());
                    log.error("the server exception :{}", e);
                } finally {
                    Channel channel = ctx.channel();
                    if (channel.isActive() && channel.isWritable()) {
                        channel.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                            log.info("the server response completed:{}");
                        });
                    }
                }
                return null;
            }
        });
    }
}
