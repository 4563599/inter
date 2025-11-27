package com.myokhttp;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * 连接池 - 管理和复用HTTP连接
 * <p>
 * ⭐⭐⭐⭐ 这是OkHttp性能优化的关键！
 * <p>
 * 【为什么需要连接池？】
 * 1. 建立TCP连接很耗时（三次握手50-100ms）
 * 2. HTTPS还需要TLS握手（额外100-200ms）
 * 3. 复用连接可以跳过这些步骤
 * 4. 显著提升性能（提升50-70%）
 * <p>
 * 欢迎来到 OkHttp 性能优化的核心地带——ConnectionPool（连接池）。
 * <p>
 * 如果说 RealConnection 是“网线”，那么 ConnectionPool 就是**“网线收纳箱”**。它的存在只有一个目的：省钱、省时间。
 * <p>
 * 建立一个 TCP 连接（特别是 HTTPS）是非常昂贵的（耗时 100ms+）。如果每次请求都新建连接，APP 就会很卡。连接池通过“用完不关，下次接着用”的策略，实现了连接复用。
 * <p>
 * 【核心思想】
 * - HTTP/1.1支持Keep-Alive
 * - 一个TCP连接可以发送多个请求
 * - 使用完的连接不关闭，放入连接池
 * - 下次请求时，从连接池获取
 * <p>
 * 【工作流程】
 * 1. 需要连接 → 查找连接池
 * 2. 找到匹配的连接 → 复用（快！）
 * 3. 没找到 → 创建新连接
 * 4. 使用完毕 → 放回连接池
 * 5. 后台线程 → 定期清理空闲连接
 * <p>
 * 【清理策略】
 * - 最多保持5个空闲连接
 * - 空闲连接保持5分钟
 * - 超过时间或数量，关闭最老的连接
 *
 * @author Your Name
 */
public class ConnectionPool {

    // ========== 配置 ==========

    /**
     * 最大空闲连接数
     * <p>
     * 【默认值】5
     * <p>
     * 【为什么是5？】
     * - 平衡性能和资源占用
     * - 大部分应用不会同时访问超过5个服务器
     * - Chrome浏览器也是6个并发连接
     */
    private final int maxIdleConnections;

    /**
     * 连接保持时间（纳秒）
     * <p>
     * 【默认值】5分钟
     * <p>
     * 【为什么是5分钟？】
     * - 太短：频繁重建连接
     * - 太长：占用服务器资源
     * - 5分钟是经验值
     */
    private final long keepAliveDurationNs;

    /**
     * 连接池（双端队列）
     * <p>
     * 【为什么用Deque？】
     * - 可以从头部添加（新连接）
     * - 可以从尾部移除（最老的连接）
     * - 高效的FIFO操作
     */
    private final Deque<RealConnection> connections = new ArrayDeque<>();

    /**
     * 清理任务
     * <p>
     * 【作用】
     * - 定期检查空闲连接
     * - 清理过期或超量的连接
     */
    private final Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                // 执行清理逻辑，并计算下次清理需要等待的时间
                long waitNanos = cleanup(System.nanoTime());

                // 如果返回 -1，说明池子空了，不需要清理线程了，退出循环
                if (waitNanos == -1) return;

                // 如果有等待时间，就睡一会儿
                if (waitNanos > 0) {
                    synchronized (ConnectionPool.this) {
                        try {
                            ConnectionPool.this.wait(TimeUnit.NANOSECONDS.toMillis(waitNanos));
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }
    }
}

;

/**
 * 清理线程
 */
private static final Executor executor = new ThreadPoolExecutor(
        0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        r -> {
            Thread t = new Thread(r, "OkHttp ConnectionPool");
            t.setDaemon(true); // 设置为守护线程，JVM 退出时自动结束
            return t;
        });

// ========== 构造函数 ==========

/**
 * 创建连接池
 *
 * @param maxIdleConnections 最大空闲连接数
 * @param keepAliveDuration  连接保持时间
 * @param unit               时间单位
 */
public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit unit) {
    this.maxIdleConnections = maxIdleConnections;
    this.keepAliveDurationNs = unit.toNanos(keepAliveDuration);
}

/**
 * 默认连接池：5个空闲连接，保持5分钟
 */
public ConnectionPool() {
    this(5, 5, TimeUnit.MINUTES);
}

// ========== 核心方法 ==========

/**
 * 获取一个可用的连接
 * <p>
 * 【查找条件】
 * 1. host和port必须匹配
 * 2. 连接未关闭
 * 3. 连接未被占用（引用计数为0）
 * <p>
 * 【执行流程】
 * 1. 遍历连接池
 * 2. 找到匹配的连接
 * 3. 检查连接是否可用
 * 4. 标记为使用中（acquire）
 * 5. 返回连接
 *
 * @param host 主机名
 * @param port 端口
 * @return 可复用的连接，如果没有返回null
 */
