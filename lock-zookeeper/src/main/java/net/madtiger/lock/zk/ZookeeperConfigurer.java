package net.madtiger.lock.zk;

import lombok.Builder;
import lombok.Getter;
import net.madtiger.lock.provider.IProviderConfigurer;
import org.springframework.util.StringUtils;

/**
 * zookeeper 配置
 *
 * @author Fenghu.Shi
 * @version 1.2.0
 */
@Builder
@Getter
public class ZookeeperConfigurer implements IProviderConfigurer<ZookeeperConfigurer> {

  /**
   * node 命名空间
   */
  private String namespace;

  @Override
  public void merge(ZookeeperConfigurer configurer) {
    if (configurer == null || StringUtils.isEmpty(namespace)) {
      return;
    }
    // 设置
    this.namespace = configurer.getNamespace();
  }
}
