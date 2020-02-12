package net.madtiger.shared.lock;

import static net.madtiger.shared.lock.SharedLock.DEFAULT_INT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 将  用于处理 含有 {@link SharedLock} 注解的方法
 * @author Fenghu.Shi
 * @version 1.0
 * @see SetLockArgs
 */
public final class AnnotationProcess {


  /**
   * bean 容器
   */
   private BeanFactory beanFactory;

   @Autowired
   public AnnotationProcess(BeanFactory beanFactory){
     Objects.requireNonNull(beanFactory);
      this.beanFactory = beanFactory;
   }

  /**
   * 执行
   * @param jp 切入点
   * @return 执行后的数据
   * @throws Throwable 异常
   */
  public final Object handle(ProceedingJoinPoint jp) throws Throwable {
    MethodSignature signature = (MethodSignature) jp.getSignature();
    // 解析 ShareLock 注解
    SharedLock shareLockAnnotation = signature.getMethod().getAnnotation(SharedLock.class);
    // 如果不存在，则直接执行
    if (shareLockAnnotation == null) {
      return jp.proceed();
    }
    // 处理
    return lockExecute(jp, shareLockAnnotation);
  }


  /**
   * 锁执行
   * @param jp 切入点
   * @param shareLockAnnotation 注解实例
   * @return 执行结果
   * @throws Throwable  其他异常
   */
  private Object lockExecute(ProceedingJoinPoint jp, SharedLock shareLockAnnotation) throws Throwable  {
    MethodSignature signature = (MethodSignature) jp.getSignature();
    // 解析参数
    String[] parameterNames = signature.getParameterNames();
    Map<String,Object> params = new HashMap<>(8);
    Object[] args = jp.getArgs();
    if (parameterNames != null && parameterNames.length > 0){
      for (int i = 0;i < parameterNames.length;i ++) {
        params.put(parameterNames[i], args[i]);
      }
    }

    // 获取 sharedlock
    ISharedLock lockService = getShareLock(shareLockAnnotation);
    Objects.requireNonNull(lockService);
    LockResultHolder<Object> holder = null;
    // try finally 方式执行
    try(LockResultHolder<Object> resultHolder = lockService.tryLock(
        buildKey(shareLockAnnotation, jp.getTarget(), signature.getMethod(), params),
        buildArgs(shareLockAnnotation, jp.getTarget(), signature.getMethod(), args)
    )) {
      // 开始执行吧
      Object resultObject = doExecute(resultHolder, jp, shareLockAnnotation);
      holder = resultHolder;
      holder.returnData = resultObject;
    }
    // 如果有值，且释放成功
    if (holder.isDone()){
      return holder.returnData;
    }
    // 是否需要 rollback
    if (holder != null && holder.isRollback()){
      // 如果未设置 rollback 则返回数据
      if (SharedLock.DEFAULT_METHOD.equals(shareLockAnnotation.rollbackMethod())){
        return holder.returnData;
      }
      Object rollbackData;
      // 回滚数据
      if ( (rollbackData = rollback(jp, shareLockAnnotation)) != NOOP){
        holder.returnData = rollbackData;
        return rollbackData;
      }else {
        // 如果没有返回数据
        return holder.returnData;
      }
    }

    // 其他全算异常
    throw ClassUtils.newInstance(shareLockAnnotation.throwable());
  }

  /**
   * 根据 锁的状态执行
   * @param resultHolder
   * @param jp
   * @param shareLockAnnotation
   * @return
   * @throws Throwable
   */
  private Object doExecute(LockResultHolder<Object> resultHolder, ProceedingJoinPoint jp, SharedLock shareLockAnnotation) throws Throwable{
    MethodSignature signature = (MethodSignature) jp.getSignature();
    // 如果获取所成功或者失败继续执行
    if (resultHolder.isLocking()) {
      return jp.proceed();
    }
    // 开始处理失败情况
    // 计算失败规则
    FaultPolicy policy = shareLockAnnotation.faultPolicy() == FaultPolicy.AUTO && !SharedLock.DEFAULT_METHOD.equals(shareLockAnnotation.fallbackMethod()) ? FaultPolicy.REPLACE : shareLockAnnotation.faultPolicy();
    // 1、啥也不做
    if (policy == FaultPolicy.DO_NOTHING){
      return faultDoNothing(signature, shareLockAnnotation);
    }

    // 2、继续
    if (policy == FaultPolicy.CONTINUE){
      return jp.proceed();
    }

    // 3、回退
    if (policy == FaultPolicy.REPLACE) {
      return faultCallback(jp, shareLockAnnotation.fallbackMethod());
    }
    return NOOP;
  }

  /**
   * 啥也不执行时的回调 这里特别注意，如果返回值类型是 基本类型且非包装器类型
   * @param signature
   * @param shareLockAnnotation
   * @return
   */
  private Object faultDoNothing(MethodSignature signature, SharedLock shareLockAnnotation){
    return signature.getReturnType().isPrimitive() && Number.class.isAssignableFrom(signature.getReturnType()) ? SharedLock.FAULT_NUMBER_DEFAULT : null;
  }

