package com.alipay.common.tracer.core.telemetry.common;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ComponentRegistry<V> {
    private final ConcurrentMap<String, V> registry = new ConcurrentHashMap();
    private final Function<String , V> factory;

    public ComponentRegistry(Function<String, V> factory) {
        this.factory = factory;
    }

    public V get(String instrumentationScopeInfo) {
        V component = this.registry.get(instrumentationScopeInfo);
        if (component != null) {
            return component;
        } else {
            V newComponent = this.factory.apply(instrumentationScopeInfo);
            V oldComponent = this.registry.putIfAbsent(instrumentationScopeInfo, newComponent);
            return oldComponent != null ? oldComponent : newComponent;
        }
    }

    public Collection<V> getComponents() {
        return Collections.unmodifiableCollection(new ArrayList(this.registry.values()));
    }
}
