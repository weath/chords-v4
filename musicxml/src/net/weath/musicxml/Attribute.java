package net.weath.musicxml;

import javax.xml.namespace.QName;

public class Attribute {

    private String name;
    private String value;

    public Attribute(QName name) {
        this.name = name.toString();
    }

    public Attribute(QName name, String value) {
        this.name = name.toString();
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
