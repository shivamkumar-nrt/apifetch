import json
import re

with open('methods.json', 'r') as f:
    methods = json.load(f)

tab_methods_names = [
    "onProtocolChanged", "onAuthTypeChanged", "addQueryParam", "addFormData", 
    "isStandardRequestHeader", "isStandardResponseHeader", "setupPostmanHeadersTable", 
    "updateHeaderCountBadge", "addHeader", "clearAllHeaders", "addPresetJson", 
    "addPresetMultipart", "addPresetAccept", "addPresetBearer", "addPresetNoCache", 
    "addPresetUserAgent", "sendRequest", "executeStandardRequest", "handleWebSocketConnection", 
    "handleSseConnection", "updateStreamView", "updateRequestFromUI", "loadRequestIntoUI", "loadExampleIntoUI", 
    "saveExample", "saveToFavorites", "addExampleHeader", "generateAiTests", 
    "generateAiMockPayload", "generateAiExplanation", "runLoadSimulation", 
    "registerCloudMockRoute", "formatJson", "generateHighlightedJsonHtml", 
    "generateRichVisualizerHtml"
]

main_methods_names = [
    "refreshHistoryView", "refreshCollectionsTree", "setupCollectionsContextMenu", 
    "promptRename", "showInfo", "getActiveWorkspaceId", "onWorkspaceChanged", 
    "refreshEnvironments", "onEnvironmentChanged", "manageEnvironments", "createNewWorkspace", 
    "refreshWorkspacesSelector", "promptSaveRequestToCollection", "createNewCollection", 
    "createNewFolder", "promptCreateFolder", "filterHistory", "showError", "getBackendUrl", 
    "postHistoryToBackend", "onThemeChanged", "showImportDialog", "importFromCurl", 
    "importCollectionFromFile", "parsePostmanItems", "exportCollection", "showMockShare", "clearHistory"
]

# Write MainController.java
with open('MainController_header.txt', 'w') as f:
    pass # we'll write directly

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'r', encoding='utf-8') as f:
    original = f.read()
    
# Extract imports
imports_match = re.search(r'(import .*?;[\s\n]*)+', original)
imports = imports_match.group(0) if imports_match else ""

main_fields = """
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
    public ComboBox<com.apiforge.studio.model.Environment> getEnvironmentComboBox() { return environmentComboBox; }
    public List<HistoryEntry> getHistoryList() { return historyList; }
    public void addHistoryEntry(HistoryEntry entry) { 
        historyList.add(0, entry); 
        storageService.saveHistory(historyList);
        refreshHistoryView(); 
    }
"""

main_init = """    @FXML
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
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);
            
            tabController.init(this, req != null ? req : new ApiRequest());
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Failed to create new tab: " + e.getMessage());
        }
    }
    
    public void loadRequestIntoUI(ApiRequest req) {
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
"""

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\MainController.java', 'w', encoding='utf-8') as f:
    f.write("package com.apiforge.studio.controller;\n\n")
    f.write(imports + "\n\n")
    f.write("public class MainController {\n")
    f.write(main_fields)
    f.write(main_init)
    for m in main_methods_names:
        if m in methods:
            f.write("\n" + methods[m] + "\n")
    f.write("}\n")

print("MainController rewritten.")

