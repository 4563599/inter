package com.myokhttp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 调度器：管理异步请求的执行
 * 
 * 职责：
 * 1. 管理线程池
 * 2. 控制并发请求数量
 * 3. 管理等待队列
 */
public class Dispatcher {
    
    // 最大并发请求数
    private int maxRequests = 64;
    
    // 每个主机最大并发请求数
    private int maxRequestsPerHost = 5;
    
    // 线程池
    private ExecutorService executorService;
    
    // 正在运行的异步请求
    private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    
    // 等待执行的异步请求
    private final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    
    // 正在运行的同步请求
    private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();

    /**
     * 获取或创建线程池
     */
    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(
                0,                      // 核心线程数
                Integer.MAX_VALUE,      // 最大线程数
                60,                     // 线程空闲存活时间
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                threadFactory("OkHttp Dispatcher")
            );
        }
        return executorService;
    }

    /**
     * 创建线程工厂
     */
    private java.util.concurrent.ThreadFactory threadFactory(final String name) {
        return new java.util.concurrent.ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, name);
                thread.setDaemon(false);
                return thread;
            }
        };
    }

    /**
     * 添加异步请求到队列
     */
    public synchronized void enqueue(RealCall.AsyncCall call) {
        if (runningAsyncCalls.size() < maxRequests) {
            // 如果未达到最大并发数，直接执行
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            // 否则加入等待队列
            readyAsyncCalls.add(call);
        }
    }

    /**
     * 同步请求开始执行
     */
    public synchronized void executed(RealCall call) {
        runningSyncCalls.add(call);
    }

    /**
     * 同步请求执行完成
     */
    public synchronized void finished(RealCall call) {
        runningSyncCalls.remove(call);
    }

    /**
     * 异步请求执行完成
     */
    public synchronized void finished(RealCall.AsyncCall call) {
        runningAsyncCalls.remove(call);
        
        // 尝试从等待队列中取出下一个请求执行
        promoteAndExecute();
    }

    /**
     * 从等待队列中提升请求到执行队列
     */
    private void promoteAndExecute() {
        if (runningAsyncCalls.size() >= maxRequests) {
            return; // 已达到最大并发数
        }

        if (readyAsyncCalls.isEmpty()) {
            return; // 等待队列为空
        }

        // 从等待队列取出请求
        RealCall.AsyncCall call = readyAsyncCalls.removeFirst();
        runningAsyncCalls.add(call);
        executorService().execute(call);
    }

    /**
     * 取消所有请求
     */
    public synchronized void cancelAll() {
        for (RealCall.AsyncCall call : readyAsyncCalls) {
            call.get().cancel();
        }
        
        for (RealCall.AsyncCall call : runningAsyncCalls) {
            call.get().cancel();
        }
        
        for (RealCall call : runningSyncCalls) {
            call.cancel();
        }
    }

    /**
     * 获取正在运行的请求数量
     */
    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }

    /**
     * 获取等待执行的请求数量
     */
    public synchronized int queuedCallsCount() {
        return readyAsyncCalls.size();
    }

    public synchronized void setMaxRequests(int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        this.maxRequests = maxRequests;
    }

    public synchronized int getMaxRequests() {
        return maxRequests;
    }

    public synchronized void setMaxRequestsPerHost(int maxRequestsPerHost) {
        if (maxRequestsPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
        }
        this.maxRequestsPerHost = maxRequestsPerHost;
    }

    public synchronized int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }
}

