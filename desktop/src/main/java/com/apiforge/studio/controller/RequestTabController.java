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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;



public class RequestTabController {

    private javafx.scene.Node findDeepNode(javafx.scene.Node root, String id) {
        if (root == null) return null;
        if (id.equals(root.getId())) return root;
        
        if (root instanceof javafx.scene.control.SplitPane) {
            for (javafx.scene.Node item : ((javafx.scene.control.SplitPane) root).getItems()) {
                javafx.scene.Node found = findDeepNode(item, id);
                if (found != null) return found;
            }
        }
        
        if (root instanceof javafx.scene.control.TabPane) {
            for (javafx.scene.control.Tab t : ((javafx.scene.control.TabPane) root).getTabs()) {
                javafx.scene.Node found = findDeepNode(t.getContent(), id);
                if (found != null) return found;
            }
        } else if (root instanceof javafx.scene.control.ScrollPane) {
            javafx.scene.Node found = findDeepNode(((javafx.scene.control.ScrollPane) root).getContent(), id);
            if (found != null) return found;
        } else if (root instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) root).getChildrenUnmodifiable()) {
                javafx.scene.Node found = findDeepNode(child, id);
                if (found != null) return found;
            }
        }
        return null;
    }

    @FXML private void addHeader(javafx.event.ActionEvent event) {}
    @FXML private void addFormData(javafx.event.ActionEvent event) {}
    @FXML private void addQueryParam(javafx.event.ActionEvent event) {}

    @FXML
    private VBox paramsRowsBox;
    @FXML
    private VBox headersRowsBox;
    @FXML
    private VBox formDataRowsBox;
    
    private com.apiforge.studio.ui.PostmanTableEditor paramsEditor;
    private com.apiforge.studio.ui.PostmanTableEditor headersEditor;
    private com.apiforge.studio.ui.PostmanTableEditor formDataEditor;
    public String getLastResponseBody() { return lastResponseBody; }
    public int getLastResponseStatus() { return lastResponseStatus; }
    public ApiRequest getCurrentRequest() { return currentRequest; }
    
    public void applyTheme(boolean isLight) {
        if (lastResponseBody != null) {
            responseWebView.getEngine().loadContent(generateRichVisualizerHtml(lastResponseBody, isLight));
            responsePrettyWebView.getEngine().loadContent(generateHighlightedJsonHtml(formatJson(lastResponseBody), isLight));
        } else {
            String bg = isLight ? "#ffffff" : "#1e1e1e";
            String html = "<html><body style='background-color:" + bg + "; margin:0;'></body></html>";
            responseWebView.getEngine().loadContent(html);
            if (responsePrettyWebView != null) {
                responsePrettyWebView.getEngine().loadContent(html);
            }
        }
    }


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

    @FXML
    private ComboBox<String> protocolComboBox;
    @FXML
    private ComboBox<String> methodComboBox;
    @FXML
    private ComboBox<String> authTypeComboBox;
    @FXML
    private TextField urlTextField;
    @FXML
    private Button sendButton;
    @FXML
    private Button saveExampleButton;
    // New features
    @FXML
    private Button bulkEditParamsBtn;
    @FXML
    private Button saveResponseBtn;
    @FXML
    private javafx.scene.control.TextArea paramsBulkEditArea;
    @FXML
    private HBox paramsBulkEditDoneRow;
    // Advanced auth forms
    @FXML
    private VBox digestAuthForm;
    @FXML
    private VBox oauth2Form;
    @FXML
    private VBox awsForm;
    @FXML
    private TextField digestUsernameField;
    @FXML
    private PasswordField digestPasswordField;
    @FXML
    private TextField oauth2TokenField;
    @FXML
    private TextField oauth2TokenUrlField;
    @FXML
    private TextField oauth2ClientIdField;
    @FXML
    private PasswordField oauth2ClientSecretField;
    @FXML
    private TextField oauth2ScopeField;
    @FXML
    private TextField awsAccessKeyField;
    @FXML
    private PasswordField awsSecretKeyField;
    @FXML
    private TextField awsRegionField;
    @FXML
    private TextField awsServiceField;
    @FXML
    private Label advancedAuthHintLabel;
    // Cookies table
    @FXML
    private TableView<com.apiforge.studio.model.CookieEntry> responseCookiesTable;
    @FXML
    private TableColumn<com.apiforge.studio.model.CookieEntry, String> cookieNameCol;
    @FXML
    private TableColumn<com.apiforge.studio.model.CookieEntry, String> cookieValueCol;
    @FXML
    private TableColumn<com.apiforge.studio.model.CookieEntry, String> cookieDomainCol;
    @FXML
    private TableColumn<com.apiforge.studio.model.CookieEntry, String> cookiePathCol;
    private javafx.collections.ObservableList<com.apiforge.studio.model.CookieEntry> cookiesData = javafx.collections.FXCollections.observableArrayList();
    // Test results
    @FXML
    private Label testPassLabel;
    @FXML
    private Label testFailLabel;
    @FXML
    private ListView<String> testResultsList;
    @FXML
    private TextArea preRequestScriptArea;
    @FXML
    private TextArea testScriptArea;

    @FXML
    private Tab headersTab;
    @FXML
    private TableColumn<KeyValuePair, Void> headerDeleteColumn;
    @FXML
    private Label headerCountLabel;
    @FXML
    private MenuButton presetHeadersBtn;

    @FXML
    private StackPane authConfigContainer;
    @FXML
    private VBox basicAuthForm;
    @FXML
    private VBox bearerAuthForm;
    @FXML
    private VBox apiKeyAuthForm;
    @FXML
    private VBox advancedAuthForm;
    @FXML
    private Label noAuthLabel;
    @FXML
    private TextField authUsernameField;
    @FXML
    private PasswordField authPasswordField;
    @FXML
    private TextField authTokenField;
    @FXML
    private TextField apiKeyNameField;
    @FXML
    private TextField apiKeyValueField;

    @FXML
    private Tab bodyTab;
    @FXML
    private TextArea bodyTextArea;
    @FXML
    private ToggleGroup bodyTypeGroup;
    @FXML
    private RadioButton bodyNoneRadio;
    @FXML
    private RadioButton bodyFormDataRadio;
    @FXML
    private RadioButton bodyUrlEncodedRadio;
    @FXML
    private RadioButton bodyRawRadio;
    @FXML
    private RadioButton bodyBinaryRadio;
    @FXML
    private VBox formDataPane;
    @FXML
    private Label noBodyLabel;

    @FXML
    private CheckBox followRedirectsCheck;
    @FXML
    private CheckBox verifySslCheck;
    @FXML
    private CheckBox followOriginalHttpMethodCheck;
    @FXML
    private CheckBox followAuthorizationHeaderCheck;
    @FXML
    private CheckBox removeRefererHeaderOnRedirectCheck;
    @FXML
    private CheckBox enableStrictHttpParserCheck;
    @FXML
    private CheckBox encodeUrlAutomaticallyCheck;
    @FXML
    private CheckBox disableCookieJarCheck;
    @FXML
    private Label statusBadge;
    @FXML
    private TextField exampleStatusCodeField;
    @FXML
    private Label timeLabel;
    @FXML
    private Label sizeLabel;
    @FXML
    private ProgressIndicator loadingProgress;
    @FXML
    private WebView responsePrettyWebView;
    @FXML
    private TextArea exampleBodyEditor;
    @FXML
    private WebView responseWebView;
    @FXML
    private TableView<KeyValuePair> responseHeadersTable;
    @FXML
    private TableColumn<KeyValuePair, String> responseHeaderKeyColumn;
    @FXML
    private TableColumn<KeyValuePair, String> responseHeaderValueColumn;
    @FXML
    private Button addExampleHeaderBtn;
    
    @FXML
    private TextArea aiResponseTextArea;
    @FXML
    private TextField loadUsersField;
    @FXML
    private TextField loadLoopsField;
    @FXML
    private TextArea loadTestResultsTextArea;
    
    @FXML
    private TextField mockPathField;
    @FXML
    private TextField mockStatusField;
    @FXML
    private TextField mockDelayField;
    @FXML
    private TextArea mockBodyField;

    private Tab thisTab;
    
    public void init(MainController mainController, ApiRequest req, Tab tab) {
        this.mainController = mainController;
        this.currentRequest = req;
        this.thisTab = tab;
        
        setupTabTitleUpdating();

        protocolComboBox.getItems().addAll("REST", "GraphQL", "WebSocket", "gRPC", "SOAP", "SSE");
        protocolComboBox.setValue("REST");

        methodComboBox.getItems().addAll("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
        methodComboBox.setValue("GET");

        authTypeComboBox.getItems().addAll("No Auth", "API Key", "Bearer Token", "JWT Bearer", "Basic Auth", "Digest Auth", "OAuth 1.0", "OAuth 2.0", "Hawk Authentication", "AWS Signature", "NTLM Authentication [Beta]", "Akamai EdgeGrid");
        authTypeComboBox.setValue("No Auth");

        
        
        
        if (paramsRowsBox == null) {
            paramsRowsBox = new javafx.scene.layout.VBox();
            if (paramsBulkEditArea != null) {
                javafx.scene.layout.VBox parent = (javafx.scene.layout.VBox) paramsBulkEditArea.getParent();
                javafx.scene.layout.VBox v1 = (javafx.scene.layout.VBox) parent.getChildren().get(0);
                for (javafx.scene.Node child : v1.getChildren()) {
                    if (child instanceof javafx.scene.control.ScrollPane) {
                        ((javafx.scene.control.ScrollPane) child).setContent(paramsRowsBox);
                        break;
                    }
                }
            }
        }
        if (headersRowsBox == null) {
            headersRowsBox = new javafx.scene.layout.VBox();
            javafx.scene.control.TabPane tp = (javafx.scene.control.TabPane) thisTab.getContent().lookup(".request-pane > TabPane");
            if (tp != null) {
                for (javafx.scene.control.Tab t : tp.getTabs()) {
                    if ("Headers".equals(t.getText())) {
                        javafx.scene.Node content = t.getContent();
                        if (content instanceof javafx.scene.layout.VBox) {
                            for (javafx.scene.Node child : ((javafx.scene.layout.VBox) content).getChildren()) {
                                if (child instanceof javafx.scene.control.ScrollPane) {
                                    ((javafx.scene.control.ScrollPane) child).setContent(headersRowsBox);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (formDataRowsBox == null) {
            formDataRowsBox = new javafx.scene.layout.VBox();
            if (bodyTypeGroup != null && bodyFormDataRadio != null) {
                javafx.scene.layout.VBox formPane = (javafx.scene.layout.VBox) thisTab.getContent().lookup("#formDataPane");
                if (formPane != null) {
                    for (javafx.scene.Node child : formPane.getChildren()) {
                        if (child instanceof javafx.scene.control.ScrollPane) {
                            ((javafx.scene.control.ScrollPane) child).setContent(formDataRowsBox);
                            break;
                        }
                    }
                }
            }
        }

        paramsEditor = new com.apiforge.studio.ui.PostmanTableEditor(paramsRowsBox, queryParamsData, null);

        headersEditor = new com.apiforge.studio.ui.PostmanTableEditor(headersRowsBox, headersData, this::updateHeaderCountBadge);
        headersData.addListener((javafx.collections.ListChangeListener.Change<? extends KeyValuePair> c) -> updateHeaderCountBadge());
        updateHeaderCountBadge();

        responseHeaderKeyColumn.setCellValueFactory(data -> data.getValue().keyProperty());
        responseHeaderValueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        responseHeadersTable.setItems(responseHeadersData);
        
        responseHeaderKeyColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        responseHeaderValueColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        
        // Cookies table setup
        if (cookieNameCol != null) {
            cookieNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
            cookieValueCol.setCellValueFactory(data -> data.getValue().valueProperty());
            cookieDomainCol.setCellValueFactory(data -> data.getValue().domainProperty());
            cookiePathCol.setCellValueFactory(data -> data.getValue().pathProperty());
            responseCookiesTable.setItems(cookiesData);
        }

        // Test results list
        if (testResultsList != null) {
            testResultsList.setItems(javafx.collections.FXCollections.observableArrayList());
        }
        
        
        
        

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

@FXML
    private void onProtocolChanged(ActionEvent event) {
        String proto = protocolComboBox.getValue();
        if ("WebSocket".equals(proto)) {
            methodComboBox.setValue("GET");
            methodComboBox.setDisable(true);
            urlTextField.setPromptText("ws://echo.websocket.org");
            bodyTab.setDisable(false); // Can be used to send websocket frame payload
        } else {
            methodComboBox.setDisable(false);
            urlTextField.setPromptText("https://api.example.com/endpoint");
        }
    }

@FXML
    private void onAuthTypeChanged(ActionEvent event) {
        String type = authTypeComboBox.getValue();
        noAuthLabel.setVisible("No Auth".equals(type));
        noAuthLabel.setManaged("No Auth".equals(type));
        basicAuthForm.setVisible("Basic Auth".equals(type));
        basicAuthForm.setManaged("Basic Auth".equals(type));
        bearerAuthForm.setVisible("Bearer Token".equals(type) || "JWT Bearer".equals(type));
        bearerAuthForm.setManaged("Bearer Token".equals(type) || "JWT Bearer".equals(type));
        apiKeyAuthForm.setVisible("API Key".equals(type));
        apiKeyAuthForm.setManaged("API Key".equals(type));
        digestAuthForm.setVisible("Digest Auth".equals(type));
        digestAuthForm.setManaged("Digest Auth".equals(type));
        oauth2Form.setVisible("OAuth 2.0".equals(type));
        oauth2Form.setManaged("OAuth 2.0".equals(type));
        awsForm.setVisible("AWS Signature".equals(type));
        awsForm.setManaged("AWS Signature".equals(type));
        boolean showAdvanced = "OAuth 1.0".equals(type) || "Hawk Authentication".equals(type) || "NTLM Authentication [Beta]".equals(type) || "Akamai EdgeGrid".equals(type);
        if (advancedAuthForm != null) {
            advancedAuthForm.setVisible(showAdvanced);
            advancedAuthForm.setManaged(showAdvanced);
            if (showAdvanced && advancedAuthHintLabel != null) {
                advancedAuthHintLabel.setText(type + " — configure this auth type using headers or a pre-request script.");
            }
        }
    }

    @FXML
    private void onBulkEditParams(ActionEvent event) {
        boolean showing = paramsBulkEditArea.isVisible();
        if (!showing) {
            StringBuilder sb = new StringBuilder();
            for (KeyValuePair kv : queryParamsData) {
                sb.append(kv.getKey()).append(":").append(kv.getValue()).append("\n");
            }
            paramsBulkEditArea.setText(sb.toString());
        }
        paramsBulkEditArea.setVisible(!showing);
        paramsBulkEditArea.setManaged(!showing);
        paramsBulkEditDoneRow.setVisible(!showing);
        paramsBulkEditDoneRow.setManaged(!showing);
        bulkEditParamsBtn.setText(showing ? "Bulk Edit" : "Close Bulk Edit");
    }

    @FXML
    private void applyBulkEditParams(ActionEvent event) {
        queryParamsData.clear();
        for (String line : paramsBulkEditArea.getText().split("\n")) {
            if (line.trim().isEmpty()) continue;
            int idx = line.indexOf(":");
            if (idx > 0) {
                queryParamsData.add(new KeyValuePair(line.substring(0, idx).trim(), line.substring(idx + 1).trim()));
            } else {
                queryParamsData.add(new KeyValuePair(line.trim(), ""));
            }
        }
        paramsBulkEditArea.setVisible(false);
        paramsBulkEditArea.setManaged(false);
        paramsBulkEditDoneRow.setVisible(false);
        paramsBulkEditDoneRow.setManaged(false);
        bulkEditParamsBtn.setText("Bulk Edit");
    }

    @FXML
    private void cancelBulkEditParams(ActionEvent event) {
        paramsBulkEditArea.setVisible(false);
        paramsBulkEditArea.setManaged(false);
        paramsBulkEditDoneRow.setVisible(false);
        paramsBulkEditDoneRow.setManaged(false);
        bulkEditParamsBtn.setText("Bulk Edit");
    }

    @FXML
    private void saveResponse(ActionEvent event) {
        if (lastResponseBody == null || lastResponseBody.isEmpty()) return;
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Save Response");
        fc.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fc.setInitialFileName("response.json");
        java.io.File file = fc.showSaveDialog(saveResponseBtn.getScene().getWindow());
        if (file != null) {
            try {
                java.nio.file.Files.writeString(file.toPath(), lastResponseBody);
            } catch (Exception e) {
                mainController.showError("Failed to save response: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  POSTMAN-STYLE HEADERS TABLE
    // ─────────────────────────────────────────────────────────

    private static final Set<String> STANDARD_REQ_HEADERS = Set.of(
        "content-type", "accept", "authorization", "user-agent",
        "cache-control", "accept-encoding", "accept-language",
        "connection", "host", "origin", "referer", "cookie",
        "x-api-key", "x-request-id", "x-correlation-id"
    );

    private static final Set<String> STANDARD_RESP_HEADERS = Set.of(
        "content-type", "content-length", "content-encoding",
        "transfer-encoding", "cache-control", "etag", "date",
        "server", "access-control-allow-origin", "vary",
        "set-cookie", "location", "strict-transport-security",
        "connection", "keep-alive", "x-request-id"
    );

    private boolean isStandardRequestHeader(String key) {
        return key != null && STANDARD_REQ_HEADERS.contains(key.toLowerCase());
    }

private boolean isStandardResponseHeader(String key) {
        return key != null && STANDARD_RESP_HEADERS.contains(key.toLowerCase());
    }



private void updateHeaderCountBadge() {
        if (headerCountLabel == null) return;
        int count = headersData.size();
        headerCountLabel.setText(count == 0 ? "No headers" : count + " header" + (count == 1 ? "" : "s"));
        // Update the tab text too
        if (headersTab != null) {
            headersTab.setText(count == 0 ? "Headers" : "Headers (" + count + ")");
        }
    }

@FXML
    private void clearAllHeaders(ActionEvent e) {
        headersData.clear();
    }

// ── Preset Header Quick-Adds ──
    @FXML
    private void addPresetJson(ActionEvent e)       { headersData.add(new KeyValuePair("Content-Type", "application/json")); }

@FXML
    private void addPresetMultipart(ActionEvent e)  { headersData.add(new KeyValuePair("Content-Type", "multipart/form-data")); }

@FXML
    private void addPresetAccept(ActionEvent e)     { headersData.add(new KeyValuePair("Accept", "application/json")); }

@FXML
    private void addPresetBearer(ActionEvent e)     { headersData.add(new KeyValuePair("Authorization", "Bearer {token}")); }

@FXML
    private void addPresetNoCache(ActionEvent e)    { headersData.add(new KeyValuePair("Cache-Control", "no-cache")); }

@FXML
    private void addPresetUserAgent(ActionEvent e)  { headersData.add(new KeyValuePair("User-Agent", "APIForge/1.0")); }

@FXML
    private void sendRequest(ActionEvent event) {
        // Collect inputs into currentRequest
        updateRequestFromUI();

        String url = currentRequest.getUrl().trim();
        if (url.isEmpty()) {
            mainController.showError("URL cannot be empty.");
            return;
        }

        loadingProgress.setVisible(true);
        sendButton.setDisable(true);
        
        statusBadge.setText("Sending...");
        statusBadge.getStyleClass().removeAll("chip-status-error");
        statusBadge.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #9e9e9e; -fx-border-color: #e0e0e0;");

        String proto = currentRequest.getProtocol();

        if ("WebSocket".equals(proto)) {
            handleWebSocketConnection();
        } else if ("SSE".equals(proto)) {
            handleSseConnection();
        } else {
            // REST, GraphQL, SOAP, gRPC (Mock/Shell execution fallback)
            executeStandardRequest();
        }
    }

private void executeStandardRequest() {
        // Resolve Environment
        Map<String, String> currentEnv = new java.util.HashMap<>();
        if (mainController.getEnvironmentComboBox() != null && mainController.getEnvironmentComboBox().getValue() != null) {
            currentEnv = mainController.getEnvironmentService().getEnvironmentVariablesAsMap(Integer.parseInt(mainController.getEnvironmentComboBox().getValue().getId()));
        }
        
        final Map<String, String> finalEnv = currentEnv;

        new Thread(() -> {
            ApiResponse response = mainController.getHttpClientService().execute(currentRequest, finalEnv);
            
            Platform.runLater(() -> {
                loadingProgress.setVisible(false);
                sendButton.setDisable(false);

                if (response.isSuccess()) {
                    statusBadge.setText(response.getStatusCode() + " OK");
                    statusBadge.getStyleClass().removeAll("chip-status-error");
                    if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                        // Green — success
                        statusBadge.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-border-color: #c8e6c9;");
                    } else {
                        // Red — error
                        statusBadge.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-border-color: #ffcdd2;");
                    }

                    timeLabel.setText(response.getResponseTimeMs() + " ms");
                    
                    double sizeKb = response.getResponseSize() / 1024.0;
                    sizeLabel.setText(String.format("%.2f KB", sizeKb));

                    // Pretty text display
                    String formattedJson = formatJson(response.getBody());
                    boolean isLightMode = "Light Mode".equals(mainController.getTheme());
                    responsePrettyWebView.getEngine().loadContent(generateHighlightedJsonHtml(formattedJson, isLightMode));

                    // Webview HTML preview
                    this.lastResponseBody = response.getBody();
                    this.lastResponseStatus = response.getStatusCode();
                    responseWebView.getEngine().loadContent(generateRichVisualizerHtml(lastResponseBody, isLightMode));

                    // Fill headers table
                    responseHeadersData.clear();
                    response.getHeaders().forEach((k, v) -> responseHeadersData.add(new KeyValuePair(k, v)));

                    // Parse cookies
                    cookiesData.clear();
                    response.getHeaders().forEach((k, v) -> {
                        if ("set-cookie".equalsIgnoreCase(k)) {
                            String[] parts = v.split(";");
                            String name = "", value = "", domain = "", path = "/";
                            if (parts.length > 0) {
                                int eq = parts[0].indexOf("=");
                                if (eq > 0) {
                                    name = parts[0].substring(0, eq).trim();
                                    value = parts[0].substring(eq + 1).trim();
                                }
                            }
                            for (int i = 1; i < parts.length; i++) {
                                String p = parts[i].trim();
                                if (p.toLowerCase().startsWith("domain=")) domain = p.substring(7);
                                if (p.toLowerCase().startsWith("path=")) path = p.substring(5);
                            }
                            cookiesData.add(new com.apiforge.studio.model.CookieEntry(name, value, domain, path));
                        }
                    });

                    // Show save response button
                    if (saveResponseBtn != null) {
                        saveResponseBtn.setVisible(true);
                        saveResponseBtn.setManaged(true);
                    }

                    // Add to history list
                    HistoryEntry entry = new HistoryEntry(currentRequest, response.getStatusCode(), response.getResponseTimeMs());
                    mainController.addHistoryEntry(entry); // Prepends most recent
                    
                    
                    mainController.postHistoryToBackend(currentRequest.getMethod(), currentRequest.getUrl(), response.getStatusCode(), response.getResponseTimeMs());

                } else {
                    statusBadge.setText("Error");
                    statusBadge.getStyleClass().removeAll("badge-inactive", "badge-success", "badge-error");
                    statusBadge.getStyleClass().add("badge-error");
                    timeLabel.setText(response.getResponseTimeMs() + " ms");
                    boolean isLightMode = "Light Mode".equals(mainController.getTheme());
                    responsePrettyWebView.getEngine().loadContent(generateHighlightedJsonHtml("Request Failed:\n" + response.getErrorMessage(), isLightMode));
                    responseWebView.getEngine().loadContent("<h3>Request Failed</h3><pre>" + response.getErrorMessage() + "</pre>");
                    responseHeadersData.clear();
                }
            });
        }).start();
    }

private void handleWebSocketConnection() {
        if (mainController.getWebSocketService().isConnected()) {
            mainController.getWebSocketService().sendMessage(bodyTextArea.getText());
            streamBuffer.append("\n[Client Send]: ").append(bodyTextArea.getText());
            updateStreamView();
            loadingProgress.setVisible(false);
            sendButton.setDisable(false);
            return;
        }

        streamBuffer.setLength(0);
        streamBuffer.append("[System]: Connecting to websocket...");
        updateStreamView();
        
        mainController.getWebSocketService().connect(
            currentRequest.getUrl(),
            msg -> Platform.runLater(() -> { streamBuffer.append("\n[Message]: ").append(msg); updateStreamView(); }),
            status -> Platform.runLater(() -> {
                streamBuffer.append("\n[Status]: ").append(status);
                updateStreamView();
                statusBadge.setText("Connected");
                statusBadge.getStyleClass().add("chip-status");
            }),
            err -> Platform.runLater(() -> {
                streamBuffer.append("\n[Error]: ").append(err.getMessage());
                updateStreamView();
                statusBadge.setText("Closed");
                statusBadge.getStyleClass().add("chip-status-error");
                sendButton.setDisable(false);
                loadingProgress.setVisible(false);
            })
        ).thenRun(() -> Platform.runLater(() -> {
            sendButton.setText("Send Frame");
            sendButton.setDisable(false);
            loadingProgress.setVisible(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                streamBuffer.append("\n[Connection Error]: ").append(ex.getMessage());
                updateStreamView();
                sendButton.setDisable(false);
                loadingProgress.setVisible(false);
            });
            return null;
        });
    }

private void handleSseConnection() {
        streamBuffer.setLength(0);
        streamBuffer.append("[System]: Connecting to SSE Stream...\n");
        updateStreamView();
        
        mainController.getHttpClientService().executeSse(
            currentRequest,
            line -> Platform.runLater(() -> { streamBuffer.append(line).append("\n"); updateStreamView(); }),
            err -> Platform.runLater(() -> {
                streamBuffer.append("\n[Stream Closed/Error]: ").append(err.getMessage());
                updateStreamView();
                sendButton.setDisable(false);
                loadingProgress.setVisible(false);
            })
        );
        
        statusBadge.setText("Listening");
        statusBadge.getStyleClass().add("chip-status");
        sendButton.setDisable(false);
        loadingProgress.setVisible(false);
    }

private void updateStreamView() {
        boolean isLightMode = "Light Mode".equals(mainController.getTheme());
        String bg = isLightMode ? "#ffffff" : "#1e1e1e";
        String fg = isLightMode ? "#0f172a" : "#e3e3e6";
        String html = "<html><body style='background-color:" + bg + "; color:" + fg + "; font-family:monospace; white-space:pre-wrap; font-size:13px;'>" 
                      + streamBuffer.toString().replace("<", "&lt;").replace(">", "&gt;") 
                      + "</body></html>";
        responsePrettyWebView.getEngine().loadContent(html);
    }

public void updateRequestFromUI() {
        currentRequest.setUrl(urlTextField.getText());
        currentRequest.setMethod(methodComboBox.getValue());
        currentRequest.setProtocol(protocolComboBox.getValue());
        currentRequest.setBody(bodyTextArea.getText());
        currentRequest.setPreRequestScript(preRequestScriptArea.getText());
        currentRequest.setTestScript(testScriptArea.getText());

        Toggle selectedBodyType = bodyTypeGroup.getSelectedToggle();
        if (selectedBodyType == bodyNoneRadio) currentRequest.setBodyType("none");
        else if (selectedBodyType == bodyFormDataRadio) currentRequest.setBodyType("form-data");
        else if (selectedBodyType == bodyUrlEncodedRadio) currentRequest.setBodyType("urlencoded");
        else if (selectedBodyType == bodyBinaryRadio) currentRequest.setBodyType("binary");
        else currentRequest.setBodyType("raw");

        Map<String, String> fData = new HashMap<>();
        for (KeyValuePair kv : formDataList) {
            fData.put(kv.getKey(), kv.getValue());
        }
        currentRequest.setFormData(fData);

        Map<String, Boolean> reqSettings = new HashMap<>();
        reqSettings.put("followRedirects", followRedirectsCheck.isSelected());
        reqSettings.put("verifySsl", verifySslCheck.isSelected());
        reqSettings.put("followOriginalHttpMethod", followOriginalHttpMethodCheck.isSelected());
        reqSettings.put("followAuthorizationHeader", followAuthorizationHeaderCheck.isSelected());
        reqSettings.put("removeRefererHeaderOnRedirect", removeRefererHeaderOnRedirectCheck.isSelected());
        reqSettings.put("enableStrictHttpParser", enableStrictHttpParserCheck.isSelected());
        reqSettings.put("encodeUrlAutomatically", encodeUrlAutomaticallyCheck.isSelected());
        reqSettings.put("disableCookieJar", disableCookieJarCheck.isSelected());
        currentRequest.setRequestSettings(reqSettings);

        // Params
        Map<String, String> params = new HashMap<>();
        for (KeyValuePair kv : queryParamsData) {
            params.put(kv.getKey(), kv.getValue());
        }
        currentRequest.setQueryParams(params);

        // Headers
        Map<String, String> headers = new HashMap<>();
        for (KeyValuePair kv : headersData) {
            headers.put(kv.getKey(), kv.getValue());
        }
        currentRequest.setHeaders(headers);

        // Auth
        currentRequest.setAuthType(authTypeComboBox.getValue());
        Map<String, String> authConfig = new HashMap<>();
        authConfig.put("username", authUsernameField.getText());
        authConfig.put("password", authPasswordField.getText());
        authConfig.put("token", authTokenField.getText());
        authConfig.put("key", apiKeyNameField.getText());
        authConfig.put("value", apiKeyValueField.getText());
        currentRequest.setAuthConfig(authConfig);
    }

    private void setupTabTitleUpdating() {
        if (thisTab != null) {
            updateTabTitle();
            methodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTabTitle());
            urlTextField.textProperty().addListener((obs, oldVal, newVal) -> updateTabTitle());
        }
    }

    private void updateTabTitle() {
        if (thisTab == null) return;
        
        String method = methodComboBox.getValue();
        if (method == null) method = "GET";
        
        String url = urlTextField.getText();
        String title = (url == null || url.trim().isEmpty()) ? "Untitled Request" : url.trim();
        
        // Truncate long URLs for the tab
        if (title.length() > 25) {
            title = title.substring(0, 22) + "...";
        }

        HBox graphic = new HBox(6);
        graphic.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label methodLbl = new Label(method);
        methodLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        switch (method) {
            case "GET": methodLbl.setTextFill(javafx.scene.paint.Color.web("#0cbb52")); break;
            case "POST": methodLbl.setTextFill(javafx.scene.paint.Color.web("#ffb400")); break;
            case "PUT": methodLbl.setTextFill(javafx.scene.paint.Color.web("#097bed")); break;
            case "DELETE": methodLbl.setTextFill(javafx.scene.paint.Color.web("#eb2013")); break;
            default: methodLbl.setTextFill(javafx.scene.paint.Color.web("#808080")); break;
        }
        
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        
        graphic.getChildren().addAll(methodLbl, titleLbl);
        thisTab.setGraphic(graphic);
        thisTab.setText("");
    }

private void loadRequestIntoUI(ApiRequest req) {
        this.currentRequest = req;
        this.currentExample = null;
        
        if (saveExampleButton != null) {
            saveExampleButton.setVisible(false);
            saveExampleButton.setManaged(false);
        }
        sendButton.setVisible(true);
        sendButton.setManaged(true);
        
        if (exampleStatusCodeField != null) {
            exampleStatusCodeField.setVisible(false);
            exampleStatusCodeField.setManaged(false);
        }
        statusBadge.setVisible(true);
        statusBadge.setManaged(true);
        
        if (exampleBodyEditor != null) {
            exampleBodyEditor.setVisible(false);
            exampleBodyEditor.setManaged(false);
        }
        responsePrettyWebView.setVisible(true);
        responsePrettyWebView.setManaged(true);
        
        if (addExampleHeaderBtn != null) {
            addExampleHeaderBtn.setVisible(false);
            addExampleHeaderBtn.setManaged(false);
        }
        responseHeadersTable.setEditable(false);
        
        urlTextField.setText(req.getUrl());
        methodComboBox.setValue(req.getMethod());
        protocolComboBox.setValue(req.getProtocol());
        bodyTextArea.setText(req.getBody());
        preRequestScriptArea.setText(req.getPreRequestScript() != null ? req.getPreRequestScript() : "");
        testScriptArea.setText(req.getTestScript() != null ? req.getTestScript() : "");

        String bType = req.getBodyType();
        if ("none".equals(bType)) bodyNoneRadio.setSelected(true);
        else if ("form-data".equals(bType)) bodyFormDataRadio.setSelected(true);
        else if ("urlencoded".equals(bType)) bodyUrlEncodedRadio.setSelected(true);
        else if ("binary".equals(bType)) bodyBinaryRadio.setSelected(true);
        else bodyRawRadio.setSelected(true);

        formDataList.clear();
        if (req.getFormData() != null) {
            req.getFormData().forEach((k, v) -> formDataList.add(new KeyValuePair(k, v)));
        }

        if (req.getRequestSettings() != null) {
            followRedirectsCheck.setSelected(req.getRequestSettings().getOrDefault("followRedirects", true));
            verifySslCheck.setSelected(req.getRequestSettings().getOrDefault("verifySsl", true));
            followOriginalHttpMethodCheck.setSelected(req.getRequestSettings().getOrDefault("followOriginalHttpMethod", false));
            followAuthorizationHeaderCheck.setSelected(req.getRequestSettings().getOrDefault("followAuthorizationHeader", false));
            removeRefererHeaderOnRedirectCheck.setSelected(req.getRequestSettings().getOrDefault("removeRefererHeaderOnRedirect", true));
            enableStrictHttpParserCheck.setSelected(req.getRequestSettings().getOrDefault("enableStrictHttpParser", false));
            encodeUrlAutomaticallyCheck.setSelected(req.getRequestSettings().getOrDefault("encodeUrlAutomatically", true));
            disableCookieJarCheck.setSelected(req.getRequestSettings().getOrDefault("disableCookieJar", false));
        }

        // Params
        queryParamsData.clear();
        if (req.getQueryParams() != null) {
            req.getQueryParams().forEach((k, v) -> queryParamsData.add(new KeyValuePair(k, v)));
        }

        // Headers
        headersData.clear();
        if (req.getHeaders() != null) {
            req.getHeaders().forEach((k, v) -> headersData.add(new KeyValuePair(k, v)));
        }

        // Auth
        String authType = req.getAuthType();
        if ("NONE".equals(authType) || authType == null) authType = "No Auth";
        else if ("BASIC".equals(authType)) authType = "Basic Auth";
        else if ("BEARER".equals(authType)) authType = "Bearer Token";
        else if ("API_KEY".equals(authType)) authType = "API Key";
        authTypeComboBox.setValue(authType);
        if (req.getAuthConfig() != null) {
            authUsernameField.setText(req.getAuthConfig().getOrDefault("username", ""));
            authPasswordField.setText(req.getAuthConfig().getOrDefault("password", ""));
            authTokenField.setText(req.getAuthConfig().getOrDefault("token", ""));
            apiKeyNameField.setText(req.getAuthConfig().getOrDefault("key", ""));
            apiKeyValueField.setText(req.getAuthConfig().getOrDefault("value", ""));
        }
        onAuthTypeChanged(null);
    }

public void loadExampleIntoUI(com.apiforge.studio.model.RequestExample ex) {
        this.currentExample = ex;
        lastResponseBody = ex.getResponseBody();
        lastResponseStatus = ex.getStatusCode();
        
        saveExampleButton.setVisible(true);
        saveExampleButton.setManaged(true);
        sendButton.setVisible(false);
        sendButton.setManaged(false);
        
        statusBadge.setVisible(false);
        statusBadge.setManaged(false);
        exampleStatusCodeField.setVisible(true);
        exampleStatusCodeField.setManaged(true);
        exampleStatusCodeField.setText(String.valueOf(ex.getStatusCode()));
        
        responsePrettyWebView.setVisible(false);
        responsePrettyWebView.setManaged(false);
        exampleBodyEditor.setVisible(true);
        exampleBodyEditor.setManaged(true);
        exampleBodyEditor.setText(ex.getResponseBody());
        
        responseHeadersData.clear();
        if (ex.getResponseHeaders() != null) {
            ex.getResponseHeaders().forEach((k, v) -> responseHeadersData.add(new KeyValuePair(k, v)));
        }
        addExampleHeaderBtn.setVisible(true);
        addExampleHeaderBtn.setManaged(true);
        responseHeadersTable.setEditable(true);

        timeLabel.setText("0 ms");
        double sizeKb = (lastResponseBody != null ? lastResponseBody.getBytes().length : 0) / 1024.0;
        sizeLabel.setText(String.format("%.2f KB", sizeKb));

        boolean isLightMode = "Light Mode".equals(mainController.getTheme());
        responseWebView.getEngine().loadContent(generateRichVisualizerHtml(lastResponseBody, isLightMode));
    }

@FXML
    private void saveExample(ActionEvent event) {
        if (currentExample != null) {
            String body = exampleBodyEditor.getText();
            int status = currentExample.getStatusCode();
            try {
                status = Integer.parseInt(exampleStatusCodeField.getText());
            } catch (NumberFormatException e) {
                mainController.showError("Invalid status code");
                return;
            }
            
            java.util.Map<String, String> hdrs = new HashMap<>();
            for (KeyValuePair kv : responseHeadersData) {
                if (kv.getKey() != null && !kv.getKey().trim().isEmpty()) {
                    hdrs.put(kv.getKey(), kv.getValue());
                }
            }
            String hdrsJson = "{}";
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                hdrsJson = mapper.writeValueAsString(hdrs);
            } catch (Exception e) {}
            
            mainController.getDatabaseService().updateExample(currentExample.getId(), currentExample.getName(), status, body, hdrsJson);
            currentExample.setStatusCode(status);
            currentExample.setResponseBody(body);
            currentExample.setResponseHeaders(hdrs);
            mainController.showInfo("Saved", "Example updated successfully!");
        }
    }

@FXML
    private void saveToFavorites(ActionEvent event) {
        mainController.promptSaveRequestToCollection(null, null);
    }

@FXML
    private void addExampleHeader(ActionEvent event) {
        responseHeadersData.add(new KeyValuePair("New-Header", "value"));
    }

@FXML
    private void generateAiTests(ActionEvent event) {
        String method = methodComboBox.getValue();
        String url = urlTextField.getText();
        
        String testOutput = String.format(
            "// ==========================================\n" +
            "// AI GENERATED TEST SUITE FOR %s: %s\n" +
            "// ==========================================\n\n" +
            "// 1. Positive Verification Case\n" +
            "describe('Verify standard response', () => {\n" +
            "    expect(response.statusCode).toBe(200);\n" +
            "    expect(response.headers['Content-Type']).toContain('application/json');\n" +
            "    expect(response.body).toBeNonNull();\n" +
            "});\n\n" +
            "// 2. Performance SLA Checklist\n" +
            "describe('Verify latency compliance', () => {\n" +
            "    expect(response.responseTimeMs).toBeLessThan(500); // 500ms limit\n" +
            "});\n\n" +
            "// 3. Security Analysis\n" +
            "describe('Verify SSL & Encryption headers', () => {\n" +
            "    expect(response.headers['Strict-Transport-Security']).toBeDefined();\n" +
            "});\n", method, url
        );
        aiResponseTextArea.setText(testOutput);
    }

@FXML
    private void generateAiMockPayload(ActionEvent event) {
        String url = urlTextField.getText();
        String mockPayload = 
            "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"title\": \"APIResponseMock\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"status\": { \"type\": \"string\", \"example\": \"success\" },\n" +
            "    \"data\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"object\",\n" +
            "        \"properties\": {\n" +
            "          \"id\": { \"type\": \"integer\", \"example\": 101 },\n" +
            "          \"name\": { \"type\": \"string\", \"example\": \"Mock Resource\" }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        aiResponseTextArea.setText(mockPayload);
    }

@FXML
    private void generateAiExplanation(ActionEvent event) {
        String method = methodComboBox.getValue();
        String url = urlTextField.getText();
        
        String explanation = String.format(
            "### Endpoint Explanation\n" +
            "- **Method**: `%s`\n" +
            "- **Target URL**: `%s`\n\n" +
            "### Expected Behavior\n" +
            "This endpoint serves REST requests. In standard systems, a `%s` call on this URL requires valid " +
            "authorization credentials. Ensure that you have specified a `Bearer Token` or `Basic Authentication` " +
            "credentials under the 'Authorization' tab if the service returns 401 Unauthorized status.",
            method, url, method
        );
        aiResponseTextArea.setText(explanation);
    }

@FXML
    private void runLoadSimulation(ActionEvent event) {
        String url = urlTextField.getText();
        String method = methodComboBox.getValue();
        String body = bodyTextArea.getText();

        if (url == null || url.trim().isEmpty()) {
            mainController.showError("Please specify a valid Target URL before running load testing.");
            return;
        }

        int threads = 10;
        int runs = 50;
        try {
            threads = Integer.parseInt(loadUsersField.getText());
            runs = Integer.parseInt(loadLoopsField.getText());
        } catch (NumberFormatException e) {
            loadTestResultsTextArea.setText("Invalid input format for threads/runs. Defaulting to 10 threads, 50 loops.");
        }

        final int finalThreads = threads;
        final int finalRuns = runs;

        loadTestResultsTextArea.setText("[System]: Starting load simulation with " + finalThreads + " concurrent threads...\n");
        
        mainController.getLoadTestingService().runLoadTest(url, method, body, finalThreads, finalRuns, result -> {
            Platform.runLater(() -> {
                String report = String.format(
                    "==========================================\n" +
                    "           LOAD TEST METRICS REPORT       \n" +
                    "==========================================\n" +
                    "Status: Completed Successfully\n" +
                    "Target URL: %s\n" +
                    "Concurrent Users/Threads: %d\n" +
                    "Total Requests Dispatched: %d\n" +
                    "Success Code 2xx Count: %d\n" +
                    "Error/Failures Count: %d\n" +
                    "Throughput (TPS): %.2f req/sec\n\n" +
                    "--- LATENCY METRICS ---\n" +
                    "Minimum Latency: %d ms\n" +
                    "Maximum Latency: %d ms\n" +
                    "Average Latency: %.2f ms\n" +
                    "Median (P50) Latency: %d ms\n" +
                    "90th Percentile (P90): %d ms\n" +
                    "99th Percentile (P99): %d ms\n" +
                    "==========================================\n",
                    url, finalThreads, result.totalRequests, 
                    result.successCount, result.failureCount, result.tps,
                    result.minLatencyMs, result.maxLatencyMs, result.avgLatencyMs,
                    result.p50Ms, result.p90Ms, result.p99Ms
                );
                loadTestResultsTextArea.setText(report);
                
                // POST to backend API
                new Thread(() -> {
                    try {
                        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                        String requestBody = String.format(
                            "{\"workspaceId\":\"%s\",\"url\":\"%s\",\"method\":\"%s\",\"concurrentUsers\":%d,\"totalRequests\":%d,\"successCount\":%d,\"failureCount\":%d,\"minLatencyMs\":%d,\"maxLatencyMs\":%d,\"avgLatencyMs\":%.2f,\"p50Ms\":%d,\"p90Ms\":%d,\"p99Ms\":%d,\"tps\":%.2f}",
                            "00000000-0000-0000-0000-000000000001", // Default workspace ID
                            url, method, finalThreads, result.totalRequests,
                            result.successCount, result.failureCount,
                            result.minLatencyMs, result.maxLatencyMs, result.avgLatencyMs,
                            result.p50Ms, result.p90Ms, result.p99Ms, result.tps
                        );
                        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://localhost:8082/api/v1/load-test-results"))
                                .header("Content-Type", "application/json")
                                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                                .build();
                        client.send(req, java.net.http.HttpResponse.BodyHandlers.discarding());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        });
    }

@FXML
    private void registerCloudMockRoute(ActionEvent event) {
        String path = mockPathField.getText();
        String body = mockBodyField.getText();
        
        if (path == null || path.trim().isEmpty() || body == null) {
            mainController.showError("Path and response body cannot be empty.");
            return;
        }

        int status = 200;
        long delay = 0;
        try {
            status = Integer.parseInt(mockStatusField.getText().trim());
            delay = Long.parseLong(mockDelayField.getText().trim());
        } catch (NumberFormatException e) {
            // Default fallbacks
        }

        final int finalStatus = status;
        final long finalDelay = delay;

        new Thread(() -> {
            try {
                String backendUrl = mainController.getBackendUrl();
                // Escape simple JSON chars in mock response body
                String escapedBody = body.replace("\"", "\\\"").replace("\n", "\\n");
                String json = String.format("{\"method\":\"GET\",\"path\":\"%s\",\"responseBody\":\"%s\",\"statusCode\":%d,\"delayMs\":%d}",
                        path, escapedBody, finalStatus, finalDelay);

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(backendUrl + "/api/mocks"))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();

                java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Mock Created");
                        alert.setHeaderText(null);
                        alert.setContentText("Mock route created successfully on the server! Active URL path: " + path);
                        alert.showAndWait();
                        mockPathField.clear();
                        mockBodyField.clear();
                    } else {
                        mainController.showError("Failed to register mock route: " + resp.body());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> mainController.showError("Network error creating mock endpoint: " + e.getMessage()));
            }
        }).start();
    }

private String formatJson(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return "";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object jsonObject = mapper.readValue(rawJson, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Exception e) {
            return rawJson; // Fallback to raw string if it's not valid JSON
        }
    }

private String generateHighlightedJsonHtml(String json, boolean isLightMode) {
        if (json == null) json = "";
        String escapedJson = json.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String themeCss = isLightMode ? "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" 
                                      : "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css";
        String bg = isLightMode ? "#fafafa" : "#1e1e1e";
        
        return "<!DOCTYPE html><html><head>" +
               "<link href=\"" + themeCss + "\" rel=\"stylesheet\" />" +
               "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js\"></script>" +
               "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-json.min.js\"></script>" +
               "<style>body { background-color: " + bg + "; margin: 0; padding: 10px; font-size: 13px; } pre { margin: 0 !important; border: none !important; background: transparent !important; }</style>" +
               "</head><body>" +
               "<pre><code class=\"language-json\">" + escapedJson + "</code></pre>" +
               "</body></html>";
    }

private String generateRichVisualizerHtml(String jsonBody, boolean isLightMode) {
        if (jsonBody == null || jsonBody.trim().isEmpty()) {
            return "<html><body><p style='color:gray;'>No content</p></body></html>";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonBody);

            com.fasterxml.jackson.databind.JsonNode arrayNode = null;
            if (rootNode.isArray()) {
                arrayNode = rootNode;
            } else if (rootNode.isObject()) {
                java.util.Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = rootNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> field = fields.next();
                    if (field.getValue().isArray()) {
                        arrayNode = field.getValue();
                        break;
                    }
                }
            }

            if (arrayNode == null || arrayNode.size() == 0) {
                return "<html><body style='background-color:" + (isLightMode ? "#ffffff" : "#1a1a1e") + "; color:" + (isLightMode ? "#0f172a" : "#e3e3e6") + "; font-family:monospace; padding:15px;'>"
                        + "<h3>JSON Object Details</h3><pre>" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode) + "</pre></body></html>";
            }

            List<String> headers = new java.util.ArrayList<>();
            com.fasterxml.jackson.databind.JsonNode firstElement = arrayNode.get(0);
            if (firstElement.isObject()) {
                java.util.Iterator<String> fieldNames = firstElement.fieldNames();
                while (fieldNames.hasNext()) {
                    headers.add(fieldNames.next());
                }
            } else {
                headers.add("value");
            }

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            html.append("<style>");
            if (isLightMode) {
                html.append("body { background-color: #ffffff; color: #0f172a; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 15px; font-size: 13px; }");
                html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); border-radius: 6px; overflow: hidden; }");
                html.append("th { background-color: #f1f5f9; color: #475569; text-align: left; padding: 10px; font-weight: 600; border-bottom: 1px solid #e2e8f0; }");
                html.append("td { padding: 10px; border-bottom: 1px solid #f1f5f9; color: #334155; }");
                html.append("tr:nth-child(even) { background-color: #f8fafc; }");
                html.append("tr:hover { background-color: #f1f5f9; }");
                html.append(".search-box { width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; margin-bottom: 12px; box-sizing: border-box; }");
            } else {
                html.append("body { background-color: #1a1a1e; color: #e3e3e6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 15px; font-size: 13px; }");
                html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.3); border-radius: 6px; overflow: hidden; }");
                html.append("th { background-color: #27272a; color: #a1a1aa; text-align: left; padding: 10px; font-weight: 600; border-bottom: 1px solid #3f3f46; }");
                html.append("td { padding: 10px; border-bottom: 1px solid #27272a; color: #d4d4d8; }");
                html.append("tr:nth-child(even) { background-color: #1e1e24; }");
                html.append("tr:hover { background-color: #2e303f; }");
                html.append(".search-box { width: 100%; padding: 8px 12px; background-color: #121214; border: 1px solid #3f3f46; color: white; border-radius: 6px; outline: none; margin-bottom: 12px; box-sizing: border-box; }");
            }
            html.append(".img-thumb { height: 35px; width: 35px; border-radius: 50%; object-fit: cover; border: 1px solid #cbd5e1; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }");
            html.append("</style></head><body>");

            html.append("<input type='text' id='search' class='search-box' placeholder='Filter results in real-time...'>");

            html.append("<table id='visualizerTable'><thead><tr>");
            for (String h : headers) {
                html.append("<th>").append(h).append("</th>");
            }
            html.append("</tr></thead><tbody>");

            for (int i = 0; i < arrayNode.size(); i++) {
                com.fasterxml.jackson.databind.JsonNode item = arrayNode.get(i);
                html.append("<tr>");
                for (String h : headers) {
                    com.fasterxml.jackson.databind.JsonNode valNode = item.get(h);
                    String val = (valNode == null) ? "" : valNode.asText();
                    if (val.startsWith("http") && (val.toLowerCase().contains("image") || val.toLowerCase().contains("icon") || val.toLowerCase().endsWith(".png") || val.toLowerCase().endsWith(".jpg") || val.toLowerCase().endsWith(".jpeg") || val.toLowerCase().endsWith(".webp"))) {
                        html.append("<td><img src='").append(val).append("' class='img-thumb' onerror='this.style.display=\"none\"' /></td>");
                    } else {
                        html.append("<td>").append(val).append("</td>");
                    }
                }
                html.append("</tr>");
            }
            html.append("</tbody></table>");

            html.append("<script>");
            html.append("document.getElementById('search').addEventListener('keyup', function() {");
            html.append("  var filter = this.value.toLowerCase();");
            html.append("  var rows = document.querySelectorAll('#visualizerTable tbody tr');");
            html.append("  rows.forEach(function(row) {");
            html.append("    var show = false;");
            html.append("    var tds = row.getElementsByTagName('td');");
            html.append("    for (var i = 0; i < tds.length; i++) {");
            html.append("      if (tds[i].textContent.toLowerCase().indexOf(filter) > -1) { show = true; break; }");
            html.append("    }");
            html.append("    row.style.display = show ? '' : 'none';");
            html.append("  });");
            html.append("});");
            html.append("</script>");

            html.append("</body></html>");
            return html.toString();
        } catch (Exception e) {
            return "<html><body><h3>Visualizer Parsing Error</h3><pre>" + e.getMessage() + "</pre></body></html>";
        }
    }



}
