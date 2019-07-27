/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.tracer.plugins.dubbo.wrapper;

import com.alipay.sofa.tracer.plugins.dubbo.constants.AttachmentKeyConstants;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.Codec2;
import org.apache.dubbo.remoting.buffer.ChannelBuffer;
import org.apache.dubbo.remoting.exchange.Request;
import org.apache.dubbo.remoting.exchange.Response;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.RpcInvocation;
import java.io.IOException;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 7:46 PM
 * @since:
 **/
public class DataSizeCodecWrapper implements Codec2 {
    /**
     * origin codec
     */
    protected Codec2 codec;

    public DataSizeCodecWrapper(Codec2 codec) {
        this.codec = codec;
    }

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        if (message instanceof Request) {
            Object data = ((Request) message).getData();
            if (data instanceof RpcInvocation) {
                RpcInvocation invocation = (RpcInvocation) data;
                encodeRequestWithTracer(channel, buffer, message, invocation);
                return;
            }
        } else if (message instanceof Response) {
            Object response = ((Response) message).getResult();
            if (response instanceof AppResponse) {
                encodeResultWithTracer(channel, buffer, response);
                return;
            }
        }
        codec.encode(channel, buffer, message);
    }

    /**
     * @param channel       a long connection
     * @param buffer        buffer
     * @param message       the original Request object
     * @param invocation    Invocation in Request
     * @throws IOException  serialization exception
     */
    protected void encodeRequestWithTracer(Channel channel, ChannelBuffer buffer, Object message,
                                           RpcInvocation invocation) throws IOException {
        long startTime = System.currentTimeMillis();
        int index = buffer.writerIndex();
        // serialization
        codec.encode(channel, buffer, message);
        int reqSize = buffer.writerIndex() - index;
        long elapsed = System.currentTimeMillis() - startTime;
        invocation.setAttachment(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE,
            String.valueOf(reqSize));
        invocation.setAttachment(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME,
            String.valueOf(elapsed));
    }

    /**
     * @param channel       a long connection
     * @param buffer        buffer
     * @param result        the original Request object
     * @throws IOException  serialization exception
     */
    protected void encodeResultWithTracer(Channel channel, ChannelBuffer buffer, Object result)
                                                                                               throws IOException {
        Object response = ((Response) result).getResult();
        if (response instanceof AppResponse) {
            long startTime = System.currentTimeMillis();
            int index = buffer.writerIndex();
            codec.encode(channel, buffer, result);
            int respSize = buffer.writerIndex() - index;
            long elapsed = System.currentTimeMillis() - startTime;
            ((AppResponse) response).setAttachment(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE,
                String.valueOf(respSize));
            ((AppResponse) response).setAttachment(AttachmentKeyConstants.SERVER_SERIALIZE_TIME,
                String.valueOf(elapsed));
        }
    }

    /**
     * deserialization operation
     * @param channel
     * @param input
     * @return
     * @throws IOException
     */
    @Override
    public Object decode(Channel channel, ChannelBuffer input) throws IOException {
        long startTime = System.currentTimeMillis();
        int index = input.readerIndex();
        Object ret = codec.decode(channel, input);
        int size = input.readerIndex() - index;
        long elapsed = System.currentTimeMillis() - startTime;
        if (ret instanceof Request) {
            // server-side deserialize the Request
            Object data = ((Request) ret).getData();
            if (data instanceof RpcInvocation) {
                RpcInvocation invocation = (RpcInvocation) data;
                invocation.setAttachment(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE,
                    String.valueOf(size));
                invocation.setAttachment(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME,
                    String.valueOf(elapsed));
            }
        } else if (ret instanceof Response) {
            // client-side deserialize the Response
            Object result = ((Response) ret).getResult();
            if (result instanceof AppResponse) {
                AppResponse rpcResult = (AppResponse) result;
                rpcResult.setAttachment(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE,
                    String.valueOf(size));
                rpcResult.setAttachment(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME,
                    String.valueOf(elapsed));
            }
        }
        return ret;
    }
}
