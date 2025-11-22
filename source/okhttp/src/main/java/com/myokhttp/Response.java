package com.myokhttp;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 响应对象
 * 包含状态码、响应头、响应体等信息
 */
public class Response {
    private final Request request;
    private final int code;
    private final String message;
    private final Map<String, String> headers;
    private final ResponseBody body;
    private final Response priorResponse; // 重定向前的响应

    private Response(Builder builder) {
        this.request = builder.request;
        this.code = builder.code;
        this.message = builder.message;
        this.headers = new HashMap<>(builder.headers);
        this.body = builder.body;
        this.priorResponse = builder.priorResponse;
    }

    public Request request() {
        return request;
    }

    public int code() {
        return code;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    public String message() {
        return message;
    }

    public Map<String, String> headers() {
        return new HashMap<>(headers);
    }

    public String header(String name) {
        return headers.get(name);
    }

    public ResponseBody body() {
        return body;
    }

    public Response priorResponse() {
        return priorResponse;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "Response{code=" + code + ", message=" + message + "}";
    }

    /**
     * Builder 模式：构建 Response 对象
     */
    public static class Builder {
        private Request request;
        private int code = -1;
        private String message;
        private Map<String, String> headers = new HashMap<>();
        private ResponseBody body;
        private Response priorResponse;

        public Builder() {}

        private Builder(Response response) {
            this.request = response.request;
            this.code = response.code;
            this.message = response.message;
            this.headers = new HashMap<>(response.headers);
            this.body = response.body;
            this.priorResponse = response.priorResponse;
        }

        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
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

        public Builder body(ResponseBody body) {
            this.body = body;
            return this;
        }

        public Builder priorResponse(Response response) {
            this.priorResponse = response;
            return this;
        }

        public Response build() {
            if (request == null) {
                throw new IllegalStateException("request == null");
            }
            if (code < 0) {
                throw new IllegalStateException("code < 0: " + code);
            }
            return new Response(this);
        }
    }
}

