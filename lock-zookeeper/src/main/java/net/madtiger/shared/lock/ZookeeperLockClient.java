package net.madtiger.shared.lock;

import com.sun.istack.internal.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.util.StringUtils;

/**
 * zookeeper 锁
 * @author Fenghu.Shi
 * @version 1.0
 */
@Slf4j
public class ZookeeperLockClient implements IZookeeperLockClient{

  private ZooKeeper zookeeper;

  /**
   * zk 的 根目录节点，获取锁时，lock key 对应的路径 = root path + key
   */
  private String namespace;


  /**
   * 初始化一个 zk 实例，使用默认空间
   * @param zookeeper zk 的客户端
   */
  public ZookeeperLockClient(ZooKeeper zookeeper){
    this(DEFAULT_NAMESPACE, zookeeper);
  }

  /**
   * 初始化一个 zk 实例
   * @param namespace 命名空间， 分享锁的公共节点
   * @param zookeeper zk 客户端
   */
  public ZookeeperLockClient(String namespace, ZooKeeper zookeeper){
    this.namespace = namespace;
    this.zookeeper = zookeeper;
  }

  /**
   * 调用 set nx 接口
   * @param resultHolder 参数
   * @return 设置结果
   */
  @Override
  public boolean tryAcquire(LockResultHolder resultHolder) {
    try {
      // 大部分情况下 锁的父节点都存在，所以直接创建子节点，如果创建子节点失败，再回来创建父节点
      // 咱们创建节点
      String seqNode = createNode(nodePath(resultHolder.getKey()), resultHolder.getValue(), CreateMode.EPHEMERAL_SEQUENTIAL);
      // 如果父节点不存在，则创建父节点
      if (NONODE_CREATE_NODE.equals(seqNode)){
        initParentNode(parentPath(resultHolder.getKey()), null);
        // 重新创建一次 node
        seqNode = createNode(nodePath(resultHolder.getKey()), resultHolder.getValue(), CreateMode.EPHEMERAL_SEQUENTIAL);
        // 如果还是没有则抛出异常
        if (NONODE_CREATE_NODE.equals(seqNode)){
          throw new KeeperException.NoNodeException();
        }
      }
      // 开始检查
      if (checkCurrentIsMin(resultHolder, seqNode)){
        log.debug("zk -> {} 节点锁获取成功", seqNode);
        return true;
      }else {
        log.debug("zk -> {} 节点锁获取失败", seqNode);
        return false;
      }
    } catch (Exception e) {
      log.error("获取 zk 锁{} 失败：{}", resultHolder.getKey(), e);
      throw  new IllegalArgumentException(e);
    }
  }

  /**
   * 检查当前节点是否是最小节点
   * @param resultHolder
   * @param seqNode
   * @return
   * @throws KeeperException
   * @throws InterruptedException
   */
  private boolean checkCurrentIsMin(LockResultHolder resultHolder, String seqNode)
      throws KeeperException, InterruptedException {
    // 覆盖 value
    resultHolder.value = seqNode;
    // 开始检查
    String prevNode = checkCurrentLocking(resultHolder.getKey(), seqNode);
    // 如果 prev node 是空，则获取失败
    if (StringUtils.isEmpty(prevNode)) {
      return checkCurrentIsMin(resultHolder, seqNode);
    }
    // 如果是当前节点则获取成功
    if (seqNode.equals(prevNode)){
      return true;
    }
    // 如果是一次访问的，则直接返回false
    if (resultHolder.args.maxRetryTimes == 1){
      return false;
    }
    CountDownLatch latch = waitPrevNodeRelease(resultHolder.key, prevNode, seqNode);
    // 如果前一个节点不存在了，则递归执行一次
    if (latch == null){
      return checkCurrentIsMin(resultHolder, seqNode);
    }
    // 咱们监听
    if (latch.await(resultHolder.args.waitTimeoutMills, TimeUnit.MILLISECONDS)){
      return true;
    }
    log.debug("zk -> {} 节点等待超时", nodePath(resultHolder.key) + "/" + seqNode);
    return false;
  }

  /**
   * 通过Lua脚本释放锁
   * @param resultHolder 结果参数
   * @return
   */
  @Override
  public boolean release(LockResultHolder resultHolder)
      throws KeeperException, InterruptedException {
    String releaseNodePath = parentPath(resultHolder.key) + "/" + resultHolder.value;
    // 检查是否存在
    if (zookeeper.exists(releaseNodePath, false) != null){
      // 存在则删除 此节点
      zookeeper.delete(releaseNodePath, -1);
      log.debug("zk -> {} 节点释放成功", releaseNodePath);
      return true;
    }
    return false;
  }


