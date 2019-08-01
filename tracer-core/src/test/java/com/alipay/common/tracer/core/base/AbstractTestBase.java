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
package com.alipay.common.tracer.core.base;

import com.alipay.common.tracer.core.TestUtil;
import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.reporter.type.TracerSystemLogEnum;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * AbstractTestBase
 *
 * @author yangguanchao
 * @since 2017/07/01
 */
public abstract class AbstractTestBase {

    public static final String MAP_PREFIX       = "M|";

    /**
     * Tracer Log Root Director Path
     */
    public static String       logDirectoryPath = TracerLogRootDaemon.LOG_FILE_DIR;

    /**
     * Clean Tracer Log Root Directory
     */
    @BeforeClass
    public static void beforeClass() {
        File[] traceFiles = customFileLog("").listFiles();
        if (traceFiles == null) {
            return;
        }
        for (File file : traceFiles) {
            if (file.getPath().contains("tracer-self.log") || file.getPath().contains("sync.log")
                || file.getPath().contains("rpc-profile.log")
                || file.getPath().contains("middleware_error.log")) {
                continue;
            }
            FileUtils.deleteQuietly(file);
        }
    }

    @Before
    public void commonBeforeMethod() throws IOException {
        File file = customFileLog(TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName());
        if (file.exists()) {
            FileUtils.writeStringToFile(file, "");
        }
        file = customFileLog(TracerSystemLogEnum.RPC_PROFILE.getDefaultLogName());
        if (file.exists()) {
            FileUtils.writeStringToFile(file, "");
        }
        file = tracerSelfLog();
        if (file.exists()) {
            FileUtils.writeStringToFile(file, "");
        }
    }

    /**
     * Check whether is error message printed.
     *
     * @return
     * @throws IOException
     */
    protected boolean checkSelfLogContainsError() throws IOException {
        File tracerSelfLog = tracerSelfLog();
        if (!tracerSelfLog.exists()) {
            return false;
        }
        String selfLogContent = FileUtils.readFileToString(tracerSelfLog);
        return selfLogContent.contains("ERROR");
    }

    protected static File customFileLog(String fileName) {
        return new File(logDirectoryPath + File.separator + fileName);
    }

    protected static File tracerSelfLog() {
        return new File(logDirectoryPath + File.separator + "tracer-self.log");
    }

    /**
     * Whether the parameters and the contents of the log match
     *
     * @param params String Array which is compared with logContent.
     * @param logContent String Content which is split up with comma
     * @return
     */
    public static boolean checkResult(List<String> params, String logContent) {
        Assert.assertNotNull(params);
        Assert.assertNotNull(logContent);

        List<String> slots = Arrays.asList(logContent.split(String
            .valueOf(XStringBuilder.DEFAULT_SEPARATOR)));

        assertEquals("The number of fields in the log content is " + slots.size()
                     + "; the number of fields in the parameter is " + params.size()
                     + "; the two are inconsistent", params.size(), slots.size());

        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            String slot = slots.get(i);
            if (param == null) {
                if (slot == null || "null".equals(slot)) {
                    continue;
                }
            }
            if (param.length() > 2 && param.startsWith("M|")) {
                Assert.assertTrue(
                    "The contents of the " + i
                            + " column in the log and parameters are inconsistent. The log is "
                            + slot + ";The parameter is " + param,
                    TestUtil.compareSlotMap(param.substring(2), slot));
            } else {
                Assert.assertTrue(
                    "The contents of the " + i
                            + " column in the log and parameters are inconsistent. The log is "
                            + slot + ";The parameter is " + param, param.equals(slot));
            }
        }
        return true;
    }

}
