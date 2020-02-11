package net.madtiger.shared.lock.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 抽象的共享锁配置，将一些功能的方法和bean在这里初始化，所有 configuration 子类必须继承自此类
 * @author Fenghu.Shi
 * @version 1.0
 */
public abstract class AbsSharedLockConfiguration implements  ISharedLockConfiguration{


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
