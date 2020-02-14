package net.madtiger.lock;

import net.madtiger.lock.configuration.ISharedLockConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动装载
 * @author Fenghu.Shi
 * @version 1.0
 */
@Configuration
@ConditionalOnMissingBean(ISharedLockConfiguration.class)
@EnabledSharedLock
public class SharedLockAutoConfiguration {

}
