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
package com.alipay.common.tracer.core.utils;

/**
 * ByteArrayUtils
 *
 * @author yangguanchao
 * @since 2017/08/06
 */
public class ByteArrayUtils {

    /**
     * 查找指定数组的起始索引
     *
     * @param org    of type byte[] 原数组
     * @param search of type byte[] 要查找的数组
     * @return int 返回索引
     */

    public static int indexOf(byte[] org, byte[] search) {

        return indexOf(org, search, 0);

    }

    /**
     * 查找指定数组的起始索引
     *
     * @param org        of type byte[] 原数组
     * @param search     of type byte[] 要查找的数组
     * @param startIndex 起始索引
     * @return int 返回索引
     */

    public static int indexOf(byte[] org, byte[] search, int startIndex) {

        KMPMatcher kmpMatcher = new KMPMatcher();

        kmpMatcher.computeFailure4Byte(search);

        return kmpMatcher.indexOf(org, startIndex);
    }

    /**
     * KMP算法类
     * <p>
     * <p/>
     * <p>
     * Created on 2017-08-04
     */

    static class KMPMatcher {

        private int[]  failure;

        private int    matchPoint;

        private byte[] bytePattern;

        /**
         * Method indexOf …
         *
         * @param text       of type byte[]
         * @param startIndex of type int
         * @return int
         */

        public int indexOf(byte[] text, int startIndex) {

            int j = 0;

            if (text.length == 0 || startIndex > text.length) {
                return -1;
            }

            for (int i = startIndex; i < text.length; i++) {

                while (j > 0 && bytePattern[j] != text[i]) {

                    j = failure[j - 1];

                }

                if (bytePattern[j] == text[i]) {

                    j++;

                }

                if (j == bytePattern.length) {

                    matchPoint = i - bytePattern.length + 1;

                    return matchPoint;

                }

            }

            return -1;

        }

        /**
         * 找到末尾后重头开始找
         *
         * @param text       of type byte[]
         * @param startIndex of type int
         * @return int
         */

        public int lastIndexOf(byte[] text, int startIndex) {

            matchPoint = -1;

            int j = 0;

            if (text.length == 0 || startIndex > text.length) {
                return -1;
            }

            int end = text.length;

            for (int i = startIndex; i < end; i++) {

                while (j > 0 && bytePattern[j] != text[i]) {

                    j = failure[j - 1];

                }

                if (bytePattern[j] == text[i]) {

                    j++;

                }

                if (j == bytePattern.length) {

                    matchPoint = i - bytePattern.length + 1;

                    if ((text.length - i) > bytePattern.length) {

                        j = 0;

                        continue;

                    }

                    return matchPoint;

                }

                //如果从中间某个位置找，找到末尾没找到后，再重头开始找

                if (startIndex != 0 && i + 1 == end) {

                    end = startIndex;

                    i = -1;

                    startIndex = 0;

                }

            }

            return matchPoint;

        }

        /**
         * 找到末尾后不会重头开始找
         *
         * @param text       of type byte[]
         * @param startIndex of type int
         * @return int
         */

        public int lastIndexOfWithNoLoop(byte[] text, int startIndex) {

            matchPoint = -1;

            int j = 0;

            if (text.length == 0 || startIndex > text.length) {
                return -1;
            }

            for (int i = startIndex; i < text.length; i++) {

                while (j > 0 && bytePattern[j] != text[i]) {

                    j = failure[j - 1];

                }

                if (bytePattern[j] == text[i]) {

                    j++;

                }

                if (j == bytePattern.length) {

                    matchPoint = i - bytePattern.length + 1;

                    if ((text.length - i) > bytePattern.length) {

                        j = 0;

                        continue;

                    }

                    return matchPoint;

                }

            }

            return matchPoint;

        }

        /**
         * Method computeFailure4Byte …
         *
         * @param patternStr of type byte[]
         */

        public void computeFailure4Byte(byte[] patternStr) {

            bytePattern = patternStr;

            int j = 0;

            int len = bytePattern.length;

            failure = new int[len];

            for (int i = 1; i < len; i++) {

                while (j > 0 && bytePattern[j] != bytePattern[i]) {

                    j = failure[j - 1];

                }

                if (bytePattern[j] == bytePattern[i]) {

                    j++;

                }

                failure[i] = j;

            }

        }

    }
}
