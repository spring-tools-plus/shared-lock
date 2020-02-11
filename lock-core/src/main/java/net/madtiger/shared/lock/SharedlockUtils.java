package net.madtiger.shared.lock;

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
}
