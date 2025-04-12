package com.example.calorie.interceptor;

import io.grpc.*;
import com.example.calorie.config.ApiKeyConfig;

/**
 * gRPC interceptor for server-side API key authentication
 */
public class ApiKeyServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String apiKey = headers.get(Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER));
        
        if (apiKey == null || !ApiKeyConfig.isValidApiKey(apiKey)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid or missing API key"), 
                      new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
        
        return next.startCall(call, headers);
    }
} 