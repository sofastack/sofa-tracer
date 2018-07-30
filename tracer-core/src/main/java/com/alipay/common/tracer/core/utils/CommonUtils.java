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
 * CommonUtils
 *
 * @author yangguanchao
 * @since 2017/07/28
 */
public class CommonUtils {

    private static int LONG_BYTES            = Long.SIZE / 8;
    private static int LONG_HEX_STRING_BYTES = LONG_BYTES * 2;

    /***
     * 取数值
     * @param num  数字
     * @param defaultInt 默认值
     * @param <T> 类型 Number
     * @return Number
     */
    public static <T extends Number> T parseNum(T num, T defaultInt) {
        return num == null ? defaultInt : num;
    }

    /***
     * Convert a hex string to a array containing two unsigned long elements
     * @param hexString hex string
     * @return long array: [0] -- High 64 bit, [1] -- low 64 bit
     */
    public static long[] hexToDualLong(String hexString) {
        //Assert.hasText(hexString, "Can't convert empty hex string to long");
        int length = hexString.length();
        if (length < 1) {
            throw new IllegalArgumentException("Malformed id(length must be more than zero): "
                                               + hexString);
        }
        if (length > LONG_HEX_STRING_BYTES * 2) {
            throw new IllegalArgumentException(
                "Malformed id(length must be less than 2 times lengh of type long): " + hexString);
        }

        int charLast = length - 1;
        long[] result = new long[2];
        result[0] = result[1] = 0;
        //Convert the 16 chars at the end, to 2nd elem of array, low 64 bit
        int i = 0;
        while (charLast >= 0 && i < LONG_BYTES * 2) {
            result[1] += ((long) Character.digit(hexString.charAt(charLast), 16) & 0xffL) << (4 * i);
            charLast--;
            i++;
        }
        //Convert other than the 16 chars at the end, to 1st elem of array, high 64 bit
        i = 0;
        while (charLast >= 0 && i < LONG_BYTES * 2) {
            result[0] += ((long) Character.digit(hexString.charAt(charLast), 16) & 0xffL) << (4 * i);
            charLast--;
            i++;
        }

        return result;
    }

    /***
     * Convert a hex string to a unsigned long
     * @param hexString hex string
     * @return long
     */
    public static long hexToLong(String hexString) {
        //Assert.hasText(hexString, "Can't convert empty hex string to long");
        int length = hexString.length();
        if (length < 1) {
            throw new IllegalArgumentException("Malformed id(length must be more than zero): "
                                               + hexString);
        }
        if (length > LONG_HEX_STRING_BYTES) {
            throw new IllegalArgumentException(
                "Malformed id(length must be less than 2 times lengh of type long): " + hexString);
        }

        int charLast = length - 1;
        long result = 0;
        //Convert the 16 chars to 64 bit long type
        int i = 0;
        while (charLast >= 0 && i < LONG_BYTES * 2) {
            result += ((long) Character.digit(hexString.charAt(charLast), 16) & 0xffL) << (4 * i);
            charLast--;
            i++;
        }

        return result;
    }

    /***
     * Judge if a string is hex string
     * @param str string
     * @return boolean
     */
    public static boolean isHexString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
