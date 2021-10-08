/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.utils;

import java.util.Date;

/**
 * @author zhile
 * @version : DateUtils.java, v 0.1 2021年09月23日 下午3:55 zhile Exp $
 */
public class DateUtils {

    public static long diffNextMinute(Date date) {
        long now = date.getTime();
        long nextMinute = (now / (60 * 1000) + 1) * 60 * 1000;
        return nextMinute - now;
    }

}