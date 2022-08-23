package com.alipay.common.tracer.core.registry;

import io.opentracing.propagation.Binary;

import java.nio.ByteBuffer;

public class BinaryCarrierTest implements Binary {

    ByteBuffer buffer = null;

    public BinaryCarrierTest(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public BinaryCarrierTest(){

    }
    @Override
    public ByteBuffer injectionBuffer(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be greater than zero");
        }
        if (buffer == null) {
            buffer = ByteBuffer.allocate(length);
        }
        return buffer;
    }

    @Override
    public ByteBuffer extractionBuffer() {
        return buffer;
    }
}
