package com.agileandmore.slsruntime;

import java.util.Map;

/**
 * class offering some help to extract parameters from an input lambda message.
 * Note : assumes that all parameters have been URL decoded before being passed
 * to the lambda.
 */
public class ParamsHelper {
    /**
     * Get path parameters within a lambda
     *
     * @param name  parameter name
     * @param input input message for the lambda
     * @return the value of the parameter (decoded) or null if not found
     */
    @SuppressWarnings("unchecked")
    public static String getPathParameter(String name, Map<String, Object> input) {
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        if (pathParameters != null) {
            return pathParameters.get(name);
        }
        return null;
    }

    /**
     * Get query parameters within a lambda
     *
     * @param name  name of the query parameter
     * @param input input message for the lambda
     * @return the value of the parameter (decoded) or null if not found
     */
    public static String getQueryParameter(String name, Map<String, Object> input) {
        Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
        if (queryParams != null) {
            return queryParams.get(name);
        }
        return null;
    }
}