package net.madtiger.lock.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.madtiger.lock.AbsSharedLockDecorator;
import net.madtiger.lock.decorator.SpinLockDecorator;
import net.madtiger.lock.provider.ISharedLockProvider;
import net.madtiger.lock.zk.CuratorLockClient;
import net.madtiger.lock.zk.ZookeeperLockProvider;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Configuration;

/**
 *  zookeeper 配置
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
public class SharedLockCuratorConfiguration extends AbsSharedLockConfiguration<CuratorFramework> {

  /**
   * 配置 共享锁
   * @param zookeeper zk 客户端
   * @return
   */
  @Override
  protected ISharedLockProvider newSharedLockProvider(CuratorFramework zookeeper){
    return new ZookeeperLockProvider(new CuratorLockClient(zookeeper));
  }

  @Override
  protected List<Class<? extends AbsSharedLockDecorator>> defaultDecorators() {
    return new ArrayList<>(2);
  }
}
