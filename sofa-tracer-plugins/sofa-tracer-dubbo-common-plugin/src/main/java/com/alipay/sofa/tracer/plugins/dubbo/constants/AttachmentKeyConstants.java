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
package com.alipay.sofa.tracer.plugins.dubbo.constants;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/7/26 5:25 PM
 * @since:
 **/
public class AttachmentKeyConstants {

    public static final String SERVER_DESERIALIZE_SIZE = "server.deserialize.size";
    public static final String SERVER_SERIALIZE_SIZE   = "server.serialize.size";
    public static final String CLIENT_DESERIALIZE_SIZE = "client.deserialize.size";
    public static final String CLIENT_SERIALIZE_SIZE   = "client.serialize.size";

    public static final String SERVER_DESERIALIZE_TIME = "server.deserialize.time";
    public static final String SERVER_SERIALIZE_TIME   = "server.serialize.time";
    public static final String CLIENT_DESERIALIZE_TIME = "client.deserialize.time";
    public static final String CLIENT_SERIALIZE_TIME   = "client.serialize.time";
}
