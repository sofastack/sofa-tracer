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
package com.alipay.sofa.tracer.boot;

import com.alipay.sofa.tracer.boot.config.ConfigurationTest;
import com.alipay.sofa.tracer.boot.datasource.DataSourceTracerDisableTest;
import com.alipay.sofa.tracer.boot.opentracing.profiles.init.InitProfileTracerTest;
import com.alipay.sofa.tracer.boot.springmvc.SpringMvcFilterJsonOutputTest;
import com.alipay.sofa.tracer.boot.springmvc.SpringMvcFilterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/26 8:13 PM
 * @since:
 **/
@Suite.SuiteClasses({ InitProfileTracerTest.class, DataSourceTracerDisableTest.class,
                     ConfigurationTest.class, SpringMvcFilterTest.class,
                     SpringMvcFilterJsonOutputTest.class })
@RunWith(Suite.class)
public class TestSuite {

}
