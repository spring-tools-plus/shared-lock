package cn.madtiger.shared.lock;

import lombok.Builder.Default;
import lombok.Getter;

/**
 * 用于设置 共享锁 参数
 * @author Fenghu.Shi
 * @version 1.0
 */
@Getter
public abstract class SetLockArgs {

  /**
   * 默认网络超时时间，毫秒
   */
  public static final int NET_TIMEOUT = 500;

  /**
   * 默认锁超时等待时间
   */
  public static final int WAIT_TIMEOUT_SECENDS = 2;

  /**
   * 默认int 值，{@link #maxRetryTimes} 和 {@link #lockedSeconds} 默认值均是此值
   */
  public static final int DEFAULT_INT = -1;

  /**
   * 最小休眠值
   */
  public static final int SLEEP_MIN_MILLS = 50;

  /**
   * 超时失败回调
   */
  @Default
  DoCallback timeoutCallback = DoCallback.NOOP;

  /**
   * 释放锁失败回调
   */
  @Default
  DoCallback rollbackCallback = DoCallback.NOOP;

  /**
   * 锁超等待超时时间，总的获取时间，该时间应该大于等于 {@link #getTimeoutMills} ，单位秒，默认2秒
   */
  @Default
  int waitTimeoutSeconds = WAIT_TIMEOUT_SECENDS;

  /**
   * 尝试次数，-1表示不限制，以 {@link #waitTimeoutSeconds} 为准
   */
  @Default
  int maxRetryTimes = DEFAULT_INT;

  /**
   * 锁定时间，单位秒，默认是 4 倍的{@link #waitTimeoutSeconds}锁时间
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
   * 从 锁发放服务器 获取锁的等待超时时间，单位毫秒，默认1000毫秒，比如大于 500
   */
  @Default
  int getTimeoutMills = NET_TIMEOUT;

  /**
   * 获取key锁定时间
   * @return
   */
  public int getLockedSeconds() {
    return lockedSeconds <= 0 ? 3 * waitTimeoutSeconds : lockedSeconds;
  }

}
