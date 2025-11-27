package com.myokhttp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 调度器 - 管理异步请求的执行
 * <p>
 * 【为什么需要调度器？】
 * 1. 管理异步请求的执行
 * 2. 控制并发数量（防止资源耗尽）
 * 3. 管理等待队列
 * 4. 提供线程池
 * <p>
 * 【核心职责】
 * 1. 线程池管理：创建和管理线程池
 * 2. 并发控制：限制同时执行的请求数
 * 3. 队列管理：管理运行队列和等待队列
 * 4. 生命周期：跟踪请求的执行状态
 * <p>
 * 【三个队列】
 * - runningAsyncCalls：正在运行的异步请求
 * - readyAsyncCalls：等待执行的异步请求
 * - runningSyncCalls：正在运行的同步请求
 * <p>
 * 【工作流程】
 * 1. 新请求来了 → enqueue()
 * 2. 检查并发数 → 未满？直接执行：加入等待队列
 * 3. 请求完成 → finished()
 * 4. 从等待队列取下一个 → promoteAndExecute()
 *
 * @author Your Name
 */
public class Dispatcher {

    // ========== 配置 ==========

    /**
     * 最大并发请求数
     * <p>
     * 【默认值】64
     * <p>
     * 【为什么是64？】
     * - 平衡性能和资源
     * - Chrome浏览器是6个/host
     * - OkHttp更激进，64个全局
     */
    private int maxRequests = 64;

    /**
     * 每个主机最大并发请求数
     * <p>
     * 【默认值】5
     * <p>
     * 【为什么需要？】
     * - 防止对单个服务器压力过大
     * - 遵守HTTP规范建议
     */
    private int maxRequestsPerHost = 5;

    /**
     * 线程池
     * <p>
     * 【延迟创建】
     * - 第一次使用时才创建
     * - 如果只用同步请求，不需要线程池
     */
    private ExecutorService executorService;

    // ========== 队列 ==========

    /**
     * 正在运行的异步请求
     * <p>
     * 【为什么用Deque？】
     * - 需要遍历和删除
     * - ArrayDeque性能好
     */
    private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();

    /**
     * 等待执行的异步请求
     * <p>
     * 【什么时候用？】
     * - 并发数已满
     * - 请求加入等待队列
     * - 有请求完成时，从这里取
     */
    private final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();

    /**
     * 正在运行的同步请求
     * <p>
     * 【为什么需要？】
     * - 跟踪同步请求
     * - 统计总请求数
     * - 支持cancelAll()
     */
    private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();

    // ========== 线程池管理 ==========

