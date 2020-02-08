package org.shared.lock.demo;

import java.io.IOException;
import org.madtiger.shared.lock.ISharedLock;
import org.madtiger.shared.lock.LockResultHolder;
import org.madtiger.shared.lock.SpinSetLockArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * demo
 * @author Fenghu.Shi
 * @version 1.0
 */
@RestController
@RequestMapping("/demo")
public class DemoController {


  @Autowired
  private ISharedLock<SpinSetLockArgs> lockService;

  @Autowired
  private DemoService demoService;

  @GetMapping("/get")
  public Flux<String> get() throws IOException {
    try(LockResultHolder<Void> holder = lockService.tryLock("123123123", SpinSetLockArgs.builder().maxRetryTimes(5).build())) {
      if (holder.isLocking()){
        System.out.println("执行成功");
        return Flux.just("OK");
      }
      System.out.println("获取所超时");
      return Flux.error(new Throwable("失败了"));
    }
  }

  /**
   * aop 测试
   * @param param
   * @return
   */
  @GetMapping("/aop")
  public Flux<String> aop(@RequestParam String param){
    return Flux.just(demoService.testAopLock(param));
  }

}
