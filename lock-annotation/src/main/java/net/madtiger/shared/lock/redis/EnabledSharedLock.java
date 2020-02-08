package net.madtiger.shared.lock.redis;


import net.madtiger.shared.lock.redis.configuration.SharedLockConfigurationImportSelector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 开启 共享锁
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SharedLockConfigurationImportSelector.class)
public @interface EnabledSharedLock {}
