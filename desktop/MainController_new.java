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
import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainController {

    // Services
    private HttpClientService httpClientService;
    private WebSocketService webSocketService;
    private StorageService storageService;
    private DatabaseService databaseService;
    private EnvironmentService environmentService;
    private LoadTestingService loadTestingService;

    // Active state
    private final ObservableList<String> historyListItems = FXCollections.observableArrayList();
    private List<HistoryEntry> historyList;
    private List<com.apiforge.studio.model.Collection> collectionsList;

    // FXML Elements
    @FXML private BorderPane rootContainer;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<com.apiforge.studio.model.Workspace> workspaceComboBox;
    @FXML private ComboBox<com.apiforge.studio.model.Environment> environmentComboBox;

    // Sidebar
    @FXML private TextField searchHistoryField;
    @FXML private ListView<String> historyListView;
    @FXML private TreeView<Object> collectionsTreeView;
    
    // TAB PANE
    @FXML private TabPane mainTabPane;

    public HttpClientService getHttpClientService() { return httpClientService; }
    public WebSocketService getWebSocketService() { return webSocketService; }
    public StorageService getStorageService() { return storageService; }
    public DatabaseService getDatabaseService() { return databaseService; }
    public EnvironmentService getEnvironmentService() { return environmentService; }
    public LoadTestingService getLoadTestingService() { return loadTestingService; }
    public String getTheme() { return themeComboBox != null ? themeComboBox.getValue() : "Dark Mode"; }
    public EnvironmentComboBox() { return environmentComboBox; } // helper for env

    @FXML
    public void initialize() {
        httpClientService = new HttpClientService();
        webSocketService = new WebSocketService();
        storageService = new StorageService();
        databaseService = new DatabaseService();
        environmentService = new EnvironmentService(databaseService.getConnection());
        loadTestingService = new LoadTestingService();

        themeComboBox.getItems().addAll("Dark Mode", "Light Mode");
        themeComboBox.setValue("Dark Mode");
        onThemeChanged(null);

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
        
        // Load initial tab
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
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);
            
            tabController.init(this, req);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to create new tab: " + e.getMessage());
        }
    }
    
    public void loadRequestIntoUI(ApiRequest req) {
        createNewTab(req);
    }
    
    public void loadExampleIntoUI(com.apiforge.studio.model.RequestExample ex) {
        // Find active tab and load example
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            // Need a way to get controller of selected tab. We can store it in Tab's userData
            // But a new tab for example might be easier? Let's just create a new tab for now, or just use selected
        }
        // Let's create new tab for example for simplicity, or modify the signature if we can find it.
        // Actually, let's just make it call createNewTab but we don't have ApiRequest.
        // Better: store RequestTabController in Tab.setUserData(tabController)
    }
