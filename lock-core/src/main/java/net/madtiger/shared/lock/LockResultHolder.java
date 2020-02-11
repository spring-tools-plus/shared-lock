package net.madtiger.shared.lock;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.SetLockArgs;

/**
 * 锁获取结果
 * @author Fenghu.Shi
 * @version 1.0
 */
@Builder
@Getter
public class LockResultHolder<T> implements Closeable {


  /**
   * 成功
   */
  public static final int DONE = 2;


  /**
   * 初始化
   */
  public static final int INIT = 0;

  /**
   * 锁定中
   */
  public static final int LOCKING = 1;

  /**
   * 获取超时
   */
  public static final int TIMEOUT = -1;

  /**
   * 回滚
   */
  static final int ROLLBACK = -2;

  /**
   * 锁定的key
   */
  String key;

  /**
   * 锁定key对应的值
   */
  String value;

  /**
   * 状态
   */
  @Default
  int status = INIT;

  /**
   * 设置时的参数
   */
  SetLockArgs args;

  /**
   * redis 锁
   */
  ISharedLock sharedLock;

  /**
   * 扩展参数1
   */
  Object param1;

  /**
   * 扩展参数2
   */
  Object param2;

  /**
   * 回退函数
   */
  @Default
  private Supplier<T> rollback = null;

  /**
   * 返回的数据
   */
  T returnData;

  /**
   * 支持正常降级方式执行
   * @param callback 成功执行
   * @param fallback 失败执行(不包括 释放锁失败)
   * @return
   */
  public T doFallback(Supplier<T> callback, Supplier<T> fallback){
      return doFallback(callback, fallback, null);
  }

  /**
   * 支持 降级和回滚 方式执行，此方法会在执行完callback 方法后，立即释放资源，再执行 rollback（如果需要），故必须保证 callback 方法执行完后可以立即释放锁。
   * @param callback 成功执行
   * @param fallback 获取所失败执行函数
   * @param rollback 回滚执行
   * @return 如果 已经执行完，则返回 callback 值，否则返回 null
   */
  public T doFallback(Supplier<T> callback, Supplier<T> fallback, Supplier<T> rollback){
    Objects.requireNonNull(callback);
    Objects.requireNonNull(fallback);
    if (!isLocking()){
      return fallback.get();
    }
    // 开始执行
    returnData = callback.get();
    // 开始释放锁
    try {
      this.close();
    } catch (IOException e) {
      status = ROLLBACK;
      rollback();
    }
    // 如果是释放失败，则回退
    if(isRollback() && rollback != null) {
      T rollbackData = rollback.get();
      // 如果 回退返回值!= null ，则覆盖
      if (rollbackData != null){
        returnData = rollbackData;
      }
    }
    return returnData;
  }

  /**
   * 释放失败，回调
   */
  void rollback(){

  }

  /**
   * 是否完成
   * @return
   */
  public boolean isDone(){
    return status == DONE;
  }

  /**
   * 是否可能需要释放资源
   * @return
   */
  public boolean mybeRelease(){
    return status == LOCKING || status == TIMEOUT;
  }

  /**
   * 是否超时
   * @return
   */
  public boolean isTimeout(){
    return status == TIMEOUT;
  }

  /**
   * 是否需要回退
   * @return
   */
  public boolean isRollback(){
    return status == ROLLBACK;
  }

  /**
   * 是否锁定中
   * @return
   */
  public boolean isLocking(){ return status == LOCKING; }

  /**
   * 是否失败
   * @return
   */
  public boolean isFail(){ return isRollback() || isTimeout();}


  /**
   * 获取返回数据
   * @return
   */
  public T returnData(){
    return returnData;
  }


  /**
   * 初始化
   * @param <T>
   * @return
   */
  public static <T, K extends SetLockArgs> LockResultHolder<T> newInstance(){
    return newInstance(null);
  }

  /**
   * 初始化
   * @param args 参数
   * @param <T>
   * @return
   */
  public static <T, K extends SetLockArgs> LockResultHolder<T> newInstance(K args){
    return (LockResultHolder<T>) LockResultHolder.builder().status(INIT).args(args).build();
  }

  @Override
  public void close() throws IOException {
    // 检查是否已经释放
    if (sharedLock == null || !mybeRelease()){
      return;
    }
    sharedLock.unlock(this);
    sharedLock = null;
  }
}
