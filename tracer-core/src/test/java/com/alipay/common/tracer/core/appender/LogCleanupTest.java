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

import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Test log cleanup function
 *
 * @author khotyn 15/2/16 PM 3:50
 */
public class LogCleanupTest extends AbstractTestBase {

    private static final String CLEAN_UP_TEST_LOG = "cleanup.test.log";
    private File                todayFile         = null;
    private File                yesterdayFile     = null;
    private File                twoDayAgoFile     = null;
    private File                threeDayAgoFile   = null;
    private File                fourDayAgoFile    = null;
    private File                fiveDayAgoFile    = null;

    @Test
    public void test_cleanup_log_scroll_by_day() throws IOException {
        generateLogFilesToBeCleanup(new SimpleDateFormat(
            TimedRollingFileAppender.DAILY_ROLLING_PATTERN));
        TraceAppender appender = new TimedRollingFileAppender(CLEAN_UP_TEST_LOG,
            TimedRollingFileAppender.DAILY_ROLLING_PATTERN, "3");
        appender.cleanup();

        Assert.assertTrue("Today's log should still exist", todayFile.exists());
        Assert.assertTrue("Yesterday's log should still exist", yesterdayFile.exists());
        Assert.assertTrue("The log two days ago should still exist", twoDayAgoFile.exists());
        Assert.assertTrue("The log three days ago should still exist", threeDayAgoFile.exists());
        Assert.assertTrue("The log four days ago should not exist", !fourDayAgoFile.exists());
        Assert.assertTrue("The log five days ago should not exist", !fiveDayAgoFile.exists());
    }

    @Test
    public void test_cleanup_log_scroll_by_hour() throws IOException {
        generateLogFilesToBeCleanup(new SimpleDateFormat(
            TimedRollingFileAppender.HOURLY_ROLLING_PATTERN));
        TraceAppender appender = new TimedRollingFileAppender(CLEAN_UP_TEST_LOG,
            TimedRollingFileAppender.HOURLY_ROLLING_PATTERN, "2");
        appender.cleanup();

        Assert.assertTrue("Today's log should still exist", todayFile.exists());
        Assert.assertTrue("Yesterday's log should still exist", yesterdayFile.exists());
        Assert.assertTrue("The log two days ago should still exist", twoDayAgoFile.exists());
        Assert.assertTrue("The log three days ago should not exist", !threeDayAgoFile.exists());
        Assert.assertTrue("The log four days ago should not exist", !fourDayAgoFile.exists());
        Assert.assertTrue("The log five days ago should not exist", !fiveDayAgoFile.exists());
    }

    @Test
    public void rolling_by_two_hour() throws IOException {
        generateLogFilesToBeCleanupHourly(new SimpleDateFormat(
            TimedRollingFileAppender.HOURLY_ROLLING_PATTERN));
        TraceAppender appender = new TimedRollingFileAppender(CLEAN_UP_TEST_LOG,
            TimedRollingFileAppender.HOURLY_ROLLING_PATTERN, "0D2H");
        appender.cleanup();

        Assert.assertTrue("The log before this hour should still exist", todayFile.exists());
        Assert.assertTrue("The log An hour ago should still exist", yesterdayFile.exists());
        Assert.assertTrue("The log two hours ago should still exist", twoDayAgoFile.exists());
        Assert.assertTrue("The log three hours ago should not exist", !threeDayAgoFile.exists());
        Assert.assertTrue("The log four hours ago should not exist", !fourDayAgoFile.exists());
        Assert.assertTrue("The log five hours ago should not exist", !fiveDayAgoFile.exists());
    }

    @Test
    public void rolling_by_three_hour() throws IOException {
        generateLogFilesToBeCleanupHourly(new SimpleDateFormat(
            TimedRollingFileAppender.HOURLY_ROLLING_PATTERN));
        TraceAppender appender = new TimedRollingFileAppender(CLEAN_UP_TEST_LOG,
            TimedRollingFileAppender.HOURLY_ROLLING_PATTERN, "0D3H");
        appender.cleanup();

        Assert.assertTrue("The log before this hour should still exist", todayFile.exists());
        Assert.assertTrue("The log An hour ago should still exist", yesterdayFile.exists());
        Assert.assertTrue("The log two hours ago should still exist", twoDayAgoFile.exists());
        Assert.assertTrue("The log three hours ago should still exist", threeDayAgoFile.exists());
        Assert.assertTrue("The log four hours ago should not exist", !fourDayAgoFile.exists());
        Assert.assertTrue("The log five hours ago should not exist", !fiveDayAgoFile.exists());
    }

    private void generateLogFilesToBeCleanupHourly(SimpleDateFormat sdf) throws IOException {
        todayFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG);

        Calendar oneHourAgo = Calendar.getInstance();
        oneHourAgo.add(Calendar.HOUR, -1);
        yesterdayFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                + sdf.format(oneHourAgo.getTime()));

        Calendar twoHourAgo = Calendar.getInstance();
        twoHourAgo.add(Calendar.HOUR, -2);
        twoDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                + sdf.format(twoHourAgo.getTime()));

        Calendar threeHourAgo = Calendar.getInstance();
        threeHourAgo.add(Calendar.HOUR, -3);
        threeDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                  + sdf.format(threeHourAgo.getTime()));

        Calendar fourHourAgo = Calendar.getInstance();
        fourHourAgo.add(Calendar.HOUR, -4);
        fourDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                 + sdf.format(fourHourAgo.getTime()));

        Calendar fiveHourAgo = Calendar.getInstance();
        fiveHourAgo.add(Calendar.HOUR, -5);
        fiveDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                 + sdf.format(fiveHourAgo.getTime()));
    }

    private void generateLogFilesToBeCleanup(SimpleDateFormat sdf) throws IOException {
        todayFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG);

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        yesterdayFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                + sdf.format(yesterday.getTime()));

        Calendar twoDayAgo = Calendar.getInstance();
        twoDayAgo.add(Calendar.DATE, -2);
        twoDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                + sdf.format(twoDayAgo.getTime()));

        Calendar threeDayAgo = Calendar.getInstance();
        threeDayAgo.add(Calendar.DATE, -3);
        threeDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                  + sdf.format(threeDayAgo.getTime()));

        Calendar fourDayAgo = Calendar.getInstance();
        fourDayAgo.add(Calendar.DATE, -4);
        fourDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                 + sdf.format(fourDayAgo.getTime()));

        Calendar fiveDayAgo = Calendar.getInstance();
        fiveDayAgo.add(Calendar.DATE, -5);
        fiveDayAgoFile = newFile(TracerLogRootDaemon.LOG_FILE_DIR + "/" + CLEAN_UP_TEST_LOG
                                 + sdf.format(fiveDayAgo.getTime()));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File newFile(String filePath) throws IOException {
        File file = new File(filePath);

        File parentDirectory = file.getParentFile();

        if (parentDirectory != null && !parentDirectory.exists()) {
            parentDirectory.mkdir();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }
}
