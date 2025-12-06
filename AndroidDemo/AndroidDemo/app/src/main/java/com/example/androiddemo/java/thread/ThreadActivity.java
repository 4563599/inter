package com.example.androiddemo.java.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.androiddemo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ThreadActivity - Android 线程池完整使用指南
 *
 * ================================================================
 * 线程池详解 - 4种核心类型及使用场景
 * ================================================================
 *
 * 1. newFixedThreadPool(n) - 固定大小线程池
 *    ● 特点：核心线程数 = 最大线程数 = n，线程不会被回收
 *    ● 队列：LinkedBlockingQueue（无界队列）
 *    ● 适用场景：并发量可控的CPU密集型任务
 *    ● 实际应用：并行网络请求、批量图片处理、数据计算
 *    ● 优点：资源消耗可控，避免线程数过多
 *    ● 缺点：任务积压时内存可能溢出
 *
 * 2. newCachedThreadPool() - 缓存线程池
 *    ● 特点：核心线程数=0，最大线程数=Integer.MAX_VALUE
 *    ● 队列：SynchronousQueue（直接传递，不存储）
 *    ● 空闲超时：60秒后回收线程
 *    ● 适用场景：大量短时异步任务、突发性工作负载
 *    ● 实际应用：解码图片、短期IO操作、临时计算任务
 *    ● 优点：灵活扩展，处理突发请求
 *    ● 缺点：极端情况下可能创建过多线程导致OOM
 *
 * 3. newSingleThreadExecutor() - 单线程池
 *    ● 特点：只有1个工作线程，保证任务顺序执行
 *    ● 队列：LinkedBlockingQueue（无界队列）
 *    ● 适用场景：需要顺序执行的任务、避免并发冲突
 *    ● 实际应用：数据库写入、文件操作、日志记录、状态更新
 *    ● 优点：线程安全，任务按提交顺序执行
 *    ● 缺点：无法并行，处理速度受限
 *
 * 4. newScheduledThreadPool(n) - 定时线程池
 *    ● 特点：支持延时执行和周期性任务
 *    ● 核心方法：
 *      - schedule(task, delay, unit): 延时执行一次
 *      - scheduleAtFixedRate(task, initialDelay, period, unit): 固定频率执行
 *      - scheduleWithFixedDelay(task, initialDelay, delay, unit): 固定间隔执行
 *    ● 适用场景：定时任务、轮询检查、心跳包、缓存清理
 *    ● 实际应用：定时刷新数据、健康检查、数据同步
 *
 * ================================================================
 * Android线程池最佳实践
 * ================================================================
 *
 * 🔥 核心原则：
 * 1. 永远不要直接使用 new Thread() - 资源浪费且难以管理
 * 2. 根据任务特点选择合适的线程池类型
 * 3. 必须在生命周期结束时关闭线程池
 * 4. UI更新必须切换回主线程
 *
 * 📱 Android特殊考虑：
 * 1. 内存敏感 - 避免创建过多线程
 * 2. 生命周期管理 - Activity/Fragment销毁时清理资源
 * 3. 主线程更新 - 使用Handler或runOnUiThread()
 * 4. 网络权限 - 确保添加INTERNET权限
 *
 * 🚀 性能调优建议：
 * 1. CPU密集型：线程数 = CPU核心数 + 1
 * 2. IO密集型：线程数 = CPU核心数 * 2
 * 3. 混合型任务：根据实际测试调整
 * 4. 监控线程池状态，避免任务积压
 *
 * 🛡️ 资源管理：
 * 1. shutdown() - 平滑关闭，等待任务完成
 * 2. shutdownNow() - 立即关闭，中断正在执行的任务
 * 3. awaitTermination() - 等待线程池完全终止
 * 4. isShutdown() / isTerminated() - 检查状态
 *
 * ================================================================
 * 本示例演示了5个真实场景，涵盖所有线程池类型的典型用法
 * ================================================================
 */
public class ThreadActivity extends AppCompatActivity {

    // ==================== UI线程通信 ====================
    /** 主线程Handler - 用于从工作线程切换回主线程更新UI */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 日志构建器 - 收集所有执行日志，便于观察线程池行为 */
    private final StringBuilder logBuilder = new StringBuilder();

    // ==================== UI组件 ====================
    private TextView logView;      // 显示日志的文本视图
    private ScrollView logContainer; // 滚动容器，支持查看长日志

    // ==================== 四种线程池实例 ====================

