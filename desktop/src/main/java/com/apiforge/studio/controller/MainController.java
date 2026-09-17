package com.apiforge.studio.controller;

import com.apiforge.studio.model.ApiRequest;
import com.apiforge.studio.model.ApiResponse;
import com.apiforge.studio.model.HistoryEntry;
import com.apiforge.studio.model.KeyValuePair;
import com.apiforge.studio.service.HttpClientService;
import com.apiforge.studio.service.StorageService;
import com.apiforge.studio.service.WebSocketService;
import com.apiforge.studio.service.DatabaseService;
import com.apiforge.studio.service.EnvironmentService;
import com.apiforge.studio.service.LoadTestingService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.File;
import java.nio.file.Files;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.apiforge.studio.utils.DialogUtils;



public class MainController {

    // Services
    private HttpClientService httpClientService;
    private WebSocketService webSocketService;
    private StorageService storageService;
    private DatabaseService databaseService;
    private EnvironmentService environmentService;
    private LoadTestingService loadTestingService;

    // Active state
    private final ObservableList<HistoryEntry> historyListItems = FXCollections.observableArrayList();
    private List<HistoryEntry> historyList;
    private List<com.apiforge.studio.model.Collection> collectionsList;

    // FXML Elements
    @FXML private BorderPane rootContainer;

    @FXML private ComboBox<com.apiforge.studio.model.Workspace> workspaceComboBox;
    @FXML private ComboBox<com.apiforge.studio.model.Environment> environmentComboBox;

    // Sidebar
    @FXML private TextField searchHistoryField;
    @FXML private ListView<HistoryEntry> historyListView;
    @FXML private TreeView<Object> collectionsTreeView;
    
    @FXML private VBox emptyStatePane;
    
    // TAB PANE
    @FXML private TabPane mainTabPane;

