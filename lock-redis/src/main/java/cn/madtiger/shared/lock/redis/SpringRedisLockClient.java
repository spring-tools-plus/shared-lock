package cn.madtiger.shared.lock.redis;

import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
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
public class SpringRedisLockClient implements  LockRedisClient{


  protected final RedisTemplate<String, String> redisTemplate;

  public SpringRedisLockClient(RedisTemplate<String, String> redisTemplate){
    this.redisTemplate = redisTemplate;
  }

  @Override
  public String get(String key, int redisTimeoutMills) {
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public boolean setNX(String key, String value, int redisTimeoutMills, long lockedSeconds) {
    return redisTemplate.execute((RedisConnection connection) -> {
      try {
       if (connection.set(stringToBytes(key), stringToBytes(value), Expiration.seconds(lockedSeconds), SetOption.SET_IF_ABSENT)) {
         log.debug("spring data redis -> {} 获取锁{}数据成功", key, value);
         return true;
       }
      } catch (Exception e) {
        log.error("spring data redis -> {} 锁获取超时", key, e);
      }
      return false;
    });
  }

  @Override
  public boolean releaseByLua(String luaScript, String key, String value, int redisTimeoutMills) {
    return redisTemplate.execute((RedisConnection connection) -> {
      try {
        if (connection.eval(stringToBytes(luaScript), ReturnType.BOOLEAN, 1, stringToBytes(key), stringToBytes(value))) {
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
   * 将字符串转换成字节码
   * @param str
   * @return
   */
  protected byte[] stringToBytes(String str){
    str = str == null ? "" : str;
    try {
      return str.getBytes("UTF8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public boolean delete(String key, int redisTimeoutMills) {
    return redisTemplate.delete(key);
  }

}
