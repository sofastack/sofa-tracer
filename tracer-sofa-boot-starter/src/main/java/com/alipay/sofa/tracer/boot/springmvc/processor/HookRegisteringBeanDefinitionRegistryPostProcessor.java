package com.alipay.sofa.tracer.boot.springmvc.processor;

import com.alipay.common.tracer.core.reactor.SofaTracerReactorSubscriber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * @author xiang.sheng
 */
public class HookRegisteringBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private static final String SOFA_TRACE_REACTOR_KEY = "sofa-tracer-webflux";


    private final ConfigurableApplicationContext context;

    public HookRegisteringBeanDefinitionRegistryPostProcessor(
            ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
            throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        setupHooks();
    }

    private void setupHooks() {
        Hooks.onEachOperator(
                SOFA_TRACE_REACTOR_KEY,
                SofaReactor.scopePassingSpanOperator(this.context));
    }


    public static class SofaReactor {
        private static final Log log = LogFactory.getLog(SofaReactor.class);

        /**
         * Return a span operator pointcut, This can be used in
         * reactor via {@link reactor.core.publisher.Flux#transform(Function)},
         * {@link reactor.core.publisher.Mono#transform(Function)},
         * {@link reactor.core.publisher.Hooks#onLastOperator(Function)} or
         * {@link reactor.core.publisher.Hooks#onLastOperator(Function)}. The Span operator
         * pointcut will pass the Scope of the Span without ever creating any new spans.
         * @param beanFactory - {@link BeanFactory}
         * @param <T> an arbitrary type that is left unchanged by the span operator
         * @return a new lazy span operator pointcut
         */
        @SuppressWarnings("unchecked")
        public static <T> Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanOperator(
                BeanFactory beanFactory) {
            if (log.isTraceEnabled()) {
                log.trace("Scope passing operator [" + beanFactory + "]");
            }

            // Adapt if lazy bean factory
            BooleanSupplier isActive = beanFactory instanceof ConfigurableApplicationContext
                    ? ((ConfigurableApplicationContext) beanFactory)::isActive : () -> true;

            return Operators.liftPublisher((p, sub) -> {
                // if Flux/Mono #just, #empty, #error
                if (p instanceof Fuseable.ScalarCallable) {
                    return sub;
                }
                Scannable scannable = Scannable.from(p);
                // rest of the logic unchanged...
                if (isActive.getAsBoolean()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Spring Context [" + beanFactory
                                + "] already refreshed. Creating a scope "
                                + "passing span subscriber with Reactor Context " + "["
                                + sub.currentContext() + "] and name [" + scannable.name()
                                + "]");
                    }
                    return scopePassingSpanSubscription(sub, (p instanceof Mono));
                }
                if (log.isTraceEnabled()) {
                    log.trace("Spring Context [" + beanFactory
                            + "] is not yet refreshed, falling back to lazy span subscriber. Reactor Context is ["
                            + sub.currentContext() + "] and name is [" + scannable.name()
                            + "]");
                }
//                return new LazySpanSubscriber<>(
//                        lazyScopePassingSpanSubscription(beanFactory, scannable, sub));
                return null;
            });
        }

//        static <T> SpanSubscriptionProvider<T> lazyScopePassingSpanSubscription(
//                BeanFactory beanFactory, Scannable scannable, CoreSubscriber<? super T> sub) {
//            return new SpanSubscriptionProvider<>(beanFactory, sub, sub.currentContext(),
//                    scannable.name());
//        }

        static <T> CoreSubscriber<? super T> scopePassingSpanSubscription(CoreSubscriber<? super T> sub, boolean unary) {
            Context c = sub.currentContext();
            log.info(c.toString());
            return new SofaTracerReactorSubscriber<>(
                    sub, () -> {}, (s, e) -> null, unary);
        }
    }

}
