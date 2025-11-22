package com.myokhttp;

import java.io.IOException;
import java.util.List;

/**
 * 拦截器链的真实实现
 * 
 * ⭐⭐⭐⭐⭐ 这是整个 OkHttp 最核心的类！！！
 * 
 * 【核心作用】
 * 实现责任链模式，让多个拦截器按顺序处理请求和响应
 * 
 * 【设计思想（非常重要！）】
 * 1. 维护拦截器列表和当前索引
 * 2. proceed() 方法创建新的 Chain 对象（索引 + 1）
 * 3. 递归调用形成责任链
 * 4. 请求从外向内传递，响应从内向外返回
 * 
 * 【执行流程（仔细理解这个流程！）】
 * 
 * 假设有 3 个拦截器：[A, B, C]
 * 
 * RealInterceptorChain.proceed(index=0, request)
 *   │
 *   ├─ 获取 interceptor[0] = A
 *   │
 *   ├─ 创建 next chain (index=1)
 *   │
 *   └─ A.intercept(next_chain)
 *       │
 *       ├─ A 处理请求（修改请求）
 *       │
 *       ├─ next_chain.proceed(index=1, request)
 *       │   │
 *       │   ├─ 获取 interceptor[1] = B
 *       │   │
 *       │   ├─ 创建 next chain (index=2)
 *       │   │
 *       │   └─ B.intercept(next_chain)
 *       │       │
 *       │       ├─ B 处理请求
 *       │       │
 *       │       ├─ next_chain.proceed(index=2, request)
 *       │       │   │
 *       │       │   ├─ 获取 interceptor[2] = C
 *       │       │   │
 *       │       │   ├─ 创建 next chain (index=3)
 *       │       │   │
 *       │       │   └─ C.intercept(next_chain)
 *       │       │       │
 *       │       │       └─ C 执行网络请求，返回 Response
 *       │       │           │
 *       │       │           └─ Response (code=200)
 *       │       │
 *       │       ├─ B 处理响应（修改响应）
 *       │       │
 *       │       └─ 返回 Response
 *       │
 *       ├─ A 处理响应
 *       │
 *       └─ 返回 Response 给用户
 * 
 * 【关键点】
 * 1. 每次 proceed() 都创建新的 Chain 对象
 * 2. index 递增，指向下一个拦截器
 * 3. 形成递归调用
 * 4. 每个拦截器都可以修改请求和响应
 * 
 * @author Your Name
 */
public class RealInterceptorChain implements Interceptor.Chain {
    
    // ========== 字段定义 ==========
    
    /**
     * 拦截器列表（所有拦截器）
     * 
     * 【为什么是 List？】
     * - 需要按顺序执行
     * - 通过索引访问
     */
    private final List<Interceptor> interceptors;
    
    /**
     * 当前索引（指向当前要执行的拦截器）
     * 
     * 【索引的变化】
     * - 初始值：0
     * - 每次 proceed()：创建新 Chain，index + 1
     * - 最终：index == interceptors.size()（表示执行完毕）
     */
    private final int index;
    
    /**
     * 当前请求
     */
    private final Request request;
    
    /**
     * OkHttpClient 实例
     * 
     * 【为什么需要？】
     * - 获取配置（超时时间等）
     * - 获取连接池、调度器等资源
     */
    private final OkHttpClient client;
    
    /**
     * 超时设置
     */
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    
    /**
     * 调用次数（用于检测是否多次调用 proceed()）
     * 
     * 【为什么需要这个？】
     * - 每个拦截器只能调用一次 proceed()
     * - 如果调用多次，说明拦截器实现有问题
     * - 这是一个安全检查
     */
    private int calls;

    // ========== 构造函数 ==========
    
    /**
     * 构造拦截器链
     * 
     * @param interceptors 拦截器列表
     * @param index 当前索引
     * @param request 当前请求
     * @param client OkHttpClient 实例
     * @param connectTimeout 连接超时
     * @param readTimeout 读取超时
     * @param writeTimeout 写入超时
     */
    public RealInterceptorChain(
            List<Interceptor> interceptors,
            int index,
            Request request,
            OkHttpClient client,
            int connectTimeout,
            int readTimeout,
            int writeTimeout
    ) {
        // TODO: 初始化所有字段
        
    }

