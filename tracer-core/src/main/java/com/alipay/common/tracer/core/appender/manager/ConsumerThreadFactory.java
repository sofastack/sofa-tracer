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
package com.alipay.common.tracer.core.appender.manager;

import java.util.concurrent.ThreadFactory;

/**
 *
 * @author liangen
 * @version $Id: ConsumerThreadFactory.java, v 0.1 October 23, 2017 10:09 AM liangen Exp $
 */
public class ConsumerThreadFactory implements ThreadFactory {
    private String workName;

    /**
     * Getter method for property <tt>workName</tt>.
     *
     * @return property value of workName
     */
    public String getWorkName() {
        return workName;
    }

    /**
     * Setter method for property <tt>workName</tt>.
     *
     * @param workName  value to be assigned to property workName
     */
    public void setWorkName(String workName) {
        this.workName = workName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread worker = new Thread(runnable, "Tracer-AsyncConsumer-Thread-" + workName);
        worker.setDaemon(true);
        return worker;
    }
}