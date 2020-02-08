package cn.madtiger.shared.lock.redis.configuration;

import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import cn.madtiger.shared.lock.redis.AnnotationProcess;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared Lock spring  aop 拦截器，拦截相关的方法，并处理
 * @author Fenghu.Shi
 * @version 1.0
 */
@Aspect
public class SharedLockInterceptor {


  private AnnotationProcess annotationProcess;


  @Autowired
  public SharedLockInterceptor(BeanFactory beanFactory){
    Objects.requireNonNull(beanFactory);
    annotationProcess = new AnnotationProcess(beanFactory);
  }

  /**
   * 环绕执行
   * @param point
   * @return
   * @throws Throwable
   */
  @Around("@annotation(cn.madtiger.shared.lock.redis.SharedLock)")
  public Object handle(ProceedingJoinPoint point) throws Throwable{
    return annotationProcess.handle(point);
  }

}
