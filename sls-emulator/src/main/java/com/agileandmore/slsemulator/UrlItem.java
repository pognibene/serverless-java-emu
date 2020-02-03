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

/**
 * Represents one element in an URI, either of type Path or of type Parameter
 */
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
