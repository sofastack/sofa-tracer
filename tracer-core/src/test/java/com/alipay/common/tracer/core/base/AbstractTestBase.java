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

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * AbstractTestBase
 *
 * @author yangguanchao
 * @since 2017/07/01
 */
public abstract class AbstractTestBase {

    public static final String MAP_PREFIX       = "M|";

    public static String       logDirectoryPath = System.getProperty("user.home") + File.separator
                                                  + "logs" + File.separator + "tracelog";

    //    public static String logDirectoryPath = "." + File.separator            + "logs" + File.separator + "tracelog";

    public static File         logDirectory     = new File(logDirectoryPath);

    public static String       FUZZY_STR        = "fuzzy";

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty("com.alipay.ldc.zone", "GZ00A");
        String directoryCleaned = System.getProperty("DIRECTORY_CLEANED");
        if (directoryCleaned == null) {
            cleanLogDirectory();
            System.setProperty("DIRECTORY_CLEANED", "true");
        }
    }

    @After
    public void after() throws Exception {
        checkSelfLogContainsError();
        clearConfig();
    }

    public void clearConfig() throws Exception {
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY, "false");
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.DISABLE_DIGEST_LOG_KEY,
            new HashMap<String, String>());
    }

    /**
     * 清理日志文件夹
     *
     * @throws java.io.IOException
     */
    public static void cleanLogDirectory() throws IOException {
        if (!logDirectory.exists()) {
            return;
        }

        FileUtils.cleanDirectory(logDirectory);
    }

    /**
     * 检查 Tracer 本身是否含有错误
     */
    protected void checkSelfLogContainsError() throws IOException {
        File tracerSelfLog = new File(logDirectory + File.separator + "tracer-self.log");

        if (!tracerSelfLog.exists()) {
            return;
        }
        String selfLogContent = FileUtils.readFileToString(tracerSelfLog);
        boolean result = (selfLogContent == null || !selfLogContent.contains("ERROR"));
        Assert.assertTrue("Tracer 中包含错误" + selfLogContent, result);
    }

    /**
     * 检查传入的参数和日志中的内容是否匹配
     *
     * @param params
     * @param logContent
     * @return
     */
    public static boolean checkResult(List<String> params, String logContent) {
        if (logContent == null || logContent.length() == 0) {
            return params.isEmpty();
        }

        List<String> slots = Arrays.asList(logContent.split(","));

        assertEquals("日志内容中的栏位数量为 " + slots.size() + ";参数的栏位数量为" + params.size() + ";两者不一致",
            params.size(), slots.size());

        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            String slot = slots.get(i);
            if (param == null) {
                if (slot == null || "null".equals(slot)) {
                    continue;
                }
            }
            if (param.length() > 2 && param.startsWith("M|")) {
                Map<String, String> paramMap = new HashMap<String, String>();
                StringUtils.stringToMap(param.substring(2), paramMap);
                Map<String, String> slotMap = new HashMap<String, String>();
                StringUtils.stringToMap(slot, slotMap);

                assertEquals("日志和参数中的第 " + i + " 栏内容不一致，日志中为 " + slot + ";参数中为 " + param, paramMap,
                    slotMap);
            } else {
                assertEquals("日志和参数中的第 " + i + " 栏内容不一致，日志中为 " + slot + ";参数中为 " + param, param,
                    slot);
            }
        }

        return true;
    }

}