tab_fields = """
    private MainController mainController;
    
    private ApiRequest currentRequest;
    private String lastResponseBody = null;
    private int lastResponseStatus = 0;
    private final ObservableList<KeyValuePair> queryParamsData = FXCollections.observableArrayList();
    private final ObservableList<KeyValuePair> headersData = FXCollections.observableArrayList();
    private final ObservableList<KeyValuePair> responseHeadersData = FXCollections.observableArrayList();
    private com.apiforge.studio.model.RequestExample currentExample;
    private final ObservableList<KeyValuePair> formDataList = FXCollections.observableArrayList();
    private StringBuilder streamBuffer = new StringBuilder();

    @FXML private ComboBox<String> protocolComboBox;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private ComboBox<String> authTypeComboBox;
    @FXML private TextField urlTextField;
    @FXML private Button sendButton;
    @FXML private Button saveExampleButton;
    @FXML private TextArea preRequestScriptArea;
    @FXML private TextArea testScriptArea;

    @FXML private TableView<KeyValuePair> paramsTable;
    @FXML private TableColumn<KeyValuePair, String> paramKeyColumn;
    @FXML private TableColumn<KeyValuePair, String> paramValueColumn;
    @FXML private TextField paramKeyField;
    @FXML private TextField paramValueField;

    @FXML private Tab headersTab;
    @FXML private TableView<KeyValuePair> headersTable;
    @FXML private TableColumn<KeyValuePair, Boolean> headerEnabledColumn;
    @FXML private TableColumn<KeyValuePair, String> headerKeyColumn;
    @FXML private TableColumn<KeyValuePair, String> headerValueColumn;
    @FXML private TableColumn<KeyValuePair, Void> headerDeleteColumn;
    @FXML private TextField headerKeyField;
    @FXML private TextField headerValueField;
    @FXML private Label headerCountLabel;
    @FXML private MenuButton presetHeadersBtn;

    @FXML private StackPane authConfigContainer;
    @FXML private VBox basicAuthForm;
    @FXML private VBox bearerAuthForm;
    @FXML private VBox apiKeyAuthForm;
    @FXML private Label noAuthLabel;
    @FXML private TextField authUsernameField;
    @FXML private PasswordField authPasswordField;
    @FXML private TextField authTokenField;
    @FXML private TextField apiKeyNameField;
    @FXML private TextField apiKeyValueField;

    @FXML private Tab bodyTab;
    @FXML private TextArea bodyTextArea;
    @FXML private ToggleGroup bodyTypeGroup;
    @FXML private RadioButton bodyNoneRadio;
    @FXML private RadioButton bodyFormDataRadio;
    @FXML private RadioButton bodyUrlEncodedRadio;
    @FXML private RadioButton bodyRawRadio;
    @FXML private RadioButton bodyBinaryRadio;
    @FXML private VBox formDataPane;
    @FXML private TextField formKeyField;
    @FXML private TextField formValueField;
    @FXML private TableView<KeyValuePair> formDataTable;
    @FXML private TableColumn<KeyValuePair, String> formKeyColumn;
    @FXML private TableColumn<KeyValuePair, String> formValueColumn;
    @FXML private Label noBodyLabel;

    @FXML private CheckBox followRedirectsCheck;
    @FXML private CheckBox verifySslCheck;

    @FXML private Label statusBadge;
    @FXML private TextField exampleStatusCodeField;
    @FXML private Label timeLabel;
    @FXML private Label sizeLabel;
    @FXML private ProgressIndicator loadingProgress;
    @FXML private WebView responsePrettyWebView;
    @FXML private TextArea exampleBodyEditor;
    @FXML private WebView responseWebView;
    @FXML private TableView<KeyValuePair> responseHeadersTable;
    @FXML private TableColumn<KeyValuePair, String> responseHeaderKeyColumn;
    @FXML private TableColumn<KeyValuePair, String> responseHeaderValueColumn;
    @FXML private Button addExampleHeaderBtn;
    
    @FXML private TextArea aiResponseTextArea;
    @FXML private TextField loadUsersField;
    @FXML private TextField loadLoopsField;
    @FXML private TextArea loadTestResultsTextArea;
    
    @FXML private TextField mockPathField;
    @FXML private TextField mockStatusField;
    @FXML private TextField mockDelayField;
    @FXML private TextArea mockBodyField;

    public void init(MainController mainController, ApiRequest req) {
        this.mainController = mainController;
        this.currentRequest = req;

        protocolComboBox.getItems().addAll("REST", "GraphQL", "WebSocket", "gRPC", "SOAP", "SSE");
        protocolComboBox.setValue("REST");

        methodComboBox.getItems().addAll("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
        methodComboBox.setValue("GET");

        authTypeComboBox.getItems().addAll("NONE", "BASIC", "BEARER", "API_KEY");
        authTypeComboBox.setValue("NONE");

        paramKeyColumn.setCellValueFactory(data -> data.getValue().keyProperty());
        paramValueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        paramsTable.setItems(queryParamsData);

        setupPostmanHeadersTable();

        responseHeaderKeyColumn.setCellValueFactory(data -> data.getValue().keyProperty());
        responseHeaderValueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        responseHeadersTable.setItems(responseHeadersData);
        
        responseHeaderKeyColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        responseHeaderValueColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        
        formKeyColumn.setCellValueFactory(data -> data.getValue().keyProperty());
        formValueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        formDataTable.setItems(formDataList);

        bodyTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == bodyNoneRadio) {
                bodyTextArea.setVisible(false);
                formDataPane.setVisible(false);
                noBodyLabel.setVisible(true);
            } else if (newVal == bodyFormDataRadio || newVal == bodyUrlEncodedRadio) {
                bodyTextArea.setVisible(false);
                formDataPane.setVisible(true);
                noBodyLabel.setVisible(false);
            } else {
                bodyTextArea.setVisible(true);
                formDataPane.setVisible(false);
                noBodyLabel.setVisible(false);
            }
        });
        
        if (req != null && req.getUrl() != null) {
            loadRequestIntoUI(req);
        }
    }
"""

with open(r'e:\Api Tester\desktop\src\main\java\com\apiforge\studio\controller\RequestTabController.java', 'w', encoding='utf-8') as f:
    f.write("package com.apiforge.studio.controller;\n\n")
    f.write(imports + "\n\n")
    f.write("public class RequestTabController {\n")
    f.write(tab_fields)
    
    for m in tab_methods_names:
        if m in methods:
            code = methods[m]
            code = re.sub(r'\bhttpClientService\b', 'mainController.getHttpClientService()', code)
            code = re.sub(r'\bwebSocketService\b', 'mainController.getWebSocketService()', code)
            code = re.sub(r'\bstorageService\b', 'mainController.getStorageService()', code)
            code = re.sub(r'\bdatabaseService\b', 'mainController.getDatabaseService()', code)
            code = re.sub(r'\benvironmentService\b', 'mainController.getEnvironmentService()', code)
            code = re.sub(r'\bloadTestingService\b', 'mainController.getLoadTestingService()', code)
            code = re.sub(r'\bthemeComboBox(?:\.getValue\(\))?\b', 'mainController.getTheme()', code)
            code = re.sub(r'\benvironmentComboBox\b', 'mainController.getEnvironmentComboBox()', code)
            code = re.sub(r'\bshowError\(', 'mainController.showError(', code)
            code = re.sub(r'\bshowInfo\(', 'mainController.showInfo(', code)
            code = re.sub(r'\bshowMockShare\(', 'mainController.showMockShare(', code)
            code = re.sub(r'\bpromptSaveRequestToCollection\(', 'mainController.promptSaveRequestToCollection(', code)
            code = re.sub(r'\bpostHistoryToBackend\(', 'mainController.postHistoryToBackend(', code)
            
            # special case for historyList
            if "historyList.add(0, entry);" in code:
                code = code.replace("historyList.add(0, entry);", "mainController.addHistoryEntry(entry);")
                code = code.replace("storageService.saveHistory(historyList);", "")
                code = code.replace("refreshHistoryView();", "")
                
            f.write("\n" + code + "\n")
    f.write("}\n")
print("RequestTabController written.")
