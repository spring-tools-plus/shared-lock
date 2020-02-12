package org.shared.lock.demo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import net.madtiger.shared.lock.ISharedLock;
import net.madtiger.shared.lock.LockReleaseException;
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

  private static final String LOCK_KEY = "lock-test-key";


  /**
   * 可重入锁测试
   * @return
   * @throws Exception
   */
  @GetMapping("/do-reentrant")
  public Flux<String> doReentrant() throws Exception{
    log.error("来来");
    try(LockResultHolder<Flux<String>> holder = lockService.tryLock(LOCK_KEY, SetLockArgs.builder().maxRetryTimes(5).build())) {
      // 支持降级的处理
      return holder.doFallback(() -> {
        try(LockResultHolder<Flux<String>> holder2 = lockService.tryLock(LOCK_KEY)) {
          // 支持降级的处理
          return holder2.doFallback(() -> {
            System.out.println("执行成功");
            return Flux.just("OK");
          }, () -> {
            return Flux.error(new Throwable("失败了"));
          });
        }
      }, () -> {
        // 这里可以执行回退或者异常检查
        return Flux.error(new Throwable("失败了"));
      });
    }
  }


  /**
   * try-rollback 模式
   * @return
   * @throws IOException
   */
  @GetMapping("/try-basic")
  public Flux<String> doTry() throws Exception {
    // 来一个 try-with-resource 模式
    try(LockResultHolder<Flux<String>> holder = lockService.tryLock(LOCK_KEY, SetLockArgs.builder().maxRetryTimes(5).build())) {
      // 支持降级的处理
      return holder.doFallback(() -> {
        System.out.println("执行成功");
        return Flux.just("OK");
      }, () -> {
        // 这里可以执行回退或者异常检查
        return Flux.error(new Throwable("失败了"));
      });
    }
  }

  /**
   * try 模式
   * @return
   * @throws IOException
   */
  @GetMapping("/try-rollback")
  public Flux<String> doRollback() throws IOException {
    // 来一个基本模式
    LockResultHolder<Void> tryHolder = LockResultHolder.newInstance();
    Flux<String> result;
    try{
      // 尝试获取所并判断是否锁定成功
      if (lockService.tryLockGet(LOCK_KEY, 10, TimeUnit.SECONDS, tryHolder)){
        result = Flux.just("获取锁成功");
      }else {
        result = Flux.just("获取锁失败");
      }
    } finally {
      // 3. 释放锁
      try {
        lockService.unlock(tryHolder);
      } catch (LockReleaseException e) {
        result = Flux.just("回滚吧");
      }
    }
    return result;
  }


  /**
   * 使用 try-with-resource 模式
   * @return
   * @throws IOException
   */
  @GetMapping("/try-with-resource")
  public Flux<String> doTryWithResource() throws IOException {
    try(LockResultHolder<Void> tryHolder = lockService.tryLock(LOCK_KEY, 10, TimeUnit.SECONDS)){
      if (tryHolder.isLocking()){
        return Flux.just("获取所成功");
      }else {
        return Flux.just("获取所失败");
      }
    }
  }


  /**
   * execute 模式
   * @return
   * @throws TimeoutException
   */
  @GetMapping("/do-execute")
  public Flux<String> doExecute() throws TimeoutException {
    return lockService.executeGet(LOCK_KEY, () -> {
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
  @GetMapping("/do-aop")
  public Flux<String> doAop(@RequestParam("a") String param){
    return Flux.just(demoService.testAopLock(param));
  }

}
