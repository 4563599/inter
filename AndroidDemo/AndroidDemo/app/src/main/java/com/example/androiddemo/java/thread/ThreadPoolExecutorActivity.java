package com.example.androiddemo.java.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolExecutorActivity - 深入理解 ThreadPoolExecutor 7大核心参数
 *
 * ================================================================
 * ThreadPoolExecutor 构造函数参数详解
 * ================================================================
 *
 * public ThreadPoolExecutor(
 *     int corePoolSize,           // 1. 核心线程数
 *     int maximumPoolSize,        // 2. 最大线程数
 *     long keepAliveTime,         // 3. 空闲线程存活时间
 *     TimeUnit unit,              // 4. 时间单位
 *     BlockingQueue<Runnable> workQueue,  // 5. 任务队列
 *     ThreadFactory threadFactory,        // 6. 线程工厂
 *     RejectedExecutionHandler handler    // 7. 拒绝策略
 * )
 *
 * ================================================================
 * 任务提交执行流程
 * ================================================================
 *
 * 1. 当前线程数 < corePoolSize → 创建核心线程执行任务
 * 2. 当前线程数 >= corePoolSize → 任务放入队列
 * 3. 队列已满 && 当前线程数 < maximumPoolSize → 创建非核心线程
 * 4. 队列已满 && 当前线程数 >= maximumPoolSize → 执行拒绝策略
 *
 * ================================================================
 */
