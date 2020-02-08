package cn.madtiger.shared.lock.redis;

/**
 * redis 的 客户端接口定义
 * @author Fenghu.Shi
 * @version 1.0
 */
public interface LockRedisClient {

  /**
   * 将key 的值设为value ，当且仅当key 不存在，等效于 SETNX。
   */
  static final String NX = "NX";

  /**
   * 失效时间 设置
   */
  static final String EX = "EX";

  /**
   * 调用set后的返回值
   */
  static final String OK = "OK";
  /**
   * 调用 set nx 接口
   * https://redis.io/commands/set
   * @param key 锁标示
   * @param value 锁对应的值，一般是 uuid
   * @param redisTimeoutMills redis 操作超时时间，单位毫秒
   * @param lockedSeconds 锁定该key的时间，单位秒
   * @return 设置结果
   */
  boolean setNX(String key, String value, int redisTimeoutMills, long lockedSeconds);


  /**
   * 获取 get 对应的数据
   * https://redis.io/commands/get
   * @param key
   * @param redisTimeoutMills redis 操作超时时间，单位毫秒
   * @return
   */
  String get(final String key, int redisTimeoutMills);

  /**
   * 通过Lua脚本释放锁
   * https://redis.io/commands/eval
   * @param luaScript lua脚本
   * @param key
   * @param value
   * @param redisTimeoutMills
   * @return
   */
  boolean releaseByLua(String luaScript, String key, String value, int redisTimeoutMills);

  /**
   * 删除 key
   * https://redis.io/commands/del
   * @param key
   * @param redisTimeoutMills 该参数可以不用
   * @return
   */
  boolean delete(String key, int redisTimeoutMills);
}
