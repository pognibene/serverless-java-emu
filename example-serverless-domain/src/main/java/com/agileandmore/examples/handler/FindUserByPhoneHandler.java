package com.agileandmore.examples.handler;

import com.agileandmore.slsruntime.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class FindUserByPhoneHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> i, Context cntxt) {
        
        User user = new User();
        user.setFirstName("My First Name");
        user.setLastName("My Last Name");
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(user)
                .build();
    }

}
