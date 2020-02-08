# shared-lock

## 介绍

分布式共享锁，* 暂时支持 redis *，支持多种模式

1. try/finally 原始模式
2. callback 回调模式，类似 JdbcTemplate execute
3. AOP 切面注解模式

支持 enable 引用 和 starter 的开箱即用方式。

## 软件架构

需要 java 8+ ，同时依赖 spring + spring-data-redis 


## 安装教程

安装方式，主要支持两三种自动注入方式：enabled 、spring boot starter 和 自定义 

### enabled 模式

1. maven 导入

```xml
<dependency>
   <groupId>net.madtiger.shared.lock</groupId>
   <artifactId>lock-annotation</artifactId>
   <version>1.0.0-RELEASE</version>
</dependency>
```

2. 开启引用

```java
@SpringBootApplication
@EnabledSharedLock // 开启自动注入
public class DisLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisLockApplication.class, args);
    }

}
```

3. 配置 spring data redis
 
这里小伙伴们自己配置吧

### spring boot starter 模式

1. maven 导入

```xml
<dependency>
   <groupId>net.madtiger.shared.lock</groupId>
   <artifactId>spring-boot-starter-shared-lock</artifactId>
   <version>1.0.0-RELEASE</version>
</dependency>
```

2. 配置 spring data redis
 
这里小伙伴们自己配置吧


### ~~~自定义 (该方式不推荐使用)~~~

此方式稍微麻烦点，不建议大家使用哈

### 1. maven 导入 

```xml
<dependency>
   <groupId>net.madtiger.shared.lock</groupId>
   <artifactId>lock-annotation</artifactId>
   <version>1.0.0-RELEASE</version>
</dependency>
```

### 2. 初始化 service bean

SharedLock 组件使用主要涉及三个类

1. SpringRedisLockClient 
用于 操作 redis 的客户端 ，底层是通过 spring-data-redis 的 RedisTemplate 实现的

2. RedisLockService
分布式锁的Redis具体实现类

3. SharedLockInterceptor 
用于支持 AOP 模式的拦截器，这里是可选的，如果不需要支持 拦截器则不用实例化


*样例*:
```java
public class CustomizeSharedLockConfiguration {

  /**
   * 配置 共享锁
   * @param redisTemplate
   * @return
   */
  @Bean
  public ISharedLock<SpinSetLockArgs> defaultSharedLock(ObjectProvider<RedisTemplate> redisTemplate){
    return new RedisLockService(new SpringRedisLockClient(redisTemplate.getIfAvailable()));
  }
  
 /**
   * 创建 共享锁 拦截器
   * @param context
   * @return
   */
  @Bean
  public SharedLockInterceptor annotationSharedLoadInterceptor(ApplicationContext context){
    return new SharedLockInterceptor(context);
  }

}
```
 


## 使用说明

组件有三种主要使用方式：

1. try/finally 通用方式，一般常用的方式
2. execute callback 回调方式，类似 JdbcTemplate execute
3. AOP + Annotation 模式，类似 spring 事务

----

SharedLock 组件内部的锁定义了 4 种状态(定义在` LockResultHolder ` 类中)
1. INIT 初始化: 还没有尝试获取锁
2. LOCKING 锁定中: 已经成功获取锁，且没有释放，一般是 try/finally 模式获取所成功后的状态
3. DONE 完成: 处理完成，获取锁成功，并且释放锁成功，这里不保证 业务执行成功
4. TIMEOUT 超时: 获取锁超时
5. ROLLBACK 回滚: 获取所成功，释放失败时的状态，一般是由于业务执行时间过长导致。


---- 

在执行分布式锁时，可以传递一个 `SetLockArgs` 参数，用于配置 一些常用参数，比如 锁定时间等

### try/finally 使用方式

此方式是以往分布式锁使用较多的方式，需要用户手动获取并释放。

* SharedLock 在原始的使用方式基础上增加了一些比较常用/方便的特性 *
1. 实现了 ` java.io.Closeable ` 接口，支持 JDK 8 try-with-resource 特性
2. 返回数据支持链式调用
3. 支持意外回滚

#### 基础使用方式

见 [try/finally 基本使用方式](https://github.com/spring-tools-plus/shared-lock/blob/master/lock-demo/src/main/java/org/shared/lock/demo/DemoController.java) 的 `doTry()`方法

* 备注 *
1. try-with-resource 和 普通try/finally 使用场景区分
对于所有逻辑都在 try {} 内部执行时，建议使用 try-with-resource 模式，如果需要在 try {} 外部还有根据获取锁状态进行其他业务逻辑时，使用 普通的 try/finally 模式

2. rollback callback 使用
如果需要对锁释放失败，进行回退处理业务时，可以通过如下方式使用。
```java

// 来一个 try-with-resource 模式
    try(LockResultHolder<Flux<String>> holder = lockService.tryLock("123123123", SpinSetLockArgs.builder().maxRetryTimes(5).build())) {
      if (holder.isLocking()){
        // 支持降级的处理
       return holder.doFallback(() -> {
          System.out.println("执行成功");
          return Flux.just("OK");
        }, () -> {
          return Flux.just("执行回退");
        }, () -> {
         // 这里如果返回 null，则不会覆盖 上面的 数据
         return Flux.just("执行回滚");
       });
      }
      System.out.println("获取所超时");
      return Flux.error(new Throwable("失败了"));
    }

```


###  execute callback 方式

该方式锁的获取和执行都由系统管理，只有当获取所成功后才执行此方法。

使用方式见 [execute 使用方式](https://github.com/spring-tools-plus/shared-lock/blob/master/lock-demo/src/main/java/org/shared/lock/demo/DemoController.java) 的 `doExecute` 方法。


### AOP Annotation 方式

此方式最简便，只要在需要锁执行的方法上增加 `SharedLock` 注解。


使用方式见 [AOP 使用方式](https://github.com/spring-tools-plus/shared-lock/blob/master/lock-demo/src/main/java/org/shared/lock/demo/DemoController.java) 的 `doAOP` 方法。

* 此方式增加了几个特性 *
1. 支持降级

`SharedLock#fallbackMethod` 的 配置当获取锁失败后的降级方法。
*注意*：这里降级只正对 获取所失败，如果是业务执行失败，则不会调用此方法，业务失败请 catch 处理。

2. 回滚处理

如果需要对锁释放失败，进行回退处理业务时，可以通过此方式回滚，如果此函数有返回值，且类型和执行方法相同，且此方法返回的数据不为空，则rollback 执行的返回值会覆盖原数据。 
覆盖顺序 rollback -> fallback | callback

3. 其他参数

见 `SharedLock`



##  版本更新


* 1.0.0-RELEASE 发布 （2020-02-08）

完成了基本框架和 redis 实现。
