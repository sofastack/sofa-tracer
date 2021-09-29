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
package com.alipay.sofa.tracer.plugins.skywalking.model;

import lombok.Getter;
import lombok.Setter;

/**
 * SegmentReference
 * @author zhaochen
 */
@Setter
@Getter
public class SegmentReference {
    private RefType refType;
    private String  traceId;
    private String  parentTraceSegmentId;
    private int     parentSpanId;
    private String  parentService;
    private String  parentServiceInstance;
    private String  parentEndpoint;
    // The network address, including ip/hostname and port, which is used in the client side.
    // Such as Client --> use 127.0.11.8:913 -> Server
    // then, in the reference of entry span reported by Server, the value of this field is 127.0.11.8:913.
    // This plays the important role in the SkyWalking STAM(Streaming Topology Analysis Method)
    // For more details, read https://wu-sheng.github.io/STAM/
    private String  networkAddressUsedAtPeer;

}
