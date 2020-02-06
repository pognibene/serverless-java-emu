
package com.agileandmore.examples.handler;

import com.agileandmore.slsruntime.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class UpdateUserByPhoneHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>{

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> i, Context cntxt) {
        // TODO proper implementation here
        return new ApiGatewayResponse();
    }
    
}
