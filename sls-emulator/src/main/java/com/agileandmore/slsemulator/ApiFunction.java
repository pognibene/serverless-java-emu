package com.agileandmore.slsemulator;

import java.util.ArrayList;
import java.util.List;

public class ApiFunction {

    private String name;
    private String handler;
    private String path;
    private String method;
    private List<UrlItem> items = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<UrlItem> getItems() {
        return items;
    }

    public void setItems(List<UrlItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ApiFunction{" + "name=" + name + ", handler=" + handler + ", path=" + path + ", method=" + method + '}';
    }

}