public class ThreadPoolExecutorActivity extends AppCompatActivity {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder logBuilder = new StringBuilder();
    private TextView logView;
    private ScrollView logContainer;
    private ThreadPoolExecutor currentExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContentView());
        appendLog("=================================");
        appendLog("ThreadPoolExecutor 参数演示");
        appendLog("=================================");
        appendLog("✅ 界面加载成功！");
        appendLog("👇 请点击下方按钮查看演示\n");
    }

    private View buildContentView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 48);

        // 演示按钮
        root.addView(createButton("1. corePoolSize 核心线程数", v -> openDemo("corePoolSize")));
        root.addView(createButton("2. maximumPoolSize 最大线程数", v -> openDemo("maximumPoolSize")));
        root.addView(createButton("3. keepAliveTime 空闲存活时间", v -> openDemo("keepAliveTime")));
        root.addView(createButton("4. workQueue 任务队列对比", v -> openDemo("workQueue")));
        root.addView(createButton("5. threadFactory 自定义线程工厂", v -> openDemo("threadFactory")));
        root.addView(createButton("6. rejectedHandler 拒绝策略", v -> openDemo("rejectedHandler")));
        root.addView(createButton("7. 综合实战：图片下载线程池", v -> openDemo("realWorld")));
        root.addView(createButton("清空日志", v -> clearLog()));

        // 日志显示区域
        logView = new TextView(this);
        logView.setTextSize(14f);
        logView.setTextIsSelectable(true);
        logView.setPadding(24, 24, 24, 24);
        logView.setBackgroundColor(0xFFF5F5F5);
        logView.setTextColor(0xFF000000);  // 黑色文字
        logView.setText("日志区域 - 等待加载...");  // 默认文本，确保可见

        logContainer = new ScrollView(this);
        logContainer.setBackgroundColor(0xFFEEEEEE);  // 浅灰色背景
        logContainer.addView(logView);

        LinearLayout.LayoutParams logParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0);
        logParams.weight = 1f;
        logParams.topMargin = 24;
        root.addView(logContainer, logParams);
        return root;
    }

    private Button createButton(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setOnClickListener(v -> {
            appendLog("按钮被点击: " + text + "\n");
            listener.onClick(v);
        });
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = 16;
        button.setLayoutParams(params);
        
        return button;
    }


    // ==================== 参数1: corePoolSize 核心线程数 ====================
    /**
     * 演示 corePoolSize（核心线程数）的作用
     *
     * 核心线程特点：
     * - 即使空闲也不会被回收（除非设置 allowCoreThreadTimeOut）
     * - 任务优先由核心线程执行
     * - 核心线程数决定了线程池的基本并发能力
     *
     * 设置建议：
     * - CPU密集型：corePoolSize = CPU核心数 + 1
     * - IO密集型：corePoolSize = CPU核心数 * 2
     */
    private void demoCorePoolSize() {
        shutdownCurrentExecutor();
        appendLog("=== 参数1: corePoolSize 核心线程数 ===\n");

        // 创建核心线程数为2的线程池
        currentExecutor = new ThreadPoolExecutor(
                2,                      // corePoolSize: 核心线程数
                4,                      // maximumPoolSize
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10)
        );

        appendLog("配置: corePoolSize=2, maximumPoolSize=4");
        appendLog("提交3个任务，观察核心线程的创建和复用:\n");

        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            currentExecutor.execute(() -> {
                String threadName = Thread.currentThread().getName();
                appendLog(String.format(Locale.US,
                        "任务%d 开始执行 [线程: %s] [活跃线程: %d]",
                        taskId, threadName, currentExecutor.getActiveCount()));
                simulateWork(1000);
                appendLog(String.format(Locale.US, "任务%d 执行完成", taskId));
            });
        }

        // 延迟显示线程池状态
        mainHandler.postDelayed(() -> {
            appendLog(String.format(Locale.US,
                    "\n线程池状态: 核心线程数=%d, 当前线程数=%d, 队列任务数=%d",
                    currentExecutor.getCorePoolSize(),
                    currentExecutor.getPoolSize(),
                    currentExecutor.getQueue().size()));
            appendLog("结论: 核心线程会被保留，任务3在队列等待核心线程空闲\n");
        }, 500);
    }

    // ==================== 参数2: maximumPoolSize 最大线程数 ====================
    /**
     * 演示 maximumPoolSize（最大线程数）的作用
     *
     * 当队列满了之后，会创建非核心线程来处理任务
     * 非核心线程数 = maximumPoolSize - corePoolSize
     *
     * 触发条件：
     * 1. 核心线程都在忙
     * 2. 任务队列已满
     * 3. 当前线程数 < maximumPoolSize
     */
    private void demoMaximumPoolSize() {
        shutdownCurrentExecutor();
        appendLog("=== 参数2: maximumPoolSize 最大线程数 ===\n");

        // 使用有界队列，容量为2
        currentExecutor = new ThreadPoolExecutor(
                2,                      // corePoolSize
                4,                      // maximumPoolSize: 最大4个线程
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2)  // 有界队列，容量2
        );

        appendLog("配置: corePoolSize=2, maximumPoolSize=4, 队列容量=2");
        appendLog("提交6个任务，观察非核心线程的创建:\n");

        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            try {
                currentExecutor.execute(() -> {
                    appendLog(String.format(Locale.US,
                            "任务%d 执行中 [线程: %s] [总线程数: %d]",
                            taskId, Thread.currentThread().getName(),
                            currentExecutor.getPoolSize()));
                    simulateWork(2000);
                });
                appendLog(String.format(Locale.US, "任务%d 已提交", taskId));
            } catch (Exception e) {
                appendLog(String.format(Locale.US, "任务%d 被拒绝: %s", taskId, e.getClass().getSimpleName()));
            }
        }

        appendLog("\n执行流程:");
        appendLog("• 任务1-2: 创建核心线程执行");
        appendLog("• 任务3-4: 进入队列等待");
        appendLog("• 任务5-6: 队列满，创建非核心线程执行\n");
    }

    // ==================== 参数3: keepAliveTime 空闲存活时间 ====================
    /**
     * 演示 keepAliveTime（空闲线程存活时间）的作用
     *
     * 非核心线程空闲超过 keepAliveTime 后会被回收
     * 可通过 allowCoreThreadTimeOut(true) 让核心线程也能被回收
     */
    private void demoKeepAliveTime() {
        shutdownCurrentExecutor();
        appendLog("=== 参数3: keepAliveTime 空闲存活时间 ===\n");

        currentExecutor = new ThreadPoolExecutor(
                1,                      // corePoolSize
                3,                      // maximumPoolSize
                3L,                     // keepAliveTime: 3秒
                TimeUnit.SECONDS,       // 时间单位
                new ArrayBlockingQueue<>(1)
        );

        appendLog("配置: corePoolSize=1, maximumPoolSize=3, keepAliveTime=3秒");
        appendLog("提交3个任务，然后观察非核心线程的回收:\n");

        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            currentExecutor.execute(() -> {
                appendLog(String.format(Locale.US,
                        "任务%d 执行 [线程: %s]", taskId, Thread.currentThread().getName()));
                simulateWork(500);
            });
        }

        // 定时检查线程池状态
        appendLog("\n开始监控线程数变化...");
        for (int i = 1; i <= 5; i++) {
            final int second = i;
            mainHandler.postDelayed(() -> {
                appendLog(String.format(Locale.US,
                        "第%d秒: 当前线程数=%d", second, currentExecutor.getPoolSize()));
                if (second == 5) {
                    appendLog("\n结论: 非核心线程空闲3秒后被回收，只保留1个核心线程\n");
                }
            }, i * 1000L);
        }
    }


    // ==================== 参数4: workQueue 任务队列 ====================
    /**
     * 演示不同类型的 workQueue（任务队列）
     *
     * 常用队列类型：
     * 1. ArrayBlockingQueue - 有界队列，需指定容量
     * 2. LinkedBlockingQueue - 可选有界/无界，默认无界
     * 3. SynchronousQueue - 不存储任务，直接传递给线程
     * 4. PriorityBlockingQueue - 优先级队列
     */
    private void demoWorkQueue() {
        shutdownCurrentExecutor();
        appendLog("=== 参数4: workQueue 任务队列类型对比 ===\n");

        // 演示1: ArrayBlockingQueue 有界队列
        appendLog("【ArrayBlockingQueue 有界队列】");
        appendLog("特点: 固定容量，队列满时触发创建非核心线程或拒绝");
        ThreadPoolExecutor executor1 = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)  // 容量为3
        );
        appendLog("配置: core=1, max=2, 队列容量=3");
        submitTasks(executor1, 5, "ArrayQueue");
        executor1.shutdown();

        // 演示2: LinkedBlockingQueue 无界队列
        appendLog("\n【LinkedBlockingQueue 无界队列】");
        appendLog("特点: 默认无界，maximumPoolSize 参数失效");
        ThreadPoolExecutor executor2 = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()  // 无界队列
        );
        appendLog("配置: core=1, max=2, 无界队列");
        submitTasks(executor2, 5, "LinkedQueue");
        appendLog("注意: 无界队列时 max=2 不会生效，只用核心线程");
        executor2.shutdown();

        // 演示3: SynchronousQueue 同步队列
        appendLog("\n【SynchronousQueue 同步队列】");
        appendLog("特点: 不存储任务，直接传递，适合CachedThreadPool");
        ThreadPoolExecutor executor3 = new ThreadPoolExecutor(
                0, 3, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );
        appendLog("配置: core=0, max=3, 同步队列");
        submitTasks(executor3, 3, "SyncQueue");
        appendLog("特性: 每个任务都会创建新线程（直到达到max）\n");
        executor3.shutdown();
    }

    private void submitTasks(ThreadPoolExecutor executor, int count, String tag) {
        for (int i = 1; i <= count; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    appendLog(String.format(Locale.US,
                            "  %s-任务%d [线程数: %d, 队列: %d]",
                            tag, taskId, executor.getPoolSize(), executor.getQueue().size()));
                    simulateWork(300);
                });
            } catch (Exception e) {
                appendLog(String.format(Locale.US, "  %s-任务%d 被拒绝", tag, taskId));
            }
        }
    }

    // ==================== 参数5: threadFactory 线程工厂 ====================
    /**
     * 演示自定义 ThreadFactory（线程工厂）
     *
     * 作用：
     * 1. 自定义线程名称，便于调试和日志追踪
     * 2. 设置线程优先级
     * 3. 设置守护线程
     * 4. 设置未捕获异常处理器
     */
    private void demoThreadFactory() {
        shutdownCurrentExecutor();
        appendLog("=== 参数5: threadFactory 自定义线程工厂 ===\n");

        // 自定义线程工厂
        ThreadFactory customFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final String namePrefix = "ImageLoader-Worker-";

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
                // 设置为非守护线程
                t.setDaemon(false);
                // 设置线程优先级
                t.setPriority(Thread.NORM_PRIORITY);
                // 设置未捕获异常处理器
                t.setUncaughtExceptionHandler((thread, ex) ->
                        appendLog("线程异常: " + thread.getName() + " - " + ex.getMessage()));
                appendLog("创建线程: " + t.getName());
                return t;
            }
        };

        currentExecutor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                customFactory  // 使用自定义线程工厂
        );

        appendLog("\n自定义线程工厂功能:");
        appendLog("• 自定义线程名称前缀: ImageLoader-Worker-");
        appendLog("• 设置线程优先级: NORM_PRIORITY");
        appendLog("• 设置异常处理器\n");

        appendLog("提交3个任务:");
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            currentExecutor.execute(() -> {
                appendLog(String.format(Locale.US,
                        "任务%d 执行于 [%s]", taskId, Thread.currentThread().getName()));
                simulateWork(500);
            });
        }

        appendLog("\n好处: 自定义线程名便于在日志中追踪问题\n");
    }

    // ==================== 参数6: rejectedHandler 拒绝策略 ====================
    /**
     * 演示 RejectedExecutionHandler（拒绝策略）
     *
     * 4种内置拒绝策略：
     * 1. AbortPolicy - 默认，抛出 RejectedExecutionException
     * 2. CallerRunsPolicy - 由调用线程执行任务
     * 3. DiscardPolicy - 静默丢弃任务
     * 4. DiscardOldestPolicy - 丢弃队列最老的任务
     */
    private void demoRejectedHandler() {
        shutdownCurrentExecutor();
        appendLog("=== 参数6: rejectedHandler 拒绝策略 ===\n");

        appendLog("【4种内置拒绝策略演示】\n");

        // 1. AbortPolicy - 抛出异常
        appendLog("1. AbortPolicy (默认) - 抛出异常");
        testRejectedPolicy(new ThreadPoolExecutor.AbortPolicy(), "Abort");

        // 2. CallerRunsPolicy - 调用者执行
        appendLog("\n2. CallerRunsPolicy - 由调用线程执行");
        testRejectedPolicy(new ThreadPoolExecutor.CallerRunsPolicy(), "CallerRuns");

        // 3. DiscardPolicy - 静默丢弃
        appendLog("\n3. DiscardPolicy - 静默丢弃新任务");
        testRejectedPolicy(new ThreadPoolExecutor.DiscardPolicy(), "Discard");

        // 4. DiscardOldestPolicy - 丢弃最老任务
        appendLog("\n4. DiscardOldestPolicy - 丢弃队列最老任务");
        testRejectedPolicy(new ThreadPoolExecutor.DiscardOldestPolicy(), "DiscardOldest");

        // 5. 自定义拒绝策略
        appendLog("\n5. 自定义拒绝策略 - 记录日志并重试");
        RejectedExecutionHandler customHandler = (r, executor) -> {
            appendLog("  自定义处理: 任务被拒绝，可以记录日志或重试");
        };
        testRejectedPolicy(customHandler, "Custom");

        appendLog("\n建议: 生产环境推荐 CallerRunsPolicy 或自定义策略\n");
    }

    private void testRejectedPolicy(RejectedExecutionHandler handler, String tag) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                handler
        );

        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    appendLog(String.format(Locale.US, "  %s-任务%d 执行", tag, taskId));
                    simulateWork(500);
                });
                appendLog(String.format(Locale.US, "  %s-任务%d 已提交", tag, taskId));
            } catch (Exception e) {
                appendLog(String.format(Locale.US, "  %s-任务%d 异常: %s",
                        tag, taskId, e.getClass().getSimpleName()));
            }
        }
        executor.shutdown();
    }


    // ==================== 参数7: 综合实战示例 ====================
    /**
     * 综合实战：模拟图片下载线程池
     *
     * 实际Android开发中的最佳配置示例
     */
    private void demoRealWorldExample() {
        shutdownCurrentExecutor();
        appendLog("=== 综合实战: 图片下载线程池配置 ===\n");

        // 获取CPU核心数
        int cpuCount = Runtime.getRuntime().availableProcessors();
        appendLog("设备CPU核心数: " + cpuCount);

        // 计算线程池参数
        int corePoolSize = cpuCount + 1;           // IO密集型任务
        int maxPoolSize = cpuCount * 2 + 1;        // 最大线程数
        long keepAliveTime = 30L;                   // 30秒空闲回收
        int queueCapacity = 128;                    // 队列容量

        appendLog(String.format(Locale.US,
                "\n推荐配置 (IO密集型):\n" +
                "• corePoolSize = %d (CPU核心数+1)\n" +
                "• maximumPoolSize = %d (CPU核心数*2+1)\n" +
                "• keepAliveTime = %d秒\n" +
                "• 队列容量 = %d\n",
                corePoolSize, maxPoolSize, keepAliveTime, queueCapacity));

        // 自定义线程工厂
        ThreadFactory imageThreadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ImageDownloader-" + count.getAndIncrement());
                t.setPriority(Thread.NORM_PRIORITY - 1);  // 稍低优先级，不影响UI
                return t;
            }
        };

        // 自定义拒绝策略：记录日志
        RejectedExecutionHandler imageRejectedHandler = (r, executor) -> {
            appendLog("⚠️ 任务被拒绝，队列已满，考虑增加队列容量或降低并发");
        };

        // 创建线程池
        currentExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                imageThreadFactory,
                imageRejectedHandler
        );

        // 允许核心线程超时回收（可选，适合低频使用场景）
        // currentExecutor.allowCoreThreadTimeOut(true);

        appendLog("模拟下载10张图片:\n");

        for (int i = 1; i <= 10; i++) {
            final int imageId = i;
            currentExecutor.execute(() -> {
                String threadName = Thread.currentThread().getName();
                appendLog(String.format(Locale.US,
                        "📥 下载图片%d [%s] [活跃:%d/队列:%d]",
                        imageId, threadName,
                        currentExecutor.getActiveCount(),
                        currentExecutor.getQueue().size()));
                // 模拟网络下载耗时
                simulateWork(800 + (int)(Math.random() * 400));
                appendLog(String.format(Locale.US, "✅ 图片%d 下载完成", imageId));
            });
        }

        // 显示最终统计
        mainHandler.postDelayed(() -> {
            appendLog(String.format(Locale.US,
                    "\n📊 线程池统计:\n" +
                    "• 完成任务数: %d\n" +
                    "• 当前线程数: %d\n" +
                    "• 历史最大线程数: %d\n",
                    currentExecutor.getCompletedTaskCount(),
                    currentExecutor.getPoolSize(),
                    currentExecutor.getLargestPoolSize()));
        }, 5000);
    }

    // ==================== 工具方法 ====================

    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void appendLog(String text) {
        mainHandler.post(() -> {
            logBuilder.append(text).append('\n');
            logView.setText(logBuilder.toString());
            logContainer.post(() -> logContainer.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void openDemo(String demoType) {
        android.content.Intent intent = new android.content.Intent(this, ThreadPoolDemoActivity.class);
        intent.putExtra("DEMO_TYPE", demoType);
        startActivity(intent);
    }

    private void clearLog() {
        logBuilder.setLength(0);
        logView.setText("");
    }

    private void shutdownCurrentExecutor() {
        if (currentExecutor != null && !currentExecutor.isShutdown()) {
            currentExecutor.shutdownNow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdownCurrentExecutor();
    }
}
