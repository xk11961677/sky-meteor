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

import com.sky.meteor.remoting.Response;
import com.sky.meteor.rpc.AbstractProcessor;
import com.sky.meteor.rpc.future.DefaultInvokeFuture;
import com.sky.meteor.common.threadpool.CommonThreadPool;
import com.sky.meteor.common.threadpool.DefaultAsynchronousHandler;
import com.sky.meteor.common.threadpool.ThreadPoolProperties;
import io.netty.channel.ChannelHandlerContext;

/**
 * todo 修改线程池
 *
 * @author
 */
public class ConsumerProcessorHandler extends AbstractProcessor {

    public ConsumerProcessorHandler() {
        ThreadPoolProperties properties = new ThreadPoolProperties();
        CommonThreadPool.initThreadPool(properties);
    }

    @Override
    public void handler(ChannelHandlerContext ctx, Response response) {
        CommonThreadPool.execute(new DefaultAsynchronousHandler() {
            @Override
            public Object call() throws Exception {
                DefaultInvokeFuture.received(response);
                return null;
            }
        });
    }

}
