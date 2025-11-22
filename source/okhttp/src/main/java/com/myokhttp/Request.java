package com.myokhttp;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求对象
 * 使用 Builder 模式构建，确保对象不可变
 */
public class Request {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final RequestBody body;

    private Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = new HashMap<>(builder.headers);
        this.body = builder.body;
    }

    public String url() {
        return url;
    }

    public String method() {
        return method;
    }

    public Map<String, String> headers() {
        return new HashMap<>(headers);
    }

    public String header(String name) {
        return headers.get(name);
    }

    public RequestBody body() {
        return body;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "Request{method=" + method + ", url=" + url + "}";
    }

    /**
     * Builder 模式：构建 Request 对象
     */
    public static class Builder {
        private String url;
        private String method = "GET";
        private Map<String, String> headers = new HashMap<>();
        private RequestBody body;

        public Builder() {}

        private Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.headers = new HashMap<>(request.headers);
            this.body = request.body;
        }

        public Builder url(String url) {
            if (url == null) {
                throw new IllegalArgumentException("url == null");
            }
            this.url = url;
            return this;
        }

        public Builder method(String method, RequestBody body) {
            if (method == null || method.isEmpty()) {
                throw new IllegalArgumentException("method is empty");
            }
            this.method = method;
            this.body = body;
            return this;
        }

        public Builder get() {
            return method("GET", null);
        }

        public Builder post(RequestBody body) {
            return method("POST", body);
        }

        public Builder put(RequestBody body) {
            return method("PUT", body);
        }

        public Builder delete() {
            return method("DELETE", null);
        }

        public Builder header(String name, String value) {
            if (name == null || value == null) {
                throw new IllegalArgumentException("header name or value is null");
            }
            headers.put(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            return header(name, value);
        }

        public Builder removeHeader(String name) {
            headers.remove(name);
            return this;
        }

        public Request build() {
            if (url == null) {
                throw new IllegalStateException("url == null");
            }
            return new Request(this);
        }
    }
}