    public HttpClientService getHttpClientService() { return httpClientService; }
    public WebSocketService getWebSocketService() { return webSocketService; }
    public StorageService getStorageService() { return storageService; }
    public DatabaseService getDatabaseService() { return databaseService; }
    public EnvironmentService getEnvironmentService() { return environmentService; }
    public LoadTestingService getLoadTestingService() { return loadTestingService; }
    public String getTheme() { return "Light Mode"; }
    public ComboBox<com.apiforge.studio.model.Environment> getEnvironmentComboBox() { return environmentComboBox; }
    public List<HistoryEntry> getHistoryList() { return historyList; }
    public void addHistoryEntry(HistoryEntry entry) { 
        historyList.add(0, entry); 
        storageService.saveHistory(historyList);
        refreshHistoryView(); 
    }
    @FXML
    public RequestTabController getActiveTabController() {
        Tab selected = mainTabPane.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getUserData() instanceof RequestTabController) {
            return (RequestTabController) selected.getUserData();
        }
        return null;
    }

    @FXML
    public void initialize() {
        httpClientService = new HttpClientService();
        webSocketService = new WebSocketService();
        storageService = new StorageService();
        databaseService = new DatabaseService();
        environmentService = new EnvironmentService(databaseService.getConnection());
        loadTestingService = new LoadTestingService();

        onThemeChanged(null);

        // Setup '+' Add Tab
        Tab addTab = new Tab("+");
        addTab.setClosable(false);
        addTab.getStyleClass().add("add-tab-btn");
        mainTabPane.getTabs().add(addTab);
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == addTab) {
                Platform.runLater(() -> createNewEmptyTab(null));
            }
        });

        Platform.runLater(() -> {
            javafx.scene.Scene scene = rootContainer.getScene();
            if (scene != null) {
                scene.getAccelerators().put(
                    new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.S, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                    () -> saveToFavorites(null)
                );
            }
        });

        refreshWorkspacesSelector();
        historyList = storageService.loadHistory();
        refreshHistoryView();

        historyListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() >= 0 && newVal.intValue() < historyList.size()) {
                loadRequestIntoUI(historyList.get(newVal.intValue()).getRequest());
            }
        });

        collectionsTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.getValue() instanceof ApiRequest) {
                    loadRequestIntoUI((ApiRequest) newVal.getValue());
                } else if (newVal.getValue() instanceof com.apiforge.studio.model.RequestExample) {
                    loadExampleIntoUI((com.apiforge.studio.model.RequestExample) newVal.getValue());
                }
            }
        });

        setupCollectionsContextMenu();
        searchHistoryField.textProperty().addListener((obs, oldVal, newVal) -> filterHistory(newVal));
        
        mainTabPane.getTabs().addListener((ListChangeListener<Tab>) c -> {
            emptyStatePane.setVisible(mainTabPane.getTabs().size() <= 1);
        });
        emptyStatePane.setVisible(mainTabPane.getTabs().size() <= 1);

        historyListView.setCellFactory(lv -> new ListCell<HistoryEntry>() {
            @Override
            protected void updateItem(HistoryEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Label methodLabel = new Label(item.getRequest().getMethod());
                    methodLabel.getStyleClass().add("history-method-" + item.getRequest().getMethod().toLowerCase());
                    methodLabel.getStyleClass().add("history-method");
                    Label urlLabel = new Label(item.getRequest().getUrl());
                    urlLabel.getStyleClass().add("history-url");
                    hbox.getChildren().addAll(methodLabel, urlLabel);
                    setGraphic(hbox);
                }
            }
        });

        createNewTab();
    }
    
    @FXML
    private void createNewEmptyTab(ActionEvent event) {
        createNewTab();
    }
    
    public void createNewTab() {
        createNewTab(null);
    }
    
    public void createNewTab(ApiRequest req) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/apiforge/studio/request_tab.fxml"));
            Parent tabContent = loader.load();
            RequestTabController tabController = loader.getController();
            
            Tab tab = new Tab(req != null && req.getUrl() != null && !req.getUrl().isEmpty() ? req.getUrl() : "New Request");
            tab.setContent(tabContent);
            tab.setUserData(tabController);
            int size = mainTabPane.getTabs().size();
            if (size > 0 && "+".equals(mainTabPane.getTabs().get(size - 1).getText())) {
                mainTabPane.getTabs().add(size - 1, tab);
            } else {
                mainTabPane.getTabs().add(tab);
            }
            mainTabPane.getSelectionModel().select(tab);
            
            tabController.init(this, req != null ? req : new ApiRequest(), tab);
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Failed to create new tab: " + e.getMessage());
        }
    }
    
    public void loadRequestIntoUI(ApiRequest req) {
        if (req != null && req.getId() != null && !req.getId().isEmpty()) {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getUserData() instanceof RequestTabController) {
                    RequestTabController tc = (RequestTabController) tab.getUserData();
                    if (tc.getCurrentRequest() != null && req.getId().equals(tc.getCurrentRequest().getId())) {
                        mainTabPane.getSelectionModel().select(tab);
                        return;
                    }
                }
            }
        }
        createNewTab(req);
    }
    
    public void loadExampleIntoUI(com.apiforge.studio.model.RequestExample ex) {
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getUserData() instanceof RequestTabController) {
            ((RequestTabController) selectedTab.getUserData()).loadExampleIntoUI(ex);
        } else {
            createNewTab();
            Tab newTab = mainTabPane.getSelectionModel().getSelectedItem();
            if (newTab != null && newTab.getUserData() instanceof RequestTabController) {
                ((RequestTabController) newTab.getUserData()).loadExampleIntoUI(ex);
            }
        }
    }

private void refreshHistoryView() {
        historyListItems.clear();
        historyListItems.addAll(historyList);
        historyListView.setItems(historyListItems);
    }

    private void refreshCollectionsTree() {
        try {
            int wsId = getActiveWorkspaceId();
            collectionsList = databaseService.getCollections(wsId);
            TreeItem<Object> rootItem = new TreeItem<>("Collections Workspace");
            rootItem.setExpanded(true);
            
            System.out.println("Refreshing tree. Found " + collectionsList.size() + " collections.");

            for (com.apiforge.studio.model.Collection col : collectionsList) {
                TreeItem<Object> colNode = new TreeItem<>(col);
                colNode.setExpanded(true);
                
                System.out.println("Col: " + col.getName() + " has " + col.getFolders().size() + " folders and " + col.getRequests().size() + " requests.");
                
                // Add folders
                for (com.apiforge.studio.model.Folder fold : col.getFolders()) {
                    TreeItem<Object> foldNode = new TreeItem<>(fold);
                    foldNode.setExpanded(true);
                    System.out.println("  Fold: " + fold.getName() + " has " + fold.getRequests().size() + " requests.");
                    for (ApiRequest req : fold.getRequests()) {
                        TreeItem<Object> reqNode = new TreeItem<>(req);
                        for (com.apiforge.studio.model.RequestExample ex : req.getExamples()) {
                            reqNode.getChildren().add(new TreeItem<>(ex));
                        }
                        foldNode.getChildren().add(reqNode);
                    }
                    colNode.getChildren().add(foldNode);
                }

                // Add direct collection requests
                for (ApiRequest req : col.getRequests()) {
                    TreeItem<Object> reqNode = new TreeItem<>(req);
                    for (com.apiforge.studio.model.RequestExample ex : req.getExamples()) {
                        reqNode.getChildren().add(new TreeItem<>(ex));
                    }
                    colNode.getChildren().add(reqNode);
                }

                rootItem.getChildren().add(colNode);
            }

            collectionsTreeView.setRoot(rootItem);
            collectionsTreeView.setShowRoot(false);
        } catch (Exception e) {
            System.err.println("Error refreshing tree: " + e.getMessage());
            e.printStackTrace();
        }
    }

