package net.madtiger.lock.configuration;

import static net.madtiger.lock.SharedLockConstants.PROPERTIES_PREFIX;

import java.util.List;
import javax.annotation.PostConstruct;
import net.madtiger.lock.AbsSharedLockDecorator;
import net.madtiger.lock.SharedLockContextHolder;
import net.madtiger.lock.SharedLockEnvironment;
import net.madtiger.lock.provider.ISharedLockProvider;
import net.madtiger.lock.zk.ZookeeperConfigurer;
import net.madtiger.lock.zk.ZookeeperLockProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 抽象的共享锁配置，将一些功能的方法和bean在这里初始化，所有 configuration 子类必须继承自此类
 * @param <DepenOn> 依赖的资源类型
 * @author Fenghu.Shi
 * @version 1.0
 */
public abstract class AbsSharedLockConfiguration<DepenOn> implements ISharedLockConfiguration{

  @Autowired
  private DepenOn depenOn;

  /**
   * 创建 共享锁 拦截器
   * @param context
   * @return
   */
  @Bean
  public SharedLockInterceptor annotationSharedLoadInterceptor(ApplicationContext context){
    return new SharedLockInterceptor(context);
  }

  @PostConstruct
  public void initSharedLock(){
    ISharedLockProvider provider = newSharedLockProvider(depenOn);
    // 全局增加 装饰者类
    SharedLockEnvironment.getInstance().addDecoratorClasses(defaultDecorators()).setDefaultProvder(provider);
  }


  /**
   * 支持 的 装饰者服务类
   * @return
   */
  protected abstract List<Class<? extends AbsSharedLockDecorator>> defaultDecorators();


  /**
   * 子类用于初始化 共享锁
   * @param depenOn
   * @return
   */
  protected abstract ISharedLockProvider newSharedLockProvider(DepenOn depenOn);

}
