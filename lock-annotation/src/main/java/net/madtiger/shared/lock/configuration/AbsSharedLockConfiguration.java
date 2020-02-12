package net.madtiger.shared.lock.configuration;

import static net.madtiger.shared.lock.SharedLockConstants.PROPERTIES_PREFIX;

import java.util.ArrayList;
import java.util.List;
import net.madtiger.shared.lock.CuratorLockClient;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.SharedlockUtils;
import net.madtiger.shared.lock.ZookeeperLockService;
import net.madtiger.shared.lock.decorator.AbsSharedLockDecorator;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

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
   * 包装器类
   */
  @Value("${" + PROPERTIES_PREFIX + "decorator-classes:net.madtiger.shared.lock.decorator.reentrant.ReentrantLockDecorator}")
  private String decoratorClasses;

  /**
   * 创建 共享锁 拦截器
   * @param context
   * @return
   */
  @Bean
  public SharedLockInterceptor annotationSharedLoadInterceptor(ApplicationContext context){
    return new SharedLockInterceptor(context);
  }

  @Bean
  public ISharedLock defaultSharedLock(){
    // 获取 lock
    ISharedLock sourceLock = newSharedLock(depenOn);
    // 这里来实现包装器
    return SharedlockUtils.newDecorators(sourceLock, getDecorators());
  }


  /**
   * 获取 包装器类列表
   * @return 列表
   */
  private List<Class<AbsSharedLockDecorator>> getDecorators(){
    List<Class<AbsSharedLockDecorator>> classes = new ArrayList<>();
    if (StringUtils.isEmpty(decoratorClasses)){
      return classes;
    }
    String[] strs = decoratorClasses.split(",");
    for (String str : strs) {
      try {
        classes.add((Class<AbsSharedLockDecorator>) ClassUtils.forName(str, getClass().getClassLoader()));
      } catch (Exception e) {
        e.printStackTrace();
        throw new IllegalArgumentException(String.format("%s 类不存或不是net.madtiger.shared.lock.decorator.AbsSharedLockDecorator的子类:%s", str, e.getMessage()));
      }
    }
    return classes;
  }


  /**
   * 子类用于初始化 共享锁
   * @param depenOn
   * @return
   */
  protected abstract ISharedLock newSharedLock(DepenOn depenOn);

}
