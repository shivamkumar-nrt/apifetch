package com.apiforge.studio.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.TransferMode;
import javafx.event.ActionEvent;
import java.io.File;

import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;

public class ImportController {

    @FXML private TextField curlInputField;
    @FXML private VBox dropZone;

    private MainController mainController;
    private double xOffset = 0;
    private double yOffset = 0;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        setupDropZone();
        setupCurlInput();
    }

    private void setupDropZone() {
        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                if (!dropZone.getStyleClass().contains("import-drop-zone-active")) {
                    dropZone.getStyleClass().add("import-drop-zone-active");
                }
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.getStyleClass().remove("import-drop-zone-active");
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            dropZone.getStyleClass().remove("import-drop-zone-active");
            if (event.getDragboard().hasFiles()) {
                for (File file : event.getDragboard().getFiles()) {
                    handleFileImport(file);
                }
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    private void setupCurlInput() {
        curlInputField.setOnAction(event -> {
            String input = curlInputField.getText();
            if (input != null && !input.trim().isEmpty()) {
                handleCurlImport(input.trim());
            }
        });
    }

    @FXML
    private void browseFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open ApiForge / Postman Collection");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        Stage stage = (Stage) dropZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            handleFileImport(file);
        }
    }

    private void handleFileImport(File file) {
        if (mainController != null) {
            mainController.importCollectionFromFile(file);
            closeModal();
        }
    }

    private void handleCurlImport(String curl) {
        if (mainController != null) {
            mainController.importFromCurl(curl);
            closeModal();
        }
    }

    private void closeModal() {
        Stage stage = (Stage) dropZone.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void closeModalAction(ActionEvent event) {
        closeModal();
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) dropZone.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
}
