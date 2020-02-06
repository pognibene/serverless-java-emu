package com.agileandmore.examples.handler;

import com.agileandmore.slsruntime.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class FindUserByPhoneHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> i, Context cntxt) {
        // TODO normally you'd create a User and encapsulate it in an ApiGatewayResponse, in the body field.
        return new ApiGatewayResponse();
    }

}
