package net.madtiger.shared.lock.configuration;

import static net.madtiger.shared.lock.SharedLockConstants.PROPERTIES_PREFIX;

import net.madtiger.shared.lock.CuratorLockClient;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.ZookeeperLockService;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 *  zookeeper 配置
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
public class SharedLockCuratorConfiguration extends AbsSharedLockConfiguration<CuratorFramework> {

  @Value("${" + PROPERTIES_PREFIX + "zk-namespace:''}")
  private String namespace;

  /**
   * 配置 共享锁
   * @param zookeeper zk 客户端
   * @return
   */
  @Override
  protected ISharedLock newSharedLock(CuratorFramework zookeeper){
    if (StringUtils.isEmpty(namespace)) {
      return new ZookeeperLockService(new CuratorLockClient(zookeeper));
    }
    return new ZookeeperLockService(new CuratorLockClient(namespace, zookeeper));
  }

}
