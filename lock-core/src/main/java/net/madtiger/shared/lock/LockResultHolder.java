package net.madtiger.shared.lock;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 锁获取结果
 * @author Fenghu.Shi
 * @version 1.0
 */
@Getter
@Slf4j
public class LockResultHolder<T> implements AutoCloseable {


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
  protected String key;

  /**
   * 锁定key对应的值
   */
  protected String value;

  /**
   * 状态
   */
  @Default
  protected int status = INIT;

  /**
   * 设置时的参数
   */
  protected SetLockArgs args;

  /**
   * redis 锁
   */
  protected ISharedLock sharedLock;

  /**
   * 扩展参数1
   */
  protected Object param1;

  /**
   * 扩展参数2
   */
  protected Object param2;

  /**
   * 回退函数
   */
  @Default
  private IDoCallback<T> rollback = null;

  /**
   * 获取到锁的时间
   */
  protected long lockedTime;

  /**
   * 结束时间，失败/成功都算
   */
  protected long finishedTime;

  /**
   * 返回的数据
   */
  protected T returnData;

  protected  LockResultHolder(){}

  @Builder
  public LockResultHolder(String key, String value, int status,
      SetLockArgs args, ISharedLock sharedLock, Object param1, Object param2,
      IDoCallback<T> rollback, long lockedTime, long finishedTime, T returnData) {
    this.key = key;
    this.value = value;
    this.status = status;
    this.args = args;
    this.sharedLock = sharedLock;
    this.param1 = param1;
    this.param2 = param2;
    this.rollback = rollback;
    this.lockedTime = lockedTime;
    this.finishedTime = finishedTime;
    this.returnData = returnData;
  }


  /**
   * 支持 降级 方式执行，此方法会在执行完 callback 方法后
   * @param callback 成功执行
   * @param fallback 获取锁失败执行函数
   * @return 如果 已经执行完，则返回 callback 值，否则返回 null
   * @throws Exception 异常
   */
  public T doFallback(IDoCallback<T> callback, IDoCallback<T> fallback) throws Exception{
    Objects.requireNonNull(callback);
    Objects.requireNonNull(fallback);
    if (!isLocking()){
      return fallback.callback();
    }
    // 开始执行
    returnData = callback.callback();
    return returnData;
  }

  /**
   * 释放失败，回调
   */
  void rollback(){}

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
   * 设置状态，子类必须使用这个方法设置状态
   * @param status
   * @return
   */
  LockResultHolder<T> status(int status){
    if (status == LOCKING){
      lockedTime = System.currentTimeMillis();
    } else if (status != INIT) {
      // 如果不是 初始化，都认为结束
      finishedTime = System.currentTimeMillis();
    }
    this.status = status;
    return this;
  }


  /**
   * 检查当前锁是否可用
   * @return
   */
  public boolean isAvaliable(){
    return isLocking() && args != null && (System.currentTimeMillis() - lockedTime > TimeUnit.SECONDS.toMillis(args.lockedSeconds)) ;
  }

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
  public static <T> LockResultHolder<T> newInstance(){
    return newInstance(null);
  }

  /**
   * 初始化
   * @param args 参数
   * @param <T> 返回值类型
   * @return
   */
  public static <T> LockResultHolder<T> newInstance(SetLockArgs args){
    return new LockResultHolderWrapper(args);
  }

  /**
   * 释放锁资源
   */
  public boolean unlock(){
    try {
      this.close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * 将当前对象复制到 目标对象
   * @param target 要复制到的对象
   */
  public void copyTo(LockResultHolder<T> target){
    Objects.requireNonNull(target);
    target.args = this.args;
    target.key = this.key;
    target.sharedLock = this.sharedLock;
    target.value = this.value;
    target.status(this.getStatus());
    target.param1 = this.param1;
    target.param2 = this.param2;
  }

  @Override
  public void close() throws IOException {
    try{
      // 检查是否已经释放
      if (sharedLock == null || !mybeRelease()){
        throwIOException();
      }
      if(!sharedLock.unlock(this)){
        throwIOException();
      }
      log.debug("{} 共享锁 {} 锁释放成功", sharedLock.getClass().getName(), getKey());
    }finally{
      sharedLock = null;
    }
  }

  /**
   * 抛出 IO 异常
   * @throws IOException
   */
  protected void throwIOException () throws IOException{
    throw new IOException(String.format("共享锁 %s 资源已被释放或不存在", key));
  }


}
