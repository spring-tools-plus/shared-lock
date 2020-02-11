package net.madtiger.shared.lock.configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    for (Map.Entry<String[], String> classes: CONFIGURATION_CLASSES.entrySet()){
      try{
        // 如果含有则初始化对应的 configuration
        for (String clazz : classes.getKey()) {
          Class.forName(clazz, false, getClass().getClassLoader());
        }
        log.debug("共享锁 {} 客户端加载成功", classes.getValue());
        return new String[] { classes.getValue() };
      }catch(Throwable ex){
        log.debug("共享锁 {} 客户端装载失败，没有引用相关依赖包", classes.getValue());
      }
    }
    return new String[0];
  }

  // 为了保证顺序，这里使用 linked hash map
  private static final Map<String[], String> CONFIGURATION_CLASSES = new LinkedHashMap<>();

  static {
    // 注册 redis 配置
    CONFIGURATION_CLASSES.put(new String[] {"net.madtiger.shared.lock.RedisLockService", "org.springframework.data.redis.core.RedisTemplate"}, "SharedLockRedisConfiguration");
    // 开始 注册 ZK
    // 优先  curator , 这里 curator 还有点问题等待处理再发布
//    CONFIGURATION_CLASSES.put(new String[] {"net.madtiger.shared.lock.ZookeeperLockService", "org.apache.curator.framework.CuratorFramework"}, "net.madtiger.shared.lock.configuration.SharedLockCuratorConfiguration");
    // 注册 zk
    CONFIGURATION_CLASSES.put(new String[] {"net.madtiger.shared.lock.ZookeeperLockService", "org.apache.zookeeper.ZooKeeper"}, "net.madtiger.shared.lock.configuration.SharedLockZKConfiguration");
  }
}
