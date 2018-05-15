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
package com.alipay.common.tracer.core.registry;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.utils.ByteArrayUtils;
import io.opentracing.propagation.Format;

import java.nio.ByteBuffer;

/**
 * BinaryFormater
 * <p>
 * 注意:只支持堆内存不支持堆外内存
 *
 * @author yangguanchao
 * @since 2017/06/23
 */
public class BinaryFormater implements RegistryExtractorInjector<ByteBuffer> {

    /***
     * 作为跨进程传输字段的关键字key或者头部标识信息,其 value 就是 {@link SofaTracerSpanContext} 的序列化表现:sofa tracer head
     * 转换为字节码,这组字节码会作为 byteArray 中的 spanContext 起始标示
     */
    private static final byte[] FORMATER_KEY_HEAD_BYTES = FORMATER_KEY_HEAD
                                                            .getBytes(SofaTracerConstant.DEFAULT_UTF8_CHARSET);

    @Override
    public Format<ByteBuffer> getFormatType() {
        return Format.Builtin.BINARY;
    }

    @Override
    public SofaTracerSpanContext extract(ByteBuffer carrier) {
        if (carrier == null || carrier.array().length < FORMATER_KEY_HEAD_BYTES.length) {
            //从新开始
            return SofaTracerSpanContext.rootStart();
        }
        byte[] carrierDatas = carrier.array();
        //head
        byte[] formaterKeyHeadBytes = FORMATER_KEY_HEAD_BYTES;
        int index = ByteArrayUtils.indexOf(carrierDatas, formaterKeyHeadBytes);
        if (index < 0) {
            //从新开始
            return SofaTracerSpanContext.rootStart();
        }
        try {
            //(UTF-8)放在头部从 0 开始
            carrier.position(index + formaterKeyHeadBytes.length);
            //value 字节数组
            byte[] contextDataBytes = new byte[carrier.getInt()];
            carrier.get(contextDataBytes);
            String spanContextInfos = new String(contextDataBytes,
                SofaTracerConstant.DEFAULT_UTF8_CHARSET);
            return SofaTracerSpanContext.deserializeFromString(spanContextInfos);
        } catch (Exception e) {
            SelfLog
                .error(
                    "com.alipay.common.tracer.core.registry.BinaryFormater.extract Error.Recover by root start",
                    e);
            //从新开始
            return SofaTracerSpanContext.rootStart();
        }
    }

    @Override
    public void inject(SofaTracerSpanContext spanContext, ByteBuffer carrier) {
        if (carrier == null || spanContext == null) {
            return;
        }
        //head
        carrier.put(FORMATER_KEY_HEAD_BYTES);
        String spanContextInfos = spanContext.serializeSpanContext();
        byte[] value = spanContextInfos.getBytes(SofaTracerConstant.DEFAULT_UTF8_CHARSET);
        //length
        carrier.putInt(value.length);
        //data
        carrier.put(value);
    }

}