private void setupCollectionsContextMenu() {
        collectionsTreeView.setCellFactory(tv -> {
            TreeCell<Object> cell = new TreeCell<Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setContextMenu(null);
                    } else {
                        if (item instanceof ApiRequest) {
                            ApiRequest req = (ApiRequest) item;
                            setText(null);
                            HBox graphic = new HBox(6);
                            graphic.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            Label methodLbl = new Label(req.getMethod());
                            methodLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
                            switch (req.getMethod()) {
                                case "GET": methodLbl.setTextFill(javafx.scene.paint.Color.web("#0cbb52")); break;
                                case "POST": methodLbl.setTextFill(javafx.scene.paint.Color.web("#ffb400")); break;
                                case "PUT": methodLbl.setTextFill(javafx.scene.paint.Color.web("#097bed")); break;
                                case "DELETE": methodLbl.setTextFill(javafx.scene.paint.Color.web("#eb2013")); break;
                                default: methodLbl.setTextFill(javafx.scene.paint.Color.web("#808080")); break;
                            }
                            Label nameLbl = new Label(req.getName());
                            graphic.getChildren().addAll(methodLbl, nameLbl);
                            setGraphic(graphic);
                        } else {
                            // Folders and Collections
                            setText(item.toString());
                            if (item instanceof com.apiforge.studio.model.Collection) {
                                Label icon = new Label("🗂️");
                                icon.setStyle("-fx-font-size: 14px;");
                                setGraphic(icon);
                            } else if (item instanceof com.apiforge.studio.model.Folder) {
                                Label icon = new Label("📁");
                                icon.setStyle("-fx-font-size: 14px;");
                                setGraphic(icon);
                            } else {
                                setGraphic(null);
                            }
                        }

                        ContextMenu menu = new ContextMenu();
                        if (item instanceof com.apiforge.studio.model.Collection) {
                            com.apiforge.studio.model.Collection col = (com.apiforge.studio.model.Collection) item;
                            
                            MenuItem addFolderItem = new MenuItem("Add Folder");
                            addFolderItem.setOnAction(e -> promptCreateFolder(col.getId()));
                            MenuItem addRequestItem = new MenuItem("Add Request");
                            addRequestItem.setOnAction(e -> {
                                promptRename("New Request", "My Request").ifPresent(name -> {
                                    com.apiforge.studio.model.ApiRequest req = new com.apiforge.studio.model.ApiRequest();
                                    req.setProtocol("REST");
                                    req.setMethod("GET");
                                    req.setUrl("http://");
                                    databaseService.saveRequest(getActiveWorkspaceId(), col.getId(), null, name, req);
                                    refreshCollectionsTree();
                                });
                            });
                            MenuItem exportItem = new MenuItem("Export");
                            exportItem.setOnAction(e -> exportCollection(col));
                            MenuItem renameItem = new MenuItem("Rename");
                            renameItem.setOnAction(e -> {
                                promptRename("Collection", col.getName()).ifPresent(newName -> {
                                    databaseService.renameCollection(col.getId(), newName);
                                    refreshCollectionsTree();
                                });
                            });
                            MenuItem dupItem = new MenuItem("Duplicate");
                            dupItem.setOnAction(e -> {
                                databaseService.duplicateCollection(col.getId());
                                refreshCollectionsTree();
                            });
                            MenuItem shareItem = new MenuItem("Share");
                            shareItem.setOnAction(e -> showMockShare("Collection"));
                            MenuItem deleteColItem = new MenuItem("Delete");
                            deleteColItem.setOnAction(e -> {
                                databaseService.deleteCollection(col.getId());
                                refreshCollectionsTree();
                            });
                            
                            menu.getItems().addAll(addFolderItem, addRequestItem, new SeparatorMenuItem(), exportItem, renameItem, dupItem, shareItem, new SeparatorMenuItem(), deleteColItem);
                        } else if (item instanceof com.apiforge.studio.model.Folder) {
                            com.apiforge.studio.model.Folder fold = (com.apiforge.studio.model.Folder) item;
                            
                            MenuItem addReqItem = new MenuItem("Add Request");
                            addReqItem.setOnAction(e -> promptSaveRequestToCollection(fold.getCollectionId(), fold.getId()));
                            MenuItem renameItem = new MenuItem("Rename");
                            renameItem.setOnAction(e -> {
                                promptRename("Folder", fold.getName()).ifPresent(newName -> {
                                    databaseService.renameFolder(fold.getId(), newName);
                                    refreshCollectionsTree();
                                });
                            });
                            MenuItem dupItem = new MenuItem("Duplicate");
                            dupItem.setOnAction(e -> {
                                databaseService.duplicateFolder(fold.getId());
                                refreshCollectionsTree();
                            });
                            MenuItem shareItem = new MenuItem("Share");
                            shareItem.setOnAction(e -> showMockShare("Folder"));
                            MenuItem deleteFoldItem = new MenuItem("Delete");
                            deleteFoldItem.setOnAction(e -> {
                                databaseService.deleteFolder(fold.getId());
                                refreshCollectionsTree();
                            });
                            
                            menu.getItems().addAll(addReqItem, new SeparatorMenuItem(), renameItem, dupItem, shareItem, new SeparatorMenuItem(), deleteFoldItem);
                        } else if (item instanceof ApiRequest) {
                            ApiRequest req = (ApiRequest) item;
                            
                            MenuItem addExampleItem = new MenuItem("Add example");
                            addExampleItem.setOnAction(e -> {
                                promptRename("Example", "Mock Response").ifPresent(name -> {
                                    RequestTabController tc = getActiveTabController();
        String body = (tc != null && tc.getLastResponseBody() != null) ? tc.getLastResponseBody() : "{}";
                                    int status = (tc != null && tc.getLastResponseStatus() != 0) ? tc.getLastResponseStatus() : 200;
                                    databaseService.addExample(Integer.parseInt(req.getId()), name, status, body, "{}");
                                    refreshCollectionsTree();
                                });
                            });
                            MenuItem renameItem = new MenuItem("Rename");
                            renameItem.setOnAction(e -> {
                                promptRename("Request", req.getCustomName() != null ? req.getCustomName() : req.getUrl()).ifPresent(newName -> {
                                    databaseService.renameSavedRequest(Integer.parseInt(req.getId()), newName);
                                    refreshCollectionsTree();
                                });
                            });
                            MenuItem dupItem = new MenuItem("Duplicate");
                            dupItem.setOnAction(e -> {
                                databaseService.duplicateSavedRequest(Integer.parseInt(req.getId()));
                                refreshCollectionsTree();
                            });
                            MenuItem shareItem = new MenuItem("Share");
                            shareItem.setOnAction(e -> showMockShare("Request"));
                            MenuItem copyLinkItem = new MenuItem("Copy link");
                            copyLinkItem.setOnAction(e -> showMockShare("Request Link"));
                            MenuItem deleteReqItem = new MenuItem("Delete");
                            deleteReqItem.setOnAction(e -> {
                                databaseService.deleteSavedRequest(Integer.parseInt(req.getId()));
                                refreshCollectionsTree();
                            });
                            menu.getItems().addAll(addExampleItem, new SeparatorMenuItem(), shareItem, copyLinkItem, new SeparatorMenuItem(), renameItem, dupItem, new SeparatorMenuItem(), deleteReqItem);
                        } else if (item instanceof com.apiforge.studio.model.RequestExample) {
                            com.apiforge.studio.model.RequestExample ex = (com.apiforge.studio.model.RequestExample) item;
                            MenuItem renameExItem = new MenuItem("Rename");
                            renameExItem.setOnAction(e -> showInfo("Not Yet Implemented", "Rename example will be added later."));
                            MenuItem deleteExItem = new MenuItem("Delete Example");
                            deleteExItem.setOnAction(e -> {
                                databaseService.deleteExample(ex.getId());
                                refreshCollectionsTree();
                            });
                            menu.getItems().addAll(renameExItem, new SeparatorMenuItem(), deleteExItem);
                        }
                        setContextMenu(menu);
                    }
                }
            };
            return cell;
        });
    }

