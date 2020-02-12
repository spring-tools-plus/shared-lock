package net.madtiger.shared.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 当前线程的 holder ，只有在 execute 执行方法块中有效
 * @author Fenghu.Shi
 * @version 1.0
 */
public class SharedLockContextHolder {

  private static final ThreadLocal<Map<String, LockResultHolder>> THREAD_HOLDERS = new ThreadLocal<Map<String, LockResultHolder>>(){
    @Override
    protected Map<String, LockResultHolder> initialValue() {
      return new HashMap<>(8);
    }
  };


  /**
   * 添加一个 holder
   * @param holder
   * @return
   */
  public static boolean add(LockResultHolder holder){
    Objects.requireNonNull(holder);
    if (THREAD_HOLDERS.get().containsKey(holder.getKey())){
      return false;
    }
    // 设置
    THREAD_HOLDERS.get().put(holder.getKey(), holder);
    return true;
  }

  /**
   * 获取
   * @param key
   * @return
   */
  public static LockResultHolder get(String key){
    Objects.requireNonNull(key);
    return THREAD_HOLDERS.get().get(key);
  }

  /**
   * 删除
   * @param key
   * @return
   */
  public static LockResultHolder remove(String key){
    return THREAD_HOLDERS.get().remove(key);
  }
}