public synchronized RealConnection get(String host, int port) {
    Iterator<RealConnection> i = connections.iterator();
    while (i.hasNext()) {
        RealConnection connection = i.next();

        // 如果这个连接匹配 (Host 和 Port 一样)
        if (connection.getHost().equals(host) && connection.getPort() == port) {
            // 并且是可用的 (这里简化判断，源码还有更多检查)
            if (connection.isClosed()) {
                i.remove();
                continue;
            }

            // 找到了！标记为“使用中”，并返回
            connection.acquire();
            return connection;
        }
    }
    return null; // 没找到，这就需要去新建连接了

}

/**
 * 将连接放回池中
 * <p>
 * 【执行流程】
 * 1. 启动清理线程（如果还没启动）
 * 2. 释放连接（release）
 * 3. 将连接加入池中
 * <p>
 * 【为什么要启动清理线程？】
 * - 只有在有连接时才需要清理
 * - 延迟启动，节省资源
 *
 * @param connection 连接
 */
public synchronized void put(RealConnection connection) {
    if (!cleanupRunning) {
        cleanupRunning = true;
        executor.execute(cleanupRunnable); // 只要有连接进来，就确后台保清理线程在跑
    }
    connections.add(connection); // 加到队列里
}

/**
 * 清理空闲连接
 * <p>
 * 【清理策略】
 * 1. 找出最长空闲的连接
 * 2. 如果空闲时间超过keepAliveDuration，关闭它
 * 3. 如果空闲连接数超过maxIdleConnections，关闭它
 * 4. 计算下次清理的等待时间
 * <p>
 * 【返回值】
 * - >0：下次清理需要等待的时间（纳秒）
 * - 0：立即进行下一次清理
 * - -1：没有连接，停止清理线程
 *
 * @param now 当前时间（纳秒）
 * @return 等待时间（纳秒），-1表示退出
 */
private synchronized long cleanup(long now) {
    int inUseConnectionCount = 0;
    int idleConnectionCount = 0;
    RealConnection longestIdleConnection = null;
    long longestIdleDurationNs = Long.MIN_VALUE;

    // 必须加锁，因为我们在遍历 connections
    synchronized (this) {
        for (Iterator<RealConnection> i = connections.iterator(); i.hasNext(); ) {
            RealConnection connection = i.next();

            // 如果连接正在使用，跳过，只统计数量
            if (connection.isInUse()) {
                inUseConnectionCount++;
                continue;
            }

            // 统计空闲连接
            idleConnectionCount++;

            // 计算它闲了多久
            long idleDurationNs = now - connection.getIdleAtNanos();

            // 记录下那个“最懒”的连接（闲得最久的）
            if (idleDurationNs > longestIdleDurationNs) {
                longestIdleDurationNs = idleDurationNs;
                longestIdleConnection = connection;
            }
        }

        // 情况 A: 找到了最懒的连接，而且它已经超时了 (超过 5 分钟)
        // 或者：空闲连接太多了 (超过 5 个)
        if (longestIdleDurationNs >= this.keepAliveDurationNs
                || idleConnectionCount > this.maxIdleConnections) {

            // 把这个连接踢出池子
            connections.remove(longestIdleConnection);

            // 在锁外面关闭 Socket (避免阻塞锁)
            // 这里为了演示写在锁内，实际应该 close 在外面
            try {
                if (longestIdleConnection != null) longestIdleConnection.close();
            } catch (IOException ignored) {
            }

            // 既然刚清理了一个，可能还有别的要清理，所以返回 0 (立即再次检查)
            return 0;
        }

        // 情况 B: 有空闲连接，但还没超时
        else if (idleConnectionCount > 0) {
            // 就算再睡一会儿，睡到它超时为止
            return keepAliveDurationNs - longestIdleDurationNs;
        }

        // 情况 C: 所有连接都在干活，或者没有连接
        else if (inUseConnectionCount > 0) {
            // 所有连接都在忙，那我 5 分钟后再来看看吧
            return keepAliveDurationNs;
        }

        // 情况 D: 池子彻底空了
        else {
            cleanupRunning = false;
            return -1; // 告诉 run 方法，可以退出线程了
        }
    }
}

/**
 * 获取连接数
 *
 * @return 连接总数（包括使用中和空闲）
 */
public synchronized int connectionCount() {
    // TODO: return connections.size();
    return 0;
}

