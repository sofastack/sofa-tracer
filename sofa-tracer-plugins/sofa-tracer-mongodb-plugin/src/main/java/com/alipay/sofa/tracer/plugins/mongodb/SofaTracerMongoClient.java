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
package com.alipay.sofa.tracer.plugins.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/6 11:16 AM
 * @since:
 **/
public class SofaTracerMongoClient extends MongoClient {

    public SofaTracerMongoClient(SofaTracerCommandListener listener) {
        this(listener, new ServerAddress());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final String host) {
        this(listener, new ServerAddress(host));
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final String host,
                                 final MongoClientOptions options) {
        this(listener, new ServerAddress(host), options);
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final String host,
                                 final int port) {
        this(listener, new ServerAddress(host, port));
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final ServerAddress addr) {
        this(listener, addr, new MongoClientOptions.Builder().build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final ServerAddress addr,
                                 final List<MongoCredential> credentialsList) {
        this(listener, addr, credentialsList, new MongoClientOptions.Builder().build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final ServerAddress addr,
                                 final MongoClientOptions options) {
        super(addr, MongoClientOptions.builder(options).addCommandListener(listener).build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final ServerAddress addr,
                                 final List<MongoCredential> credentialsList,
                                 final MongoClientOptions options) {
        super(addr, credentialsList, MongoClientOptions.builder(options)
            .addCommandListener(listener).build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final ServerAddress addr,
                                 final MongoCredential credential, final MongoClientOptions options) {
        super(addr, credential, MongoClientOptions.builder(options).addCommandListener(listener)
            .build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final List<ServerAddress> seeds) {
        this(listener, seeds, new MongoClientOptions.Builder().build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener,
                                 final List<ServerAddress> seeds,
                                 final List<MongoCredential> credentialsList) {
        this(listener, seeds, credentialsList, new MongoClientOptions.Builder().build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener,
                                 final List<ServerAddress> seeds, final MongoClientOptions options) {
        super(seeds, MongoClientOptions.builder(options).addCommandListener(listener).build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener,
                                 final List<ServerAddress> seeds,
                                 final List<MongoCredential> credentialsList,
                                 final MongoClientOptions options) {
        super(seeds, credentialsList, MongoClientOptions.builder(options)
            .addCommandListener(listener).build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener,
                                 final List<ServerAddress> seeds, final MongoCredential credential,
                                 final MongoClientOptions options) {
        super(seeds, credential, MongoClientOptions.builder(options).addCommandListener(listener)
            .build());
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final MongoClientURI uri) {
        this(listener, uri, null);
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener,
                                 final List<ServerAddress> seeds, final MongoCredential credential,
                                 final MongoClientOptions options,
                                 final MongoDriverInformation mongoDriverInformation) {
        super(seeds, credential, MongoClientOptions.builder(options).addCommandListener(listener)
            .build(), mongoDriverInformation);
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final MongoClientURI uri,
                                 final MongoDriverInformation mongoDriverInformation) {
        this(listener, toServerAddressList(uri.getHosts()),
            uri.getCredentials() != null ? Collections.singletonList(uri.getCredentials())
                : Collections.<MongoCredential> emptyList(), uri.getOptions(),
            mongoDriverInformation);
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener, final ServerAddress addr,
                                 final List<MongoCredential> credentialsList,
                                 final MongoClientOptions options,
                                 final MongoDriverInformation mongoDriverInformation) {
        super(addr, credentialsList, MongoClientOptions.builder(options)
            .addCommandListener(listener).build(), mongoDriverInformation);
    }

    public SofaTracerMongoClient(SofaTracerCommandListener listener,
                                 final List<ServerAddress> seeds,
                                 final List<MongoCredential> credentialsList,
                                 final MongoClientOptions options,
                                 final MongoDriverInformation mongoDriverInformation) {
        super(seeds, credentialsList, MongoClientOptions.builder(options)
            .addCommandListener(listener).build(), mongoDriverInformation);
    }

    private static List<ServerAddress> toServerAddressList(List<String> hosts) {
        List<ServerAddress> list = new ArrayList<>();
        for (String host : hosts) {
            list.add(new ServerAddress(host));
        }
        return list;
    }
}
