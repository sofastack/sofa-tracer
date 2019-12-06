package com.alipay.sofa.tracer.boot.springcloud.configuration.stream.integration;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageChannelBinder;
import org.springframework.cloud.stream.binder.rocketmq.consuming.RocketMQListenerBindingContainer;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/29 5:56 PM
 * @since:
 **/
public class SofaTracerRocketMQListenerBindingContainer extends RocketMQListenerBindingContainer {

    RocketMQListenerBindingContainer delegate;

    public SofaTracerRocketMQListenerBindingContainer(ExtendedConsumerProperties<RocketMQConsumerProperties> rocketMQConsumerProperties, RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties, RocketMQMessageChannelBinder rocketMQMessageChannelBinder) {
        super(rocketMQConsumerProperties, rocketBinderConfigurationProperties, rocketMQMessageChannelBinder);
    }

    public SofaTracerRocketMQListenerBindingContainer(RocketMQListenerBindingContainer rocketMQListenerBindingContainer){
        this.delegate = rocketMQListenerBindingContainer;
    }
}
