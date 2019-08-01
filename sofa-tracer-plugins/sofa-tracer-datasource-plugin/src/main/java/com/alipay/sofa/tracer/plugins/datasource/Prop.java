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
package com.alipay.sofa.tracer.plugins.datasource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class Prop {

    private final List<Interceptor> interceptors   = new ArrayList<Interceptor>();

    private final MethodRegistry    methodRegistry = new MethodRegistry();

    private boolean                 fastDelegate   = true;

    public Method getTargetMethod(String methodName) {
        return methodRegistry.getMethod(methodName);
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public void addAll(Collection<Interceptor> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    public boolean isFastDelegate() {
        return fastDelegate;
    }

    public void setFastDelegate(boolean fastDelegate) {
        this.fastDelegate = fastDelegate;
    }
}