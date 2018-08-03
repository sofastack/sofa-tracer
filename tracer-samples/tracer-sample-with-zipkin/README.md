# 使用 SOFATracer 远程汇报数据到 Zipkin

本示例演示如何在集成了 SOFATracer 的应用，通过配置 SOFATracer 将链路数据远程汇报到 [Zipkin](https://zipkin.io/)。

## 环境准备

要使用 SOFABoot，需要先准备好基础环境，SOFABoot 依赖以下环境：
- JDK7 或 JDK8
- 需要采用 Apache Maven 3.2.5 或者以上的版本来编译

## 引入 SOFATracer

在创建好一个 Spring Boot 的工程之后，接下来就需要引入 SOFABoot 的依赖，首先，需要将上文中生成的 Spring Boot 工程的 `zip` 包解压后，修改 Maven 项目的配置文件 `pom.xml`，将

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>${spring.boot.version}</version>
    <relativePath/>
</parent>
```

替换为：

```xml
<parent>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofaboot-dependencies</artifactId>
    <version>2.4.4</version>
</parent>
```

然后，在工程中添加 SOFATracer 依赖：

```
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>tracer-sofa-boot-starter</artifactId>
</dependency>
```

最后，在工程的 `application.properties` 文件下添加一个 SOFATracer 要使用的参数，包括`spring.application.name` 用于标示当前应用的名称；`logging.path` 用于指定日志的输出目录。

```
# Application Name
spring.application.name=SOFATracerReportZipkin
# logging path
logging.path=./logs
```

## 启动 Zipkin 服务端

启动 Zipkin 服务端用于接收 SOFATracer 汇报的链路数据，并做展示。Zipkin Server 的搭建可以[参考此文档](https://zipkin.io/)进行配置和服务端的搭建。

## 配置 Zipkin 依赖

考虑到 Zipkin 的数据上报能力不是 SOFATracer 默认开启的能力，所以期望使用 SOFATracer 做数据上报时，需要添加如下的 Zipkin 数据汇报的依赖：

```xml
 <dependency>
    <groupId>io.zipkin.java</groupId>
    <artifactId>zipkin</artifactId>
    <version>1.19.2</version>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter</groupId>
    <artifactId>zipkin-reporter</artifactId>
    <version>0.6.12</version>
</dependency>
```

## 启用 SOFATracer 汇报数据到 Zipkin

在配置文件 `application.properties` 中，配置 Zipkin Server 端的地址 `com.alipay.sofa.tracer.zipkin.baseUrl=http://${ip}:${port}`。

按照上文完成了依赖和 Zipkin Server 的配置后，即激活了远程上报的能力。本示例中已经搭建好的 Zipkin Server 端地址是 `http://zipkin-cloud-3.host.net:9411`。

## 运行

可以将工程导入到 IDE 中运行生成的工程里面中的 `main` 方法（一般上在 XXXApplication 这个类中）启动应用，也可以直接在该工程的根目录下运行 `mvn spring-boot:run`，将会在控制台中看到启动日志：

```
2018-05-12 13:12:05.868  INFO 76572 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'SpringMvcSofaTracerFilter' to urls: [/*]
2018-05-12 13:12:06.543  INFO 76572 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/zipkin]}" onto public java.util.Map<java.lang.String, java.lang.Object> com.alipay.sofa.tracer.examples.zipkin.controller.SampleRestController.zipkin(java.lang.String)
2018-05-12 13:12:07.164  INFO 76572 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
```

可以通过在浏览器中输入 [http://localhost:8080/zipkin](http://localhost:8080/zipkin) 来访问 REST 服务，结果类似如下：

```json
{
	content: "Hello, SOFATracer Zipkin Remote Report!",
	id: 1,
	success: true
}
```

## 查看 Zipkin 服务端展示

打开 Zipkin 服务端界面，假设我们部署的 Zipkin 服务端的地址是 `http://zipkin-cloud-3.host.net:9411`，打开 URL 并搜索 `zipkin`(由于我们本地访问的地址是 localhost:8080/zipkin)，可以看到展示的链路图。


