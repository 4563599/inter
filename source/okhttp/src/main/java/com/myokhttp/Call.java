package com.myokhttp;

import java.io.IOException;

/**
 * Call 接口：表示一个准备好执行的请求
 * 可以取消，每个 Call 只能执行一次
 */
public interface Call {
    
    /**
     * 获取原始请求
     */
    Request request();
    
    /**
     * 同步执行请求
     * 阻塞当前线程直到响应返回
     */
    Response execute() throws IOException;
    
    /**
     * 异步执行请求
     * 在后台线程执行，通过回调返回结果
     */
    void enqueue(Callback callback);
    
    /**
     * 取消请求
     */
    void cancel();
    
    /**
     * 检查是否已执行
     */
    boolean isExecuted();
    
    /**
     * 检查是否已取消
     */
    boolean isCanceled();
    
    /**
     * 创建一个新的相同请求的 Call
     * 因为每个 Call 只能执行一次
     */
    Call clone();
    
    /**
     * 回调接口
     */
    interface Callback {
        /**
         * 请求失败时调用
         */
        void onFailure(Call call, IOException e);
        
        /**
         * 请求成功时调用
         */
        void onResponse(Call call, Response response) throws IOException;
    }
}

