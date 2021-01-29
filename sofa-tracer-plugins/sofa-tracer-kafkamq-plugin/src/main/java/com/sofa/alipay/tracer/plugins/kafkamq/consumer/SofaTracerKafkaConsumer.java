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
package com.sofa.alipay.tracer.plugins.kafkamq.consumer;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.sofa.alipay.tracer.plugins.kafkamq.carrier.KafkaMqExtractCarrier;
import com.sofa.alipay.tracer.plugins.kafkamq.tracers.KafkaMQConsumeTracer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 *  SofaTracerKafkaConsumer。
 *
 * @author chenchen6   2020/8/25 19:06
 * @since 3.1.0
 */
public class SofaTracerKafkaConsumer<K, V> implements Consumer<K, V> {

    private KafkaMQConsumeTracer kafkaMQConsumeTracer;

    private final Consumer<K, V> consumer;

    /**
     *  constructor.
     * @param kafkaMQConsumeTracer
     */
    public SofaTracerKafkaConsumer(Consumer<K, V> consumer,
                                   KafkaMQConsumeTracer kafkaMQConsumeTracer) {
        this.consumer = consumer;
        this.kafkaMQConsumeTracer = kafkaMQConsumeTracer;
    }

    public SofaTracerKafkaConsumer(Consumer<K, V> consumer) {
        this.consumer = consumer;
        this.kafkaMQConsumeTracer = KafkaMQConsumeTracer.getKafkaMQConsumeTracerSingleton();
    }

    @Override
    public Set<TopicPartition> assignment() {
        return consumer.assignment();
    }

    @Override
    public Set<String> subscription() {
        return consumer.subscription();
    }

    @Override
    public void subscribe(Collection<String> topics) {
        consumer.subscribe(topics);
    }

    @Override
    public void subscribe(Collection<String> topics, ConsumerRebalanceListener callback) {
        consumer.subscribe(topics, callback);
    }

    @Override
    public void assign(Collection<TopicPartition> partitions) {
        consumer.assign(partitions);
    }

    @Override
    public void subscribe(Pattern pattern, ConsumerRebalanceListener callback) {
        consumer.subscribe(pattern, callback);
    }

    @Override
    public void subscribe(Pattern pattern) {
        consumer.subscribe(pattern);
    }

    @Override
    public void unsubscribe() {
        consumer.unsubscribe();
    }

    @Override
    @Deprecated
    public ConsumerRecords<K, V> poll(long timeout) {

        ConsumerRecords<K, V> records = consumer.poll(timeout);
        for (ConsumerRecord<K, V> record : records) {
            //no ss, depends on kafka consumption model.
            //we can only do it in the aspect.
            appendSpanAndServerReceive(record);
        }
        return records;
    }

    @Override
    public ConsumerRecords<K, V> poll(Duration timeout) {
        ConsumerRecords<K, V> records = consumer.poll(timeout);
        for (ConsumerRecord<K, V> record : records) {
            //sr, no ss.
            appendSpanAndServerReceive(record);
        }
        return records;
    }

    @Override
    public void commitSync() {
        consumer.commitSync();
    }

    @Override
    public void commitSync(Duration timeout) {
        consumer.commitSync(timeout);
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets) {
        consumer.commitSync(offsets);
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets, Duration timeout) {
        consumer.commitSync(offsets, timeout);
    }

    @Override
    public void commitAsync() {
        consumer.commitSync();
    }

    @Override
    public void commitAsync(OffsetCommitCallback callback) {
        consumer.commitAsync(callback);
    }

    @Override
    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets,
                            OffsetCommitCallback callback) {
        consumer.commitAsync(offsets, callback);
    }

    @Override
    public void seek(TopicPartition partition, long offset) {
        consumer.seek(partition, offset);
    }

    @Override
    public void seekToBeginning(Collection<TopicPartition> partitions) {
        consumer.seekToBeginning(partitions);
    }

    @Override
    public void seekToEnd(Collection<TopicPartition> partitions) {
        consumer.seekToEnd(partitions);
    }

    @Override
    public long position(TopicPartition partition) {
        return consumer.position(partition);
    }

    @Override
    public long position(TopicPartition partition, Duration timeout) {
        return consumer.position(partition, timeout);
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition) {
        return consumer.committed(partition);
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition, Duration timeout) {
        return consumer.committed(partition, timeout);
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return consumer.metrics();
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic) {
        return consumer.partitionsFor(topic);
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic, Duration timeout) {
        return consumer.partitionsFor(topic, timeout);
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics() {
        return consumer.listTopics();
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics(Duration timeout) {
        return consumer.listTopics(timeout);
    }

    @Override
    public Set<TopicPartition> paused() {
        return consumer.paused();
    }

    @Override
    public void pause(Collection<TopicPartition> partitions) {
        consumer.pause(partitions);
    }

    @Override
    public void resume(Collection<TopicPartition> partitions) {
        consumer.resume(partitions);
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch) {
        return consumer.offsetsForTimes(timestampsToSearch);
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch,
                                                                   Duration timeout) {
        return consumer.offsetsForTimes(timestampsToSearch, timeout);
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions) {
        return consumer.beginningOffsets(partitions);
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions,
                                                      Duration timeout) {
        return consumer.beginningOffsets(partitions, timeout);
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions) {
        return consumer.endOffsets(partitions);
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions,
                                                Duration timeout) {
        return consumer.endOffsets(partitions, timeout);
    }

    @Override
    public void close() {
        consumer.close();
    }

    @Override
    public void close(long timeout, TimeUnit unit) {
        consumer.close(timeout, unit);
    }

    @Override
    public void close(Duration timeout) {
        consumer.close(timeout);
    }

    @Override
    public void wakeup() {
        consumer.wakeup();
    }

    private void appendSpanAndServerReceive(ConsumerRecord<K, V> record) {
        if (null == kafkaMQConsumeTracer) {
            kafkaMQConsumeTracer = KafkaMQConsumeTracer.getKafkaMQConsumeTracerSingleton();
        }
        SofaTracerSpanContext spanContext = getSpanContextFromHeaders(record.headers());
        SofaTracerSpan sofaTracerSpan = kafkaMQConsumeTracer.serverReceive(spanContext);
        appendSpanTags(sofaTracerSpan, record);
    }

    private void appendSpanTags(SofaTracerSpan tracerSpan, ConsumerRecord<K, V> record) {
        tracerSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        tracerSpan.setTag(CommonSpanTags.LOCAL_APP,
            SofaTracerConfiguration.getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY));
        tracerSpan.setTag(CommonSpanTags.KAFKA_TOPIC, record.topic());
        tracerSpan.setTag(CommonSpanTags.KAFKA_PARTITION, record.partition());
        tracerSpan.setTag(CommonSpanTags.KAFKA_OFFSET, record.offset());
    }

    private SofaTracerSpanContext getSpanContextFromHeaders(Headers headers) {

        SofaTracer sofaTracer = this.kafkaMQConsumeTracer.getSofaTracer();
        SofaTracerSpanContext spanContext = (SofaTracerSpanContext) sofaTracer.extract(
            ExtendFormat.Builtin.B3_TEXT_MAP, new KafkaMqExtractCarrier(headers));
        return spanContext;
    }
}
