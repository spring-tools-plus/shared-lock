package net.madtiger.shared.lock;

/**
 * zookeeper 锁 服务类
 * @author Fenghu.Shi
 * @version 1.0
 */
public class ZookeeperLockService extends AbsSharedLock{

  private IZookeeperLockClient lockClient;

  public ZookeeperLockService(IZookeeperLockClient lockClient) {
    this.lockClient = lockClient;
  }

  @Override
  protected boolean tryAcquire(LockResultHolder resultHolder) throws Exception {
    return lockClient.tryAcquire(resultHolder);
  }

  @Override
  protected boolean release(LockResultHolder resultHolder) throws Exception {
    return lockClient.release(resultHolder);
  }
}
