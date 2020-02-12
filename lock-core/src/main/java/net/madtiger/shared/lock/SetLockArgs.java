package net.madtiger.shared.lock;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

/**
 * 用于设置 共享锁 参数
 * @author Fenghu.Shi
 * @version 1.0
 */
@Getter
@Builder
public class SetLockArgs {

  /**
   * 默认锁超时等待时间
   */
  public static final int WAIT_TIMEOUT_SECENDS = 5;

  /**
   * 默认int 值，{@link #maxRetryTimes} 和 {@link #lockedSeconds} 默认值均是此值
   */
  public static final int DEFAULT_INT = -1;

  /**
   * 最小休眠值
   */
  public static final int SLEEP_MIN_MILLS = 50;

  /**
   * 锁超等待超时时间，总的获取时间
   */
  @Default
  int waitTimeoutMills = WAIT_TIMEOUT_SECENDS * 1000;

  /**
   * 尝试次数，-1表示不限制，以 {@link #waitTimeoutMills} 为准
   */
  @Default
  int maxRetryTimes = DEFAULT_INT;

  /**
   * 每次自旋的次数
   */
  @Default
  int spinTimes = 3;

  /**
   * 锁定时间，单位秒，默认是 4 倍的{@link #waitTimeoutMills}锁时间
   */
  @Default
  int lockedSeconds = DEFAULT_INT;

  /**
   * 每次休眠最小毫秒数
   */
  @Default
  int sleepMinMills = SLEEP_MIN_MILLS;

  /**
   * 每次休眠最大毫秒数，默认是 {@link #sleepMinMills} 4 倍
   */
  @Default
  int sleepMaxMills = DEFAULT_INT;

  /**
   * 获取key锁定时间，单位秒
   * @return lock 时间
   */
  public int getLockedSeconds() {
    return lockedSeconds <= 0 ? 4 * waitTimeoutMills / 1000 : lockedSeconds;
  }


  /**
   * 创建一个 默认的参数
   * @return
   */
  public static SetLockArgs buildDefaultArgs(){
    return SetLockArgs.builder().build();
  }

}