private java.util.Optional<String> promptRename(String type, String currentName) {
        TextInputDialog dialog = DialogUtils.createTextInputDialog(currentName, "Rename " + type, "Enter new name for " + type, "Name:");
        return dialog.showAndWait().filter(name -> !name.trim().isEmpty());
    }

public void showInfo(String title, String message) {
        Alert alert = DialogUtils.createAlert(Alert.AlertType.INFORMATION, title, null, message);
        alert.showAndWait();
    }

private int getActiveWorkspaceId() {
        if (workspaceComboBox != null && workspaceComboBox.getValue() != null) {
            return workspaceComboBox.getValue().getId();
        }
        return 1;
    }

@FXML
    private void onWorkspaceChanged(ActionEvent event) {
        refreshCollectionsTree();
        refreshEnvironments();
    }

private void refreshEnvironments() {
        if (environmentComboBox != null && environmentService != null) {
            int wsId = getActiveWorkspaceId();
            environmentComboBox.getItems().clear();
            environmentComboBox.getItems().addAll(environmentService.getEnvironments(wsId));
        }
    }

@FXML
    private void onEnvironmentChanged(ActionEvent event) {
        // Handle environment change if needed
    }

    @FXML
    private void manageEnvironments(ActionEvent event) {
        Dialog<Void> dialog = DialogUtils.createCustomDialog("Manage Environments", "Create a new Environment");
        
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Environment Name (e.g. Production)");
        TextField keyField = new TextField();
        keyField.setPromptText("Variable Key (e.g. API_URL)");
        TextField valueField = new TextField();
        valueField.setPromptText("Variable Value");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Var Key:"), 0, 1);
        grid.add(keyField, 1, 1);
        grid.add(new Label("Var Value:"), 0, 2);
        grid.add(valueField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                String key = keyField.getText().trim();
                String value = valueField.getText().trim();
                
                if (!name.isEmpty()) {
                    int workspaceId = getActiveWorkspaceId();
                    int envId = environmentService.createEnvironment(workspaceId, name);
                    if (envId != -1 && !key.isEmpty()) {
                        environmentService.addVariable(envId, key, value, false);
                    }
                    refreshEnvironments();
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }

@FXML
    private void createNewWorkspace(ActionEvent event) {
        TextInputDialog nameDialog = DialogUtils.createTextInputDialog("New Workspace", "Create Workspace", "Create a new logical Workspace container", "Workspace Name:");
        
        nameDialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                List<String> types = java.util.Arrays.asList("PERSONAL", "TEAM", "ENTERPRISE");
                ChoiceDialog<String> typeChoice = DialogUtils.createChoiceDialog("PERSONAL", types, "Workspace Type", "Select workspace tier classification", "Type:");
                typeChoice.showAndWait().ifPresent(type -> {
                    databaseService.createWorkspace(name.trim(), type);
                    refreshWorkspacesSelector();
                });
            }
        });
    }

