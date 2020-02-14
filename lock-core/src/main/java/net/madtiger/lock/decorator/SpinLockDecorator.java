package net.madtiger.lock.decorator;

import static net.madtiger.lock.SharedLockStatus.CANCEL;
import static net.madtiger.lock.SharedLockStatus.LOCKED;
import static net.madtiger.lock.SharedLockStatus.NEW;
import static net.madtiger.lock.SharedLockStatus.TIMEOUT;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.lock.AbsSharedLockDecorator;
import net.madtiger.lock.CompositeSharedLock;
import net.madtiger.lock.exception.TimeoutSharedLockException;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 支持自旋的锁，一般用于需要多次主动拉取的服务，比如 Redis
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpinLockDecorator extends AbsSharedLockDecorator {

  /**
   * 构造一个 包装器实例
   *
   * @param delegate 实际执行者
   */
  public SpinLockDecorator(CompositeSharedLock delegate) {
    super(delegate);
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(unit);
    // 检查状态
    if (getStatus() != NEW) {
      throw new InterruptedException(String.format("%s 锁的当前状态是 %s 不能再次获取锁", getKey(), getStatus()));
    }
    // 如果小于0 则降级成 try lock
    if (time <= 0){
      return getProvider().doAcquire(this);
    }
    // 获取成功
    try{
      if (doAcquire(time, unit)) {
        setStatus(LOCKED);
        return true;
      }else {
        // 如果 是 新的，则设置为 长时间
        if (getStatus() == NEW) {
          setStatus(TIMEOUT);
        }
        return false;
      }
    }catch (TimeoutSharedLockException timeout){
      setStatus(TIMEOUT);
      return false;
    }
  }

  @Override
  protected Logger getLogger() {
    return log;
  }


  /**
   * 支持自旋的 获取
   * @param time
   * @param unit
   * @return
   * @throws InterruptedException
   */
  protected boolean doAcquire(long time, TimeUnit unit) throws InterruptedException, TimeoutSharedLockException {
    long timeout = System.currentTimeMillis() + unit.toMillis(time);
    int times;
    int timesCount = 0;
    do {
      times = SPIN_TIMES;
      // 自旋 times 次
      for (; ;) {
        // 检查是否 cancel
        if (getStatus() == CANCEL) {
          return false;
        }
        checkTimeout(getKey(), timeout);
        // 如果获取成功则返回成功
        if (getProvider().doAcquire(this)){
          debugMessage(String.format(" %s次获取成功，自旋 %s 次", timesCount, SPIN_TIMES - times + 1));
          return true;
        }
        if (--times <= 0){
          break;
        }
        timesCount ++;
        debugMessage(String.format(" %s次获取失败，自旋 %s 次", timesCount, SPIN_TIMES - times));
      }
      // 随机休眠
      try {
        debugMessage(String.format(" %s次获取失败，自旋失败，开始随机休眠", timesCount));
        Thread.sleep(MIN_SLEEP_MILLS + (long) ((MAX_SLEEP_MILLS - MIN_SLEEP_MILLS) * Math.random()));
      } catch (Exception e) {
        return  false;
      }
    } while (true);
  }

  /**
   * 检查 超时
   * @param key 检查的锁 key
   * @param timeout 检查的时间
   * @throws TimeoutSharedLockException
   */
  private void checkTimeout(String key, long timeout) throws TimeoutSharedLockException {
    if (System.currentTimeMillis() >= timeout){
      throw new TimeoutSharedLockException(key);
    }
  }

  /**
   * 自旋次数
   */
  private static final int SPIN_TIMES = 3;

  /**
   * 最小休眠时间
   */
  private static final int MIN_SLEEP_MILLS = 100;

  /**
   * 最大休眠时间
   */
  private static final int MAX_SLEEP_MILLS = 500;
}
