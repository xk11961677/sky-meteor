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
package com.sky.meteor.remoting.netty.protocol;

import com.sky.meteor.remoting.Request;
import com.sky.meteor.remoting.Response;
import com.sky.meteor.remoting.protocol.PayloadHolder;
import com.sky.meteor.remoting.protocol.ProtocolHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author
 */
@Slf4j
public class ProtocolEncoder extends MessageToByteEncoder<PayloadHolder> {

    @Override
    protected void encode(ChannelHandlerContext ctx, PayloadHolder msg, ByteBuf out) throws Exception {
        if (msg instanceof Request) {
            doEncodeRequest((Request) msg, out);
        } else if (msg instanceof Response) {
            doEncodeResponse((Response) msg, out);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 编码request对象
     *
     * @param request
     * @param out
     */
    private void doEncodeRequest(Request request, ByteBuf out) {
        byte sign = ProtocolHeader.toSign(request.getSerializerCode(), ProtocolHeader.REQUEST);
        long invokeId = request.getId();
        byte[] bytes = request.getBytes();
        int length = bytes.length;
        out.writeShort(ProtocolHeader.MAGIC)
                .writeByte(sign)
                .writeByte(0x00)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(bytes);
    }

    /**
     * 编码response对象
     *
     * @param response
     * @param out
     */
    private void doEncodeResponse(Response response, ByteBuf out) {
        byte sign = ProtocolHeader.toSign(response.getSerializerCode(), ProtocolHeader.RESPONSE);
        byte status = response.getStatus();
        long invokeId = response.getId();
        byte[] bytes = response.getBytes();
        int length = bytes.length;
        out.writeShort(ProtocolHeader.MAGIC)
                .writeByte(sign)
                .writeByte(status)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(bytes);
    }
}
