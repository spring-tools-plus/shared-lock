package net.madtiger.shared.lock.redis;

import net.madtiger.shared.lock.redis.configuration.SharedLockRedisConfiguration;
import net.madtiger.shared.lock.redis.configuration.ISharedLockConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动装载
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
@ConditionalOnMissingBean(ISharedLockConfiguration.class)
@ImportAutoConfiguration(SharedLockRedisConfiguration.class)
public class SharedLockRedisAutoConfiguration {

}
