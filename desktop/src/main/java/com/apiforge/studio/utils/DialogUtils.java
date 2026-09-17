package com.apiforge.studio.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.StageStyle;

import java.util.List;

public class DialogUtils {

    public static void applyDialogStyle(Dialog<?> dialog) {
        dialog.initStyle(StageStyle.UNDECORATED); // Or use standard if we just want dark theme. Let's keep decorations for now to allow moving/closing, so remove UNDECORATED unless needed, but to make it truly custom we could use UNDECORATED. Wait, let's just style the content first.
        dialog.getDialogPane().getStylesheets().add(DialogUtils.class.getResource("/css/dialog.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dark-theme");
        
        // Remove header graphic if present
        dialog.setGraphic(null);
        dialog.setHeaderText(null);
    }

    public static Alert createAlert(Alert.AlertType type, String title, String headerText, String contentText) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText); // Often null for our custom ones
        alert.setContentText(contentText);
        applyDialogStyle(alert);
        return alert;
    }

    public static TextInputDialog createTextInputDialog(String defaultValue, String title, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        applyDialogStyle(dialog);
        return dialog;
    }

    public static <T> ChoiceDialog<T> createChoiceDialog(T defaultChoice, List<T> choices, String title, String headerText, String contentText) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        applyDialogStyle(dialog);
        return dialog;
    }

    public static <T> Dialog<T> createCustomDialog(String title, String headerText) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        applyDialogStyle(dialog);
        return dialog;
    }
}
