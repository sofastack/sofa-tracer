# Sofa Tracer Reactor Plugin

> Note: this module requires Java 8 or later version.

Sofa Tracer Reactor Plugin provides integration module for [Reactor](https://projectreactor.io/).

Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-tracer-reactor-plugin</artifactId>
    <version>x.y.z</version>
</dependency>
```

Example:

```java
someService.doSomething() // return type: Mono<T> or Flux<T>
   .transform(new SofaTracerReactorTransformer<>(
           () -> startSpan(),
           (springMvcSpan, throwable) -> finishSpan(springMvcSpan, throwable)
   )) // transform here
   .subscribe();
```