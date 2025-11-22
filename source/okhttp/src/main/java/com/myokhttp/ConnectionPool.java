package com.myokhttp;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * 连接池：管理和复用 HTTP 连接
 * 
 * 核心思想：
 * 1. HTTP/1.1 支持 Keep-Alive，可以在一个 TCP 连接上发送多个请求
 * 2. 建立 TCP 连接的开销很大（三次握手 + TLS 握手）
 * 3. 连接池可以显著提升性能
 * 
 * 工作流程：
 * 1. 需要连接时，先从池中查找可复用的连接
 * 2. 如果没有，创建新连接
 * 3. 使用完毕后，放回池中
 * 4. 定期清理空闲连接
 */
public class ConnectionPool {
    
    // 最大空闲连接数
    private final int maxIdleConnections;
    
    // 连接保持时间（纳秒）
    private final long keepAliveDurationNs;
    
    // 连接池
    private final Deque<RealConnection> connections = new ArrayDeque<>();
    
    // 清理任务
    private final Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                long waitNanos = cleanup(System.nanoTime());
                if (waitNanos == -1) {
                    return; // 没有连接，退出
                }
                if (waitNanos > 0) {
                    try {
                        Thread.sleep(TimeUnit.NANOSECONDS.toMillis(waitNanos));
                    } catch (InterruptedException e) {
                        // 忽略
                    }
                }
            }
        }
    };
    
    private Thread cleanupThread;

    /**
     * 创建连接池
     * 
     * @param maxIdleConnections 最大空闲连接数
     * @param keepAliveDuration 连接保持时间
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

    /**
     * 获取一个可用的连接
     * 
     * @param host 主机名
     * @param port 端口
     * @return 可复用的连接，如果没有返回 null
     */
    public synchronized RealConnection get(String host, int port) {
        Iterator<RealConnection> it = connections.iterator();
        while (it.hasNext()) {
            RealConnection connection = it.next();
            
            // 检查连接是否可用
            if (connection.isClosed()) {
                it.remove();
                continue;
            }
            
            // 检查主机和端口是否匹配
            if (connection.getHost().equals(host) && connection.getPort() == port) {
                // 标记为使用中
                connection.acquire();
                return connection;
            }
        }
        
        return null;
    }

    /**
     * 将连接放回池中
     */
    public synchronized void put(RealConnection connection) {
        // 启动清理线程（如果还没启动）
        if (cleanupThread == null) {
            cleanupThread = new Thread(cleanupRunnable, "OkHttp ConnectionPool");
            cleanupThread.setDaemon(true);
            cleanupThread.start();
        }
        
        // 释放连接
        connection.release();
        
        // 将连接加入池中
        connections.add(connection);
    }

    /**
     * 清理空闲连接
     * 
     * @param now 当前时间（纳秒）
     * @return 下次清理需要等待的时间（纳秒），如果没有连接返回 -1
     */
    private synchronized long cleanup(long now) {
        int inUseConnectionCount = 0;
        int idleConnectionCount = 0;
        RealConnection longestIdleConnection = null;
        long longestIdleDurationNs = Long.MIN_VALUE;

        // 遍历所有连接，找出最长空闲时间的连接
        for (RealConnection connection : connections) {
            // 检查是否正在使用
            if (connection.isInUse()) {
                inUseConnectionCount++;
                continue;
            }

            idleConnectionCount++;

            // 计算空闲时间
            long idleDurationNs = now - connection.getIdleAtNanos();
            if (idleDurationNs > longestIdleDurationNs) {
                longestIdleDurationNs = idleDurationNs;
                longestIdleConnection = connection;
            }
        }

        // 如果空闲时间超过限制，或者空闲连接数超过最大值，关闭最长空闲的连接
        if (longestIdleDurationNs >= this.keepAliveDurationNs
            || idleConnectionCount > this.maxIdleConnections) {
            connections.remove(longestIdleConnection);
            try {
                longestIdleConnection.close();
            } catch (IOException e) {
                // 忽略关闭异常
            }
            
            // 立即进行下一次清理
            return 0;
        } else if (idleConnectionCount > 0) {
            // 有空闲连接，等待到最长空闲连接过期
            return keepAliveDurationNs - longestIdleDurationNs;
        } else if (inUseConnectionCount > 0) {
            // 所有连接都在使用中，等待 keepAliveDuration 后再检查
            return keepAliveDurationNs;
        } else {
            // 没有连接，停止清理线程
            cleanupThread = null;
            return -1;
        }
    }

    /**
     * 获取连接数
     */
    public synchronized int connectionCount() {
        return connections.size();
    }

    /**
     * 获取空闲连接数
     */
    public synchronized int idleConnectionCount() {
        int count = 0;
        for (RealConnection connection : connections) {
            if (!connection.isInUse()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 关闭所有连接
     */
    public synchronized void evictAll() {
        for (RealConnection connection : connections) {
            try {
                connection.close();
            } catch (IOException e) {
                // 忽略
            }
        }
        connections.clear();
    }
}

