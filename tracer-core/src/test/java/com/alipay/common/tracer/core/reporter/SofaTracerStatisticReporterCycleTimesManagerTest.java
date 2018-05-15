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
package com.alipay.common.tracer.core.reporter;

import com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterCycleTimesManager;
import com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterManager;
import org.junit.Assert;
import org.junit.Test;

/** 
* SofaTracerStatisticReporterCycleTimesManager Tester. 
* 
* @author <guanchao.ygc> 
* @since <pre>六月 22, 2017</pre> 
* @version 1.0 
*/
public class SofaTracerStatisticReporterCycleTimesManagerTest {

    /** 
    * 
    * Method: getSofaTracerStatisticReporterManager(Long cycleTime) 
    * 
    */
    @Test
    public void testGetSofaTracerStatisticReporterManager() throws Exception {
        //单位 秒
        Long cyc1 = 600L;
        SofaTracerStatisticReporterManager manager1 = SofaTracerStatisticReporterCycleTimesManager
            .getSofaTracerStatisticReporterManager(cyc1);

        SofaTracerStatisticReporterManager manager2 = SofaTracerStatisticReporterCycleTimesManager
            .getSofaTracerStatisticReporterManager(600L);

        SofaTracerStatisticReporterManager manager3 = SofaTracerStatisticReporterCycleTimesManager
            .getSofaTracerStatisticReporterManager(new Long(600L));

        Assert.assertTrue((manager1 == manager2) && (manager2 == manager3));

    }

}
