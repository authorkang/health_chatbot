package com.example.calorie.interceptor;

import io.grpc.*;
import com.example.calorie.config.ApiKeyConfig;

/**
 * gRPC interceptor for API key authentication
 */
public class ApiKeyInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER), 
                          ApiKeyConfig.getApiKey());
                super.start(responseListener, headers);
            }
        };
    }
} 