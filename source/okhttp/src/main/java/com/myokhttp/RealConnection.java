package com.myokhttp;

import java.io.*;
import java.net.Socket;

/**
 * 真实的 HTTP 连接
 * 封装了 Socket 连接
 * 
 * 核心功能：
 * 1. 建立 TCP 连接
 * 2. 管理连接状态（空闲/使用中）
 * 3. 提供输入输出流
 */
public class RealConnection implements Closeable {
    
    private final String host;
    private final int port;
    private Socket socket;
    
    private InputStream inputStream;
    private OutputStream outputStream;
    
    // 引用计数：表示有多少个请求正在使用此连接
    private int referenceCount = 0;
    
    // 空闲开始时间（纳秒）
    private long idleAtNanos;
    
    private boolean closed = false;

    public RealConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 建立连接
     */
    public void connect(int connectTimeout, int readTimeout) throws IOException {
        if (socket != null) {
            throw new IllegalStateException("已经连接");
        }

        // 创建 Socket 并连接
        socket = new Socket();
        socket.setSoTimeout(readTimeout);
        socket.connect(new java.net.InetSocketAddress(host, port), connectTimeout);

        // 获取输入输出流
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        System.out.println("已建立连接: " + host + ":" + port);
    }

    /**
     * 获取输入流
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 获取输出流
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * 增加引用计数
     */
    public void acquire() {
        referenceCount++;
    }

    /**
     * 减少引用计数
     */
    public void release() {
        referenceCount--;
        if (referenceCount == 0) {
            idleAtNanos = System.nanoTime();
        }
    }

    /**
     * 检查是否正在使用
     */
    public boolean isInUse() {
        return referenceCount > 0;
    }

    /**
     * 获取空闲开始时间
     */
    public long getIdleAtNanos() {
        return idleAtNanos;
    }

    /**
     * 检查连接是否已关闭
     */
    public boolean isClosed() {
        return closed || socket == null || socket.isClosed();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        
        closed = true;
        
        if (socket != null) {
            socket.close();
        }
        
        System.out.println("已关闭连接: " + host + ":" + port);
    }

    @Override
    public String toString() {
        return "RealConnection{" + host + ":" + port + ", refs=" + referenceCount + "}";
    }
}

