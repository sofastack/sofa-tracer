package com.alipay.sofa.tracer.boot.springcloud.configuration.stream.integration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.stream.binder.rocketmq.consuming.RocketMQListenerBindingContainer;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/29 5:55 PM
 * @since:
 **/
public class RocketMQListenerBindingContainerPostProcessor implements BeanPostProcessor, EnvironmentAware,
        PriorityOrdered {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RocketMQListenerBindingContainer){
            RocketMQListenerBindingContainer container = (RocketMQListenerBindingContainer) bean;
            return new SofaTracerRocketMQListenerBindingContainer(container.getRocketMQConsumerProperties(),container.);
        }
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {

    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
