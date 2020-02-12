package net.madtiger.shared.lock;

import java.util.concurrent.TimeoutException;

/**
 *
 * 自旋锁基类
 * @author Fenghu.Shi
 * @version 1.0
 */
public abstract class AbsSpinLock extends AbsSharedLock {

  /**
   * 通过自旋锁形式获取锁
   * @param resultHolder 结果持有者，里面包含所有参数
   * @return 获取结果
   */
  @Override
  protected boolean tryAcquire(LockResultHolder resultHolder){
    SetLockArgs args = (SetLockArgs) resultHolder.args;
    args = args == null ? SetLockArgs.builder().build(): args;
    long timeout = System.currentTimeMillis() + args.waitTimeoutMills;
    int times = args.spinTimes;
    int timesCount = 0;
    try {
      do {
        times = args.spinTimes;
        // 自旋 times 次
        for (; ; ) {
          // 如果最大尝试次数大于1，则
          if (args.maxRetryTimes > 0 && timesCount >= args.maxRetryTimes){
            return false;
          }
          checkTimeout(resultHolder.key, timeout);
          // 如果获取成功则返回成功
          if (doAcquire(resultHolder)){
            return true;
          }
          if (--times <= 0){
            break;
          }
          timesCount ++;
        }
        // 随机休眠
        try {
          Thread.sleep(args.sleepMinMills + (long) ((args.sleepMaxMills - args.sleepMinMills) * Math.random()));
        } catch (Exception e) {
          return  false;
        }
      } while (true);
    }catch(TimeoutException ex){
      return false;
    }
  }

  /**
   * 检查 超时
   * @param key 检查的锁 key
   * @param timeout 检查的时间
   * @throws TimeoutException
   */
  private void checkTimeout(String key, long timeout) throws TimeoutException {
    if (System.currentTimeMillis() >= timeout){
      throw new TimeoutException(String.format("获取%s锁超时", key));
    }
  }

  /**
   * 参考 redis set nx 方法
   * @param resultHolder
   * @return
   */
  protected abstract boolean doAcquire(LockResultHolder resultHolder);
}
