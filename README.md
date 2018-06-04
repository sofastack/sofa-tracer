## SOFATracer

[![Build Status](https://travis-ci.org/alipay/sofa-tracer.svg?branch=master)](https://travis-ci.org/alipay/sofa-tracer)
[![Coverage Status](https://coveralls.io/repos/github/alipay/sofa-tracer/badge.svg?branch=master)](https://coveralls.io/github/alipay/sofa-tracer?branch=master)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![maven](https://img.shields.io/badge/Maven-2.1.1-brightgreen.svg)

SOFATracer 是一个用于分布式系统调用跟踪的组件，通过统一的 `traceId` 将调用链路中的各种网络调用情况以日志的方式记录下来，以达到透视化网络调用的目的。这些日志可用于故障的快速发现，服务治理等。

## 一、背景

在当下的技术架构实施中，统一采用面向服务的分布式架构，通过服务来支撑起一个个应用，而部署在应用中的各种服务通常都是用复杂大规模分布式集群来实现的，同时，这些应用又构建在不同的软件模块上，这些软件模块，有可能是由不同的团队开发，可能使用不同的编程语言来实现、有可能部署了几千台服务器。因此，就需要一些可以帮助理解各个应用的线上调用行为，并可以分析远程调用性能的组件。

为了能够分析应用的线上调用行为以及调用性能，蚂蚁金服基于 [OpenTracing 规范](http://opentracing.io/documentation/pages/spec.html) 提供了分布式链路跟踪 SOFATracer 的解决方案。

## 二、功能简介

为了解决在实施大规模微服务架构时的链路跟踪问题，SOFATracer 提供了以下的能力：

### 2.1 基于 OpenTracing 规范提供分布式链路跟踪解决方案

基于 [OpenTracing 规范](http://opentracing.io/documentation/pages/spec.html) 并扩展其能力提供链路跟踪的解决方案。各个框架或者组件可以基于此实现，通过在各个组件中埋点的方式来提供链路跟踪的能力。

### 2.2 提供异步落地磁盘的日志打印能力

基于 [Disruptor](https://github.com/LMAX-Exchange/disruptor) 高性能无锁循环队列，提供异步打印日志到本地磁盘的能力。框架或者组件能够在接入时，在异步日志打印的前提下可以自定义日志文件的输出格式。SOFATracer 提供两种类似的日志打印类型即摘要日志和统计日志，摘要日志：每一次调用均会落地磁盘的日志；统计日志：每隔一定时间间隔进行统计输出的日志。

### 2.3 支持日志自清除和滚动能力

异步落地磁盘的 SOFATracer 日志支持自清除和滚动能力，支持按照按照天清除和按照小时或者天滚动的能力

### 2.4 基于 SLF4J MDC 的扩展能力

SLF4J 提供了 MDC（Mapped Diagnostic Contexts）功能，可以支持用户定义和修改日志的输出格式以及内容。SOFATracer 集成了 SLF4J MDC 功能，方便用户在只简单修改日志配置文件即可输出当前 Tracer 上下文的 `tracerId` 和 `spanId`。

### 2.5 界面展示能力

SOFATracer 可以将链路跟踪数据远程上报到开源产品 [Zipkin](https://zipkin.io/) 做分布式链路跟踪的展示。

### 2.6 统一配置能力

配置文件中提供丰富的配置能力以定制化应用的个性需求。

## 三、快速开始

请查看文档中的[快速开始](https://github.com/alipay/sofa-tracer/wiki/QuickStart)来了解如何快速上手使用 SOFATracer。

## 四、如何贡献

在贡献代码之前，请阅读[如何贡献](./CONTRIBUTING.md)来了解如何向 SOFATracer 贡献代码。

SOFATracer 的编译环境的要求为 JDK7 或者 JDK8，需要采用 [Apache Maven 3.2.5](https://archive.apache.org/dist/maven/maven-3/3.2.5/binaries/) 或者更高的版本进行编译。

## 五、示例

在此工程的 `tracer-samples` 目录下的是 SOFATracer 的示例工程，分别为：

* [SOFATracer 示例工程（基于 Spring MVC 示例落地日志）](./tracer-samples/tracer-sample-with-springmvc)
* [SOFATracer 示例工程（基于 Spring MVC 示例远程上报 Zipkin）](./tracer-samples/tracer-sample-with-zipkin)
* [SOFATracer 示例工程（基于日志编程接口 SLF4J 示例打印 traceId）](./tracer-samples/tracer-sample-with-slf4j)
 
## 六、文档

请参考 [WIKI 中的 SOFATracer 的文档](https://github.com/alipay/sofa-tracer/wiki)。



