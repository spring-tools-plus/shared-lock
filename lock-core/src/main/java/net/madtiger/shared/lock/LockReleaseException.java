package net.madtiger.shared.lock;

import java.io.IOException;

/**
 * 释放资源失败异常
 * @author Fenghu.Shi
 * @version 1.0
 */
public class LockReleaseException extends IOException {
  public LockReleaseException() {}
}