    /**
     * 获取或创建线程池
     * <p>
     * 【线程池配置】
     * - 核心线程数：0（不保留空闲线程）
     * - 最大线程数：无限制（依靠maxRequests控制）
     * - 空闲存活：60秒
     * - 队列：SynchronousQueue（不缓存任务）
     * <p>
     * 【为什么这样配置？】
     * 1. 核心线程数0：节省资源，按需创建
     * 2. 最大线程数无限：依靠maxRequests控制并发
     * 3. 60秒超时：空闲线程自动回收
     * 4. SynchronousQueue：直接交给线程，不缓存
     *
     * @return 线程池
     */
    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            // ⭐ 线程池配置精髓：
            // corePoolSize = 0: 不保留空闲线程
            // maximumPoolSize = MAX: 线程数无限 (因为我们自己在外面控制了 maxRequests)
            // keepAlive = 60s: 闲置 60 秒自动销毁
            // queue = SynchronousQueue: 不缓存任务，直接交给线程跑
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory("OkHttp Dispatcher", false));
        }
        return executorService;
    }

    /**
     * 创建线程工厂
     * <p>
     * 【作用】
     * - 设置线程名称（方便调试）
     * - 设置是否守护线程
     *
     * @param name 线程名称
     * @return 线程工厂
     */
    private java.util.concurrent.ThreadFactory threadFactory(final String name) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }

    // ========== 异步请求管理 ==========

    /**
     * 添加异步请求到队列
     * <p>
     * 【执行流程】
     * 1. 检查并发数
     * 2. 未满：加入运行队列，提交到线程池
     * 3. 已满：加入等待队列
     * <p>
     * 【为什么同步？】
     * - 多个线程可能同时调用
     * - 保护队列的线程安全
     *
     * @param call 异步调用
     */
    public synchronized void enqueue(RealCall.AsyncCall call) {
// 先看看正在跑的有没有超过 64，并且同一个 host 有没有超过 5
        if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
            // 名额没满，直接进运行队列，开始跑
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            // 名额满了，去等待队列排队
            readyAsyncCalls.add(call);
        }
    }

    /**
     * 同步请求开始执行
     * <p>
     * 【作用】
     * - 记录同步请求
     * - 统计总请求数
     *
     * @param call 同步调用
     */
    public synchronized void executed(RealCall call) {
        // TODO: 加入同步请求队列
        runningSyncCalls.add(call);
    }

    /**
     * 同步请求执行完成
     * <p>
     * 【作用】
     * - 从队列中移除
     *
     * @param call 同步调用
     */
    public synchronized void finished(RealCall call) {
        // TODO: 从队列移除
        // runningSyncCalls.remove(call);
    }

    /**
     * 异步请求执行完成
     * <p>
     * 【执行流程】
     * 1. 从运行队列移除
     * 2. 尝试从等待队列取下一个
     * 3. 如果有，提交到线程池
     *
     * @param call 异步调用
     */
    public synchronized void finished(RealCall.AsyncCall call) {
// 从运行队列移除
        synchronized (this) {
            if (!runningAsyncCalls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
        }
        // 尝试去等待队列捞人
        promoteAndExecute();
    }

    /**
     * 从等待队列中提升请求到执行队列
     * <p>
     * 【为什么需要这个方法？】
     * - 请求完成后，有空闲槽位
     * - 从等待队列取下一个请求执行
     * <p>
     * 【执行流程】
     * 1. 检查是否还有空闲槽位
     * 2. 从等待队列取出请求
     * 3. 加入运行队列
     * 4. 提交到线程池
     */
    private void promoteAndExecute() {
// 注意：这里不能加 synchronized，因为 execute() 可能会耗时，我们不希望锁住整个调度器
        // 但为了简化演示，我们把锁加在内部操作上

        synchronized (this) {
            // 只要等待队列还有人
            Iterator<RealCall.AsyncCall> i = readyAsyncCalls.iterator();
            while (i.hasNext()) {
                RealCall.AsyncCall call = i.next();

                // 检查名额够不够
                if (runningAsyncCalls.size() >= maxRequests) break; // 总数超了，不叫了
                if (runningCallsForHost(call) >= maxRequestsPerHost)
                    continue; // 这个 host 超了，跳过，看下一个人

                // 叫号成功！
                i.remove(); // 从等待队列移除
                runningAsyncCalls.add(call); // 加入运行队列
                executorService().execute(call); // 扔给线程池跑
            }
        }

        /**
         * 取消所有请求
         * <p>
         * 【什么时候用？】
         * - 应用关闭时
         * - 用户取消所有请求
         * <p>
         * 【执行流程】
         * 1. 取消等待队列中的请求
         * 2. 取消运行中的异步请求
         * 3. 取消运行中的同步请求
         */
        public synchronized void cancelAll () {
            // TODO: 取消所有请求
            // for (RealCall.AsyncCall call : readyAsyncCalls) {
            //     call.get().cancel();
            // }
            //
            // for (RealCall.AsyncCall call : runningAsyncCalls) {
            //     call.get().cancel();
            // }
            //
            // for (RealCall call : runningSyncCalls) {
            //     call.cancel();
            // }
        }

        // ========== 统计信息 ==========

        /**
         * 获取正在运行的请求数量
         *
         * @return 运行中的请求数（同步+异步）
         */
        public synchronized int runningCallsCount () {
            // TODO: return runningAsyncCalls.size() + runningSyncCalls.size();
            return 0;
        }

        /**
         * 获取等待执行的请求数量
         *
         * @return 等待队列大小
         */
        public synchronized int queuedCallsCount () {
            // TODO: return readyAsyncCalls.size();
            return 0;
        }

        // ========== 配置方法 ==========

        /**
         * 设置最大并发请求数
         *
         * @param maxRequests 最大并发数
         */
        public synchronized void setMaxRequests ( int maxRequests){
            // TODO: 实现设置
            // if (maxRequests < 1) {
            //     throw new IllegalArgumentException("max < 1: " + maxRequests);
            // }
            // this.maxRequests = maxRequests;
        }

        public synchronized int getMaxRequests () {
            // TODO: return maxRequests;
            return 0;
        }

        /**
         * 设置每个主机最大并发数
         *
         * @param maxRequestsPerHost 每个主机最大并发数
         */
        public synchronized void setMaxRequestsPerHost ( int maxRequestsPerHost){
            // TODO: 实现设置
            // if (maxRequestsPerHost < 1) {
            //     throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
            // }
            // this.maxRequestsPerHost = maxRequestsPerHost;
        }

        public synchronized int getMaxRequestsPerHost () {
            // TODO: return maxRequestsPerHost;
            return 0;
        }
    }

/*
【编写提示】

1. 【理解三个队列】
   
   runningAsyncCalls（运行中）：
   - 正在线程池中执行的异步请求
   - 最多maxRequests个
   
   readyAsyncCalls（等待中）：
   - 等待执行的异步请求
   - 当有请求完成时，从这里取
   
   runningSyncCalls（同步）：
   - 正在执行的同步请求
   - 不受maxRequests限制
   - 只用于统计和取消

2. 【理解执行流程】
   
   添加异步请求：
   ```
   enqueue(call)
       ↓
   检查：runningAsyncCalls.size() < maxRequests?
       ↓                           ↓
     是：直接执行                 否：加入等待队列
   runningAsyncCalls.add(call)  readyAsyncCalls.add(call)
   executorService.execute(call)
   ```
   
   请求完成：
   ```
   finished(call)
       ↓
   runningAsyncCalls.remove(call)
       ↓
   promoteAndExecute()
       ↓
   从readyAsyncCalls取下一个
       ↓
   加入runningAsyncCalls
       ↓
   executorService.execute(next)
   ```

3. 【线程池的配置】
   
   为什么核心线程数是0？
   - 不保留空闲线程
   - 节省资源
   - 按需创建
   
   为什么最大线程数是无限？
   - 依靠maxRequests控制并发
   - 不在线程池层面限制
   
   为什么用SynchronousQueue？
   - 不缓存任务
   - 任务直接交给线程
   - 如果没有空闲线程，创建新线程

4. 【为什么所有方法都synchronized？】
   
   多线程并发访问：
   - 主线程：enqueue(), executed()
   - 工作线程：finished()
   - 可能同时修改队列
   
   需要同步：
   - 保护三个队列
   - 保证线程安全

【使用示例】

```java
// 创建调度器
Dispatcher dispatcher = new Dispatcher();

// 设置并发数
dispatcher.setMaxRequests(10);

// 添加异步请求
for (int i = 0; i < 20; i++) {
    RealCall.AsyncCall call = ...;
    dispatcher.enqueue(call);
}

// 前10个立即执行，后10个在等待队列
System.out.println("运行中：" + dispatcher.runningCallsCount());  // 10
System.out.println("等待中：" + dispatcher.queuedCallsCount());    // 10

// 请求完成后，等待队列中的会被自动执行
```

【常见错误】

❌ 错误1：忘记synchronized
```java
public void enqueue(AsyncCall call) {
    // 没有synchronized，多线程不安全！
    runningAsyncCalls.add(call);
}
```

❌ 错误2：finished()后没有promoteAndExecute()
```java
public void finished(AsyncCall call) {
    runningAsyncCalls.remove(call);
    // 忘记promoteAndExecute()
    // 等待队列中的请求永远不会被执行！
}
```

❌ 错误3：线程池是守护线程
```java
thread.setDaemon(true);  // 错误！
// 结果：JVM退出时，请求可能没执行完
```
正确：设置为false，让请求执行完

【测试方法】
```java
// 测试并发控制
Dispatcher dispatcher = new Dispatcher();
dispatcher.setMaxRequests(2);

// 添加3个请求
dispatcher.enqueue(call1);
dispatcher.enqueue(call2);
dispatcher.enqueue(call3);

// 2个运行，1个等待
System.out.println("运行中：" + dispatcher.runningCallsCount());  // 2
System.out.println("等待中：" + dispatcher.queuedCallsCount());    // 1

// 完成一个请求
dispatcher.finished(call1);

// 等待的请求被提升
System.out.println("运行中：" + dispatcher.runningCallsCount());  // 2
System.out.println("等待中：" + dispatcher.queuedCallsCount());    // 0
```

【预计编写时间】1 小时

【难度】⭐⭐⭐⭐☆

【重点】
- 理解三个队列的作用
- 理解并发控制的实现
- 理解promoteAndExecute()的作用
- 掌握线程池的配置
- 理解为什么需要synchronized
*/

