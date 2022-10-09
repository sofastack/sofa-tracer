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
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.Format;

import java.nio.ByteBuffer;

/**
 * BinaryFormater
 * <p>
 *     Note: only supports the heap memory does not support outside the heap memory
 * </p>
 * @author yangguanchao
 * @since 2017/06/23
 */
public class BinaryFormater implements RegistryExtractorInjector<Binary> {

    /**
     * As the keyword key or header identification information of the cross-process transmission field,
     * its value is the serialization representation of {@link SofaTracerSpanContext}: sofa tracer head
     *
     * Converted to bytecode, this set of bytecodes will be used as the start of the spanContext in the byteArray
     */
    private static final byte[] FORMATER_KEY_HEAD_BYTES = FORMATER_KEY_HEAD
                                                            .getBytes(SofaTracerConstant.DEFAULT_UTF8_CHARSET);

    @Override
    public Format<Binary> getFormatType() {
        return Format.Builtin.BINARY;
    }

    @Override
    public SofaTracerSpanContext extract(Binary carrier) {
        if (carrier == null) {
            return SofaTracerSpanContext.rootStart();
        }
        ByteBuffer buf = carrier.extractionBuffer();
        if (buf == null || buf.array().length < FORMATER_KEY_HEAD_BYTES.length) {
            return SofaTracerSpanContext.rootStart();
        }
        byte[] carrierDatas = buf.array();
        //head
        byte[] formaterKeyHeadBytes = FORMATER_KEY_HEAD_BYTES;
        int index = ByteArrayUtils.indexOf(carrierDatas, formaterKeyHeadBytes);
        if (index < 0) {
            return SofaTracerSpanContext.rootStart();
        }
        try {
            //(UTF-8)Put the head from 0
            buf.position(index + formaterKeyHeadBytes.length);
            //value byte arrays
            byte[] contextDataBytes = new byte[buf.getInt()];
            buf.get(contextDataBytes);
            String spanContextInfos = new String(contextDataBytes,
                    SofaTracerConstant.DEFAULT_UTF8_CHARSET);
            return SofaTracerSpanContext.deserializeFromString(spanContextInfos);
        } catch (Exception e) {
            SelfLog
                    .error(
                            "com.alipay.common.tracer.core.registry.BinaryFormater.extract Error.Recover by root start",
                            e);
            return SofaTracerSpanContext.rootStart();
        }
    }

    @Override
    public void inject(SofaTracerSpanContext spanContext, Binary carrier) {
        if (carrier == null || spanContext == null) {
            return;
        }
        ByteBuffer buf = carrier.injectionBuffer(64);
        //head
        buf.put(FORMATER_KEY_HEAD_BYTES);
        String spanContextInfos = spanContext.serializeSpanContext();
        byte[] value = spanContextInfos.getBytes(SofaTracerConstant.DEFAULT_UTF8_CHARSET);
        //length
        buf.putInt(value.length);
        //data
        buf.put(value);
    }

}
