package com.apiforge.studio.ui;

import com.apiforge.studio.model.KeyValuePair;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class PostmanTableEditor {
    private VBox container;
    private ObservableList<KeyValuePair> data;
    private Runnable onChange;

    public PostmanTableEditor(VBox container, ObservableList<KeyValuePair> data, Runnable onChange) {
        this.container = container;
        this.data = data;
        this.onChange = onChange;
        render();
    }

    private void render() {
        container.getChildren().clear();
        for (int i = 0; i < data.size(); i++) {
            container.getChildren().add(createRow(i, false));
        }
        container.getChildren().add(createRow(data.size(), true));
    }

    private HBox createRow(int index, boolean isEmptyRow) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("kv-row");

        CheckBox enableCheck = new CheckBox();
        enableCheck.setPrefWidth(40);
        enableCheck.setMinWidth(40);
        enableCheck.setAlignment(Pos.CENTER);
        enableCheck.setPadding(new Insets(0, 0, 0, 10));

        TextField keyField = new TextField();
        keyField.setPromptText("Key");
        keyField.getStyleClass().add("kv-row-input");
        HBox.setHgrow(keyField, Priority.ALWAYS);

        TextField valField = new TextField();
        valField.setPromptText("Value");
        valField.getStyleClass().add("kv-row-input");
        HBox.setHgrow(valField, Priority.ALWAYS);

        Button delBtn = new Button("×");
        delBtn.getStyleClass().add("kv-del-btn");
        delBtn.setPrefWidth(40);
        delBtn.setMinWidth(40);
        
        row.getChildren().addAll(enableCheck, keyField, valField, delBtn);

        if (!isEmptyRow) {
            KeyValuePair kv = data.get(index);
            keyField.setText(kv.getKey());
            valField.setText(kv.getValue());
            enableCheck.setSelected(kv.isEnabled());

            keyField.textProperty().addListener((obs, o, n) -> {
                kv.setKey(n);
                if (onChange != null) onChange.run();
            });
            valField.textProperty().addListener((obs, o, n) -> {
                kv.setValue(n);
                if (onChange != null) onChange.run();
            });
            enableCheck.selectedProperty().addListener((obs, o, n) -> {
                kv.setEnabled(n);
                if (onChange != null) onChange.run();
            });
            delBtn.setOnAction(e -> {
                data.remove(index);
                render();
                if (onChange != null) onChange.run();
            });
            
            // Hover effect for delete button
            delBtn.setVisible(false);
            row.setOnMouseEntered(e -> delBtn.setVisible(true));
            row.setOnMouseExited(e -> delBtn.setVisible(false));
        } else {
            enableCheck.setVisible(false);
            delBtn.setVisible(false);

            keyField.textProperty().addListener((obs, o, n) -> {
                if (o.isEmpty() && !n.isEmpty()) {
                    data.add(new KeyValuePair(n, ""));
                    render();
                    if (onChange != null) onChange.run();
                    Platform.runLater(() -> {
                        HBox newRow = (HBox) container.getChildren().get(data.size() - 1);
                        TextField tf = (TextField) newRow.getChildren().get(1);
                        tf.requestFocus();
                        tf.positionCaret(n.length());
                    });
                }
            });
            valField.textProperty().addListener((obs, o, n) -> {
                if (o.isEmpty() && !n.isEmpty()) {
                    data.add(new KeyValuePair("", n));
                    render();
                    if (onChange != null) onChange.run();
                    Platform.runLater(() -> {
                        HBox newRow = (HBox) container.getChildren().get(data.size() - 1);
                        TextField tf = (TextField) newRow.getChildren().get(2);
                        tf.requestFocus();
                        tf.positionCaret(n.length());
                    });
                }
            });
        }

        return row;
    }
}