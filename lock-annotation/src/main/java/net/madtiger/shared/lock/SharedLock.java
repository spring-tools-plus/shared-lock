package net.madtiger.shared.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.springframework.core.annotation.AliasFor;

/**
 * 共享锁配置
 * @author fenghu.shi
 * @version 1.0
 * @see SetLockArgs
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SharedLock {

  /**
   * 锁定的key，支持变量，#{paramName1} #{paramName2} #{paramName3}，调用 tostring 方法生成，null=""
   * 默认 使用 {@link Method#toString()} 方法生成
   */
  @AliasFor("key")
  String value() default DEFAULT_METHOD;

  /**
   * key 值
   */
  String key() default DEFAULT_METHOD;

  /**
   * 执行服务提供bean名称，默认根据 类型获取
   */
  String provider() default DEFAULT_PROVIDER;

  /**
   * 最大等待时间，单位秒
   */
  int waitTimeoutMills() default DEFAULT_INT;

  /**
   * 最大重试次数
   * 默认不限制，以 {@link #waitTimeoutMills()} 为准，如果设置了，则满足一个即退出
   */
  int maxRetryTimes() default DEFAULT_INT;


  /**
   * 锁最长可以持有时间，单位秒，默认 是 {@link SetLockArgs#WAIT_TIMEOUT_SECENDS} 的 4 倍时间
   */
  int lockedSeconds() default DEFAULT_INT;

  /**
   * 每次重试时，最小 sleep 时间
   */
  int sleepMinMills() default DEFAULT_INT;

  /**
   * 每次重试时，最大 sleep 时间
   */
  int sleepMaxMills() default DEFAULT_INT;

  /**
   * 自旋次数，该参数只对 {@link AbsSpinLock} 锁有效
   */
  int spinTimes() default DEFAULT_INT;

  /**
   * 回调函数，该函数必须是当前对象的公共方法，参数也相同，不需要返回值
   */
  String rollbackMethod() default DEFAULT_METHOD;

  /**
   * 失败降级方法，当获取所失败时，替代方法，方法签名必须跟当前方法一致
   */
  String fallbackMethod() default DEFAULT_METHOD;

  /**
   * 获取所失败执行策略，默认是自动，具体见 {@link FaultPolicy#AUTO}
   */
  FaultPolicy faultPolicy() default FaultPolicy.AUTO;

  /**
   * 失败时抛出的异常，默认 LockFailedException ，必须有一个默认构造函数
   */
  Class<? extends RuntimeException> throwable() default LockFailedException.class;

  /**
   * 默认值
   */
  static final int DEFAULT_INT = Integer.MAX_VALUE;

  /**
   * 默认共享锁执行者
   */
  static final String DEFAULT_PROVIDER = "DEFAULT_PROVIDER";


  /**
   * 默认方法
   */
  static final String DEFAULT_METHOD = "__default";


  /**
   * 失败返回的默认值
   */
  static final byte FAULT_NUMBER_DEFAULT = -1;

}