    // ========== 实现 Interceptor.Chain 接口 ==========
    
    /**
     * 获取当前请求
     */
    @Override
    public Request request() {
        // TODO: 返回当前请求
        return null;
    }

    /**
     * 继续执行下一个拦截器
     * 
     * ⭐⭐⭐⭐⭐ 这是最核心的方法！！！
     * 
     * 【执行步骤】
     * 1. 检查索引是否越界
     * 2. 检查是否多次调用
     * 3. 创建下一个 Chain（index + 1）
     * 4. 获取当前拦截器
     * 5. 执行拦截器的 intercept() 方法
     * 6. 检查返回的响应
     * 7. 返回响应
     * 
     * @param request 请求对象
     * @return 响应对象
     * @throws IOException 执行失败时抛出
     */
    @Override
    public Response proceed(Request request) throws IOException {
        // TODO: 步骤 1 - 检查索引是否超出范围
        // 如果 index >= interceptors.size()，说明已经执行完所有拦截器
        // 这是一个错误，应该抛出 AssertionError
        
        // TODO: 步骤 2 - 检查调用次数
        // calls++;
        // 如果 calls > 1，说明拦截器多次调用了 proceed()
        // 这是错误的使用方式，应该抛出 IllegalStateException
        
        // TODO: 步骤 3 - 创建下一个拦截器链
        // 关键点：index + 1，指向下一个拦截器
        // RealInterceptorChain next = new RealInterceptorChain(
        //     interceptors,
        //     index + 1,  // ← 这里是关键！
        //     request,
        //     client,
        //     connectTimeout,
        //     readTimeout,
        //     writeTimeout
        // );

        // TODO: 步骤 4 - 获取当前拦截器
        // Interceptor interceptor = interceptors.get(index);
        
        // TODO: 步骤 5 - 执行当前拦截器
        // 这里会递归调用！拦截器内部会调用 chain.proceed()
        // Response response = interceptor.intercept(next);

        // TODO: 步骤 6 - 检查响应是否为 null
        // 如果拦截器返回 null，这是一个错误
        // 应该抛出 NullPointerException
        
        // TODO: 步骤 7 - 返回响应
        // return response;
        
        return null;  // 替换为你的实现
    }

    @Override
    public OkHttpClient client() {
        // TODO: 返回 OkHttpClient
        return null;
    }

    @Override
    public int connectTimeoutMillis() {
        // TODO: 返回连接超时时间
        return 0;
    }

    @Override
    public int readTimeoutMillis() {
        // TODO: 返回读取超时时间
        return 0;
    }

    @Override
    public int writeTimeoutMillis() {
        // TODO: 返回写入超时时间
        return 0;
    }
}

/*
【编写提示】

1. 【最重要】理解 proceed() 方法的递归逻辑
   - 每次创建新的 Chain，index + 1
   - 形成递归调用
   - 这是责任链模式的核心

2. 【画图帮助理解】
   建议你画一个流程图：
   - 横向：拦截器链 [A, B, C]
   - 纵向：递归调用的层级
   - 标注每次的 index 值

3. 【调试技巧】
   在 proceed() 方法开始处打印：
   System.out.println("Chain.proceed() - index: " + index);
   
   运行时你会看到：
   Chain.proceed() - index: 0  (进入 A)
   Chain.proceed() - index: 1  (进入 B)
   Chain.proceed() - index: 2  (进入 C)

4. 【常见错误】
   - 忘记 index + 1
   - 忘记检查 calls > 1
   - 忘记检查 response == null

【测试方法】
写完后，可以这样测试：
1. 创建几个简单的拦截器
2. 在拦截器的 intercept() 方法中打印日志
3. 观察执行顺序

【预计编写时间】1 小时

【重要程度】⭐⭐⭐⭐⭐
这是最核心的类，一定要理解透彻！
*/

