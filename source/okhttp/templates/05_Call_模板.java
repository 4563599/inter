package com.myokhttp;

import java.io.IOException;

/**
 * Call 接口：表示一个准备好执行的请求
 * 
 * 【为什么需要这个接口？】
 * - 将请求的创建和执行分离
 * - 支持同步和异步两种执行方式
 * - 可以取消正在执行的请求
 * 
 * 【Call 的生命周期】
 * 1. 创建：client.newCall(request)
 * 2. 执行：call.execute() 或 call.enqueue()
 * 3. 完成：获得 Response 或异常
 * 
 * 【为什么每个 Call 只能执行一次？】
 * - 执行过程中可能会修改内部状态
 * - 避免并发问题
 * - 如果需要重复执行，使用 clone()
 * 
 * 【同步 vs 异步】
 * 同步（execute）：
 * - 阻塞当前线程
 * - 直到获得响应或异常
 * - 适合后台线程
 * 
 * 异步（enqueue）：
 * - 提交到线程池
 * - 不阻塞当前线程
 * - 通过回调返回结果
 * - 适合 UI 线程
 * 
 * @author Your Name
 */
public interface Call {
    
    /**
     * 获取原始请求
     * 
     * 【为什么需要这个方法？】
     * - 有时需要知道 Call 对应的请求
     * - 调试、日志记录等场景
     * 
     * @return 原始请求
     */
    Request request();
    
    /**
     * 同步执行请求
     * 
     * 【工作方式】
     * - 阻塞当前线程
     * - 执行拦截器链
     * - 返回响应或抛出异常
     * 
     * 【什么时候用？】
     * - 在后台线程中
     * - 不需要回调，直接获取结果
     * 
     * 【注意】
     * - 不要在 UI 线程调用（会阻塞 UI）
     * - 每个 Call 只能执行一次
     * 
     * @return 响应对象
     * @throws IOException 请求失败时抛出
     */
    Response execute() throws IOException;
    
    /**
     * 异步执行请求
     * 
     * 【工作方式】
     * - 提交到 Dispatcher 的线程池
     * - 立即返回，不阻塞
     * - 通过 Callback 回调结果
     * 
     * 【什么时候用？】
     * - 在 UI 线程中
     * - 不想阻塞当前线程
     * 
     * 【回调在哪个线程？】
     * - 在 Dispatcher 的工作线程
     * - 不是主线程！
     * - 如果要更新 UI，需要切换到主线程
     * 
     * @param callback 回调接口
     */
    void enqueue(Callback callback);
    
    /**
     * 取消请求
     * 
     * 【什么时候用？】
     * - 用户取消操作（如点击取消按钮）
     * - Activity/Fragment 销毁时
     * - 请求超时
     * 
     * 【如何工作？】
     * - 设置 canceled 标志
     * - 如果请求正在执行，会尽快停止
     * - 如果在等待队列，会被移除
     * 
     * 【注意】
     * - cancel() 是"尽力而为"，不保证立即停止
     * - 可能在取消后仍然收到响应
     */
    void cancel();
    
    /**
     * 检查是否已执行
     * 
     * @return 是否已调用 execute() 或 enqueue()
     */
    boolean isExecuted();
    
    /**
     * 检查是否已取消
     * 
     * @return 是否已调用 cancel()
     */
    boolean isCanceled();
    
    /**
     * 克隆 Call
     * 
     * 【为什么需要？】
     * - Call 只能执行一次
     * - 如果需要重复执行，克隆一个新的
     * 
     * 【使用示例】
     * Call call1 = client.newCall(request);
     * call1.execute();  // 执行一次
     * 
     * Call call2 = call1.clone();
     * call2.execute();  // 执行第二次
     * 
     * @return 新的 Call 实例
     */
    Call clone();
    
    /**
     * 回调接口
     * 
     * 【为什么是内部接口？】
     * - 逻辑上属于 Call
     * - 使用时更清晰：Call.Callback
     */
    interface Callback {
        /**
         * 请求失败时调用
         * 
         * 【什么情况会失败？】
         * - 网络不可用
         * - 连接超时
         * - DNS 解析失败
         * - 服务器无响应
         * 
         * 【注意】
         * - 在工作线程回调，不是主线程
         * - 4xx, 5xx 状态码不算失败，会调用 onResponse
         * 
         * @param call Call 对象
         * @param e 异常
         */
        void onFailure(Call call, IOException e);
        
        /**
         * 请求成功时调用
         * 
         * 【什么算成功？】
         * - 收到了 HTTP 响应
         * - 即使状态码是 4xx, 5xx 也算成功
         * - 需要自己检查 response.isSuccessful()
         * 
         * 【注意】
         * - 在工作线程回调，不是主线程
         * - ResponseBody 只能读取一次
         * - 需要在这个方法中处理完响应
         * 
         * @param call Call 对象
         * @param response 响应对象
         * @throws IOException 处理响应时可能抛出
         */
        void onResponse(Call call, Response response) throws IOException;
    }
}

/*
【编写提示】

这是一个接口，所以：
1. 只需要声明方法，不需要实现
2. 所有方法都是 public abstract（可以省略）
3. 内部接口 Callback 也是接口

【关键理解】

1. 【为什么需要接口？】
   - 面向接口编程
   - 隐藏实现细节（RealCall）
   - 便于测试（可以 mock）

2. 【同步 vs 异步的选择】
   
   同步（execute）：
   ```java
   new Thread(() -> {
       try {
           Response response = call.execute();
           // 处理响应
       } catch (IOException e) {
           // 处理异常
       }
   }).start();
   ```
   
   异步（enqueue）：
   ```java
   call.enqueue(new Callback() {
       @Override
       public void onResponse(Call call, Response response) {
           // 已经在工作线程
           // 可以直接处理响应
       }
       
       @Override
       public void onFailure(Call call, IOException e) {
           // 处理失败
       }
   });
   ```

3. 【为什么只能执行一次？】
   
   错误示例：
   ```java
   Call call = client.newCall(request);
   call.execute();  // OK
   call.execute();  // 错误！会抛出 IllegalStateException
   ```
   
   正确做法：
   ```java
   Call call1 = client.newCall(request);
   call1.execute();  // 第一次
   
   Call call2 = call1.clone();
   call2.execute();  // 第二次
   ```

【使用示例】

同步：
```java
Call call = client.newCall(request);
try {
    Response response = call.execute();
    System.out.println(response.body().string());
} catch (IOException e) {
    e.printStackTrace();
}
```

异步：
```java
Call call = client.newCall(request);
call.enqueue(new Call.Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        System.out.println("请求失败: " + e.getMessage());
    }
    
    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            System.out.println(response.body().string());
        } else {
            System.out.println("请求失败: " + response.code());
        }
    }
});
```

【常见误解】

❌ 误解1：onResponse 就是成功
实际：只要收到 HTTP 响应就会调用，需要检查 isSuccessful()

❌ 误解2：onResponse 在主线程
实际：在工作线程，更新 UI 需要切换线程

❌ 误解3：cancel() 会立即停止
实际："尽力而为"，可能在取消后仍收到响应

【预计编写时间】30 分钟

【难度】⭐⭐☆☆☆（只是定义接口）

【重点】
- 理解同步和异步的区别
- 理解为什么只能执行一次
- 理解回调的线程模型
*/

