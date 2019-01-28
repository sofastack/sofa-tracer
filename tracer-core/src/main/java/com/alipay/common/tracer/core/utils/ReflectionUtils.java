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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author qilong.zql
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Costin Leau
 * @author Sam Brannen
 * @author Chris Beams
 * @since 2.3.2
 */
public class ReflectionUtils {
    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied name
     * and parameter types. Searches all superclasses up to {@code Object}.
     * <p>Returns {@code null} if no {@link Method} can be found.
     * @param clazz the class to introspect
     * @param name the name of the method
     * @param paramTypes the parameter types of the method
     * (may be {@code null} to indicate any signature)
     * @return the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods()
                : getDeclaredMethods(searchType));
            for (Method method : methods) {
                if (name.equals(method.getName())
                    && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * This variant retrieves {@link Class#getDeclaredMethods()} from a local cache
     * in order to avoid the JVM's SecurityManager check and defensive array copying.
     * In addition, it also includes Java 8 default methods from locally implemented
     * interfaces, since those are effectively to be treated just like declared methods.
     * @param clazz the class to introspect
     * @return the cached array of methods
     * @see Class#getDeclaredMethods()
     */
    private static Method[] getDeclaredMethods(Class<?> clazz) {
        Method[] result;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
        if (defaultMethods != null) {
            result = new Method[declaredMethods.length + defaultMethods.size()];
            System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
            int index = declaredMethods.length;
            for (Method defaultMethod : defaultMethods) {
                result[index] = defaultMethod;
                index++;
            }
        } else {
            result = declaredMethods;
        }
        return result;
    }

    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new LinkedList<Method>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }
}