private void refreshWorkspacesSelector() {
        List<com.apiforge.studio.model.Workspace> list = databaseService.getWorkspaces();
        workspaceComboBox.getItems().clear();
        workspaceComboBox.getItems().addAll(list);
        if (!list.isEmpty()) {
            workspaceComboBox.setValue(list.get(0));
        }
        refreshCollectionsTree();
        refreshEnvironments();
    }

    @FXML
    private void saveToFavorites(ActionEvent event) {
        TreeItem<Object> selectedItem = collectionsTreeView.getSelectionModel().getSelectedItem();
        Integer defaultColId = null;
        Integer defaultFolderId = null;

        if (selectedItem != null && selectedItem.getValue() != null) {
            Object val = selectedItem.getValue();
            if (val instanceof com.apiforge.studio.model.Collection) {
                defaultColId = ((com.apiforge.studio.model.Collection) val).getId();
            } else if (val instanceof com.apiforge.studio.model.Folder) {
                com.apiforge.studio.model.Folder f = (com.apiforge.studio.model.Folder) val;
                defaultColId = f.getCollectionId();
                defaultFolderId = f.getId();
            } else if (val instanceof ApiRequest) {
                // If they selected a request, default to its parent folder/collection
                TreeItem<Object> parent = selectedItem.getParent();
                if (parent != null && parent.getValue() instanceof com.apiforge.studio.model.Folder) {
                    com.apiforge.studio.model.Folder f = (com.apiforge.studio.model.Folder) parent.getValue();
                    defaultColId = f.getCollectionId();
                    defaultFolderId = f.getId();
                } else if (parent != null && parent.getValue() instanceof com.apiforge.studio.model.Collection) {
                    defaultColId = ((com.apiforge.studio.model.Collection) parent.getValue()).getId();
                }
            }
        }
        promptSaveRequestToCollection(defaultColId, defaultFolderId);
    }

    public void promptSaveRequestToCollection(Integer defaultColId, Integer defaultFolderId) {
        RequestTabController tc_save = getActiveTabController();
        if (tc_save != null) tc_save.updateRequestFromUI();
        ApiRequest currentRequest = tc_save != null ? tc_save.getCurrentRequest() : new ApiRequest();
        int wsId = getActiveWorkspaceId();
        
        if (currentRequest.getId() != null && !currentRequest.getId().isEmpty()) {
            // Existing request, just update it silently
            boolean success = databaseService.updateRequest(currentRequest);
            refreshCollectionsTree();
            showSaveResult(success ? Integer.parseInt(currentRequest.getId()) : -1, currentRequest.getName());
            return;
        }

        TextInputDialog nameDialog = DialogUtils.createTextInputDialog("My Request", "Save Request", "Save current request to your workspace", "Enter request display name:");
        
        nameDialog.showAndWait().ifPresent(requestName -> {
            if (defaultColId == null && defaultFolderId == null) {
                List<com.apiforge.studio.model.Collection> cols = databaseService.getCollections(wsId);
                if (cols.isEmpty()) {
                    int colId = databaseService.createCollection(wsId, "Default Collection");
                    int newId = databaseService.saveRequest(wsId, colId, null, requestName, currentRequest);
                    showSaveResult(newId, requestName);
                } else {
                    ChoiceDialog<com.apiforge.studio.model.Collection> colChoice = DialogUtils.createChoiceDialog(cols.get(0), cols, "Select Collection", "Choose destination Collection", "Collection:");
                    
                    colChoice.showAndWait().ifPresent(chosenCol -> {
                        int newId = databaseService.saveRequest(wsId, chosenCol.getId(), null, requestName, currentRequest);
                        showSaveResult(newId, requestName);
                    });
                }
            } else {
                int newId = databaseService.saveRequest(wsId, defaultColId, defaultFolderId, requestName, currentRequest);
                showSaveResult(newId, requestName);
            }
            refreshCollectionsTree();
        });
    }

    private void showSaveResult(int id, String requestName) {
        Platform.runLater(() -> {
            if (id > 0) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Request '" + requestName + "' saved successfully!");
                DialogUtils.applyDialogStyle(alert);
                alert.showAndWait();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to save request. Check logs.");
                DialogUtils.applyDialogStyle(alert);
                alert.showAndWait();
            }
        });
    }

