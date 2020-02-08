package cn.madtiger.shared.lock;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import lombok.Builder;
import lombok.Builder.Default;

/**
 * 锁获取结果
 * @author Fenghu.Shi
 * @version 1.0
 */
@Builder
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
   * 返回的数据
   */
  T returnData;

  /**
   * 成功回调
   * @param callback
   * @return
   */
  public LockResultHolder doneAfter(DoCallback callback){
    if(isDone()) {callback.callback();}
    return this;
  }

  /**
   * 获取锁失败回调
   * @param callback
   * @return
   */
  public LockResultHolder timeoutAfter(DoCallback callback){
    if(isTimeout()) {callback.callback();}
    return this;
  }

  /**
   * 释放锁时失败，需要回滚
   * @param callback
   * @return
   */
  public LockResultHolder rollback(DoCallback callback){
    if(isRollback()) {callback.callback();}
    return this;
  }

  /**
   * 失败之后
   * @param callback
   * @return
   */
  public LockResultHolder failAfter(DoCallback callback){
    if(isFail()) {callback.callback();}
    return this;
  }

  /**
   * 是否完成
   * @return
   */
  public boolean isDone(){
    return status == DONE;
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

  @Override
  public void close() throws IOException {
    Objects.requireNonNull(sharedLock);
    sharedLock.release(this);
    sharedLock = null;
  }
}
