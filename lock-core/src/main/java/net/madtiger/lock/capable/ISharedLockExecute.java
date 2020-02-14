package net.madtiger.lock.capable;

import java.util.concurrent.TimeUnit;
import net.madtiger.lock.IDoCallback;
import net.madtiger.lock.exception.TimeoutSharedLockException;
import org.springframework.lang.Nullable;

/**
 * 支持委托执行的锁
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public interface ISharedLockExecute {


  /**
   * 委托执行，支持降级和回滚，只尝试获取一次，失败立即返回
   * @param callback 获取锁成功执行的函数
   * @param faultCallback 获取所失败后降级参数，=null 时，获取失败将抛出 SharedLockTimeoutException
   * @param rollback 释放资源失败回退操作
   * @param <T> 返回值类型
   * @return 返回值
   * @throws TimeoutSharedLockException 获取锁失败
   * @throws Throwable 其他异常，包括业务异常等
   */
 default <T> T execute(IDoCallback<T> callback, @Nullable IDoCallback<T> faultCallback, @Nullable IDoCallback<T> rollback) throws TimeoutSharedLockException, Throwable {
   return execute(callback, faultCallback, rollback, -1, TimeUnit.SECONDS);
 }


  /**
   * 委托执行，支持降级，只尝试获取一次，失败立即返回
   * @param callback 获取所成功执行的函数
   * @param faultCallback 获取所失败后降级参数，=null 时，获取失败将抛出 LockTimeoutException
   * @param <T> 返回值类型
   * @return 返回值
   * @throws TimeoutSharedLockException  获取锁失败
   * @throws Throwable 其他异常，包括业务异常等
   */
  default <T> T execute(IDoCallback<T> callback, @Nullable IDoCallback<T> faultCallback)  throws TimeoutSharedLockException, Throwable{
    return execute(callback, faultCallback, null);
  }



  /**
   * 委托执行，支持降级和回滚，如果获取所失败，则抛出异常，只尝试获取一次，失败立即返回
   * @param callback 获取锁成功执行的函数
   * @param <T> 返回值类型
   * @return 返回值
   * @throws TimeoutSharedLockException 获取锁失败
   * @throws Throwable 其他异常
   */
  default <T> T execute(IDoCallback<T> callback)  throws TimeoutSharedLockException, Throwable{
    return execute(callback, null);
  }

  /**
   * 委托执行，支持降级和回滚，等待特定时间段，如果超时则失败
   * @param callback 获取锁成功执行的函数
   * @param faultCallback 获取所失败后降级参数，=null 时，获取失败将抛出 SharedLockTimeoutException
   * @param rollback 释放资源失败回退操作
   * @param time 等待时间
   * @param unit 时间单位
   * @param <T> 返回值类型
   * @return 返回值
   * @throws TimeoutSharedLockException 获取锁失败
   * @throws Throwable 其他异常，包括业务异常等
   */
  <T> T execute(IDoCallback<T> callback, @Nullable IDoCallback<T> faultCallback, @Nullable IDoCallback<T> rollback, int time, TimeUnit unit) throws TimeoutSharedLockException, Throwable;


  /**
   * 委托执行，支持降级和回滚，等待特定时间段，如果超时则失败
   * @param callback 获取锁成功执行的函数
   * @param faultCallback 获取所失败后降级参数，=null 时，获取失败将抛出 SharedLockTimeoutException
   * @param time 等待时间
   * @param unit 时间单位
   * @param <T> 返回值类型
   * @return 返回值
   * @throws TimeoutSharedLockException 获取锁失败
   * @throws Throwable 其他异常，包括业务异常等
   */
  default <T> T execute(IDoCallback<T> callback, @Nullable IDoCallback<T> faultCallback, int time, TimeUnit unit) throws TimeoutSharedLockException, Throwable {
    return execute(callback, faultCallback, null, time, unit);
  }

  /**
   * 委托执行，支持降级和回滚，等待特定时间段，如果超时则失败
   * @param callback 获取锁成功执行的函数
   * @param time 等待时间
   * @param unit 时间单位
   * @param <T> 返回值类型
   * @return 返回值
   * @throws TimeoutSharedLockException 获取锁失败
   * @throws Throwable 其他异常，包括业务异常等
   */
  default <T> T execute(IDoCallback<T> callback, int time, TimeUnit unit) throws TimeoutSharedLockException, Throwable {
    return execute(callback, time, unit);
  }

}
