package org.shared.lock.demo;

import lombok.extern.slf4j.Slf4j;
import net.madtiger.shared.lock.SharedLock;
import org.springframework.stereotype.Service;

/**
 * 样例
 * @author Fenghu.Shi
 * @version 1.0
 */
@Service
@Slf4j
public class DemoService {


  /**
   * 执行入口
   * @param abc
   * @return
   */
  @SharedLock(key ="demo-test-#{abc}", fallbackMethod = "faultBack", rollbackMethod = "rollback")
  public String testAopLock(String abc){
      log.error("我获取到锁了");
      return "这是我得知";
  }

  /**
   * 失败降级方法
   * @param bac
   * @return
   */
  public String faultBack(String bac){
    log.error("我被降级了");
    return "降级哈哈";
  }

  /**
   * 回滚方法
   * @param abc
   */
  public String rollback(String abc){
    System.out.println("回滚了");
    return "我被回滚了";
  }
}
