
package com.agileandmore.examples.handler;

import com.agileandmore.slsruntime.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class DeleteUserHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>{

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        // here you would place the real code for your lambda
        return new ApiGatewayResponse();
    }
}
