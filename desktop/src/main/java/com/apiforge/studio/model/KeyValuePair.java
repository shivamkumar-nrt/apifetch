package com.apiforge.studio.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KeyValuePair {
    private final BooleanProperty enabled;
    private final StringProperty key;
    private final StringProperty value;

    public KeyValuePair(String key, String value) {
        this.enabled = new SimpleBooleanProperty(true);
        this.key   = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public KeyValuePair(boolean enabled, String key, String value) {
        this.enabled = new SimpleBooleanProperty(enabled);
        this.key   = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public boolean isEnabled()  { return enabled.get(); }
    public void setEnabled(boolean v) { enabled.set(v); }
    public BooleanProperty enabledProperty() { return enabled; }

    public String getKey()      { return key.get(); }
    public void setKey(String k){ key.set(k); }
    public StringProperty keyProperty() { return key; }

    public String getValue()        { return value.get(); }
    public void setValue(String v)  { value.set(v); }
    public StringProperty valueProperty() { return value; }
}
