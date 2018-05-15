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
package com.alipay.common.tracer.core.appender;

import java.io.IOException;

/**
 * @author yangyanzhao
 */
public interface TraceAppender {

    /***
     * 刷新数据
     *
     * @throws IOException 操作异常
     */
    void flush() throws IOException;

    /***
     * 添加要被输出的 log 文件
     * @param log 字符串
     * @throws IOException 操作异常
     */
    void append(String log) throws IOException;

    /**
     * 清理日志
     */
    void cleanup();
}
