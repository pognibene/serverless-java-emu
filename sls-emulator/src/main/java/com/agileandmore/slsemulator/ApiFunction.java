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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one API function found in the serverless yaml file
 */
public class ApiFunction {

    private String name;
    private String handler;
    private String path;
    private String method;
    private Object handlerInstance;
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

    public Object getHandlerInstance() {
        return handlerInstance;
    }

    public void setHandlerInstance(Object handlerInstance) {
        this.handlerInstance = handlerInstance;
    }
    
    @Override
    public String toString() {
        return "ApiFunction{" + "name=" + name + ", handler=" + handler + ", path=" + path + ", method=" + method + '}';
    }

}
