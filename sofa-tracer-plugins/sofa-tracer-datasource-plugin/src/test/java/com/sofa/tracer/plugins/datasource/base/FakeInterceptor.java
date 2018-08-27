/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.sofa.tracer.plugins.datasource.base;

import com.alipay.sofa.tracer.plugins.datasource.Interceptor;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @sicne 2.2.0
 */
public class FakeInterceptor implements Interceptor {

    @Override
    public Object intercept(Chain chain) throws Exception {
        Object reVal = null;
        try {
            System.out.println("before interceptor");
            reVal = chain.proceed();
            System.out.println("after interceptor");
        } finally {
            System.out.println("finally interceptor");
        }
        return reVal;
    }
}
