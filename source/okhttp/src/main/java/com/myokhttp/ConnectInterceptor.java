package com.myokhttp;

import java.io.IOException;

/**
 * 连接拦截器
 * 
 * 职责：
 * 1. 从连接池获取可复用的连接
 * 2. 如果没有可复用的连接，建立新连接
 * 3. 使用完毕后，将连接放回连接池
 * 
 * 这是网络层和应用层的分界点：
 * - 之前的拦截器处理应用层逻辑（重试、缓存等）
 * - 这个拦截器建立网络连接
 * - 之后的拦截器执行真实的网络 I/O
 */
public class ConnectInterceptor implements Interceptor {
    
    private final OkHttpClient client;

    public ConnectInterceptor(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        // 解析 URL，提取 host 和 port
        String[] hostPort = parseUrl(request.url());
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);

        // 从连接池获取连接
        RealConnection connection = client.connectionPool().get(host, port);
        
        if (connection == null) {
            // 没有可复用的连接，创建新连接
            connection = new RealConnection(host, port);
            
            try {
                // 建立连接
                connection.connect(
                    chain.connectTimeoutMillis(),
                    chain.readTimeoutMillis()
                );
                
                // 标记为使用中
                connection.acquire();
            } catch (IOException e) {
                // 连接失败
                throw new IOException("无法连接到 " + host + ":" + port, e);
            }
        }

        try {
            // 将连接保存到 Chain 中，供 CallServerInterceptor 使用
            // 这里简化处理，直接传递给下一个拦截器
            // 实际 OkHttp 会将连接保存在 StreamAllocation 中
            
            // 创建一个包装的 Chain，携带连接信息
            ConnectedChain connectedChain = new ConnectedChain(chain, connection);
            
            // 执行下一个拦截器
            Response response = chain.proceed(request);
            
            // 使用完毕，放回连接池
            client.connectionPool().put(connection);
            
            return response;
        } catch (IOException e) {
            // 如果发生异常，关闭连接
            try {
                connection.close();
            } catch (IOException closeException) {
                // 忽略
            }
            throw e;
        }
    }

    /**
     * 解析 URL，提取 host 和 port
     * 
     * @return [host, port]
     */
    private String[] parseUrl(String url) {
        try {
            // 去除协议
            if (url.startsWith("http://")) {
                url = url.substring(7);
            } else if (url.startsWith("https://")) {
                url = url.substring(8);
            }
            
            // 提取 host 和 port
            int slashIndex = url.indexOf("/");
            if (slashIndex != -1) {
                url = url.substring(0, slashIndex);
            }
            
            int colonIndex = url.indexOf(":");
            if (colonIndex != -1) {
                String host = url.substring(0, colonIndex);
                String port = url.substring(colonIndex + 1);
                return new String[]{host, port};
            } else {
                // 使用默认端口
                String port = "80";
                if (url.startsWith("https://")) {
                    port = "443";
                }
                return new String[]{url, port};
            }
        } catch (Exception e) {
            return new String[]{"localhost", "80"};
        }
    }

    /**
     * 包装的 Chain，携带连接信息
     * 这是一个简化的实现，实际 OkHttp 更复杂
     */
    static class ConnectedChain implements Interceptor.Chain {
        private final Interceptor.Chain delegate;
        private final RealConnection connection;

        ConnectedChain(Interceptor.Chain delegate, RealConnection connection) {
            this.delegate = delegate;
            this.connection = connection;
        }

        public RealConnection connection() {
            return connection;
        }

        @Override
        public Request request() {
            return delegate.request();
        }

        @Override
        public Response proceed(Request request) throws IOException {
            return delegate.proceed(request);
        }

        @Override
        public OkHttpClient client() {
            return delegate.client();
        }

        @Override
        public int connectTimeoutMillis() {
            return delegate.connectTimeoutMillis();
        }

        @Override
        public int readTimeoutMillis() {
            return delegate.readTimeoutMillis();
        }

        @Override
        public int writeTimeoutMillis() {
            return delegate.writeTimeoutMillis();
        }
    }
}

