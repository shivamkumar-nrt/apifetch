package com.apiforge.studio.service;

import com.apiforge.studio.model.ApiRequest;
import com.apiforge.studio.model.Collection;
import com.apiforge.studio.model.Folder;
import com.apiforge.studio.model.Workspace;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String DB_NAME = "apiforge.db";
    private Connection connection;

    public DatabaseService() {
        try {
            String userHome = System.getProperty("user.home");
            File appDir = new File(userHome, ".apiforge");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            String url = "jdbc:sqlite:" + new File(appDir, DB_NAME).getAbsolutePath();
            this.connection = DriverManager.getConnection(url);
            createTables();
            migrateSchema();
            seedDefaultWorkspace();
        } catch (Exception e) {
            System.err.println("Failed to initialize SQLite local cache database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // History table
            stmt.execute("CREATE TABLE IF NOT EXISTS offline_history (" +
                    "id TEXT PRIMARY KEY, " +
                    "method TEXT, " +
                    "url TEXT, " +
                    "status_code INTEGER, " +
                    "latency INTEGER, " +
                    "timestamp INTEGER" +
                    ");");

            // Workspaces table
            stmt.execute("CREATE TABLE IF NOT EXISTS workspaces (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT, " +
                    "type TEXT NOT NULL" +
                    ");");

            // Collections table (includes workspace_id)
            stmt.execute("CREATE TABLE IF NOT EXISTS collections (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "workspace_id INTEGER DEFAULT 1, " +
                    "name TEXT NOT NULL, " +
                    "FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE" +
                    ");");

            // Folders table
            stmt.execute("CREATE TABLE IF NOT EXISTS folders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "collection_id INTEGER, " +
                    "name TEXT NOT NULL, " +
                    "FOREIGN KEY(collection_id) REFERENCES collections(id) ON DELETE CASCADE" +
                    ");");

            // Saved Requests table (includes workspace_id)
            stmt.execute("CREATE TABLE IF NOT EXISTS saved_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "workspace_id INTEGER DEFAULT 1, " +
                    "collection_id INTEGER, " +
                    "folder_id INTEGER, " +
                    "name TEXT NOT NULL, " +
                    "protocol TEXT, " +
                    "method TEXT, " +
                    "url TEXT, " +
                    "auth_type TEXT, " +
                    "headers TEXT, " +
                    "body TEXT, " +
                    "auth_username TEXT, " +
                    "auth_password TEXT, " +
                    "auth_token TEXT, " +
                    "api_key_name TEXT, " +
                    "api_key_value TEXT, " +
                    "pre_request_script TEXT, " +
                    "test_script TEXT, " +
                    "body_type TEXT, " +
                    "form_data TEXT, " +
                    "request_settings TEXT, " +
                    "FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(collection_id) REFERENCES collections(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(folder_id) REFERENCES folders(id) ON DELETE CASCADE" +
                    ");");

            // Environments table
            stmt.execute("CREATE TABLE IF NOT EXISTS environments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "workspace_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "is_default BOOLEAN DEFAULT 0, " +
                    "FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE" +
                    ");");

            // Environment variables table
            stmt.execute("CREATE TABLE IF NOT EXISTS env_variables (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "environment_id INTEGER NOT NULL, " +
                    "key TEXT NOT NULL, " +
                    "value TEXT, " +
                    "is_secret BOOLEAN DEFAULT 0, " +
                    "FOREIGN KEY(environment_id) REFERENCES environments(id) ON DELETE CASCADE" +
                    ");");

            // Request Examples table
            stmt.execute("CREATE TABLE IF NOT EXISTS request_examples (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "request_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "status_code INTEGER, " +
                    "response_body TEXT, " +
                    "response_headers TEXT, " +
                    "FOREIGN KEY(request_id) REFERENCES saved_requests(id) ON DELETE CASCADE" +
                    ");");
        } catch (Exception e) {
            System.err.println("Failed to create SQLite tables: " + e.getMessage());
        }
    }

    /**
     * Migrate existing databases: add columns that may be missing from older schema versions.
     * ALTER TABLE ... ADD COLUMN is a no-op if the column already exists (handled via try/catch).
     */
    private void migrateSchema() {
        String[] migrations = {
            "ALTER TABLE collections ADD COLUMN workspace_id INTEGER DEFAULT 1",
            "ALTER TABLE saved_requests ADD COLUMN workspace_id INTEGER DEFAULT 1",
            "ALTER TABLE request_examples ADD COLUMN response_headers TEXT",
            "ALTER TABLE saved_requests ADD COLUMN body_type TEXT",
            "ALTER TABLE saved_requests ADD COLUMN form_data TEXT",
            "ALTER TABLE saved_requests ADD COLUMN request_settings TEXT",
            "ALTER TABLE saved_requests ADD COLUMN pre_request_script TEXT",
            "ALTER TABLE saved_requests ADD COLUMN test_script TEXT"
        };
        for (String sql : migrations) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                // Column already exists — safe to ignore "duplicate column name" error
                if (!e.getMessage().contains("duplicate column")) {
                    System.err.println("Migration note: " + e.getMessage());
                }
            }
        }
    }

    private void seedDefaultWorkspace() {
        String countSql = "SELECT COUNT(*) FROM workspaces;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                createWorkspace("My Personal Workspace", "PERSONAL");
                createWorkspace("Enterprise Banking API Gateway", "ENTERPRISE");
            }
        } catch (Exception e) {
            System.err.println("Failed to seed default workspace: " + e.getMessage());
        }
    }

    public void cacheRequest(String id, String method, String url, int statusCode, long latency) {
        String sql = "INSERT OR REPLACE INTO offline_history (id, method, url, status_code, latency, timestamp) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, method);
            pstmt.setString(3, url);
            pstmt.setInt(4, statusCode);
            pstmt.setLong(5, latency);
            pstmt.setLong(6, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to insert cache log into SQLite: " + e.getMessage());
        }
    }

    // Workspaces CRUD
    public int createWorkspace(String name, String type) {
        String sql = "INSERT INTO workspaces (name, description, type) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, "Default generated " + type + " workspace container.");
            pstmt.setString(3, type);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create workspace: " + e.getMessage());
        }
        return -1;
    }

    public List<Workspace> getWorkspaces() {
        List<Workspace> list = new ArrayList<>();
        String query = "SELECT * FROM workspaces ORDER BY id ASC;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Workspace(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("type")
                ));
            }
        } catch (Exception e) {
            System.err.println("Failed to load workspaces: " + e.getMessage());
        }
        return list;
    }

    // Collections Management scoped to workspace_id
    public int createCollection(int workspaceId, String name) {
        String sql = "INSERT INTO collections (workspace_id, name) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, workspaceId);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create collection: " + e.getMessage());
        }
        return -1;
    }

    public int createFolder(int collectionId, String name) {
        String sql = "INSERT INTO folders (collection_id, name) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, collectionId);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create folder: " + e.getMessage());
        }
        return -1;
    }

    public int saveRequest(int workspaceId, Integer collectionId, Integer folderId, String requestName, ApiRequest req) {
        String sql = "INSERT INTO saved_requests (workspace_id, collection_id, folder_id, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, workspaceId);
            if (collectionId == null) pstmt.setNull(2, Types.INTEGER); else pstmt.setInt(2, collectionId);
            if (folderId == null) pstmt.setNull(3, Types.INTEGER); else pstmt.setInt(3, folderId);
            pstmt.setString(4, requestName);
            pstmt.setString(5, req.getProtocol());
            pstmt.setString(6, req.getMethod());
            pstmt.setString(7, req.getUrl());
            pstmt.setString(8, req.getAuthType());
            pstmt.setString(9, req.getHeadersJson());
            pstmt.setString(10, req.getBody());
            pstmt.setString(11, req.getAuthUsername());
            pstmt.setString(12, req.getAuthPassword());
            pstmt.setString(13, req.getAuthToken());
            pstmt.setString(14, req.getApiKeyName());
            pstmt.setString(15, req.getApiKeyValue());
            pstmt.setString(16, req.getPreRequestScript());
            pstmt.setString(17, req.getTestScript());
            pstmt.setString(18, req.getBodyType());
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String fDataJson = "{}";
            try { fDataJson = mapper.writeValueAsString(req.getFormData()); } catch(Exception e){}
            pstmt.setString(19, fDataJson);
            
            String reqSettingsJson = "{}";
            try { reqSettingsJson = mapper.writeValueAsString(req.getRequestSettings()); } catch(Exception e){}
            pstmt.setString(20, reqSettingsJson);

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to save request: " + e.getMessage());
            com.apiforge.studio.utils.LogUtil.log("Failed to save request", e);
        }
        return -1;
    }

    public boolean updateRequest(ApiRequest req) {
        if (req.getId() == null || req.getId().isEmpty()) return false;
        
        String sql = "UPDATE saved_requests SET name = ?, protocol = ?, method = ?, url = ?, auth_type = ?, headers = ?, body = ?, auth_username = ?, auth_password = ?, auth_token = ?, api_key_name = ?, api_key_value = ?, pre_request_script = ?, test_script = ?, body_type = ?, form_data = ?, request_settings = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, req.getName());
            pstmt.setString(2, req.getProtocol());
            pstmt.setString(3, req.getMethod());
            pstmt.setString(4, req.getUrl());
            pstmt.setString(5, req.getAuthType());
            pstmt.setString(6, req.getHeadersJson());
            pstmt.setString(7, req.getBody());
            pstmt.setString(8, req.getAuthUsername());
            pstmt.setString(9, req.getAuthPassword());
            pstmt.setString(10, req.getAuthToken());
            pstmt.setString(11, req.getApiKeyName());
            pstmt.setString(12, req.getApiKeyValue());
            pstmt.setString(13, req.getPreRequestScript());
            pstmt.setString(14, req.getTestScript());
            pstmt.setString(15, req.getBodyType());
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String fDataJson = "{}";
            try { fDataJson = mapper.writeValueAsString(req.getFormData()); } catch(Exception e){}
            pstmt.setString(16, fDataJson);
            
            String reqSettingsJson = "{}";
            try { reqSettingsJson = mapper.writeValueAsString(req.getRequestSettings()); } catch(Exception e){}
            pstmt.setString(17, reqSettingsJson);
            
            pstmt.setInt(18, Integer.parseInt(req.getId()));

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Failed to update request: " + e.getMessage());
            com.apiforge.studio.utils.LogUtil.log("Failed to update request", e);
            return false;
        }
    }

    public List<Collection> getCollections(int workspaceId) {
        List<Collection> collections = new ArrayList<>();
        String query = "SELECT * FROM collections WHERE workspace_id = ? ORDER BY id ASC;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, workspaceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Collection col = new Collection(rs.getInt("id"), rs.getString("name"));
                    col.setFolders(getFoldersForCollection(col.getId()));
                    col.setRequests(getRequestsForCollection(col.getId(), workspaceId));
                    collections.add(col);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load collections: " + e.getMessage());
        }
        return collections;
    }

    private List<Folder> getFoldersForCollection(int colId) {
        List<Folder> folders = new ArrayList<>();
        String query = "SELECT * FROM folders WHERE collection_id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, colId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Folder f = new Folder(rs.getInt("id"), colId, rs.getString("name"));
                    f.setRequests(getRequestsForFolder(f.getId()));
                    folders.add(f);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load folders: " + e.getMessage());
        }
        return folders;
    }

    private List<ApiRequest> getRequestsForCollection(int colId, int workspaceId) {
        List<ApiRequest> list = new ArrayList<>();
        String query = "SELECT * FROM saved_requests WHERE workspace_id = ? AND collection_id = ? AND folder_id IS NULL;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, workspaceId);
            pstmt.setInt(2, colId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRequest(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load collection requests: " + e.getMessage());
        }
        return list;
    }

    private List<ApiRequest> getRequestsForFolder(int folderId) {
        List<ApiRequest> list = new ArrayList<>();
        String query = "SELECT * FROM saved_requests WHERE folder_id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, folderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRequest(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load folder requests: " + e.getMessage());
        }
        return list;
    }

    public void addExample(int requestId, String name, int statusCode, String responseBody, String responseHeaders) {
        String sql = "INSERT INTO request_examples (request_id, name, status_code, response_body, response_headers) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            pstmt.setString(2, name);
            pstmt.setInt(3, statusCode);
            pstmt.setString(4, responseBody);
            pstmt.setString(5, responseHeaders);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to add example: " + e.getMessage());
        }
    }

    public void updateExample(int exampleId, String name, int statusCode, String responseBody, String responseHeaders) {
        String sql = "UPDATE request_examples SET name = ?, status_code = ?, response_body = ?, response_headers = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, statusCode);
            pstmt.setString(3, responseBody);
            pstmt.setString(4, responseHeaders);
            pstmt.setInt(5, exampleId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to update example: " + e.getMessage());
        }
    }

    public void deleteExample(int exampleId) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM request_examples WHERE id = ?;")) {
            pstmt.setInt(1, exampleId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete example: " + e.getMessage());
        }
    }

    private List<com.apiforge.studio.model.RequestExample> getExamplesForRequest(int requestId) {
        List<com.apiforge.studio.model.RequestExample> list = new ArrayList<>();
        String sql = "SELECT * FROM request_examples WHERE request_id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                com.apiforge.studio.model.RequestExample ex = new com.apiforge.studio.model.RequestExample(
                        rs.getInt("id"),
                        rs.getInt("request_id"),
                        rs.getString("name"),
                        rs.getInt("status_code"),
                        rs.getString("response_body")
                );
                String headersJson = rs.getString("response_headers");
                if (headersJson != null && !headersJson.isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.Map<String, String> map = mapper.readValue(headersJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                        ex.setResponseHeaders(map);
                    } catch (Exception ex2) {
                        // ignore parsing error
                    }
                }
                list.add(ex);
            }
        } catch (Exception e) {
            System.err.println("Failed to load examples: " + e.getMessage());
        }
        return list;
    }

    private ApiRequest mapRequest(ResultSet rs) throws SQLException {
        ApiRequest req = new ApiRequest();
        req.setId(String.valueOf(rs.getInt("id")));
        req.setProtocol(rs.getString("protocol"));
        req.setMethod(rs.getString("method"));
        req.setUrl(rs.getString("url"));
        req.setAuthType(rs.getString("auth_type"));
        req.setBody(rs.getString("body"));
        req.setAuthUsername(rs.getString("auth_username"));
        req.setAuthPassword(rs.getString("auth_password"));
        req.setAuthToken(rs.getString("auth_token"));
        req.setApiKeyName(rs.getString("api_key_name"));
        req.setApiKeyValue(rs.getString("api_key_value"));
        req.setPreRequestScript(rs.getString("pre_request_script"));
        req.setTestScript(rs.getString("test_script"));
        req.setHeadersFromJson(rs.getString("headers"));
        req.setCustomName(rs.getString("name"));
        
        try {
            String bType = rs.getString("body_type");
            if (bType != null) req.setBodyType(bType);
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String fDataJson = rs.getString("form_data");
            if (fDataJson != null && !fDataJson.isEmpty()) {
                req.setFormData(mapper.readValue(fDataJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {}));
            }
            
            String rSettingsJson = rs.getString("request_settings");
            if (rSettingsJson != null && !rSettingsJson.isEmpty()) {
                req.setRequestSettings(mapper.readValue(rSettingsJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Boolean>>() {}));
            }
        } catch(Exception e) {
            // ignore JSON parse exceptions for older records
        }
        
        req.setExamples(getExamplesForRequest(rs.getInt("id")));
        return req;
    }

    public void deleteCollection(int colId) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM collections WHERE id = ?;")) {
            pstmt.setInt(1, colId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete collection: " + e.getMessage());
        }
    }

    public void deleteFolder(int folderId) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM folders WHERE id = ?;")) {
            pstmt.setInt(1, folderId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete folder: " + e.getMessage());
        }
    }

    public void deleteSavedRequest(int reqId) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM saved_requests WHERE id = ?;")) {
            pstmt.setInt(1, reqId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to delete request: " + e.getMessage());
        }
    }

    public void renameCollection(int colId, String newName) {
        try (PreparedStatement pstmt = connection.prepareStatement("UPDATE collections SET name = ? WHERE id = ?;")) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, colId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to rename collection: " + e.getMessage());
        }
    }

    public void renameFolder(int folderId, String newName) {
        try (PreparedStatement pstmt = connection.prepareStatement("UPDATE folders SET name = ? WHERE id = ?;")) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, folderId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to rename folder: " + e.getMessage());
        }
    }

    public void renameSavedRequest(int reqId, String newName) {
        try (PreparedStatement pstmt = connection.prepareStatement("UPDATE saved_requests SET name = ? WHERE id = ?;")) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, reqId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to rename request: " + e.getMessage());
        }
    }

    public void duplicateSavedRequest(int reqId) {
        String query = "INSERT INTO saved_requests (workspace_id, collection_id, folder_id, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings) " +
                       "SELECT workspace_id, collection_id, folder_id, name || ' Copy', protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings " +
                       "FROM saved_requests WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reqId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to duplicate request: " + e.getMessage());
        }
    }

    public void duplicateFolder(int folderId) {
        // Deep copy folder
        String folderQuery = "INSERT INTO folders (collection_id, name) SELECT collection_id, name || ' Copy' FROM folders WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(folderQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, folderId);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int newFolderId = rs.getInt(1);
                // Copy requests
                String reqQuery = "INSERT INTO saved_requests (workspace_id, collection_id, folder_id, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings) " +
                                  "SELECT workspace_id, collection_id, ?, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings " +
                                  "FROM saved_requests WHERE folder_id = ?;";
                try (PreparedStatement rpstmt = connection.prepareStatement(reqQuery)) {
                    rpstmt.setInt(1, newFolderId);
                    rpstmt.setInt(2, folderId);
                    rpstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to duplicate folder: " + e.getMessage());
        }
    }

    public void duplicateCollection(int colId) {
        // Deep copy collection
        String colQuery = "INSERT INTO collections (workspace_id, name) SELECT workspace_id, name || ' Copy' FROM collections WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(colQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, colId);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int newColId = rs.getInt(1);
                
                // Copy requests directly in collection
                String reqQuery = "INSERT INTO saved_requests (workspace_id, collection_id, folder_id, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings) " +
                                  "SELECT workspace_id, ?, NULL, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings " +
                                  "FROM saved_requests WHERE collection_id = ? AND folder_id IS NULL;";
                try (PreparedStatement rpstmt = connection.prepareStatement(reqQuery)) {
                    rpstmt.setInt(1, newColId);
                    rpstmt.setInt(2, colId);
                    rpstmt.executeUpdate();
                }

                // For each folder, duplicate it into new collection
                String getFoldersQuery = "SELECT id FROM folders WHERE collection_id = ?;";
                try (PreparedStatement fget = connection.prepareStatement(getFoldersQuery)) {
                    fget.setInt(1, colId);
                    ResultSet frs = fget.executeQuery();
                    while (frs.next()) {
                        int oldFolderId = frs.getInt("id");
                        // Insert new folder
                        String newFolderQuery = "INSERT INTO folders (collection_id, name) SELECT ?, name FROM folders WHERE id = ?;";
                        try (PreparedStatement fpost = connection.prepareStatement(newFolderQuery, Statement.RETURN_GENERATED_KEYS)) {
                            fpost.setInt(1, newColId);
                            fpost.setInt(2, oldFolderId);
                            fpost.executeUpdate();
                            ResultSet nfrs = fpost.getGeneratedKeys();
                            if (nfrs.next()) {
                                int newFolderId = nfrs.getInt(1);
                                // Copy requests for this folder
                                String freqQuery = "INSERT INTO saved_requests (workspace_id, collection_id, folder_id, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings) " +
                                                  "SELECT workspace_id, ?, ?, name, protocol, method, url, auth_type, headers, body, auth_username, auth_password, auth_token, api_key_name, api_key_value, pre_request_script, test_script, body_type, form_data, request_settings " +
                                                  "FROM saved_requests WHERE folder_id = ?;";
                                try (PreparedStatement freqStmt = connection.prepareStatement(freqQuery)) {
                                    freqStmt.setInt(1, newColId);
                                    freqStmt.setInt(2, newFolderId);
                                    freqStmt.setInt(3, oldFolderId);
                                    freqStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to duplicate collection: " + e.getMessage());
        }
    }
}
