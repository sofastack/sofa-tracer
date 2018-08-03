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
package com.alipay.common.tracer.core.appender.file;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.appender.self.SelfLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yangyanzhao
 */
public abstract class AbstractRollingFileAppender implements TraceAppender {

    /**
     * 日志刷新间隔，buffer 时间超过间隔，则把缓存的日志数据刷新出去
     */
    private static final long      LOG_FLUSH_INTERVAL         = TimeUnit.SECONDS.toMillis(1);
    /**
     * 默认输出缓冲大小
     */
    public static final int        DEFAULT_BUFFER_SIZE        = 8 * 1024;                    // 8KB
    private static final long      IOEXCEPTION_PRINT_INTERVAL = 60 * 1000;

    /**
     * 日志缓存大小
     */
    private final int              bufferSize;

    protected final String         fileName;

    private final AtomicBoolean    isRolling                  = new AtomicBoolean(false);

    protected File                 logFile                    = null;

    protected BufferedOutputStream bos                        = null;

    private long                   nextFlushTime              = 0L;
    private long                   nextIOExceptionPrintTime   = 0L;

    public AbstractRollingFileAppender(String file, boolean append) {
        this(file, DEFAULT_BUFFER_SIZE, append);
    }

    public AbstractRollingFileAppender(String file, int bufferSize, boolean append) {
        this.fileName = TracerLogRootDaemon.LOG_FILE_DIR + File.separator + file;
        this.bufferSize = bufferSize;
        setFile(append);
    }

    protected void setFile(boolean append) {
        try {
            logFile = new File(fileName);
            if (!logFile.exists()) {
                File parentFile = logFile.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    doSelfLog("[ERROR] Fail to mkdirs: " + parentFile.getAbsolutePath());
                    return;
                }
                if (!logFile.createNewFile()) {
                    doSelfLog("[ERROR] Fail to create file to write: " + logFile.getAbsolutePath());
                    return;
                }
            }
            if (!logFile.isFile() || !logFile.canWrite()) {
                doSelfLog("[ERROR] Invalid file, exists=" + logFile.exists() + ", isFile="
                          + logFile.isFile() + ", canWrite=" + logFile.canWrite() + ", path="
                          + logFile.getAbsolutePath());
                return;
            }
            //append == true
            FileOutputStream ostream = new FileOutputStream(logFile, append);
            bos = new BufferedOutputStream(ostream, bufferSize);
        } catch (Throwable e) {
            SelfLog.error("setFile error", e);
        }
    }

    public void append(String log) throws IOException {
        if (bos != null) {
            waitUntilRollFinish();
            if (shouldRollOverNow() && isRolling.compareAndSet(false, true)) {
                try {
                    rollOver();
                    nextFlushTime = System.currentTimeMillis() + LOG_FLUSH_INTERVAL;
                } finally {
                    isRolling.set(false);
                }
            } else {
                // 超过指定刷新时间没刷新，就刷新一次
                long now;
                if ((now = System.currentTimeMillis()) >= nextFlushTime) {
                    flush();
                    nextFlushTime = now + LOG_FLUSH_INTERVAL;
                }
            }
            // 无论有没有做 RollOver，都需要将输入往 bos 中写入
            byte[] bytes = log.getBytes(TracerLogRootDaemon.DEFAULT_CHARSET);
            write(bytes);
        }
    }

    /**
     * 是否现在马上进行滚动
     *
     * @return true: 是
     */
    protected abstract boolean shouldRollOverNow();

    /**
     * 进行 RollOver
     * WARNING：RollOver 的时候日志不要用 SelfLog 打印，因为这个时候可能 SelfLog 自己在 RollOver。
     */
    protected abstract void rollOver();

    private void write(byte[] bytes) {
        try {
            bos.write(bytes);
        } catch (IOException e) {
            long now = System.currentTimeMillis();
            if (now > nextIOExceptionPrintTime) {
                nextIOExceptionPrintTime = now + IOEXCEPTION_PRINT_INTERVAL;
                SelfLog.error("Failed to write file " + fileName, e);
            }
        }
    }

    /**
     * 超过指定刷新时间没刷新，就刷新一次
     */
    public void flush() {
        if (bos != null) {
            try {
                bos.flush();
            } catch (IOException e) {
                long now = System.currentTimeMillis();
                if (now > nextIOExceptionPrintTime) {
                    nextIOExceptionPrintTime = now + IOEXCEPTION_PRINT_INTERVAL;
                    SelfLog.error("Failed to flush file " + fileName, e);
                }
            }
        }
    }

    void waitUntilRollFinish() {
        while (isRolling.get()) {
            try {
                Thread.sleep(1L);
            } catch (Exception e) {
                SelfLog.error("WaitUntilRollFinish error!", e);
            }
        }
    }

    private void doSelfLog(String log) {
        System.out.println("[TraceSelfLog]" + log);
    }
}
