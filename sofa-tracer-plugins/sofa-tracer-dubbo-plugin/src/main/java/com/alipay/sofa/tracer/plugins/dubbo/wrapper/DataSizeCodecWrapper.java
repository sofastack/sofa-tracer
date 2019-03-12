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

import com.alipay.common.tracer.core.span.CommonSpanTags;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.Codec2;
import org.apache.dubbo.remoting.buffer.ChannelBuffer;
import org.apache.dubbo.remoting.exchange.Request;
import org.apache.dubbo.remoting.exchange.Response;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.RpcResult;
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
            Object result = ((Response) message).getResult();
            if (result instanceof RpcResult) {
                RpcResult rpcResult = (RpcResult) result;
                encodeResultWithTracer(channel, buffer, message, rpcResult);
                return;
            }
        }
        // 其它走原来
        codec.encode(channel, buffer, message);
    }

    /**
     * @param channel    长连接
     * @param buffer        UnsafeByteArrayOutputStream
     * @param message    原生Request对象
     * @param invocation Request里的Invocation
     * @throws IOException 序列化出现异常
     */
    protected void encodeRequestWithTracer(Channel channel, ChannelBuffer buffer, Object message,
                                           RpcInvocation invocation) throws IOException {
        long startTime = System.currentTimeMillis();
        int index = buffer.writerIndex();
        // 序列化
        codec.encode(channel, buffer, message);
        int reqSize = buffer.writerIndex() - index;
        long elapsed = System.currentTimeMillis() - startTime;
        invocation.setAttachment(Constants.INPUT_KEY, String.valueOf(reqSize));
        invocation.setAttachment(CommonSpanTags.CLIENT_SERIALIZE_TIME, String.valueOf(elapsed));
    }

    /**
     * @param channel   长连接
     * @param buffer       UnsafeByteArrayOutputStream
     * @param result    原生Resopnse对象
     * @param rpcResult Resopnse对象的结果
     * @throws IOException 序列化出现异常
     */
    protected void encodeResultWithTracer(Channel channel, ChannelBuffer buffer, Object result,
                                          RpcResult rpcResult) throws IOException {

        long startTime = System.currentTimeMillis();
        int index = buffer.writerIndex();
        codec.encode(channel, buffer, result);
        int respSize = buffer.writerIndex() - index;
        long elapsed = System.currentTimeMillis() - startTime;
        rpcResult.setAttachment(Constants.OUTPUT_KEY, String.valueOf(respSize));
        rpcResult.setAttachment(CommonSpanTags.SERVER_SERIALIZE_TIME, String.valueOf(elapsed));
    }

    /**
     * 反序列化操作
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
            // 服务端反序列化Request
            Object data = ((Request) ret).getData();
            if (data instanceof RpcInvocation) {
                RpcInvocation invocation = (RpcInvocation) data;
                invocation.setAttachment(Constants.INPUT_KEY, String.valueOf(size));
                invocation.setAttachment(CommonSpanTags.SERVER_DESERIALIZE_TIME,
                    String.valueOf(elapsed));
            }

        } else if (ret instanceof Response) {
            // 客户端反序列化Response
            Object result = ((Response) ret).getResult();
            if (result instanceof RpcResult) {
                RpcResult rpcResult = (RpcResult) result;
                rpcResult.setAttachment(Constants.OUTPUT_KEY, String.valueOf(size));
                rpcResult.setAttachment(CommonSpanTags.CLIENT_DESERIALIZE_TIME,
                    String.valueOf(elapsed));
            }
        }
        return ret;
    }
}
