package com.agileandmore.examples.handler;

import com.agileandmore.slsruntime.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public class CreateUserHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        // note that even though the lambda returns an ApiGatewayResponse,
        // this type just is just a wrapper for the actual response and we are expected
        // to populate it.
        // note also that if your API is CORS enabled it should return some CORS headers
        // with each call (setHeaders operation on the builder)
        return ApiGatewayResponse.builder()
                .setStatusCode(201)
                .build();
    }
}
