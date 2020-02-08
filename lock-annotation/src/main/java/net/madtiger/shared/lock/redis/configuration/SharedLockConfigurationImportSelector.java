package net.madtiger.shared.lock.redis.configuration;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 共享锁 配置导入类
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class SharedLockConfigurationImportSelector implements ImportSelector {


  @Override
  public String[] selectImports(AnnotationMetadata importingClassMetadata) {
    // 逐个加载
    for (Map.Entry<String, String> clazz: CONFIGURATION_CLASSES.entrySet()){
      try{
        // 如果含有则初始化对应的 configuration
        Class.forName(clazz.getKey(), false, getClass().getClassLoader());
        log.debug("共享锁 {} 客户端加载成功", clazz.getValue());
        return new String[] { clazz.getValue() };
      }catch(Throwable ex){
        log.debug("共享锁 {} 客户端装载失败，没有引用相关依赖包", clazz.getValue());
      }
    }
    return new String[0];
  }


  private static final Map<String, String> CONFIGURATION_CLASSES = new HashMap<>();

  static {
    // 注册 redis 配置
    CONFIGURATION_CLASSES.put("org.springframework.data.redis.core.RedisTemplate", "net.madtiger.shared.lock.redis.configuration.SharedLockRedisConfiguration");
  }
}
