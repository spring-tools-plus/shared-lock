package org.shared.lock.demo;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.LockResultHolder;
import net.madtiger.shared.lock.SetLockArgs;
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
@Slf4j
public class DemoController {


  @Autowired
  private ISharedLock lockService;

  @Autowired
  private DemoService demoService;

  /**
   * try-rollback 模式
   * @return
   * @throws IOException
   */
  @GetMapping("/try-rollback")
  public Flux<String> doTryRollback() throws Exception {
//     curatorFramework.create().forPath("/test/db", "aaaa".getBytes());
//     return Flux.just(zooKeeper.create("/test", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT));
//    return Flux.just("OK");
      return null;
  }

  /**
   * try 模式
   * @return
   * @throws IOException
   */
  @GetMapping("/try")
  public Flux<String> doTry() throws IOException {

    // 来一个基本模式
    // 1. 初始化一个 所持有对象，这个对象不能重复使用
//    LockResultHolder<Void> tryHolder = LockResultHolder.newInstance();
//    try{
//      // 2. 尝试获取所并判断是否锁定成功
//      if (lockService.tryLockGet("123123123", tryHolder)){
//        // 获取锁成功
//        log.error("获取锁成功");
//      }else {
//        // 获取锁失败
//        System.out.println("获取锁失败");
//        log.error("获取锁失败");
//      }
//    } finally {
//      // 3. 释放锁
//      if (lockService.unlock(tryHolder)){
//        System.out.println("释放锁成功");
//      }else {
//        System.out.println("释放锁失败");
//      }
//    }

    // 来一个 try-with-resource 模式
    try(LockResultHolder<Flux<String>> holder = lockService.tryLock("12312355123", SetLockArgs.builder().maxRetryTimes(5).build())) {
      if (holder.isLocking()){
        // 支持降级的处理
       return holder.doFallback(() -> {
          System.out.println("执行成功");
         return Flux.just("OK");
        }, () -> {
          return Flux.just("执行回退");
        }, () -> {
         // 这里如果返回 null，则不会覆盖 上面的 数据
         return Flux.just("执行回滚");
       });
      }
      System.out.println("获取所超时");
      return Flux.error(new Throwable("失败了"));
    }
  }


  /**
   * execute 模式
   * @return
   * @throws TimeoutException
   */
  @GetMapping("/execute")
  public Flux<String> doExecute() throws TimeoutException {
    return lockService.executeGet("do-execute-key", () -> {
      return Flux.just("获取到锁了");
    }, () -> {
      return Flux.error(new Throwable("获取所失败"));
    });
  }

  /**
   * aop 测试
   * @param param
   * @return
   */
  @GetMapping("/aop")
  public Flux<String> doAop(@RequestParam String param){
    return Flux.just(demoService.testAopLock(param));
  }

}
