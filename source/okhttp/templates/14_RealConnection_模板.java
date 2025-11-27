package com.myokhttp;

import java.io.*;
import java.net.Socket;

/**
 * 真实的HTTP连接
 * <p>
 * 【为什么需要这个类？】
 * - 封装Socket连接
 * - 管理连接的状态（空闲/使用中）
 * - 提供输入输出流
 * - 支持连接复用
 * <p>
 * 【核心功能】
 * 1. 建立TCP连接
 * 2. 管理引用计数（记录有多少请求在使用）
 * 3. 管理空闲时间（用于连接池清理）
 * 4. 提供输入输出流
 * <p>
 * 【连接的生命周期】
 * 创建 → 连接 → 使用 → 空闲 → 复用或关闭
 * new   connect() acquire() release()  close()
 * <p>
 * 【引用计数的作用】
 * - 0：空闲，可以被复用或清理
 * - >0：使用中，不能被清理
 *
 * @author Your Name
 */
public class RealConnection implements Closeable {

    // ========== 连接信息 ==========

    /**
     * 主机名
     * <p>
     * 【用途】
     * - 连接池匹配：host和port相同才能复用
     */
    private final String host;

    /**
     * 端口号
     */
    private final int port;

    /**
     * Socket连接
     * <p>
     * 【什么是Socket？】
     * - TCP连接的抽象
     * - 提供输入输出流
     * - 用于网络通信
     */
    private Socket socket;

    /**
     * 输入流
     * <p>
     * 【用途】
     * - 读取服务器返回的数据
     */
    private InputStream inputStream;

    /**
     * 输出流
     * <p>
     * 【用途】
     * - 发送请求到服务器
     */
    private OutputStream outputStream;

    // ========== 状态管理 ==========

    /**
     * 引用计数
     * <p>
     * 【作用】
     * - 记录有多少个请求正在使用此连接
     * - 0：空闲
     * - >0：使用中
     * <p>
     * 【为什么需要引用计数？】
     * - 防止正在使用的连接被清理
     * - 知道何时连接变为空闲
     */
    private int referenceCount = 0;

    /**
     * 空闲开始时间（纳秒）
     * <p>
     * 【作用】
     * - 记录连接何时变为空闲
     * - 连接池清理时判断是否超时
     * <p>
     * 【为什么用纳秒？】
     * - 精度更高
     * - System.nanoTime()比currentTimeMillis()更适合计时
     */
    private long idleAtNanos;

    /**
     * 是否已关闭
     * <p>
     * 【作用】
     * - 防止重复关闭
     * - 标记连接不可用
     */
    private boolean closed = false;

    // ========== 构造函数 ==========

    /**
     * 构造函数
     *
     * @param host 主机名
     * @param port 端口号
     */
    public RealConnection(String host, int port) {
        // TODO: 初始化字段
        this.host = host;
        this.port = port;

    }

    // ========== 连接管理 ==========

    /**
     * 建立连接
     * <p>
     * 【执行流程】
     * 1. 检查是否已连接
     * 2. 创建Socket
     * 3. 设置超时
     * 4. 连接到服务器（TCP三次握手）
     * 5. 获取输入输出流
     * <p>
     * 【TCP三次握手】
     * 客户端 → SYN → 服务器
     * 客户端 ← SYN-ACK ← 服务器
     * 客户端 → ACK → 服务器
     * 连接建立！
     * <p>
     * 【耗时】
     * - 本地网络：1-10ms
     * - 外网：50-200ms
     * - 这就是为什么要复用连接！
     *
     * @param connectTimeout 连接超时（毫秒）
     * @param readTimeout    读取超时（毫秒）
     * @throws IOException 连接失败时抛出
     */
    public void connect(int connectTimeout, int readTimeout) throws IOException {
        if (socket != null) {
            throw new IllegalStateException("已经连接过了");
        }

        // 1. 创建一个无连接的 Socket
        socket = new Socket();

        // 2. 设置读取超时（防止读数据卡死）
        socket.setSoTimeout(readTimeout);

        // 3. 开始连接！(这里会阻塞，直到三次握手完成)
        // 这里的 InetSocketAddress 会触发 DNS 解析（如果是域名的话）
        socket.connect(new InetSocketAddress(host, port), connectTimeout);

        // 4. 握手成功！拿到输入输出流，准备干活
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        System.out.println("✅ TCP 连接建立成功: " + host + ":" + port);
    }