  /**
   * 回退处理
   * @param jp
   * @param methodName
   * @return
   */
  private Object faultCallback(ProceedingJoinPoint jp, String methodName)
      throws InvocationTargetException, IllegalAccessException {
    MethodSignature signature = (MethodSignature) jp.getSignature();
    // 获取回退方法
    if (StringUtils.isEmpty(methodName)) {
      throw new UnsupportedOperationException(String.format("%s 方法的SharedLock注解未定义 faultMethod 属性", signature.getMethod()));
    }
    try{
      // 获取方法
      Method method = jp.getTarget().getClass().getMethod(methodName, signature.getParameterTypes());
      // 回退方法
      if (!signature.getReturnType().isAssignableFrom(method.getReturnType())) {
        throw new UnsupportedOperationException(String.format("%s 方法的SharedLock注解定义 faultMethod=%s 方法签名错误", signature.getMethod(), methodName));
      }
      // 执行回退
      return method.invoke(jp.getTarget(), jp.getArgs());
    }catch (NoSuchMethodException ex) {
      throw new UnsupportedOperationException(String.format("%s 方法的SharedLock注解定义 faultMethod=%s 方法签名错误", signature.getMethod(), methodName));
    }
  }


  /**
   * 执行回滚
   * @param jp
   * @param shareLockAnnotation
   */
  private Object rollback(ProceedingJoinPoint jp, SharedLock shareLockAnnotation)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    MethodSignature signature = (MethodSignature) jp.getSignature();
    Method method = jp.getTarget().getClass().getMethod(shareLockAnnotation.rollbackMethod(), signature.getParameterTypes());
    if (method.getReturnType() != null && method.getReturnType().isAssignableFrom(((MethodSignature) jp.getSignature()).getReturnType())){
      return method.invoke(jp.getTarget(), jp.getArgs());
    } else {
      return NOOP;
    }
  }

  /**
   * 获取 共享锁服务
   * @param lock
   * @return
   */
  private ISharedLock getShareLock(SharedLock lock){
    // 默认，则根据类型获取
    if (SharedLock.DEFAULT_PROVIDER.equals(lock.provider()) || StringUtils.isEmpty(lock.provider())) {
      return beanFactory.getBean(ISharedLock.class);
    }

    // 通过名称获取
    return (ISharedLock) beanFactory.getBean(lock.provider());
  }

  /**
   * 将 lock 和 args 转换成 LockArgs
   * @param lock
   * @param target
   * @param method
   * @param args
   * @return
   */
  private SetLockArgs buildArgs(SharedLock lock, Object target, Method method, Object[] args){
    SetLockArgs.SetLockArgsBuilder builder = SetLockArgs.builder();


    // 锁定时间
    if (lock.lockedSeconds() != DEFAULT_INT) {
      builder.lockedSeconds(lock.lockedSeconds());
    }

    // 最大尝试次数
    if (lock.maxRetryTimes() != DEFAULT_INT) {
      builder.maxRetryTimes(lock.maxRetryTimes());
    }

    // 最大等待时间
    if (lock.waitTimeoutMills() != DEFAULT_INT) {
      builder.waitTimeoutMills(lock.waitTimeoutMills());
    }

    // 自旋次数
    if (lock.spinTimes() != DEFAULT_INT) {
      builder.spinTimes(lock.spinTimes());
    }
    // sleep max mills
    if (lock.sleepMaxMills() != DEFAULT_INT) {
      builder.sleepMaxMills(lock.sleepMaxMills());
    }

    // 最小 mills
    if (lock.sleepMinMills() != DEFAULT_INT) {
      builder.sleepMinMills(lock.sleepMinMills());
    }

    return builder.build();
  }

  /**
   * 生成 key
   * @param lock
   * @param target
   * @param method
   * @param args
   * @return
   */
  private String buildKey(SharedLock lock, Object target, Method method, Map<String, Object> args){
    String key = StringUtils.isEmpty(lock.key()) ? lock.value() : lock.key();
    // 解析 key
    // 如果是默认的，则直接生成
    if (SharedLock.DEFAULT_METHOD.equals(key)){
      return method.toString();
    }
    // 生成
    return parseKeyVariables(key, args);
  }


  /**
   * 解析 key 中的 变量
   * @param key
   * @param args
   * @return
   */
  private static String parseKeyVariables(String key, Map<String, Object> args){
    if (args == null || args.isEmpty()){
      return key;
    }
    // 开始逐个参数处理
    for (Map.Entry<String, Object> arg : args.entrySet()) {
      key = key.replaceAll(String.format("\\#\\{%s\\}", arg.getKey()), arg.getValue() == null ? "" : arg.getValue().toString());
    }
    return key;
  }


  private static final Object NOOP = new Object();
}
