package net.madtiger.lock.decorator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.lock.AbsSharedLockDecorator;
import net.madtiger.lock.CompositeSharedLock;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 本地(JVM)预制锁，对于需要循环获取的 实例使用
 * <p>
 *   在一些特定的情况下（比如机器比较少，如5台以内），可能锁的竞争都在当前 jvm 中竞争资源，如果是这种情况，可以先通过 jvm 级别的锁获取，成功后再获取远程所，这样可以节省 远程 IO 消耗
 * </p>
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public final class LocalLockDecorator extends AbsSharedLockDecorator {

  private static AtomicLong atomicLong = new AtomicLong(0);

  /**
   * JVM 共享锁
   */
  private static final Map<String, ReentrantLock> LOCAL_LOCKS = new HashMap<>(64);

  /**
   * 构造一个 包装器实例
   *
   * @param delegate 实际执行者
   */
  public LocalLockDecorator(CompositeSharedLock delegate) {
    super(delegate);
  }


  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    ReentrantLock lock = newLockByThread();
    try{
      lock.lock();
      return delegate.tryLock(time, unit);
    } catch (Throwable ex) {
      unLockFromThread();
      throw ex;
    }
  }

  @Override
  public void unlock() {
    try{
      delegate.unlock();
    }finally{
      unLockFromThread();
    }
  }

  @Override
  protected Logger getLogger() {
    return log;
  }

  /**
   * 新建 一个 java 可重入 lock
   * @return
   */
  private ReentrantLock newLockByThread(){
    //JDK1.7 以后 string 常量池放入了堆中，所以不用怕OOM
    synchronized ((LOCAL_LOCK_PREFX + getKey()).intern()) {
      // 获取锁
      ReentrantLock lock = LOCAL_LOCKS.get(getKey());
      if (lock == null) {
        lock = new ReentrantLock();
        LOCAL_LOCKS.put(getKey(), lock);
      }
      debugMessage(String.format("获取锁成功 %s", atomicLong.incrementAndGet()));
      return lock;
    }
  }

  /**
   * 当前线程解锁
   * @return 是否整个释放资源
   */
  private boolean unLockFromThread(){
    synchronized ((LOCAL_LOCK_PREFX + getKey()).intern()) {
      // 获取锁
      ReentrantLock lock = LOCAL_LOCKS.get(getKey());
      if (lock != null) {
        lock.unlock();
        debugMessage(String.format("当前块锁释放成功 %s", atomicLong.decrementAndGet()));
        // 检查是否已全部释放
        if (!lock.isLocked()) {
          debugMessage("当前jvm锁彻底释放成功");
          // 移除 map 中的 Lock
          LOCAL_LOCKS.remove(getKey());
        }else {
          return false;
        }
      }
    }
    return true;
  }

  private static final String LOCAL_LOCK_PREFX = "____local_lock__";
}