    /**
     * 获取输入流
     * <p>
     * 【用途】
     * - CallServerInterceptor用来读取响应
     *
     * @return 输入流
     */
    public InputStream getInputStream() {
        // TODO: 返回 inputStream
        return inputStream;
    }

    /**
     * 获取输出流
     * <p>
     * 【用途】
     * - CallServerInterceptor用来发送请求
     *
     * @return 输出流
     */
    public OutputStream getOutputStream() {
        // TODO: 返回 outputStream
        return outputStream;
    }

    // ========== 引用计数管理 ==========

    /**
     * 增加引用计数
     * <p>
     * 【什么时候调用？】
     * - ConnectInterceptor获取到连接时
     * - 表示开始使用这个连接
     * <p>
     * 【效果】
     * - referenceCount++
     * - 连接变为"使用中"状态
     * - 不会被连接池清理
     */
    public void acquire() {
        // TODO: 引用计数+1
        referenceCount++;
        System.out.println("🔗 连接被借出: " + this);

    }

    /**
     * 减少引用计数
     * <p>
     * 【什么时候调用？】
     * - ConnectInterceptor使用完连接时
     * - 表示释放这个连接
     * <p>
     * 【效果】
     * - referenceCount--
     * - 如果变为0，记录空闲开始时间
     * - 连接可以被复用或清理
     */
    public void release() {
        referenceCount--;
        if (referenceCount == 0) {
            // 如果没人用了，记下当前时间，开始计算空闲时长
            idleAtNanos = System.nanoTime();
            System.out.println("⏳ 连接变为空闲: " + this);
        }

    }

    /**
     * 检查是否正在使用
     * <p>
     * 【用途】
     * - 连接池判断连接是否可以清理
     * - 正在使用的连接不能清理
     *
     * @return true表示正在使用，false表示空闲
     */
    public boolean isInUse() {
        // TODO: return referenceCount > 0;
        return referenceCount > 0;
    }

    /**
     * 获取空闲开始时间
     * <p>
     * 【用途】
     * - 连接池计算连接空闲了多久
     * - 判断是否需要清理
     *
     * @return 空闲开始时间（纳秒）
     */
    public long getIdleAtNanos() {
        // TODO: 返回 idleAtNanos
        return idleAtNanos;
    }

    // ========== 状态检查 ==========

    /**
     * 检查连接是否已关闭
     * <p>
     * 【判断标准】
     * - closed标志为true
     * - 或Socket为null
     * - 或Socket已关闭
     *
     * @return true表示已关闭
     */
    public boolean isClosed() {
        // TODO: return closed || socket == null || socket.isClosed();
        return closed || (socket != null && socket.isClosed());
    }

    /**
     * 获取主机名
     */
    public String getHost() {
        // TODO: 返回 host
        return host;
    }

    /**
     * 获取端口号
     */
    public int getPort() {
        // TODO: 返回 port
        return port;
    }

    // ========== 关闭连接 ==========

    /**
     * 关闭连接
     * <p>
     * 【什么时候调用？】
     * - 连接池清理空闲连接时
     * - 连接发生错误时
     * - 应用关闭时
     * <p>
     * 【执行流程】
     * 1. 检查是否已关闭
     * 2. 设置closed标志
     * 3. 关闭Socket
     * 4. 打印日志
     * <p>
     * 【注意】
     * - 可以重复调用（幂等性）
     * - 关闭后连接不可用
     *
     * @throws IOException 关闭失败时抛出
     */
    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        if (socket != null) {
            socket.close(); // 关闭 Socket，断开 TCP 连接（四次挥手）
            System.out.println("❌ 连接已关闭: " + host + ":" + port);
        }
    }

    @Override
    public String toString() {
        return "RealConnection{" + host + ":" + port + ", refs=" + referenceCount + "}";
    }
}

