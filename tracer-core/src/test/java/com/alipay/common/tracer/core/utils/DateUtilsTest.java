/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.utils;

import org.junit.Test;

import java.util.Date;

import static junit.framework.TestCase.assertTrue;

/**
 * @author zhile
 * @version : DateUtilsTest.java, v 0.1 2021年10月08日 下午3:47 zhile Exp $
 */
public class DateUtilsTest {

    @Test
    public void testDiffNextMinute(){
        long diff = DateUtils.diffNextMinute(new Date());
        assertTrue(diff > 0);
    }
}