@FXML
    private void createNewCollection(ActionEvent event) {
        TextInputDialog dialog = DialogUtils.createTextInputDialog("New Collection", "Create Collection", "Create a new Collections suite", "Collection Name:");
        int wsId = getActiveWorkspaceId();
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                databaseService.createCollection(wsId, name.trim());
                refreshCollectionsTree();
            }
        });
    }

@FXML
    private void createNewFolder(ActionEvent event) {
        int wsId = getActiveWorkspaceId();
        List<com.apiforge.studio.model.Collection> cols = databaseService.getCollections(wsId);
        if (cols.isEmpty()) {
            showError("Please create a Collection first!");
            return;
        }
        
        ChoiceDialog<com.apiforge.studio.model.Collection> colChoice = DialogUtils.createChoiceDialog(cols.get(0), cols, "Select Collection", "Select which Collection to place folder in", "Collection:");
        
        colChoice.showAndWait().ifPresent(chosenCol -> {
            TextInputDialog nameDialog = DialogUtils.createTextInputDialog("New Folder", "Create Folder", "Create folder inside " + chosenCol.getName(), "Folder Name:");
            nameDialog.showAndWait().ifPresent(folderName -> {
                if (!folderName.trim().isEmpty()) {
                    databaseService.createFolder(chosenCol.getId(), folderName.trim());
                    refreshCollectionsTree();
                }
            });
        });
    }

