package net.madtiger.shared.lock;

import java.util.List;
import java.util.Objects;
import net.madtiger.shared.lock.decorator.AbsSharedLockDecorator;
import org.springframework.util.CollectionUtils;

/**
 * 共享锁 工具
 * @author Fenghu.Shi
 * @version 1.0
 */
public class SharedlockUtils {


  /**
   * 字符串转换成字符串
   * @param str
   * @return
   */
  public static byte[] stringToBytes(String str){
    try{
      return str == null ? null : str.getBytes("UTF8");
    }catch (Throwable ex){
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * 装饰 共享锁服务
   * @param sharedLock 要包装的 服务
   * @param classes 包装器类
   * @return
   */
  public static ISharedLock newDecorators(ISharedLock sharedLock, List<Class<AbsSharedLockDecorator>> classes){
      Objects.requireNonNull(sharedLock);
      if (CollectionUtils.isEmpty(classes)) {
        return sharedLock;
      }
      //逐个安装
      for (Class<AbsSharedLockDecorator>  decoratorClass : classes) {
        try {
          sharedLock = decoratorClass.getConstructor(ISharedLock.class).newInstance(sharedLock);
        } catch (Exception e) {
          throw new IllegalArgumentException(e);
        }
      }
      return  sharedLock;
  }
}