/**
 * 获取空闲连接数
 *
 * @return 空闲连接数
 */
public synchronized int idleConnectionCount() {
    // TODO: 实现计数
    // int count = 0;
    // for (RealConnection connection : connections) {
    //     if (!connection.isInUse()) {
    //         count++;
    //     }
    // }
    // return count;

    return 0;
}

/**
 * 关闭所有连接
 * <p>
 * 【什么时候用？】
 * - 应用关闭时
 * - 清空连接池
 */
public synchronized void evictAll() {
    // TODO: 关闭并清空所有连接
    // for (RealConnection connection : connections) {
    //     try {
    //         connection.close();
    //     } catch (IOException e) {
    //         // 忽略
    //     }
    // }
    // connections.clear();
}
}

/*
【编写提示】

1. 【理解清理循环】
   
   清理线程的循环：
   ```
   while (true) {
       long waitNanos = cleanup(now);
       
       if (waitNanos == -1) {
           // 没有连接，退出线程
           return;
       }
       
       if (waitNanos > 0) {
           // 等待一段时间
           Thread.sleep(waitNanos);
       }
       
       // waitNanos == 0，立即进行下一次清理
   }
   ```

2. 【清理策略的决策树】
   
   ```
   遍历所有连接
       ↓
   找出最长空闲的连接
       ↓
   判断：空闲时间 >= keepAliveDuration?
       |
       ├─ 是 → 清理，返回0（立即再次清理）
       |
       └─ 否 → 判断：空闲连接数 > maxIdleConnections?
               |
               ├─ 是 → 清理最老的，返回0
               |
               └─ 否 → 判断：有空闲连接?
                       |
                       ├─ 是 → 返回等待时间
                       |
                       └─ 否 → 判断：有使用中的连接?
                               |
                               ├─ 是 → 返回keepAliveDuration
                               |
                               └─ 否 → 返回-1（退出线程）
   ```

3. 【为什么用synchronized？】
   
   多线程访问：
   - 请求线程：get(), put()
   - 清理线程：cleanup()
   
   需要同步：
   - 防止并发修改connections
   - 保证线程安全

4. 【守护线程vs用户线程】
   
   守护线程（Daemon Thread）：
   - JVM退出时不等待
   - 适合后台任务
   - cleanupThread设置为守护线程
   
   用户线程：
   - JVM退出时要等待
   - 主线程、工作线程等

【使用示例】

```java
// 创建连接池
ConnectionPool pool = new ConnectionPool();

// 第一次请求
RealConnection conn1 = pool.get("example.com", 80);
if (conn1 == null) {
    // 没有可用连接，创建新的
    conn1 = new RealConnection("example.com", 80);
    conn1.connect(10000, 10000);
    conn1.acquire();
}

// 使用连接...

// 放回连接池
pool.put(conn1);

// 第二次请求（相同服务器）
RealConnection conn2 = pool.get("example.com", 80);
// conn2 != null，复用了conn1！

// 使用连接...

pool.put(conn2);

// 查看连接池状态
System.out.println("总连接数：" + pool.connectionCount());  // 1
System.out.println("空闲连接数：" + pool.idleConnectionCount());  // 1
```

【常见错误】

❌ 错误1：忘记synchronized
```java
public RealConnection get(String host, int port) {
    // 没有synchronized，多线程不安全！
}
```

❌ 错误2：清理正在使用的连接
```java
// 错误！没有检查isInUse()
for (RealConnection conn : connections) {
    conn.close();  // 可能正在使用！
}
```
正确：
```java
if (!conn.isInUse()) {
    conn.close();  // 只清理空闲的
}
```

❌ 错误3：清理线程不是守护线程
```java
cleanupThread = new Thread(cleanupRunnable);
// 忘记setDaemon(true)
// 结果：JVM无法退出
```

【测试方法】
```java
// 测试连接复用
ConnectionPool pool = new ConnectionPool();

// 创建并放入连接
RealConnection conn1 = new RealConnection("example.com", 80);
conn1.connect(10000, 10000);
conn1.acquire();
conn1.release();
pool.put(conn1);

// 获取连接（应该复用）
RealConnection conn2 = pool.get("example.com", 80);
System.out.println("复用成功：" + (conn1 == conn2));  // true

// 测试清理
Thread.sleep(6 * 60 * 1000);  // 等待6分钟
System.out.println("清理后连接数：" + pool.connectionCount());  // 0
```

【预计编写时间】1 小时 15 分钟

【难度】⭐⭐⭐⭐☆

【重点】
- 理解连接复用的原理
- 掌握清理策略的实现
- 理解多线程同步
- 理解守护线程的作用
- 这是性能优化的关键！
*/