private void promptCreateFolder(int colId) {
        TextInputDialog dialog = DialogUtils.createTextInputDialog("New Folder", "Create Folder", "Add folder to collection", "Folder Name:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                databaseService.createFolder(colId, name.trim());
                refreshCollectionsTree();
            }
        });
    }

private void filterHistory(String query) {
        if (query == null || query.isEmpty()) {
            refreshHistoryView();
            return;
        }
        List<HistoryEntry> filtered = historyList.stream()
                .filter(e -> e.getRequest().getUrl().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        historyListView.setItems(FXCollections.observableArrayList(filtered));
    }

public void showError(String message) {
        Alert alert = DialogUtils.createAlert(Alert.AlertType.ERROR, "Error", null, message);
        alert.showAndWait();
    }

public String getBackendUrl() {
        String baseUrl = "http://localhost:8080";
        java.io.File configFile = new java.io.File("config.properties");
        if (configFile.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(configFile)) {
                java.util.Properties props = new java.util.Properties();
                props.load(fis);
                baseUrl = props.getProperty("backend.url", baseUrl);
            } catch (Exception e) {
                System.err.println("Failed to read config.properties: " + e.getMessage());
            }
        }
        return baseUrl;
    }

public void postHistoryToBackend(String method, String url, int statusCode, long responseTime) {
        new Thread(() -> {
            try {
                // Offline caching
                if (databaseService != null) {
                    databaseService.cacheRequest(java.util.UUID.randomUUID().toString(), method, url, statusCode, responseTime);
                }

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                // Escape simple JSON characters in URL
                String safeUrl = url.replace("\"", "\\\"");
                String json = String.format("{\"method\":\"%s\",\"url\":\"%s\",\"statusCode\":%d,\"responseTimeMs\":%d}",
                        method, safeUrl, statusCode, responseTime);
                String backendUrl = getBackendUrl();
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(backendUrl + "/api/history"))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();
                client.send(req, java.net.http.HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                System.err.println("Could not sync history to Spring Boot backend: " + e.getMessage());
            }
        }).start();
    }

@FXML
    private void onThemeChanged(ActionEvent event) {
        String theme = "Light Mode";
        boolean isLight = "Light Mode".equals(theme);
        if (!isLight) {
            if (!rootContainer.getStyleClass().contains("dark-theme")) {
                rootContainer.getStyleClass().add("dark-theme");
            }
        } else {
            rootContainer.getStyleClass().remove("dark-theme");
        }
        
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getUserData() instanceof RequestTabController) {
                ((RequestTabController) tab.getUserData()).applyTheme(isLight);
            }
        }

    }

@FXML
    private void showImportDialog(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/apiforge/studio/import_modal.fxml"));
            Parent root = loader.load();
            ImportController importController = loader.getController();
            importController.setMainController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Import");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 650, 500);
            
            // Apply current theme (Light Mode enforced)
            scene.getRoot().getStyleClass().remove("dark-theme");
            
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open Import dialog: " + e.getMessage());
        }
    }

    public void importFromCurl(String curlCommand) {
        try {
            com.apiforge.studio.model.ApiRequest req = com.apiforge.studio.utils.CurlParser.parse(curlCommand);
            req.setName("Imported cURL");
            
            // Try to populate current tab if it's empty, otherwise create a new tab
            RequestTabController tc = getActiveTabController();
            if (tc != null && (tc.getCurrentRequest().getUrl() == null || tc.getCurrentRequest().getUrl().isEmpty())) {
                tc.init(this, req, mainTabPane.getSelectionModel().getSelectedItem());
            } else {
                createNewTab(req);
            }
        } catch (Exception e) {
            showError("Failed to parse cURL: " + e.getMessage());
        }
    }

