package org.madtiger.shared.lock.autoconfiguration;

import org.madtiger.shared.lock.configuration.ISharedLockConfiguration;
import org.madtiger.shared.lock.configuration.SharedLockRedisConfiguration;
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
