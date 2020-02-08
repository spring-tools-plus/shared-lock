package cn.madtiger.shared.lock;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

/**
 * 自旋锁参数
 * @author Fenghu.Shi
 * @version 1.0
 */
@Getter
public class SpinSetLockArgs extends SetLockArgs{

  /**
   * 每次自旋的次数
   */
  @Default
  int spinTimes = 3;

  @Builder
  public SpinSetLockArgs( DoCallback timeoutCallback, DoCallback rollbackCallback,
      int waitTimeoutSeconds, int maxRetryTimes, int lockedSeconds, int sleepMinMills,
      int sleepMaxMills, int getTimeoutMills, int spinTimes) {
    super();
    // 设置 最大 时间
    this.sleepMaxMills = this.sleepMinMills * 4;
    this.lockedSeconds = this.waitTimeoutSeconds * 4;
  }
}
