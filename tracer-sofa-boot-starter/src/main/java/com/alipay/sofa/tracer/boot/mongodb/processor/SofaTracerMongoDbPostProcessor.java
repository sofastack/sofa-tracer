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
package com.alipay.sofa.tracer.boot.mongodb.processor;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.sofa.tracer.plugins.mongodb.SofaTracerCommandListener;
import com.alipay.sofa.tracer.plugins.mongodb.SofaTracerMongoClient;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/6 11:27 AM
 * @since:
 **/
public class SofaTracerMongoDbPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
                                                                              throws BeansException {
        if (bean instanceof MongoClient && !(bean instanceof SofaTracerMongoClient)) {
            MongoClient client = (MongoClient) bean;
            List<ServerAddress> serverAddressList = client.getServerAddressList();
            List<MongoCredential> credentialsList = client.getCredentialsList();
            MongoClientOptions mongoClientOptions = client.getMongoClientOptions();
            String appName = environment.getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY);
            SofaTracerCommandListener commandListener = new SofaTracerCommandListener(appName);
            return new SofaTracerMongoClient(commandListener, serverAddressList, credentialsList,
                mongoClientOptions);
        }
        return bean;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
