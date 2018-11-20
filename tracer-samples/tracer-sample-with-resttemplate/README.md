## 使用 SOFATracer 记录 RestTemplate 链路调用数据

本示例演示如何在集成了 SOFATracer 的应用中，通过 sofa-tracer-resttemplate-plugin 插件，将 RestTemplate 的请求链路数据记录在文件中。

## 环境准备

要使用 SOFABoot，需要先准备好基础环境，SOFABoot 依赖以下环境：

- JDK8
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
    <version>${sofa.boot.version}</version>
</parent>
```
这里的 ${sofa.boot.version} 指定具体的 SOFABoot 版本，[参考发布历史](https://github.com/alipay/sofa-boot/releases)。

然后，在工程中添加 SOFATracer 依赖：

```xml
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>tracer-sofa-boot-starter</artifactId>
</dependency>
```

最后，在工程的 `application.properties` 文件下添加一个 SOFATracer 要使用的参数，包括 `spring.application.name` 用于标示当前应用的名称；`logging.path` 用于指定日志的输出目录。

```properties
# Application Name
spring.application.name=HttpClientDemo
# logging path
logging.path=./logs
```

## 添加 RestTemplate、SOFATracer 依赖和 SOFATracer 的 RestTemplate 插件

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>tracer-sofa-boot-starter</artifactId>
</dependency>

<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-tracer-resttmplate-plugin</artifactId>
</dependency>
```



## 添加一个提供 RESTFul 服务的 Controller

```java
@RestController
public class SampleController {
    private final AtomicLong counter = new AtomicLong(0);
    @RequestMapping("/rest")
    public Map<String, Object> rest() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("count", counter.incrementAndGet());
        return map;
    }

    @RequestMapping("/asyncrest")
    public Map<String, Object> asyncrest() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("count", counter.incrementAndGet());
        Thread.sleep(5000);
        return map;
    }
}
```

## 以 API 方式构造 RestTemplate 发起一次对上文的 RESTFul 服务的调用

* 构造 RestTemplate 同步调用实例

```java
RestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildRestTemplate();
ResponseEntity<String> responseEntity = restTemplate.getForEntity(
            "http://sac.alipay.net:8080/rest", String.class);
```

* 构造 RestTemplate 异步调用实例

```java
AsyncRestTemplate asyncRestTemplate = SofaTracerRestTemplateBuilder
    .buildAsyncRestTemplate();
ListenableFuture<ResponseEntity<String>> forEntity = asyncRestTemplate.getForEntity(
            "http://sac.alipay.net:8080/asyncrest", String.class);
```

## 以自动注入的方式获取 RestTemplate

```java
@Autowired
RestTemplate             restTemplate;
```


## 运行

可以将工程导入到 IDE 中运行工程里面中的 `main` 方法（本实例 main 方法在 HttpClientDemoApplication 中）启动应用，在控制台中看到启动打印的日志如下：

```
2018-10-24 10:45:28.683  INFO 5081 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2018-10-24 10:45:28.733  INFO 5081 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2018-10-24 10:45:28.736  INFO 5081 --- [           main] c.a.s.t.e.r.RestTemplateDemoApplication  : Started RestTemplateDemoApplication in 2.163 seconds (JVM running for 3.603)
```

调用成功：

```
2018-10-24 10:45:28.989  INFO 5081 --- [           main] c.a.s.t.e.r.RestTemplateDemoApplication  : Response is {"count":1}
2018-10-24 10:45:34.014  INFO 5081 --- [           main] c.a.s.t.e.r.RestTemplateDemoApplication  : Async Response is {"count":2}
2018-10-24 10:45:34.014  INFO 5081 --- [           main] c.a.s.t.e.r.RestTemplateDemoApplication  : test finish .......
```



## 查看日志

在上面的 `application.properties` 里面，我们配置的日志打印目录是 `./logs` 即当前应用的根目录（我们可以根据自己的实践需要进行配置），在当前工程的根目录下可以看到类似如下结构的日志文件：

```
./logs
├── spring.log
└── tracelog
    ├── resttemplate-digest.log
    ├── resttemplate-stat.log
    ├── spring-mvc-digest.log
    ├── spring-mvc-stat.log
    ├── static-info.log
    └── tracer-self.log
```

示例中通过构造两个 RestTemplate（一个同步一个异步） 发起对同一个 RESTful 服务的调用，调用完成后可以在 restTemplate-digest.log 中看到类似如下的日志，而对于每一个输出字段的含义可以看 SOFATracer 的说明文档：

* 摘要日志

```json
{"time":"2018-10-24 10:45:28.977","local.app":"RestTemplateDemo","traceId":"0a0fe8b3154034912878910015081","spanId":"0","request.url":"http://sac.alipay.net:8080/rest","method":"GET","result.code":"200","resp.size.bytes":0,"time.cost.milliseconds":188,"current.thread.name":"main","remote.app":"","baggage":""}
{"time":"2018-10-24 10:45:34.013","local.app":"RestTemplateDemo","traceId":"0a0fe8b3154034912900410025081","spanId":"0","request.url":"http://sac.alipay.net:8080/asyncrest","method":"GET","result.code":"200","resp.size.bytes":0,"time.cost.milliseconds":5009,"current.thread.name":"SimpleAsyncTaskExecutor-1","remote.app":"","baggage":""}
```

* 统计日志

```json
{"time":"2018-10-24 10:46:28.769","stat.key":{"method":"GET","local.app":"RestTemplateDemo","request.url":"http://sac.alipay.net:8080/asyncrest"},"count":1,"total.cost.milliseconds":5009,"success":"true","load.test":"F"}
{"time":"2018-10-24 10:46:28.770","stat.key":{"method":"GET","local.app":"RestTemplateDemo","request.url":"http://sac.alipay.net:8080/rest"},"count":1,"total.cost.milliseconds":188,"success":"true","load.test":"F"}
```