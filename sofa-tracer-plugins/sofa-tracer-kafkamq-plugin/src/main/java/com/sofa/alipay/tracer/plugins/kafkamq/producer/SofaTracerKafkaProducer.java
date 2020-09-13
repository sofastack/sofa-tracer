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
package com.sofa.alipay.tracer.plugins.kafkamq.producer;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.sofa.alipay.tracer.plugins.kafkamq.carrier.KafkaMqInjectCarrier;
import com.sofa.alipay.tracer.plugins.kafkamq.tracers.KafkaMQSendTracer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.header.Headers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * SofaTracerKafkaProducer.
 *
 * @author chenchen6   2020/8/24 23:05
 *
 */
public class SofaTracerKafkaProducer<K, V> implements Producer<K, V> {

    private final String      kafkaSendPostFix = "-kafka-send";

    private Producer<K, V>    producer;

    private KafkaMQSendTracer kafkaMQSendTracer;

    public SofaTracerKafkaProducer(Producer<K, V> producer, KafkaMQSendTracer kafkaMQSendTracer) {
        this.producer = producer;
        this.kafkaMQSendTracer = kafkaMQSendTracer;
    }

    public SofaTracerKafkaProducer(Producer<K, V> producer) {
        this.producer = producer;
        this.kafkaMQSendTracer = KafkaMQSendTracer.getKafkaMQSendTracerSingleton();
    }

    @Override
    public void initTransactions() {
        producer.initTransactions();
    }

    @Override
    public void beginTransaction() throws ProducerFencedException {
        producer.beginTransaction();
    }

    @Override
    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets,
                                         String consumerGroupId) throws ProducerFencedException {
        producer.sendOffsetsToTransaction(offsets, consumerGroupId);
    }

    @Override
    public void commitTransaction() throws ProducerFencedException {
        producer.commitTransaction();
    }

    @Override
    public void abortTransaction() throws ProducerFencedException {
        producer.abortTransaction();
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> producerRecord) {
        return send(producerRecord, null);
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> producerRecord, Callback callback) {
        // interceptor or producer send  mark cs.
        //
        if (null == this.kafkaMQSendTracer) {
            this.kafkaMQSendTracer = KafkaMQSendTracer.getKafkaMQSendTracerSingleton();
        }

        SofaTracerSpan clientSpan = kafkaMQSendTracer.clientSend("mq" + kafkaSendPostFix);
        appendSpanTagsAndInject(producerRecord, clientSpan);
        // header read only when record is sent second time.
        return producer.send(producerRecord, new SofaTracerCallback(callback, kafkaMQSendTracer));
    }

    @Override
    public void flush() {
        producer.flush();
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic) {
        return producer.partitionsFor(topic);
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return producer.metrics();
    }

    @Override
    public void close() {
        producer.close();
    }

    @Override
    public void close(long timeout, TimeUnit timeUnit) {
        producer.close(timeout, timeUnit);
    }

    private void appendSpanTagsAndInject(ProducerRecord<K, V> producerRecord,
                                         SofaTracerSpan clientSpan) {
        appendSpanTags(producerRecord, clientSpan);
        injectCarrier(clientSpan, producerRecord.headers());
    }

    private void appendSpanTags(ProducerRecord<K, V> producerRecord, SofaTracerSpan clientSpan) {
        //public tags.
        clientSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        clientSpan.setTag(CommonSpanTags.LOCAL_APP,
            SofaTracerConfiguration.getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY));
        // topic name, partition。 partition always is null。
        clientSpan.setTag(CommonSpanTags.KAFKA_TOPIC, producerRecord.topic());
        clientSpan.setTag(CommonSpanTags.KAFKA_PARTITION, producerRecord.partition() == null ? -1
            : producerRecord.partition());
    }

    private void injectCarrier(SofaTracerSpan tracerSpan, Headers properties) {
        SofaTracer sofaTracer = this.kafkaMQSendTracer.getSofaTracer();
        sofaTracer.inject(tracerSpan.getSofaTracerSpanContext(), ExtendFormat.Builtin.B3_TEXT_MAP,
            new KafkaMqInjectCarrier(properties));
    }

    /**
     *
     * call back for producer.
     */
    static final class SofaTracerCallback implements Callback {

        final Callback       callback;

        final AbstractTracer kafkaSendTracer;

        public SofaTracerCallback(Callback callback, AbstractTracer kafkaSendTracer) {
            this.callback = callback;
            this.kafkaSendTracer = kafkaSendTracer;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            boolean successFlag = true;
            if (Objects.nonNull(exception)) {
                successFlag = false;
            }
            if (Objects.nonNull(callback)) {
                callback.onCompletion(metadata, exception);
            }
            //cr.
            kafkaSendTracer.clientReceive(successFlag ? SofaTracerConstant.RESULT_CODE_SUCCESS
                : SofaTracerConstant.RESULT_CODE_ERROR);
        }
    }
}
