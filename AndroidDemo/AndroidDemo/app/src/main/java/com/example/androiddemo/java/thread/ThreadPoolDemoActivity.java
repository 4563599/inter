package com.example.androiddemo.java.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
 * 线程池演示详情页 - 全屏显示日志
 */
public class ThreadPoolDemoActivity extends AppCompatActivity {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder logBuilder = new StringBuilder();
    private TextView logView;
    private ScrollView logContainer;
    private ThreadPoolExecutor currentExecutor;
    private String demoType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 获取演示类型
        demoType = getIntent().getStringExtra("DEMO_TYPE");
        
        setContentView(buildContentView());
        
        // 根据类型执行对应的演示
        runDemo();
    }

    private View buildContentView() {
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // 创建根布局
        android.widget.LinearLayout rootLayout = new android.widget.LinearLayout(this);
        rootLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        rootLayout.setFitsSystemWindows(true);  // 自动适配系统窗口（包括 ActionBar）
        
        logView = new TextView(this);
        logView.setTextSize(14f);
        logView.setTextIsSelectable(true);
        logView.setPadding(32, 32, 32, 48);  // 正常的 padding
        logView.setTextColor(0xFF000000);
        logView.setBackgroundColor(0xFFFFFFFF);
        
        // 设置 TextView 的布局参数，确保内容完全显示
        android.widget.LinearLayout.LayoutParams textParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        logView.setLayoutParams(textParams);

        logContainer = new ScrollView(this);
        logContainer.setFillViewport(false);  // 不填充视口，让内容自然滚动
        logContainer.addView(logView);
        
        // 将 ScrollView 添加到根布局
        android.widget.LinearLayout.LayoutParams scrollParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT
        );
        rootLayout.addView(logContainer, scrollParams);
        
        return rootLayout;
    }

    private void runDemo() {
        if (demoType == null) return;
        
        switch (demoType) {
            case "corePoolSize":
                demoCorePoolSize();
                break;
            case "maximumPoolSize":
                demoMaximumPoolSize();
                break;
            case "keepAliveTime":
                demoKeepAliveTime();
                break;
            case "workQueue":
                demoWorkQueue();
                break;
            case "threadFactory":
                demoThreadFactory();
                break;
            case "rejectedHandler":
                demoRejectedHandler();
                break;
            case "realWorld":
                demoRealWorldExample();
                break;
        }
    }

    // ==================== 演示方法 ====================
    
    private void demoCorePoolSize() {
        appendLog("=== corePoolSize 核心线程数 ===\n");
        appendLog("核心线程特点：");
        appendLog("• 即使空闲也不会被回收");
        appendLog("• 任务优先由核心线程执行");
        appendLog("• 决定线程池的基本并发能力\n");

        currentExecutor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
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

        mainHandler.postDelayed(() -> {
            appendLog(String.format(Locale.US,
                    "\n线程池状态: 核心线程数=%d, 当前线程数=%d, 队列任务数=%d",
                    currentExecutor.getCorePoolSize(),
                    currentExecutor.getPoolSize(),
                    currentExecutor.getQueue().size()));
            appendLog("\n✅ 结论: 核心线程会被保留，任务3在队列等待核心线程空闲");
        }, 500);
    }

    private void demoMaximumPoolSize() {
        appendLog("=== maximumPoolSize 最大线程数 ===\n");
        appendLog("触发条件：");
        appendLog("1. 核心线程都在忙");
        appendLog("2. 任务队列已满");
        appendLog("3. 当前线程数 < maximumPoolSize\n");

        currentExecutor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2)
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
                appendLog(String.format(Locale.US, "✓ 任务%d 已提交", taskId));
            } catch (Exception e) {
                appendLog(String.format(Locale.US, "✗ 任务%d 被拒绝: %s", 
                        taskId, e.getClass().getSimpleName()));
            }
        }

        appendLog("\n执行流程:");
        appendLog("• 任务1-2: 创建核心线程执行");
        appendLog("• 任务3-4: 进入队列等待");
        appendLog("• 任务5-6: 队列满，创建非核心线程执行");
    }

    private void demoKeepAliveTime() {
        appendLog("=== keepAliveTime 空闲存活时间 ===\n");
        appendLog("非核心线程空闲超过 keepAliveTime 后会被回收\n");

        currentExecutor = new ThreadPoolExecutor(
                1, 3, 3L, TimeUnit.SECONDS,
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

        appendLog("\n开始监控线程数变化...");
        for (int i = 1; i <= 5; i++) {
            final int second = i;
            mainHandler.postDelayed(() -> {
                appendLog(String.format(Locale.US,
                        "第%d秒: 当前线程数=%d", second, currentExecutor.getPoolSize()));
                if (second == 5) {
                    appendLog("\n✅ 结论: 非核心线程空闲3秒后被回收，只保留1个核心线程");
                }
            }, i * 1000L);
        }
    }

    private void demoWorkQueue() {
        appendLog("=== workQueue 任务队列类型对比 ===\n");
        appendLog("💡 类比: 厨师做菜");
        appendLog("• 厨师 = 线程");
        appendLog("• 点餐单 = 任务");
        appendLog("• 点餐架 = 队列\n");

        // 演示1: ArrayBlockingQueue 有界队列
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【1. ArrayBlockingQueue - 有界队列】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 点餐架只能放3张点餐单");
        appendLog("配置: 核心厨师=1人, 最大厨师=2人, 点餐架容量=3\n");
        
        ThreadPoolExecutor executor1 = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)
        );
        
        appendLog("提交5个任务，观察执行流程:");
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            try {
                executor1.execute(() -> {
                    appendLog(String.format(Locale.US,
                            "  ✓ 任务%d 正在执行 [线程数:%d, 队列中:%d]",
                            taskId, executor1.getPoolSize(), executor1.getQueue().size()));
                    simulateWork(300);
                });
                
                // 提交后立即显示状态
                mainHandler.postDelayed(() -> {
                    appendLog(String.format(Locale.US,
                            "  → 任务%d 已提交 [线程数:%d, 队列中:%d]",
                            taskId, executor1.getPoolSize(), executor1.getQueue().size()));
                }, 50);
                
            } catch (Exception e) {
                appendLog(String.format(Locale.US, "  ✗ 任务%d 被拒绝!", taskId));
            }
        }
        
        mainHandler.postDelayed(() -> {
            appendLog("\n📊 执行流程分析:");
            appendLog("  任务1: 核心厨师接单，直接做菜");
            appendLog("  任务2-4: 厨师忙，放到点餐架排队");
            appendLog("  任务5: 点餐架满了，叫第2个厨师来帮忙");
            appendLog("\n✅ 结论: 有界队列可以控制等待任务数量\n");
            executor1.shutdown();
            
            // 延迟执行第二个演示
            mainHandler.postDelayed(this::demoLinkedBlockingQueue, 1500);
        }, 1000);
    }
    
    private void demoLinkedBlockingQueue() {
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【2. LinkedBlockingQueue - 无界队列】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 点餐架无限大，可以放无数张点餐单");
        appendLog("配置: 核心厨师=1人, 最大厨师=2人, 点餐架=无限\n");
        
        ThreadPoolExecutor executor2 = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()  // 无界队列
        );
        
        appendLog("提交5个任务，观察执行流程:");
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor2.execute(() -> {
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 正在执行 [线程数:%d, 队列中:%d]",
                        taskId, executor2.getPoolSize(), executor2.getQueue().size()));
                simulateWork(300);
            });
            
            final int currentTask = i;
            mainHandler.postDelayed(() -> {
                appendLog(String.format(Locale.US,
                        "  → 任务%d 已提交 [线程数:%d, 队列中:%d]",
                        currentTask, executor2.getPoolSize(), executor2.getQueue().size()));
            }, 50);
        }
        
        mainHandler.postDelayed(() -> {
            appendLog("\n📊 执行流程分析:");
            appendLog("  任务1: 核心厨师接单，直接做菜");
            appendLog("  任务2-5: 全部放到点餐架排队");
            appendLog("  ⚠️ 第2个厨师永远不会被叫来!");
            appendLog("\n✅ 结论: 无界队列会让 maximumPoolSize 失效");
            appendLog("⚠️ 风险: 任务太多可能导致内存溢出!\n");
            executor2.shutdown();
            
            // 延迟执行第三个演示
            mainHandler.postDelayed(this::demoSynchronousQueue, 1500);
        }, 1000);
    }
    
    private void demoSynchronousQueue() {
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【3. SynchronousQueue - 同步队列】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 没有点餐架，顾客直接把单子递给厨师");
        appendLog("配置: 核心厨师=0人, 最大厨师=3人, 无点餐架\n");
        
        ThreadPoolExecutor executor3 = new ThreadPoolExecutor(
                0, 3, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );
        
        appendLog("提交3个任务，观察执行流程:");
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            executor3.execute(() -> {
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 正在执行 [线程数:%d]",
                        taskId, executor3.getPoolSize()));
                simulateWork(300);
            });
            
            final int currentTask = i;
            mainHandler.postDelayed(() -> {
                appendLog(String.format(Locale.US,
                        "  → 任务%d 已提交 [线程数:%d]",
                        currentTask, executor3.getPoolSize()));
            }, 50);
        }
        
        mainHandler.postDelayed(() -> {
            appendLog("\n📊 执行流程分析:");
            appendLog("  任务1: 立即创建厨师1，直接做菜");
            appendLog("  任务2: 立即创建厨师2，直接做菜");
            appendLog("  任务3: 立即创建厨师3，直接做菜");
            appendLog("\n✅ 结论: 适合任务量不确定的场景");
            appendLog("💡 应用: Executors.newCachedThreadPool() 就用这个");
            appendLog("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("📚 总结对比:");
            appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("ArrayBlockingQueue: 有容量限制，可控");
            appendLog("LinkedBlockingQueue: 无限排队，有风险");
            appendLog("SynchronousQueue: 不排队，直接执行");
            executor3.shutdown();
        }, 1000);
    }

    private void demoThreadFactory() {
        appendLog("=== threadFactory 自定义线程工厂 ===\n");
        appendLog("💡 类比: 工厂生产工人");
        appendLog("• ThreadFactory = 招聘部门");
        appendLog("• Thread = 工人");
        appendLog("• 可以给工人起名字、设置技能等级\n");

        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【对比：默认 vs 自定义】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 1. 默认线程工厂
        appendLog("1️⃣ 使用默认线程工厂:");
        ThreadPoolExecutor defaultExecutor = new ThreadPoolExecutor(
                2, 2, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10)
                // 不指定 ThreadFactory，使用默认的
        );

        appendLog("提交2个任务，观察线程名称:");
        for (int i = 1; i <= 2; i++) {
            final int taskId = i;
            defaultExecutor.execute(() -> {
                String threadName = Thread.currentThread().getName();
                appendLog(String.format(Locale.US,
                        "  任务%d → 线程名: [%s]", taskId, threadName));
            });
        }

        mainHandler.postDelayed(() -> {
            appendLog("\n❌ 问题: 线程名像 'pool-1-thread-1'");
            appendLog("  • 看不出是做什么的");
            appendLog("  • 多个线程池时容易混淆");
            appendLog("  • 调试时很难定位问题\n");
            defaultExecutor.shutdown();

            // 延迟执行自定义线程工厂演示
            mainHandler.postDelayed(this::demoCustomThreadFactory, 1000);
        }, 800);
    }

    private void demoCustomThreadFactory() {
        appendLog("2️⃣ 使用自定义线程工厂:\n");

        // 自定义线程工厂
        ThreadFactory customFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final String namePrefix = "ImageLoader-";

            @Override
            public Thread newThread(Runnable r) {
                // 创建线程时的回调
                String threadName = namePrefix + threadNumber.getAndIncrement();
                Thread t = new Thread(r, threadName);

                // 1. 设置线程名称
                appendLog(String.format(Locale.US,
                        "  🏭 工厂创建线程: [%s]", threadName));

                // 2. 设置是否为守护线程
                t.setDaemon(false);  // false = 用户线程，程序会等它执行完
                appendLog(String.format(Locale.US,
                        "     └─ 守护线程: %s (用户线程，程序会等待)", t.isDaemon()));

                // 3. 设置线程优先级
                t.setPriority(Thread.NORM_PRIORITY);
                appendLog(String.format(Locale.US,
                        "     └─ 优先级: %d (1=最低, 5=普通, 10=最高)", t.getPriority()));

                // 4. 设置未捕获异常处理器
                t.setUncaughtExceptionHandler((thread, ex) -> {
                    appendLog(String.format(Locale.US,
                            "  ⚠️ [%s] 发生异常: %s", thread.getName(), ex.getMessage()));
                });
                appendLog("     └─ 异常处理器: 已设置\n");

                return t;
            }
        };

        appendLog("创建线程池（使用自定义工厂）:");
        currentExecutor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                customFactory  // 使用自定义线程工厂
        );

        appendLog("\n提交3个任务，观察线程创建过程:\n");
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            currentExecutor.execute(() -> {
                String threadName = Thread.currentThread().getName();
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 执行中 → 线程: [%s]", taskId, threadName));
                simulateWork(500);
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 完成", taskId));
            });
        }

        mainHandler.postDelayed(() -> {
            appendLog("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("✅ 自定义线程工厂的好处:");
            appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("1. 有意义的线程名");
            appendLog("   • ImageLoader-1, ImageLoader-2");
            appendLog("   • 一眼就知道是图片加载线程");
            appendLog("");
            appendLog("2. 便于调试和监控");
            appendLog("   • Logcat 中容易过滤");
            appendLog("   • 性能分析工具中容易识别");
            appendLog("");
            appendLog("3. 统一的异常处理");
            appendLog("   • 捕获所有未处理的异常");
            appendLog("   • 记录日志或上报");
            appendLog("");
            appendLog("4. 灵活的线程配置");
            appendLog("   • 设置优先级（UI线程优先级更高）");
            appendLog("   • 设置守护线程（后台任务）");

            // 演示异常处理
            mainHandler.postDelayed(this::demoExceptionHandling, 1000);
        }, 2000);
    }

    private void demoExceptionHandling() {
        appendLog("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【演示：异常处理】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        appendLog("提交一个会抛异常的任务:\n");

        currentExecutor.execute(() -> {
            String threadName = Thread.currentThread().getName();
            appendLog(String.format(Locale.US,
                    "  任务开始执行 → 线程: [%s]", threadName));
            appendLog("  模拟发生异常...");

            // 故意抛出异常
            throw new RuntimeException("模拟的异常：网络连接失败");
        });

        mainHandler.postDelayed(() -> {
            appendLog("\n💡 注意:");
            appendLog("  • 异常被自定义的处理器捕获了");
            appendLog("  • 线程池不会崩溃");
            appendLog("  • 可以记录日志或上报到服务器\n");

            appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("📚 实际应用场景:");
            appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("• 图片加载: ImageLoader-1, ImageLoader-2");
            appendLog("• 网络请求: NetworkWorker-1, NetworkWorker-2");
            appendLog("• 数据库操作: DBWorker-1, DBWorker-2");
            appendLog("• 文件下载: Downloader-1, Downloader-2");
            appendLog("\n✅ 结论: 生产环境必须使用自定义线程工厂!");
        }, 1000);
    }

    private void demoRejectedHandler() {
        appendLog("=== rejectedHandler 拒绝策略 ===\n");
        appendLog("💡 类比: 餐厅爆满时的处理方式");
        appendLog("• 厨师都在忙 = 线程都在工作");
        appendLog("• 点餐架满了 = 队列满了");
        appendLog("• 新顾客来了 = 新任务提交");
        appendLog("• 怎么办？ = 拒绝策略\n");

        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【触发拒绝策略的条件】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("1. 所有核心线程都在忙");
        appendLog("2. 任务队列已满");
        appendLog("3. 已达到最大线程数");
        appendLog("4. 新任务提交 → 触发拒绝策略\n");

        // 演示1: AbortPolicy
        demoAbortPolicy();
    }

    private void demoAbortPolicy() {
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【1. AbortPolicy - 抛出异常】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 直接拒绝顾客，告诉他\"餐厅满了！\"\n");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.AbortPolicy()  // 默认策略
        );

        appendLog("配置: 核心=1, 最大=1, 队列=2");
        appendLog("提交4个任务（容量只有3）:\n");

        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    appendLog(String.format(Locale.US,
                            "  ✓ 任务%d 执行中", taskId));
                    simulateWork(500);
                });
                appendLog(String.format(Locale.US,
                        "  → 任务%d 提交成功", taskId));
            } catch (java.util.concurrent.RejectedExecutionException e) {
                appendLog(String.format(Locale.US,
                        "  ✗ 任务%d 被拒绝！抛出异常: %s", taskId, e.getClass().getSimpleName()));
            }
        }

        mainHandler.postDelayed(() -> {
            appendLog("\n📊 执行流程:");
            appendLog("  任务1: 线程执行");
            appendLog("  任务2: 进入队列 [1/2]");
            appendLog("  任务3: 进入队列 [2/2]");
            appendLog("  任务4: 队列满了！抛出异常");
            appendLog("\n✅ 优点: 明确知道任务被拒绝");
            appendLog("❌ 缺点: 需要捕获异常处理");
            appendLog("💡 适用: 不能丢失任务的场景\n");
            executor.shutdown();

            mainHandler.postDelayed(this::demoCallerRunsPolicy, 1000);
        }, 1000);
    }

    private void demoCallerRunsPolicy() {
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【2. CallerRunsPolicy - 调用者执行】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 让顾客自己进厨房做菜\n");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.CallerRunsPolicy()  // 调用者执行
        );

        appendLog("配置: 核心=1, 最大=1, 队列=2");
        appendLog("提交4个任务（容量只有3）:\n");

        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            String beforeThread = Thread.currentThread().getName();
            
            executor.execute(() -> {
                String executeThread = Thread.currentThread().getName();
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 执行中 [线程: %s]", 
                        taskId, executeThread));
                simulateWork(300);
            });
            
            String afterThread = Thread.currentThread().getName();
            if (beforeThread.equals(afterThread)) {
                appendLog(String.format(Locale.US,
                        "  → 任务%d 提交成功", taskId));
            } else {
                appendLog(String.format(Locale.US,
                        "  ⚠️ 任务%d 被拒绝，由调用线程 [%s] 执行", 
                        taskId, beforeThread));
            }
        }

        mainHandler.postDelayed(() -> {
            appendLog("\n📊 执行流程:");
            appendLog("  任务1: 工作线程执行");
            appendLog("  任务2: 进入队列 [1/2]");
            appendLog("  任务3: 进入队列 [2/2]");
            appendLog("  任务4: 队列满了！由主线程执行");
            appendLog("\n✅ 优点: 不会丢失任务，提供反压机制");
            appendLog("❌ 缺点: 可能阻塞调用线程（如UI线程）");
            appendLog("💡 适用: 后台任务，不在UI线程调用\n");
            executor.shutdown();

            mainHandler.postDelayed(this::demoDiscardPolicy, 1000);
        }, 1500);
    }

    private void demoDiscardPolicy() {
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【3. DiscardPolicy - 静默丢弃】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 假装没看见顾客，直接忽略\n");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.DiscardPolicy()  // 静默丢弃
        );

        appendLog("配置: 核心=1, 最大=1, 队列=2");
        appendLog("提交4个任务（容量只有3）:\n");

        AtomicInteger submittedCount = new AtomicInteger(0);
        AtomicInteger executedCount = new AtomicInteger(0);

        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            submittedCount.incrementAndGet();
            
            executor.execute(() -> {
                executedCount.incrementAndGet();
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 执行中", taskId));
                simulateWork(300);
            });
            
            appendLog(String.format(Locale.US,
                    "  → 任务%d 已提交（不知道是否被接受）", taskId));
        }

        mainHandler.postDelayed(() -> {
            appendLog(String.format(Locale.US,
                    "\n📊 统计: 提交%d个，执行%d个，丢失%d个",
                    submittedCount.get(),
                    executedCount.get(),
                    submittedCount.get() - executedCount.get()));
            
            appendLog("\n📊 执行流程:");
            appendLog("  任务1: 工作线程执行");
            appendLog("  任务2: 进入队列 [1/2]");
            appendLog("  任务3: 进入队列 [2/2]");
            appendLog("  任务4: 队列满了！静默丢弃（没有任何提示）");
            appendLog("\n✅ 优点: 不抛异常，不阻塞");
            appendLog("❌ 缺点: 任务丢失，无法感知");
            appendLog("💡 适用: 可以丢失的任务（如日志、统计）\n");
            executor.shutdown();

            mainHandler.postDelayed(this::demoDiscardOldestPolicy, 1000);
        }, 1500);
    }

    private void demoDiscardOldestPolicy() {
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【4. DiscardOldestPolicy - 丢弃最老任务】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 把排队最久的顾客赶走，让新顾客进来\n");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.DiscardOldestPolicy()  // 丢弃最老
        );

        appendLog("配置: 核心=1, 最大=1, 队列=2");
        appendLog("提交4个任务（容量只有3）:\n");

        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            
            executor.execute(() -> {
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 执行中", taskId));
                simulateWork(300);
            });
            
            appendLog(String.format(Locale.US,
                    "  → 任务%d 已提交", taskId));
        }

        mainHandler.postDelayed(() -> {
            appendLog("\n📊 执行流程:");
            appendLog("  任务1: 工作线程执行");
            appendLog("  任务2: 进入队列 [1/2]");
            appendLog("  任务3: 进入队列 [2/2]");
            appendLog("  任务4: 队列满了！丢弃任务2，任务4进入队列");
            appendLog("\n实际执行: 任务1 → 任务3 → 任务4");
            appendLog("被丢弃: 任务2（最老的）");
            appendLog("\n✅ 优点: 保证最新任务被执行");
            appendLog("❌ 缺点: 老任务丢失，可能不公平");
            appendLog("💡 适用: 只关心最新数据的场景（如实时监控）\n");
            executor.shutdown();

            mainHandler.postDelayed(this::demoCustomPolicy, 1000);
        }, 1500);
    }

    private void demoCustomPolicy() {
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("【5. 自定义拒绝策略】");
        appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        appendLog("比喻: 餐厅自己决定怎么处理\n");

        // 自定义策略：记录日志并重试
        RejectedExecutionHandler customHandler = (r, executor) -> {
            appendLog("  ⚠️ 任务被拒绝！执行自定义处理:");
            appendLog("     1. 记录日志");
            appendLog("     2. 发送通知");
            appendLog("     3. 保存到数据库，稍后重试");
            appendLog("     4. 或者降级处理");
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                customHandler  // 自定义策略
        );

        appendLog("配置: 核心=1, 最大=1, 队列=2");
        appendLog("提交4个任务（容量只有3）:\n");

        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            
            executor.execute(() -> {
                appendLog(String.format(Locale.US,
                        "  ✓ 任务%d 执行中", taskId));
                simulateWork(300);
            });
            
            if (i <= 3) {
                appendLog(String.format(Locale.US,
                        "  → 任务%d 已提交", taskId));
            }
        }

        mainHandler.postDelayed(() -> {
            appendLog("\n✅ 优点: 完全自定义，灵活处理");
            appendLog("💡 常见自定义策略:");
            appendLog("  • 记录日志 + 上报监控");
            appendLog("  • 保存到数据库，稍后重试");
            appendLog("  • 降级处理（返回默认值）");
            appendLog("  • 发送通知给开发者");
            
            appendLog("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("📚 总结对比:");
            appendLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            appendLog("AbortPolicy: 抛异常，明确失败");
            appendLog("CallerRunsPolicy: 调用者执行，提供反压");
            appendLog("DiscardPolicy: 静默丢弃，无感知");
            appendLog("DiscardOldestPolicy: 丢弃最老，保证最新");
            appendLog("自定义: 灵活处理，推荐使用");
            
            appendLog("\n💡 生产环境推荐:");
            appendLog("  1. CallerRunsPolicy（后台任务）");
            appendLog("  2. 自定义策略（记录+重试）");
            executor.shutdown();
        }, 1500);
    }

    private void demoRealWorldExample() {
        appendLog("=== 综合实战: 图片下载线程池配置 ===\n");

        int cpuCount = Runtime.getRuntime().availableProcessors();
        appendLog("设备CPU核心数: " + cpuCount);

        int corePoolSize = cpuCount + 1;
        int maxPoolSize = cpuCount * 2 + 1;
        long keepAliveTime = 30L;
        int queueCapacity = 128;

        appendLog(String.format(Locale.US,
                "\n推荐配置 (IO密集型):\n" +
                "• corePoolSize = %d (CPU核心数+1)\n" +
                "• maximumPoolSize = %d (CPU核心数*2+1)\n" +
                "• keepAliveTime = %d秒\n" +
                "• 队列容量 = %d\n",
                corePoolSize, maxPoolSize, keepAliveTime, queueCapacity));

        ThreadFactory imageThreadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ImageDownloader-" + count.getAndIncrement());
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            }
        };

        RejectedExecutionHandler imageRejectedHandler = (r, executor) -> {
            appendLog("⚠️ 任务被拒绝，队列已满，考虑增加队列容量或降低并发");
        };

        currentExecutor = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                imageThreadFactory, imageRejectedHandler
        );

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
                simulateWork(800 + (int)(Math.random() * 400));
                appendLog(String.format(Locale.US, "✅ 图片%d 下载完成", imageId));
            });
        }

        mainHandler.postDelayed(() -> {
            appendLog(String.format(Locale.US,
                    "\n📊 线程池统计:\n" +
                    "• 完成任务数: %d\n" +
                    "• 当前线程数: %d\n" +
                    "• 历史最大线程数: %d",
                    currentExecutor.getCompletedTaskCount(),
                    currentExecutor.getPoolSize(),
                    currentExecutor.getLargestPoolSize()));
        }, 5000);
    }

    // ==================== 工具方法 ====================

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

    private void testRejectedPolicy(RejectedExecutionHandler handler, String tag) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1), handler
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
            // 延迟滚动，确保 TextView 已经更新完成
            logContainer.postDelayed(() -> {
                logContainer.fullScroll(View.FOCUS_DOWN);
            }, 50);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentExecutor != null && !currentExecutor.isShutdown()) {
            currentExecutor.shutdownNow();
        }
    }
}
