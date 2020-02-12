package net.madtiger.shared.lock;

import java.util.function.Supplier;

/**
 * callback 函数
 * @param <T>
 * @author Fenghu.Shi
 * @version 1.0
 */
@FunctionalInterface
public interface IDoCallback<T> {


  /**
   * 执行函数
   * @return 执行结果
   * @throws Exception 异常
   */
  T callback() throws Exception;

}
