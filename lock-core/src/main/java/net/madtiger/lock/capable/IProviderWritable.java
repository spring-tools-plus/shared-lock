package net.madtiger.lock.capable;

import net.madtiger.lock.provider.IProviderConfigurer;
import net.madtiger.lock.provider.ISharedLockProvider;
import org.springframework.lang.NonNull;

/**
 * 服务提供者数据变更接口，包括修改 服务提供者实例和服务提供者需要的参数
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public interface IProviderWritable {


  /**
   * 获取具体锁服务实例
   * @return
   */
  @NonNull ISharedLockProvider getProvider();

  /**
   * 获取 服务名称
   * @return 名称
   */
  String getProviderName();

  /**
   * 设置 服务提供者
   * @param provider 服务提供者对象
   */
  void setProvider(ISharedLockProvider provider);

  /**
   * 获取 服务提供商数据，改方法一般由服务提供者调用
   * @return 数据
   */
  @NonNull <T extends ISharedLockProvider, K> K getProviderData();

  /**
   * 设置 服务提供者数据，改方法一般由服务提供者调用
   * @param data 要设置的新的数据
   * @param <K> 类型
   */
  <K> void setProviderData(K data);

  /**
   * 获取特定服务提供者的配置对象
   * @param <T> 类型
   * @return 提供者对象
   */
  @NonNull <T extends IProviderConfigurer> T getProviderConfigurer();

  /**
   * 设置 服务提供者
   * @param configurer 提供者配置
   * @param <K> 类型
   * @return 数据
   */
  @NonNull <K extends IProviderConfigurer> void setProviderConfigurer(K configurer);

}