    /**
     * 固定线程池 - 演示并发控制
     * 使用场景：并行网络请求、批量数据处理
     * 特点：线程数固定，避免资源过度消耗
     */
    private ExecutorService fixedThreadPool;

    /**
     * 缓存线程池 - 演示动态伸缩
     * 使用场景：大量短时任务、图片解码、临时计算
     * 特点：按需创建线程，空闲60秒后回收
     */
    private ExecutorService cachedThreadPool;

    /**
     * 单线程池 - 演示顺序执行
     * 使用场景：文件写入、数据库操作、日志记录
     * 特点：保证任务按提交顺序串行执行
     */
    private ExecutorService singleThreadExecutor;

    /**
     * 定时线程池 - 演示周期性任务
     * 使用场景：定时刷新、心跳检测、缓存清理
     * 特点：支持延时执行和固定频率执行
     */
    private ScheduledExecutorService scheduledExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContentView());
        appendLog("Thread pool demo is ready. Pick any scenario below.\n");
    }

    private View buildContentView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 48);

        Button manualButton = createButton("A. Manual threads: start 3 network calls", v -> startManualThreadsDownload());
        Button fixedButton = createButton("B. FixedThreadPool: parallel network calls", v -> startFixedThreadPoolDownload());
        Button cachedButton = createButton("C. CachedThreadPool: decode bitmap + write cache", v -> startCachedThreadPoolBitmapWork());
        Button singleButton = createButton("D. SingleThreadExecutor: serial disk writes", v -> startSingleThreadExecutorForDisk());
        Button scheduledButton = createButton("E. ScheduledThreadPool: poll cache stats", v -> startScheduledThreadPoolTask());
        Button clearButton = createButton("Clear log", v -> {
            logBuilder.setLength(0);
            logView.setText("");
        });

        root.addView(manualButton);
        root.addView(fixedButton);
        root.addView(cachedButton);
        root.addView(singleButton);
        root.addView(scheduledButton);
        root.addView(clearButton);

        logView = new TextView(this);
        logView.setTextSize(14f);
        logView.setTextIsSelectable(true);
        logView.setPadding(16, 16, 16, 16);

        logContainer = new ScrollView(this);
        logContainer.addView(logView);

        LinearLayout.LayoutParams logParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
        );
        logParams.weight = 1f;
        root.addView(logContainer, logParams);
        return root;
    }

    private Button createButton(String text, View.OnClickListener clickListener) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setOnClickListener(clickListener);
        return button;
    }

    /**
     * 演示场景A：手动创建线程的问题
     *
     * ❌ 不推荐的做法 - 直接使用new Thread()
     * 问题分析：
     * 1. 每个任务都创建新线程，资源开销大
     * 2. 无法控制并发数量，可能导致系统过载
     * 3. 线程创建和销毁频繁，影响性能
     * 4. 难以统一管理和监控线程状态
     * 5. 无法复用线程，浪费系统资源
     *
     * 实际项目中应该避免这种做法！
     */
    private void startManualThreadsDownload() {
        appendLog("=== 场景A：手动线程管理 (不推荐) ===");
        appendLog("问题：每个任务创建新线程，资源浪费且难以管理");

        // 为每个网络请求创建独立线程 - 这是错误的做法
        for (int i = 1; i <= 3; i++) {
            final int todoId = i;
            // ❌ 错误做法：每次都new Thread()
            Thread thread = new Thread(() -> performNetworkDownload("Manual-" + todoId, todoId),
                    "manual-thread-" + todoId);
            appendLog(String.format(Locale.US, "第%d步 -> 启动线程 %s", i, thread.getName()));
            thread.start();
        }
        appendLog("结论：手动管理线程在任务增多时会变得混乱且低效\n");
    }

    /**
     * 演示场景B：FixedThreadPool - 固定大小线程池
     *
     * ✅ 推荐用法 - 并发控制
     * 核心特点：
     * 1. 线程数量固定，不会无限增长
     * 2. 超出的任务会在队列中等待
     * 3. 适合CPU密集型任务或需要限制并发数的场景
     * 4. 线程会一直存活，避免频繁创建销毁
     *
     * 最佳实践：
     * - CPU密集型：线程数 = CPU核心数 + 1
     * - IO密集型：可以设置更多线程
     * - 网络请求：根据服务器承受能力设置
     */
    private void startFixedThreadPoolDownload() {
        appendLog("=== 场景B：FixedThreadPool 固定线程池 (大小=2) ===");

        // 先关闭之前的线程池实例（避免资源泄露）
        shutdownExecutor(fixedThreadPool);

        // 创建固定大小为2的线程池
        fixedThreadPool = Executors.newFixedThreadPool(2);
        appendLog("✅ 创建固定线程池：核心线程数=2, 最大线程数=2");

        appendLog("第1步：提交4个网络请求任务，但线程池限制并发数为2");

        // 提交4个任务，但只有2个线程并发执行
        for (int i = 1; i <= 4; i++) {
            final int todoId = i;
            fixedThreadPool.execute(() -> performNetworkDownload("FixedPool-" + todoId, todoId));
            appendLog(String.format(Locale.US, "  -> 提交任务%d到线程池", todoId));
        }

        appendLog("第2步：调用shutdown()优雅关闭，等待队列中的任务完成");
        appendLog("观察：同时只有2个任务在执行，其他任务在队列等待\n");

        // 优雅关闭：不接受新任务，但会执行完队列中的任务
        fixedThreadPool.shutdown();
    }

    /**
     * 演示场景C：CachedThreadPool - 缓存线程池
     *
     * ✅ 推荐用法 - 处理突发短任务
     * 核心特点：
     * 1. 核心线程数=0，最大线程数=Integer.MAX_VALUE
     * 2. 使用SynchronousQueue，任务直接传递给线程
     * 3. 空闲线程60秒后自动回收
     * 4. 适合大量短时间的异步任务
     *
     * 适用场景：
     * - 图片解码、文件处理等短时IO操作
     * - 突发性任务处理
     * - 任务执行时间短且不可预测的场景
     *
     * ⚠️ 注意：极端情况下可能创建大量线程
     */
    private void startCachedThreadPoolBitmapWork() {
        appendLog("=== 场景C：CachedThreadPool 缓存线程池 (适合短时突发任务) ===");

        // 关闭旧实例
        shutdownExecutor(cachedThreadPool);

        // 创建缓存线程池
        cachedThreadPool = Executors.newCachedThreadPool();
        appendLog("✅ 创建缓存线程池：按需创建线程，空闲60秒后回收");

        appendLog("第1步：提交5个图片处理任务（解码应用图标并保存到缓存）");

        // 提交5个短时间的图片处理任务
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            cachedThreadPool.execute(() -> decodeAndSaveBitmap(index));
            appendLog(String.format(Locale.US, "  -> 提交图片处理任务%d", index));
        }

        appendLog("第2步：缓存线程池特性演示");
        appendLog("  - 如果有空闲线程立即复用");
        appendLog("  - 没有空闲线程则创建新线程");
        appendLog("  - 空闲超过60秒的线程会被自动回收");
        appendLog("观察：线程名称可能会重复使用（线程复用）\n");

        cachedThreadPool.shutdown();
    }

    /**
     * 演示场景D：SingleThreadExecutor - 单线程执行器
     *
     * ✅ 推荐用法 - 保证任务顺序执行
     * 核心特点：
     * 1. 只有1个工作线程，永远不会并发执行
     * 2. 任务按照提交顺序严格执行（FIFO）
     * 3. 使用无界队列LinkedBlockingQueue存储等待任务
     * 4. 线程异常终止时会创建新线程继续工作
     *
     * 适用场景：
     * - 数据库写入操作（避免并发冲突）
     * - 文件写入操作（保证数据完整性）
     * - 日志记录（按时间顺序）
     * - 状态更新（避免竞态条件）
     *
     * 优势：线程安全、简单可靠
     * 劣势：无法利用多核并行处理
     */
    private void startSingleThreadExecutorForDisk() {
        appendLog("=== 场景D：SingleThreadExecutor 单线程执行器 (保证顺序) ===");

        // 关闭旧实例
        shutdownExecutor(singleThreadExecutor);

        // 创建单线程执行器
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        appendLog("✅ 创建单线程执行器：确保任务按提交顺序串行执行");

        appendLog("第1步：提交3个文件写入任务，观察执行顺序");

        // 提交3个文件写入任务，必须按顺序执行
        for (int i = 1; i <= 3; i++) {
            final int index = i;
            singleThreadExecutor.execute(() -> writeCacheFile(index));
            appendLog(String.format(Locale.US, "  -> 提交写入任务%d到队列", index));
        }

        appendLog("第2步：单线程执行器特性说明");
        appendLog("  - 所有任务使用同一个线程执行");
        appendLog("  - 任务严格按提交顺序执行（先进先出）");
        appendLog("  - 适合需要避免并发冲突的场景");
        appendLog("观察：所有任务的线程名称相同\n");

        singleThreadExecutor.shutdown();
    }

    /**
     * 演示场景E：ScheduledThreadPool - 定时线程池
     *
     * ✅ 推荐用法 - 定时和周期性任务
     * 核心特点：
     * 1. 支持延时执行任务
     * 2. 支持固定频率和固定间隔的周期执行
     * 3. 基于时间轮算法，性能优于Timer
     * 4. 线程异常不会影响其他定时任务
     *
     * 核心方法对比：
     * - schedule(task, delay, unit): 延时执行一次
     * - scheduleAtFixedRate(): 固定频率执行（不考虑任务耗时）
     * - scheduleWithFixedDelay(): 固定间隔执行（任务完成后等待）
     *
     * 适用场景：
     * - 定时数据同步和刷新
     * - 系统健康检查和监控
     * - 缓存过期清理
     * - 心跳包发送
     * - 定时统计和报告
     *
     * 与Timer对比的优势：
     * - 多线程执行，性能更好
     * - 异常处理更稳定
     * - 更灵活的调度选项
     */
    private void startScheduledThreadPoolTask() {
        appendLog("=== 场景E：ScheduledThreadPool 定时线程池 (每2秒轮询) ===");

        // 关闭旧实例
        shutdownExecutor(scheduledExecutor);

        // 创建定时线程池，核心线程数为1
        scheduledExecutor = Executors.newScheduledThreadPool(1);
        appendLog("✅ 创建定时线程池：支持延时和周期性任务执行");

        appendLog("第1步：安排立即执行，然后每2秒重复执行的缓存统计任务");

        // 使用scheduleAtFixedRate实现固定频率执行
        // 参数：任务、初始延时、执行周期、时间单位
        scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            private int counter = 0;

            @Override
            public void run() {
                counter++;
                CacheStats stats = calculateCacheStats();
                appendLog(String.format(Locale.US,
                        "  -> 第%d次检查: 缓存有%d个文件, 总大小%.1f KB",
                        counter, stats.fileCount, stats.totalBytes / 1024f));

                // 执行5次后自动停止
                if (counter >= 5) {
                    appendLog("  -> 完成5次统计，停止定时任务");
                    appendLog("第2步：定时线程池特性总结");
                    appendLog("  - scheduleAtFixedRate: 固定频率，不受任务耗时影响");
                    appendLog("  - scheduleWithFixedDelay: 固定间隔，任务完成后再等待");
                    appendLog("  - 适合定时数据刷新、健康检查等场景\n");
                    shutdownExecutor(scheduledExecutor);
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
        // 参数说明：0=立即开始, 2=每隔2秒, TimeUnit.SECONDS=时间单位秒
    }

    /**
     * 网络请求任务 - 模拟真实的网络IO操作
     *
     * 这是一个典型的IO密集型任务示例：
     * 1. 建立网络连接（等待时间）
     * 2. 发送HTTP请求（网络传输）
     * 3. 接收响应数据（网络传输）
     * 4. 解析响应内容（少量CPU处理）
     *
     * 线程池使用建议：
     * - 网络请求适合使用FixedThreadPool限制并发数
     * - 避免同时发起过多请求导致服务器压力
     * - CachedThreadPool在突发请求场景下也适用
     * - 不要使用SingleThreadExecutor，会严重影响性能
     *
     * @param taskName 任务标识名称
     * @param todoId 请求的资源ID
     */
    private void performNetworkDownload(String taskName, int todoId) {
        String urlString = "https://jsonplaceholder.typicode.com/todos/" + todoId;
        long start = System.currentTimeMillis();
        HttpURLConnection connection = null;
        try {
            // 建立HTTP连接
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setConnectTimeout(5000);  // 连接超时5秒
            connection.setReadTimeout(5000);     // 读取超时5秒

            // 获取响应流并读取数据
            InputStream inputStream = connection.getInputStream();
            String response = readStream(inputStream);

            // 计算耗时并记录结果
            long duration = System.currentTimeMillis() - start;
            appendLog(String.format(Locale.US,
                    "%s 成功: 长度=%d字符, 耗时=%d毫秒, 线程=%s",
                    taskName, response.length(), duration, Thread.currentThread().getName()));
        } catch (IOException e) {
            // 网络异常处理
            appendLog(taskName + " 失败: " + e.getMessage());
        } finally {
            // 确保连接被正确关闭，避免资源泄露
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 图片解码和保存任务 - 演示CPU+IO混合型任务
     *
     * 任务特点分析：
     * 1. 图片解码：CPU密集型操作，消耗计算资源
     * 2. 文件写入：IO密集型操作，主要等待磁盘写入
     * 3. 执行时间短：适合CachedThreadPool的突发处理能力
     * 4. 可并行：多个图片可以同时处理，无依赖关系
     *
     * 线程池选择建议：
     * - CachedThreadPool：适合这种短时间突发任务
     * - FixedThreadPool：如果要控制同时处理的图片数量
     * - 避免SingleThread：会导致图片依次处理，效率低
     *
     * Android内存管理：
     * - 及时调用bitmap.recycle()释放内存
     * - 避免同时解码大量大尺寸图片
     *
     * @param index 图片处理任务的序号
     */
    private void decodeAndSaveBitmap(int index) {
        // 第1步：解码应用图标资源（CPU密集型操作）
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        // 第2步：创建缓存文件路径（包含时间戳避免冲突）
        File outFile = new File(getCacheDir(), "cached_pool_" + index + "_" + System.currentTimeMillis() + ".png");

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            // 第3步：压缩图片并写入文件（IO密集型操作）
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            appendLog(String.format(Locale.US,
                    "  -> 图片%d已保存: %s (线程=%s)",
                    index, outFile.getName(), Thread.currentThread().getName()));
        } catch (IOException e) {
            // 文件写入异常处理
            appendLog("  -> 图片" + index + "保存失败: " + e.getMessage());
        } finally {
            // 第4步：重要！释放Bitmap内存，避免OOM
            bitmap.recycle();
        }
    }

    /**
     * 文件写入任务 - 演示典型的串行IO操作
     *
     * 为什么需要单线程执行：
     * 1. 数据一致性：避免多线程同时写入造成数据混乱
     * 2. 文件锁定：某些文件系统不支持并发写入
     * 3. 顺序保证：日志需要按时间顺序记录
     * 4. 简化逻辑：避免复杂的并发控制代码
     *
     * 适用SingleThreadExecutor的场景：
     * - 数据库事务操作
     * - 日志文件写入
     * - 配置文件更新
     * - 状态持久化
     * - 消息队列处理
     *
     * 性能考虑：
     * - 虽然无法并行，但避免了线程竞争开销
     * - 适合IO为主、计算量小的任务
     * - 任务间有顺序依赖时的最佳选择
     *
     * @param index 写入任务的序号
     */
    private void writeCacheFile(int index) {
        // 第1步：创建文件路径（每个任务写入独立文件）
        File outFile = new File(getCacheDir(), "single_thread_" + index + ".txt");

        // 第2步：准备写入内容（包含任务序号和时间戳）
        String content = "日志条目 " + index + " 写入时间: " + System.currentTimeMillis() + "\n";

        try (FileOutputStream fos = new FileOutputStream(outFile, true)) {
            // 第3步：将内容写入文件（IO操作，在单线程中串行执行）
            fos.write(content.getBytes());
            appendLog(String.format(Locale.US,
                    "  -> 写入完成: %s (线程=%s)",
                    outFile.getName(), Thread.currentThread().getName()));
        } catch (IOException e) {
            // 文件写入异常处理
            appendLog("  -> 写入失败: " + e.getMessage());
        }

        // 注意：观察日志中的线程名称，所有任务都使用同一个线程执行
        // 这确保了写入操作的顺序性和线程安全性
    }

    private CacheStats calculateCacheStats() {
        File[] files = getCacheDir().listFiles();
        if (files == null) {
            return new CacheStats(0, 0);
        }
        long totalBytes = 0;
        for (File file : files) {
            totalBytes += file.length();
        }
        return new CacheStats(files.length, totalBytes);
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    private void appendLog(String text) {
        mainHandler.post(() -> {
            logBuilder.append(text).append('\n');
            logView.setText(logBuilder.toString());
            logContainer.post(() -> logContainer.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void shutdownExecutor(@Nullable ExecutorService service) {
        if (service != null && !service.isShutdown()) {
            service.shutdownNow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdownExecutor(fixedThreadPool);
        shutdownExecutor(cachedThreadPool);
        shutdownExecutor(singleThreadExecutor);
        shutdownExecutor(scheduledExecutor);
    }

    private static class CacheStats {
        final int fileCount;
        final long totalBytes;

        CacheStats(int fileCount, long totalBytes) {
            this.fileCount = fileCount;
            this.totalBytes = totalBytes;
        }
    }
}