/*
【编写提示】

1. 【理解引用计数】
   
   引用计数的变化：
   ```
   创建连接：referenceCount = 0
   获取连接：acquire() → referenceCount = 1（使用中）
   使用完毕：release() → referenceCount = 0（空闲）
   再次获取：acquire() → referenceCount = 1（复用！）
   ```
   
   为什么需要引用计数？
   ```
   线程1正在使用连接：referenceCount = 1
   清理线程检查：isInUse() = true，不清理
   线程1使用完毕：release() → referenceCount = 0
   清理线程检查：isInUse() = false，可以清理
   ```

2. 【Socket的基础知识】
   
   什么是Socket？
   - TCP连接的编程接口
   - 提供输入输出流
   - 用于网络通信
   
   创建和使用：
   ```java
   Socket socket = new Socket();
   socket.connect(new InetSocketAddress(host, port), timeout);
   InputStream in = socket.getInputStream();    // 读取
   OutputStream out = socket.getOutputStream(); // 写入
   socket.close();  // 关闭
   ```

3. 【为什么用System.nanoTime()？】
   
   System.currentTimeMillis()：
   - 返回当前时间戳
   - 受系统时间调整影响
   - 不适合计时
   
   System.nanoTime()：
   - 返回相对时间
   - 不受系统时间影响
   - 适合计时
   
   使用示例：
   ```java
   long start = System.nanoTime();
   // 做某事...
   long duration = System.nanoTime() - start;
   ```

4. 【连接的状态转换】
   
   ```
   [创建] --connect()--> [已连接]
                            |
                     acquire()
                            |
                            v
                        [使用中] --release()--> [空闲]
                            ^                      |
                            |____acquire()_________|
                                    (复用)
                                       |
                                 close()
                                       v
                                   [已关闭]
   ```

【使用示例】

```java
// 创建连接
RealConnection connection = new RealConnection("example.com", 80);

// 建立连接
connection.connect(10000, 10000);

// 开始使用
connection.acquire();
System.out.println("正在使用：" + connection.isInUse());  // true

// 获取流
OutputStream out = connection.getOutputStream();
out.write("GET / HTTP/1.1\r\n\r\n".getBytes());

InputStream in = connection.getInputStream();
// 读取响应...

// 使用完毕
connection.release();
System.out.println("正在使用：" + connection.isInUse());  // false

// 可以复用
connection.acquire();
// 再次使用...
connection.release();

// 关闭
connection.close();
```

【常见错误】

❌ 错误1：忘记release()
```java
connection.acquire();
// 使用连接...
// 忘记release()
// 结果：连接永远是"使用中"，无法被清理
```

❌ 错误2：多次acquire()但只release()一次
```java
connection.acquire();  // refs = 1
connection.acquire();  // refs = 2
connection.release();  // refs = 1（还是使用中！）
```

❌ 错误3：关闭后继续使用
```java
connection.close();
connection.getInputStream();  // 错误！连接已关闭
```

【测试方法】
```java
// 测试连接
RealConnection conn = new RealConnection("httpbin.org", 80);
conn.connect(10000, 10000);

// 测试引用计数
System.out.println("空闲：" + !conn.isInUse());  // true

conn.acquire();
System.out.println("使用中：" + conn.isInUse());  // true

conn.release();
System.out.println("空闲：" + !conn.isInUse());  // true

// 测试空闲时间
long idleTime = System.nanoTime() - conn.getIdleAtNanos();
System.out.println("空闲时间（纳秒）：" + idleTime);

conn.close();
System.out.println("已关闭：" + conn.isClosed());  // true
```

【预计编写时间】45 分钟

【难度】⭐⭐⭐☆☆

【重点】
- 理解引用计数的作用
- 理解Socket的使用
- 掌握连接的生命周期
- 理解为什么需要记录空闲时间
*/

