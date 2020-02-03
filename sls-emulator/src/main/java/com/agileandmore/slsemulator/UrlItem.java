package com.agileandmore.slsemulator;


public class UrlItem {

    private String name;
    private UrlItemType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UrlItemType getType() {
        return type;
    }

    public void setType(UrlItemType type) {
        this.type = type;
    }

    enum UrlItemType {
        PathElement,
        PathParam
    }
}
