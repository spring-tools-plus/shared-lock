package net.madtiger.lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.lock.provider.IProviderConfigurer;
import net.madtiger.lock.provider.ISharedLockProvider;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 共享锁 工具
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class SharedlockUtils {

  /**
   * 字符串转换成字符串
   * @param str
   * @return
   */
  public static byte[] stringToBytes(String str){
    try{
      return str == null ? null : str.getBytes("UTF8");
    }catch (Throwable ex){
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * 装饰 共享锁服务
   * @param sharedLock 要包装的 服务
   * @param classes 装饰者类
   * @return
   */
  public static synchronized ISharedLock mergeEnv(CompositeSharedLock sharedLock, Set<Class<? extends AbsSharedLockDecorator>> classes){
    Objects.requireNonNull(sharedLock);
    // 合并处理装饰者类
    sharedLock = doMergeDecoratorClasses(sharedLock, classes);
    SharedLockEnvironment environment = SharedLockEnvironment.getInstance();
    // 设置 锁定时间
    if (sharedLock.getLockSeconds() <= 0 && environment.lockSeconds() > 0) {
        sharedLock.setLockSeconds(environment.lockSeconds());
    }
    // 设置 服务提供者
    if (StringUtils.isEmpty(sharedLock.getProviderName()) && !StringUtils.isEmpty(environment.getDefaultProviderName())) {
      sharedLock.setProvider(SharedLockContextHolder.get(environment.getDefaultProviderName(), sharedLock.getProvider()));
    }
    // 设置 configurer
    IProviderConfigurer configurer = environment.getConfigurer(sharedLock.getProvider().getClass());
    if (configurer != null){
      if (sharedLock.getProviderConfigurer() == null) {
        sharedLock.setProviderConfigurer(configurer);
      } else {
        sharedLock.getProviderConfigurer().merge(configurer);
      }
    }
    return sharedLock;
  }

  /**
   * 合并 装饰者
   * @param sharedLock
   * @param classes
   * @return
   */
  private static CompositeSharedLock doMergeDecoratorClasses(CompositeSharedLock sharedLock, Set<Class<? extends AbsSharedLockDecorator>> classes){
    Objects.requireNonNull(classes);
    classes = new HashSet<>(classes);
    classes.addAll(SharedLockEnvironment.getInstance().getDecoratorClasses());
    if (CollectionUtils.isEmpty(classes)) {
      return sharedLock;
    }
    List<Class<? extends AbsSharedLockDecorator>> decoratorList = new ArrayList<>(classes);
    // 排序
    Collections.sort(decoratorList, DECORATOR_COMPARETOR);
    //逐个安装
    for (Class<? extends AbsSharedLockDecorator>  decoratorClass : decoratorList) {
      try {
        sharedLock = decoratorClass.getConstructor(CompositeSharedLock.class).newInstance(sharedLock);
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
    return sharedLock;
  }

  /**
   * 比较器
   */
  private static final Comparator<Class<? extends AbsSharedLockDecorator>> DECORATOR_COMPARETOR = Comparator.comparing( clazz -> {
    Integer order = OrderUtils.getOrder(clazz);
    return order == null ? 1000 : order;
  });
}