public void importCollectionFromFile(File file) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String content = Files.readString(file.toPath());
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(content);
            
            int wsId = getActiveWorkspaceId();
            
            if (rootNode.has("info") && rootNode.get("info").has("name")) {
                // Postman v2.1 format
                String colName = rootNode.get("info").get("name").asText();
                int colId = databaseService.createCollection(wsId, colName);
                if (rootNode.has("item") && rootNode.get("item").isArray()) {
                    parsePostmanItems(wsId, colId, null, rootNode.get("item"));
                }
                refreshCollectionsTree();
                showInfo("Import Success", "Postman Collection imported successfully from " + file.getName());
            } else if (rootNode.has("collection")) {
                // ApiForge format
                com.fasterxml.jackson.databind.JsonNode colNode = rootNode.get("collection");
                String colName = colNode.has("name") ? colNode.get("name").asText() : "Imported Collection";
                databaseService.createCollection(wsId, colName);
                refreshCollectionsTree();
                showInfo("Import Success", "ApiForge Collection imported successfully from " + file.getName());
            } else {
                showError("Invalid Collection format. Ensure it is a Postman v2.1 or ApiForge JSON.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to import file: " + e.getMessage());
        }
    }

private void parsePostmanItems(int wsId, int colId, Integer folderId, com.fasterxml.jackson.databind.JsonNode itemsArray) {
        for (com.fasterxml.jackson.databind.JsonNode item : itemsArray) {
            String name = item.has("name") ? item.get("name").asText() : "Unnamed";
            if (item.has("item") && item.get("item").isArray()) {
                // It's a folder
                int newFolderId = databaseService.createFolder(colId, name);
                parsePostmanItems(wsId, colId, newFolderId, item.get("item"));
            } else if (item.has("request")) {
                // It's a request
                com.fasterxml.jackson.databind.JsonNode reqNode = item.get("request");
                String method = reqNode.has("method") ? reqNode.get("method").asText() : "GET";
                String url = "";
                if (reqNode.has("url")) {
                    com.fasterxml.jackson.databind.JsonNode urlNode = reqNode.get("url");
                    if (urlNode.isTextual()) url = urlNode.asText();
                    else if (urlNode.has("raw")) url = urlNode.get("raw").asText();
                }
                
                com.apiforge.studio.model.ApiRequest req = new com.apiforge.studio.model.ApiRequest();
                req.setProtocol("REST");
                req.setMethod(method);
                req.setUrl(url);
                
                // Parse body if present
                if (reqNode.has("body") && reqNode.get("body").has("raw")) {
                    req.setBody(reqNode.get("body").get("raw").asText());
                }
                
                int newReqId = databaseService.saveRequest(wsId, colId, folderId, name, req);
                
                // Parse Examples
                if (item.has("response") && item.get("response").isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode resp : item.get("response")) {
                        String respName = resp.has("name") ? resp.get("name").asText() : "Example";
                        int code = resp.has("code") ? resp.get("code").asInt() : 200;
                        String body = "";
                        if (resp.has("body")) {
                            body = resp.get("body").asText();
                        }
                        String headersJson = "{}";
                        databaseService.addExample(newReqId, respName, code, body, headersJson);
                    }
                }
            }
        }
    }

private void exportCollection(com.apiforge.studio.model.Collection col) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Collection");
            fileChooser.setInitialFileName(col.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".json");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(rootContainer.getScene().getWindow());
            
            if (file != null) {
                Map<String, Object> exportData = new HashMap<>();
                Map<String, Object> colData = new HashMap<>();
                colData.put("id", col.getId());
                colData.put("name", col.getName());
                exportData.put("collection", colData);
                
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
                Files.writeString(file.toPath(), json);
                showInfo("Export Success", "Collection exported successfully!");
            }
        } catch (Exception e) {
            showError("Failed to export collection: " + e.getMessage());
        }
    }



@FXML
    private void clearHistory(ActionEvent event) {
        historyList.clear();
        storageService.saveHistory(historyList);
        refreshHistoryView();
    }

    public void showMockShare(String type) {
        String mockLink = "apiforge://" + type.toLowerCase() + "/" + java.util.UUID.randomUUID().toString().substring(0, 8);
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(mockLink);
        clipboard.setContent(content);
        showInfo("Link Copied", type + " share link copied to clipboard:\n" + mockLink);
    }

}
