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
package com.alipay.sofa.tracer.boot.junit;

import com.alipay.sofa.tracer.plugins.springmvc.SpringMvcSofaTracerFilter;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * @author xuanbei 18/11/9
 * @since 2.3.0
 */
public class SeparateClassloaderTestRunner extends SpringJUnit4ClassRunner {
    private final SeparateClassloader separateClassloader = new SeparateClassloader();
    private Class                     clazzLoadBySeparateClassloader;
    private Method                    runMethod;
    private Object                    runnerObject;

    public SeparateClassloaderTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        try {
            Class springJUnit4ClassRunnerClass = separateClassloader
                .loadClass(SpringJUnit4ClassRunner.class.getName());
            Constructor constructor = springJUnit4ClassRunnerClass.getConstructor(Class.class);
            clazzLoadBySeparateClassloader = separateClassloader.loadClass(clazz.getName());
            runnerObject = constructor.newInstance(clazzLoadBySeparateClassloader);
            runMethod = springJUnit4ClassRunnerClass.getMethod("run", RunNotifier.class);
        } catch (Throwable e) {
            throw new InitializationError(e);
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(separateClassloader);
            runMethod.invoke(runnerObject, notifier);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    @Override
    public Description getDescription() {
        return ((Runner) runnerObject).getDescription();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        ((Filterable) runnerObject).filter(filter);
    }

    @Override
    public void sort(Sorter sorter) {
        ((Sortable) runnerObject).sort(sorter);
    }

    public static class SeparateClassloader extends URLClassLoader {
        public SeparateClassloader() {
            super(((URLClassLoader) getSystemClassLoader()).getURLs(), null);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.equals(SpringMvcSofaTracerFilter.class.getName())) {
                throw new ClassNotFoundException();
            }

            if (name.startsWith("org.junit") || name.startsWith("java")) {
                return getSystemClassLoader().loadClass(name);
            }

            return super.loadClass(name);
        }
    }
}