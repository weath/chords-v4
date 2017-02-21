package net.weath.musicxml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Characters;

public class Element {

    private String name;
    private String value = "";
    private Element child;
    private Element sibling;
    private List<Attribute> attributeList = new ArrayList<>();

    public Element(QName name) {
        this.name = name.toString();
    }

    public Element(QName name, String value) {
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

    public Element getChild() {
        return child;
    }

    public void addChild(Element child) {
        if (this.child == null) {
            this.child = child;
        } else {
            Element target = this.child;
            while (target.sibling != null) {
                target = target.sibling;
            }
            target.sibling = child;
        }
    }

    public Element getSibling() {
        return sibling;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void addAttribute(Attribute attribute) {
        attributeList.add(attribute);
    }

    public Attribute getNamedAttribute(String name) {
        for (Attribute attr : attributeList) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
        return null;
    }

    public void append(Characters characters) {
        if (characters.isIgnorableWhiteSpace()) {
            return;
        }
        String text = characters.toString();
        value += text;
    }
}
