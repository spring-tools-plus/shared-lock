package net.madtiger.lock.redis;

import lombok.extern.slf4j.Slf4j;
import net.madtiger.lock.SharedlockUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

/**
 * 基于spring 的共享锁客户端
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class RedisLockClient {

  protected final RedisTemplate<String, String> redisTemplate;

  /**
   * 创建一个 redis lock client
   * @param redisTemplate
   */
  public RedisLockClient(RedisTemplate<String, String> redisTemplate){
    this.redisTemplate = redisTemplate;
  }

  /**
   * 获取 get 对应的数据
   * https://redis.io/commands/get
   * @param key 锁的key
   * @return
   */
  public <T> String get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  /**
   * 调用 set nx 接口
   * https://redis.io/commands/set
   * @param key 锁 key
   * @param value 锁的值，一般是UUID
   * @param lockSeconds 锁定时长，单位秒
   * @return 设置结果
   */
  public <T> boolean setNX(String key, String value, int lockSeconds) {
    return redisTemplate.execute((RedisConnection connection) -> {
      try {
       if (connection.set(SharedlockUtils.stringToBytes(key), SharedlockUtils.stringToBytes(value), Expiration.seconds(lockSeconds), SetOption.SET_IF_ABSENT)) {
         log.debug("spring data redis -> {} 获取锁{}数据成功", key, value);
         return true;
       }
      } catch (Exception e) {
        log.error("spring data redis -> {} 锁获取超时", key, e);
      }
      return false;
    });
  }

  /**
   * 通过Lua脚本释放锁 https://redis.io/commands/eval
   *
   * @param luaScript lua脚本
   * @param key 锁 key * @param value 锁的值，一般是UUID
   * @return
   */
  public <T> boolean releaseByLua(String luaScript, String key, String value) {
    return redisTemplate.execute((RedisConnection connection) -> {
      try {
        if (connection.eval(SharedlockUtils.stringToBytes(luaScript), ReturnType.BOOLEAN, 1, SharedlockUtils.stringToBytes(key), SharedlockUtils.stringToBytes(value))) {
          log.debug("spring data redis -> {} 释放锁成功", key);
          return true;
        }
      } catch (Exception e) {
        log.error("spring data redis -> {} 释放锁超时", key, e);
      }
      return false;
    });
  }

  /**
   * 删除 key
   * https://redis.io/commands/del
   * @param key 锁 key
   * @return
   */
  public <T> boolean delete(String key) {
    return redisTemplate.delete(key);
  }

}
