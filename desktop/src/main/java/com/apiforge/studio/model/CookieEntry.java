package com.apiforge.studio.model;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CookieEntry {
    private final StringProperty name;
    private final StringProperty value;
    private final StringProperty domain;
    private final StringProperty path;

    public CookieEntry(String name, String value, String domain, String path) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
        this.domain = new SimpleStringProperty(domain);
        this.path = new SimpleStringProperty(path);
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty valueProperty() { return value; }
    public StringProperty domainProperty() { return domain; }
    public StringProperty pathProperty() { return path; }

    public String getName() { return name.get(); }
    public String getValue() { return value.get(); }
    public String getDomain() { return domain.get(); }
    public String getPath() { return path.get(); }
}
