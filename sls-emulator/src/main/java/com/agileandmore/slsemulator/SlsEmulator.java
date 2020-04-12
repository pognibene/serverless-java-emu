/*
    Copyright 2020 Pascal Ognibene (pognibene@gmail.com)

    This file is part of The Serverless Emulator for Java (Aka SEJ).

    SEJ is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SEJ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SEJ.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.agileandmore.slsemulator;

import com.agileandmore.slsruntime.ApiGatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class emulates both AWS API Gateway and the AWS Lambda runtime. It can
 * be used to run tests locally.
 */
public class SlsEmulator {

    private HttpServer server = null;
    private static Map<String, ApiFunction> storedFunctions = new HashMap<>();
    private String basePath = "";

    /**
     * Stop the server immediately
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * build a CORS answer in case a web browser is calling the API
     */
    private static void buildCorsAnswer(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        headers.add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, 0);
        exchange.getRequestBody().close();
        exchange.getResponseBody().close();
    }

    /**
     * Handle an HTTP request for the server
     *
     * @param exchange the input and output messages
     * @throws IOException
     */
    private static void handleRequest(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod().toLowerCase();
        URI uri = exchange.getRequestURI();
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        String sourceIp = remoteAddress.getAddress().getHostAddress();

        // http headers are not case sensitive, so to extract the user agent I need to search all of them
        String userAgent = "Unknown";
        for (String s : exchange.getRequestHeaders().keySet()) {
            List<String> values = exchange.getRequestHeaders().get(s);
            String lowerKey = s.toLowerCase();
            if (lowerKey.equals("user-agent")) {
                userAgent = values.get(values.size() - 1);
            }
        }

        String requestUri = uri.toString();
        if (requestUri.startsWith("/")) {
            requestUri = requestUri.substring(1);
        }
        String body = IOUtils.toString(exchange.getRequestBody(), "UTF-8");

        if (method.equals("options")) {
            buildCorsAnswer(exchange);
            return;
        }

        List<String> pathElements = new ArrayList<>();
        Map<String, String> queryElements = new HashMap<>();
        splitRawUrl(requestUri, pathElements, queryElements);

        ApiFunction foundFunction = findOneFunction(storedFunctions, pathElements, method);

        if (foundFunction == null) {
            buildNoHandlerResponse(exchange, requestUri);
        } else {
            try {
                Map<String, Object> callParameters = new HashMap<>();

                // I need the request context if I want to pass the caller IP address and user agent
                Map<String, Object> requestContext = new HashMap<>();
                callParameters.put("requestContext", requestContext);

                Map<String, Object> identity = new HashMap<>();
                requestContext.put("identity", identity);
                identity.put("sourceIp", sourceIp);
                identity.put("userAgent", userAgent);

                if (body != null) {
                    callParameters.put("body", body);
                }

                callParameters.put("pathParameters", buildPathParameters(foundFunction, pathElements));

                // query parameters
                Map<String, String> queryParamsLambda = new HashMap<>();
                for (String key : queryElements.keySet()) {
                    queryParamsLambda.put(key, queryElements.get(key));
                }
                callParameters.put("queryStringParameters", queryParamsLambda);

                // API gateway headers emulation.
                Map<String, String> gatewayHeaders = new HashMap<>();

                //gatewayHeaders.put("Accept", "*/*"); this should not be hardcoded
                gatewayHeaders.put("Accept-Encoding", "gzip,deflate");
                gatewayHeaders.put("CloudFront-Forwarded-Proto", "https");
                gatewayHeaders.put("CloudFront-Is-Desktop-Viewer", "true");
                gatewayHeaders.put("CloudFront-Is-Mobile-Viewer", "false");
                gatewayHeaders.put("CloudFront-Is-SmartTV-Viewer", "false");
                gatewayHeaders.put("CloudFront-Is-Tablet-Viewer", "false");
                gatewayHeaders.put("CloudFront-Viewer-Country", "FR");
                //gatewayHeaders.put("Content-Type", "application/json; charset=UTF-8"); this should not be hardcoded
                // FIXME need to add this. Not mandatory, but useful in some corner cases
                // gatewayHeaders.put("Host", req.getServerName());
                gatewayHeaders.put("User-Agent", "Apache-HttpClient/4.5.1 (Java/1.8.0_131)");
                gatewayHeaders.put("Via", "1.1 16291083b92e5aa4f2f272f1da69c5e4.cloudfront.net (CloudFront)");
                gatewayHeaders.put("X-Amz-Cf-Id", "TIHPZoMaJ8s2UYvCheicpxS8VUDKl46i9aGIDOloj6OMLWlstQ8sUw==");
                gatewayHeaders.put("X-Amzn-Trace-Id", "Root=1-592ed349-1a24a0237901e0571b0dfd16");
                gatewayHeaders.put("X-Forwarded-For", "92.154.70.227, 54.240.147.67");
                gatewayHeaders.put("X-Forwarded-Port", Integer.toString(uri.getPort()));
                gatewayHeaders.put("X-Forwarded-Proto", uri.getScheme());

                for (String s : exchange.getRequestHeaders().keySet()) {
                    List<String> values = exchange.getRequestHeaders().get(s);
                    // it appears that api gateway just retains the last value
                    // for a header that would appear multiple times
                    gatewayHeaders.put(s, values.get(values.size() - 1));
                }

                callParameters.put("headers", gatewayHeaders);

                // now invoke the operation
                Class c = Class.forName(foundFunction.getHandler());
                Object myInstance = c.newInstance();

                Method toBeInvoked = c.getMethod("handleRequest", Map.class, Context.class);
                ApiGatewayResponse handlerResponse = (ApiGatewayResponse) toBeInvoked.invoke(myInstance, callParameters,
                        null);

                // build the response
                Headers headers = exchange.getResponseHeaders();
                headers.add("Content-Type", "application/json; charset=UTF-8");

                // emulate the gateway API response headers
                headers.add("date", "Wed, 31 May 2017 15:11:39 GMT");
                headers.add("x-amzn-requestid", "726a95f3-4613-11e7-ae8f-f9f788cc0a8f");
                headers.add("x-amzn-trace-id", "sampled=0;root=1-592edd2b-a20c24f6eeb62f63a4977849");
                headers.add("x-cache", "Error from cloudfront");
                headers.add("via", "1.1 54073dd9095b9ef12d7cdaefb0bcc12c.cloudfront.net (CloudFront)");
                headers.add("x-amz-cf-id", "FA8PcHW-HpB3mziDqB49c4lzX8xrG9b6eVZfAWPAVAdY-5KhDzUC1g==");
                handlerResponse.getHeaders().forEach(headers::add);

                if (handlerResponse.getBody() != null) {
                    exchange.sendResponseHeaders(handlerResponse.getStatusCode(),
                            handlerResponse.getBody().getBytes().length);
                    exchange.getResponseBody().write(handlerResponse.getBody().getBytes());
                } else {
                    exchange.sendResponseHeaders(handlerResponse.getStatusCode(), 0);
                }
                exchange.getRequestBody().close();
                exchange.getResponseBody().close();

            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
                java.util.logging.Logger.getLogger(SlsEmulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Build an HTTP response for the case where no Java handler class was found
     * for a path. This is typically a configuration error in the
     * serverless.yaml file
     *
     * @param exchange the input/output request
     * @param requestUri the request URI. Only for debug purpose.
     * @throws IOException
     */
    private static void buildNoHandlerResponse(HttpExchange exchange, String requestUri) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        String response = ("Could not find any lambda function associated to the URL " + requestUri);
        exchange.sendResponseHeaders(502, response.getBytes().length);
        exchange.getRequestBody().close();
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Attempt to find a matching Java class/function for a given URI
     *
     * @param functions list of functions found in the serverless yaml file
     * @param pathElements list of elements found in the request path
     * @param method the http method
     * @return the api function if found, or null
     */
    private static ApiFunction findOneFunction(Map<String, ApiFunction> functions, List<String> pathElements,
            String method) {

        for (Map.Entry<String, ApiFunction> entry : functions.entrySet()) {
            ApiFunction oneFunction = entry.getValue();
            if (oneFunction.getItems().size() != pathElements.size()) {
                continue;
            }
            if (!method.equals(oneFunction.getMethod())) {
                continue;
            }

            boolean foundPathDifference = false;
            for (int i = 0; i < oneFunction.getItems().size(); i++) {
                if (oneFunction.getItems().get(i).getType() == UrlItem.UrlItemType.PathElement) {
                    if (!pathElements.get(i).equals(oneFunction.getItems().get(i).getName())) {
                        foundPathDifference = true;
                        break;
                    }
                }
            }

            if (!foundPathDifference) {
                return oneFunction;
            }
        }
        return null;
    }

    /**
     * Split the raw (not decoded URL) and extract the path elements and query
     * parameters elements
     *
     * @param requestUri the raw http URI
     * @param pathElements will be filled by path elements
     * @param queryElements will be filled with query parameters
     */
    private static void splitRawUrl(String requestUri, List<String> pathElements, Map<String, String> queryElements) {
        String[] parts = requestUri.split("\\?");
        String[] pathParts = parts[0].split("/");
        for (String s : pathParts) {
            try {
                pathElements.add(URLDecoder.decode(s, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Logger.getLogger(SlsEmulator.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        if (parts.length == 2) {
            String[] queryParts = parts[1].split("&");
            for (String pair : queryParts) {
                String[] tuple = pair.split("=");
                if (tuple.length == 2) {
                    try {
                        queryElements.put(URLDecoder.decode(tuple[0], "UTF-8"), URLDecoder.decode(tuple[1], "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(SlsEmulator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * This is the constructor to use in your tests. By default the emulator
     * runs on port 7070. This can be overridden with the endpoint.url java
     * property.
     */
    public SlsEmulator() {
        int runOnPort = 7070;

        String endpointUrl = System.getProperty("endpoint.url");
        if (endpointUrl != null && endpointUrl.trim().length() != 0) {
            if (endpointUrl.startsWith("http://localhost")) {
                // we want to run locally, but on a custom port, so let's extract the port
                String[] urlParts = endpointUrl.substring(6).split(":");
                runOnPort = Integer.parseInt(urlParts[1].trim());
            } else {
                // we run tests with deployment on AWS, so don't even start the emulator
                return;
            }
        }

        Logger.getLogger(SlsEmulator.class.getName()).log(Level.INFO,
                "Running API Gateway emulation on port : " + runOnPort);

        readServerlessFile("serverless.yml");

        try {
            server = HttpServer.create(new InetSocketAddress(runOnPort), 0);
            HttpContext context = server.createContext("/");
            context.setHandler(SlsEmulator::handleRequest);
            server.start();
        } catch (IOException e) {
            Logger.getLogger(SlsEmulator.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Read the serverless yaml file and find handlers in it. Made with
     * snakeyaml as much faster than jackson.
     */
    private void readServerlessFile(String fileName) {
        Yaml yaml = new Yaml();
        try {
            InputStream ios = new FileInputStream(new File(fileName));
            Map<String, Object> result = (Map<String, Object>) yaml.load(ios);

            List<String> plugins = (List<String>) result.getOrDefault("plugins", new ArrayList<String>());

            if (plugins.size() > 0) {
                for (String s : plugins) {
                    if (s.equals("serverless-domain-manager")) {
                        Map<String, Object> custom = (Map<String, Object>) result.getOrDefault("custom",
                                new HashMap<>());
                        Map<String, Object> customDomain = (Map<String, Object>) custom.getOrDefault("customDomain",
                                new HashMap<>());
                        basePath = (String) customDomain.getOrDefault("basePath", "");
                    }
                }
            }

            Map<String, Object> functions = (Map<String, Object>) result.getOrDefault("functions",
                    new HashMap<String, Object>());

            // for some reason, the default value should be an empty map but it's null instead
            if (functions == null || functions.isEmpty()) {
                return;
            }

            for (String s : functions.keySet()) {

                Map<String, Object> attributes = (Map<String, Object>) functions.getOrDefault(s,
                        new ArrayList<Map<String, Object>>());
                String handler = (String) attributes.getOrDefault("handler", null);
                String method = null;
                String path = null;

                List<Map<String, Object>> events = (List<Map<String, Object>>) attributes.getOrDefault("events",
                        new ArrayList<Map<String, Object>>());

                if (events.size() > 0) {
                    for (Map<String, Object> o : events) {
                        Map<String, Object> http = (Map<String, Object>) o.getOrDefault("http",
                                new ArrayList<Map<String, Object>>());
                        if (http != null) {
                            path = (String) http.getOrDefault("path", null);
                            method = (String) http.getOrDefault("method", null);
                        }
                    }
                }
                if (method != null && path != null) {
                    ApiFunction oneFunction = new ApiFunction();
                    oneFunction.setName(s);
                    oneFunction.setHandler(handler);
                    if (!basePath.isEmpty()) {
                        path = basePath + "/" + path;
                    }
                    oneFunction.setPath(path);
                    oneFunction.setMethod(method);

                    String[] parts = path.split("/");
                    List<UrlItem> listItems = new ArrayList<>();

                    for (String s2 : parts) {
                        UrlItem item = new UrlItem();
                        if (s2.startsWith("{") && s2.endsWith("}")) {
                            item.setType(UrlItem.UrlItemType.PathParam);
                            item.setName(s2.substring(1, s2.length() - 1));
                        } else {
                            item.setType(UrlItem.UrlItemType.PathElement);
                            item.setName(s2);
                        }
                        listItems.add(item);
                    }
                    oneFunction.setItems(listItems);
                    storedFunctions.put(s, oneFunction);
                }
            }
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(SlsEmulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Build the path parameters structure to pass to the lambda
     *
     * @param function one function from the serverless yaml file
     * @param pathElements path elements from the URI, made of fixed parts and
     * parameters parts
     * @return a map suitable for inclusion in the lambda function parameters
     */
    private static Map<String, String> buildPathParameters(ApiFunction function, List<String> pathElements) {

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < function.getItems().size(); i++) {
            UrlItem item = function.getItems().get(i);
            if (item.getType() == UrlItem.UrlItemType.PathParam) {
                result.put(item.getName(), pathElements.get(i));
            }
        }
        return result;
    }

    public String getBasePath() {
        return basePath;
    }
}