  /**
   * 检查当前的节点是否是最小节点，如果是
   * @param key 共享key
   * @param node 当前锁的seq node
   * @return
   */
  private String checkCurrentLocking(String key, String node) throws KeeperException, InterruptedException {
    List<String> nodes = zookeeper.getChildren(parentPath(key), false);
    // 大部分情况：如果只有一条，则说明当前的锁就是自己
    if (nodes.size() == 1) {
      return node;
    }
    // 排序 以便于获取我的下一条
    Collections.sort(nodes);
    String prev = null;
    for (String child : nodes) {
      if (child.equals(node)) {
        break;
      }else {
        prev = child;
      }
    }
    // 说明第一条就是自己
    if (prev == null) {
      return node;
    }
    return prev;
  }

  /**
   * 等待前一个节点资源释放
   * @param key 锁定的key
   * @param prevNode 观察的节点
   * @return
   * @throws KeeperException
   * @throws InterruptedException
   */
  private CountDownLatch waitPrevNodeRelease(String key, String prevNode, String seqNode)  throws KeeperException, InterruptedException {
    String releaseNodePath = parentPath(key) + "/" + prevNode;
    CountDownLatch latch = new CountDownLatch(1);
   if (zookeeper.exists(
       releaseNodePath,
        (WatchedEvent event) -> {
          try {
            // 如果已经执行，则忽略后续
            if (latch.getCount() == 0) {
              return;
            }
            // 如果删除了，则释放
            if (event.getType() == EventType.NodeDeleted) {
              latch.countDown();
            } else {
              // 再监听一次
              zookeeper.exists(nodePath(prevNode), true);
            }
          } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
          }
        }) == null){
     return null;
   }else {
     log.info("{} 节点监听 {} 节点成功", seqNode, prevNode);
   }
    return latch;
  }


  /**
   * 生成 node path
   * @param key
   * @return
   */
  private String nodePath(String key){
    return parentPath(key) + (key.startsWith("/") ? key : "/" + key) + "_";
  }

  /**
   * 生成 parent path
   * @param key
   * @return
   */
  private String parentPath(String key){
    return namespace + (key.startsWith("/") ? key : "/" + key);
  }

  /**
   * 初始化 父节点
   * @param path 创建的路径
   * @param  value 创建的值
   * @return
   */
  private String initParentNode(String path, @Nullable String value) throws Exception {
    value = value == null ? path : value;
    try{
      // 初始化父节点
      String parentNode =  zookeeper.create(path, SharedlockUtils.stringToBytes(value), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      log.error("init parent node is {}", parentNode);
      return parentNode;
    }catch (KeeperException ex){
      // 初始化 父节点
      if (ex.code() == Code.NONODE){
        initNamespace();
        //再来一次
        return initParentNode(path, value);
      }
      // 如果是 node 已存在异常，则认为成功
      if (ex.code() == Code.NODEEXISTS){
        return path;
      }
      // 原样抛出
      throw ex;
    }
  }

  /**
   * 创建节点
   * @param path node 路径
   * @param value 节点值
   * @param mode 节点类型
   * @return node 节点名称，如果 已经存在，则返回
   */
  private String createNode(String path, @Nullable String value, CreateMode mode){
    try{
      value = value == null ? path : value;
      // 初始化父节点
      String nodePath = zookeeper.create(path, SharedlockUtils.stringToBytes(value), Ids.OPEN_ACL_UNSAFE, mode);
      // 解析
      return nodePath.substring(nodePath.lastIndexOf("/") + 1);
    }catch (KeeperException ex){
      // 如果是 node 已存在异常，则认为成功
      if (ex.code() == Code.NODEEXISTS){
        return NODEEXISITS_CREATE_NODE;
      }
      // 如果父节点不存在
      if (ex.code() == Code.NONODE){
        return NONODE_CREATE_NODE;
      }
      // 抛出异常
      throw new IllegalArgumentException(ex);
    }catch (Throwable ex){
      // 抛出异常
      throw new IllegalArgumentException(ex);
    }
  }


  /**
   * 初始化 namespace
   * @throws Exception
   */
  private void initNamespace() throws Exception {
    // 初始化 namespace path, 不存在则创建
    initParentNode(namespace, NAMESPACE_DESC);
  }


  private static final String NAMESPACE_DESC = "shared lock zookeeper root";

  /**
   * 默认的 命名空间
   */
  private static final String DEFAULT_NAMESPACE = "/__SHARED_LOCK_NODE";


  /**
   * node 已经存在
   */
  private static final String NODEEXISITS_CREATE_NODE = "___NODEEXISTS";

  /**
   * node 不存在
   */
  private static final String NONODE_CREATE_NODE = "___NONODE";
}
