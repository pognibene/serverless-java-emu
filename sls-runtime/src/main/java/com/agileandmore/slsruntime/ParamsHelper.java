/*
 * (C) Copyright 2020 Pascal Ognibene (pognibene@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Pascal Ognibene (pognibene@gmail.com)
 */

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
    @SuppressWarnings("unchecked")
    public static String getQueryParameter(String name, Map<String, Object> input) {
        Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
        if (queryParams != null) {
            return queryParams.get(name);
        }
        return null;
    }
}
