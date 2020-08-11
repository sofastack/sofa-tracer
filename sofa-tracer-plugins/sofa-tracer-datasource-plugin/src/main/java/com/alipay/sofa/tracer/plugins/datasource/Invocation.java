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

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class Invocation {

    private final Method   method;

    private final Object   target;

    private final Object[] args;

    public Invocation(Method method, Object target, Object... args) {
        this.method = method;
        this.target = target;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public Object getTarget() {
        return target;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object invoke() throws Exception {
        return method.invoke(target, args);
    }
}
