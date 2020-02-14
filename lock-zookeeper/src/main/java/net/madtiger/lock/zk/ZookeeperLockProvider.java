package net.madtiger.lock.zk;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.lock.CompositeSharedLock;
import net.madtiger.lock.ISharedLock;
import net.madtiger.lock.exception.UnLockFailSharedLockException;
import net.madtiger.lock.provider.ISharedLockProvider;

/**
 * zookeeper 锁 服务类
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class ZookeeperLockProvider implements ISharedLockProvider {

  private CuratorLockClient lockClient;

  public ZookeeperLockProvider(CuratorLockClient lockClient) {
    this.lockClient = lockClient;
  }

  @Override
  public boolean doAcquire(CompositeSharedLock lock) {
    return doAcquire(lock, 300, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean doAcquire(CompositeSharedLock lock, long time, TimeUnit unit) {
    try {
      return lockClient.tryAcquire(lock, lock.getKey(), time, unit);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean doRelease(CompositeSharedLock lock) throws UnLockFailSharedLockException {
    return lockClient.release(lock);
  }
}
