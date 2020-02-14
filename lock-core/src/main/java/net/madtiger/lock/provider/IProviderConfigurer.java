package net.madtiger.lock.provider;

/**
 * 服务提供者配置接口
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
public interface IProviderConfigurer<T extends IProviderConfigurer> {


  /**
   * 合并参数
   * @param configurer
   */
  void merge(T configurer);

}
