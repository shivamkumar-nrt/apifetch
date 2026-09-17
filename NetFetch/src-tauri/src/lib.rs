use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::time::Instant;
use tauri::State;
use std::sync::Mutex;
use rusqlite::{Connection, Result as SqlResult};

#[derive(Serialize, Deserialize, Debug)]
pub struct Workspace {
    id: i64,
    name: String,
    description: Option<String>,
    #[serde(rename = "type")]
    ws_type: String,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct Environment {
    id: i64,
    workspace_id: i64,
    name: String,
    is_default: bool,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct EnvVariable {
    id: i64,
    environment_id: i64,
    key: String,
    value: String,
    is_secret: bool,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct SavedRequest {
    id: i64,
    workspace_id: i64,
    collection_id: Option<i64>,
    folder_id: Option<i64>,
    name: String,
    protocol: String,
    method: String,
    url: String,
    auth_type: String,
    headers: String,
    body: String,
    auth_username: Option<String>,
    auth_password: Option<String>,
    auth_token: Option<String>,
    api_key_name: Option<String>,
    api_key_value: Option<String>,
    pre_request_script: Option<String>,
    test_script: Option<String>,
    body_type: String,
    form_data: Option<String>,
    request_settings: Option<String>,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct Settings {
    ssl_verify: bool,
    follow_redirects: bool,
    timeout: u64,
}

#[derive(Serialize, Deserialize, Debug)]
#[allow(non_snake_case)]
pub struct ResponseData {
    status: u16,
    statusText: String,
    headers: HashMap<String, String>,
    body: String,
    time: i64,
    size: usize,
}

struct AppState {
    db: Mutex<Connection>,
}

fn init_db() -> Connection {
    let home_dir = dirs::home_dir().expect("Could not find home directory");
    let app_dir = home_dir.join(".netfetch");
    std::fs::create_dir_all(&app_dir).expect("Failed to create app directory");
    
    let db_path = app_dir.join("netfetch.db");
    let conn = Connection::open(db_path).expect("Failed to open database");
    
    // Schema
    conn.execute(
        "CREATE TABLE IF NOT EXISTS workspaces (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, description TEXT, type TEXT)",
        (),
    ).expect("Failed to create workspaces table");
    
    conn.execute(
        "CREATE TABLE IF NOT EXISTS collections (id INTEGER PRIMARY KEY AUTOINCREMENT, workspace_id INTEGER, name TEXT)",
        (),
    ).expect("Failed to create collections table");
    
    conn.execute(
        "CREATE TABLE IF NOT EXISTS folders (id INTEGER PRIMARY KEY AUTOINCREMENT, collection_id INTEGER, name TEXT)",
        (),
    ).expect("Failed to create folders table");
    
    conn.execute(
        "CREATE TABLE IF NOT EXISTS saved_requests (
            id INTEGER PRIMARY KEY AUTOINCREMENT, workspace_id INTEGER, collection_id INTEGER, folder_id INTEGER, 
            name TEXT, protocol TEXT, method TEXT, url TEXT, auth_type TEXT, headers TEXT, body TEXT, 
            auth_username TEXT, auth_password TEXT, auth_token TEXT, api_key_name TEXT, api_key_value TEXT, 
            pre_request_script TEXT, test_script TEXT, body_type TEXT, form_data TEXT, request_settings TEXT)",
        (),
    ).expect("Failed to create saved_requests table");
    
    conn.execute(
        "CREATE TABLE IF NOT EXISTS environments (id INTEGER PRIMARY KEY AUTOINCREMENT, workspace_id INTEGER, name TEXT, is_default BOOLEAN)",
        (),
    ).expect("Failed to create environments table");

    conn.execute(
        "CREATE TABLE IF NOT EXISTS env_variables (id INTEGER PRIMARY KEY AUTOINCREMENT, environment_id INTEGER, key TEXT, value TEXT, is_secret BOOLEAN)",
        (),
    ).expect("Failed to create env_variables table");
    
    conn.execute(
        "CREATE TABLE IF NOT EXISTS offline_history (id INTEGER PRIMARY KEY AUTOINCREMENT, method TEXT, url TEXT, status_code INTEGER, latency INTEGER, timestamp INTEGER)",
        (),
    ).expect("Failed to create offline_history table");
    
    conn.execute(
        "CREATE TABLE IF NOT EXISTS request_examples (id INTEGER PRIMARY KEY AUTOINCREMENT, request_id INTEGER, name TEXT, status_code INTEGER, response_body TEXT, response_headers TEXT)",
        (),
    ).expect("Failed to create request_examples table");

    conn
}

fn substitute_vars(text: &str, vars: &HashMap<String, String>) -> String {
    let mut result = text.to_string();
    for (k, v) in vars {
        let pattern = format!("{{{{{}}}}}", k);
        result = result.replace(&pattern, v);
    }
    result
}

#[tauri::command]
async fn send_request(
    method: String,
    url: String,
    headers: String,
    body: String,
    params: String,
    auth: String,
    settings: Settings,
    env_vars_json: Option<String>,
    state: State<'_, AppState>,
) -> Result<ResponseData, String> {
    
    let mut env_vars = HashMap::new();
    if let Some(ev_str) = env_vars_json {
        if let Ok(parsed) = serde_json::from_str::<HashMap<String, String>>(&ev_str) {
            env_vars = parsed;
        }
    }

    let subbed_url = substitute_vars(&url, &env_vars);
    let subbed_headers = substitute_vars(&headers, &env_vars);
    let subbed_body = substitute_vars(&body, &env_vars);
    let subbed_params = substitute_vars(&params, &env_vars);
    let subbed_auth = substitute_vars(&auth, &env_vars);

    let client_builder = reqwest::Client::builder()
        .danger_accept_invalid_certs(!settings.ssl_verify)
        .timeout(std::time::Duration::from_millis(settings.timeout));

    let client_builder = if !settings.follow_redirects {
        client_builder.redirect(reqwest::redirect::Policy::none())
    } else {
        client_builder
    };

    let client = client_builder.build().map_err(|e| e.to_string())?;

    let mut parsed_url = reqwest::Url::parse(&subbed_url).unwrap_or_else(|_| reqwest::Url::parse("http://localhost").unwrap());
    
    if !subbed_params.is_empty() {
        if let Ok(params_list) = serde_json::from_str::<Vec<HashMap<String, String>>>(&subbed_params) {
            for p in params_list {
                if let (Some(k), Some(v)) = (p.get("key"), p.get("value")) {
                    if !k.is_empty() {
                        parsed_url.query_pairs_mut().append_pair(k, v);
                    }
                }
            }
        }
    }

    let req_method = match method.to_uppercase().as_str() {
        "GET" => reqwest::Method::GET,
        "POST" => reqwest::Method::POST,
        "PUT" => reqwest::Method::PUT,
        "PATCH" => reqwest::Method::PATCH,
        "DELETE" => reqwest::Method::DELETE,
        "HEAD" => reqwest::Method::HEAD,
        "OPTIONS" => reqwest::Method::OPTIONS,
        _ => reqwest::Method::GET,
    };

    let mut request_builder = client.request(req_method.clone(), parsed_url.clone());

    if !subbed_headers.is_empty() {
        if let Ok(headers_list) = serde_json::from_str::<Vec<HashMap<String, String>>>(&subbed_headers) {
            for h in headers_list {
                if let (Some(k), Some(v)) = (h.get("key"), h.get("value")) {
                    if !k.is_empty() {
                        request_builder = request_builder.header(k, v);
                    }
                }
            }
        }
    }

    if !subbed_auth.is_empty() {
        if let Ok(auth_data) = serde_json::from_str::<serde_json::Value>(&subbed_auth) {
            if let Some(auth_type) = auth_data.get("type").and_then(|t| t.as_str()) {
                match auth_type {
                    "bearer" => {
                        if let Some(token) = auth_data.get("token").and_then(|t| t.as_str()) {
                            request_builder = request_builder.bearer_auth(token);
                        }
                    },
                    "basic" => {
                        if let (Some(user), Some(pass)) = (
                            auth_data.get("username").and_then(|u| u.as_str()),
                            auth_data.get("password").and_then(|p| p.as_str())
                        ) {
                            request_builder = request_builder.basic_auth(user, Some(pass));
                        }
                    },
                    "apikey" => {
                        if let (Some(k), Some(v), Some(in_type)) = (
                            auth_data.get("key").and_then(|x| x.as_str()),
                            auth_data.get("value").and_then(|x| x.as_str()),
                            auth_data.get("in").and_then(|x| x.as_str())
                        ) {
                            if in_type == "header" {
                                request_builder = request_builder.header(k, v);
                            } else if in_type == "query" {
                                request_builder = request_builder.query(&[(k, v)]);
                            }
                        }
                    },
                    _ => {}
                }
            }
        }
    }

    if !subbed_body.is_empty() {
        if let Ok(body_data) = serde_json::from_str::<serde_json::Value>(&subbed_body) {
            let mode = body_data.get("mode").and_then(|m| m.as_str()).unwrap_or("none");
            match mode {
                "raw" => {
                    if let Some(raw) = body_data.get("raw").and_then(|r| r.as_str()) {
                        request_builder = request_builder.body(raw.to_string());
                    }
                },
                "urlencoded" => {
                    if let Some(encoded) = body_data.get("urlencoded").and_then(|r| r.as_array()) {
                        let mut params = vec![];
                        for item in encoded {
                            if let (Some(k), Some(v)) = (
                                item.get("key").and_then(|x| x.as_str()),
                                item.get("value").and_then(|x| x.as_str())
                            ) {
                                params.push((k.to_string(), v.to_string()));
                            }
                        }
                        request_builder = request_builder.form(&params);
                    }
                },
                "formdata" => {
                    // Very basic multipart form-data support
                    if let Some(formdata) = body_data.get("formdata").and_then(|r| r.as_array()) {
                        let mut form = reqwest::multipart::Form::new();
                        for item in formdata {
                            if let (Some(k), Some(v)) = (
                                item.get("key").and_then(|x| x.as_str()),
                                item.get("value").and_then(|x| x.as_str())
                            ) {
                                form = form.text(k.to_string(), v.to_string());
                            }
                        }
                        request_builder = request_builder.multipart(form);
                    }
                },
                _ => {}
            }
        }
    }

    let start = Instant::now();
    let response = request_builder.send().await.map_err(|e| e.to_string())?;
    let duration = start.elapsed().as_millis() as i64;

    let status = response.status();
    let status_code = status.as_u16();
    let status_text = status.canonical_reason().unwrap_or("Unknown").to_string();
    
    let mut res_headers = HashMap::new();
    for (key, value) in response.headers() {
        res_headers.insert(key.to_string(), value.to_str().unwrap_or("").to_string());
    }

    let bytes = response.bytes().await.map_err(|e| e.to_string())?;
    let size = bytes.len();
    let res_body = String::from_utf8_lossy(&bytes).to_string();

    let conn = state.db.lock().unwrap();
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_millis() as i64;
    let _ = conn.execute(
        "INSERT INTO offline_history (method, url, status_code, latency, timestamp) VALUES (?1, ?2, ?3, ?4, ?5)",
        rusqlite::params![method, url, status_code, duration, now],
    );

    Ok(ResponseData {
        status: status_code,
        statusText: status_text,
        headers: res_headers,
        body: res_body,
        time: duration,
        size,
    })
}

#[derive(Serialize, Deserialize, Debug)]
pub struct HistoryEntry {
    id: i64,
    method: String,
    url: String,
    status: u16,
    duration: i64,
    timestamp: i64,
}

#[tauri::command]
fn get_history(state: State<'_, AppState>) -> Result<Vec<HistoryEntry>, String> {
    let conn = state.db.lock().unwrap();
    let mut stmt = conn.prepare("SELECT id, method, url, status_code, latency, timestamp FROM offline_history ORDER BY timestamp DESC LIMIT 50").map_err(|e| e.to_string())?;
    let history_iter = stmt.query_map([], |row| {
        Ok(HistoryEntry {
            id: row.get(0)?,
            method: row.get(1)?,
            url: row.get(2)?,
            status: row.get(3)?,
            duration: row.get(4)?,
            timestamp: row.get(5)?,
        })
    }).map_err(|e| e.to_string())?;
    
    let mut result = Vec::new();
    for entry in history_iter {
        result.push(entry.unwrap());
    }
    Ok(result)
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .manage(AppState {
            db: Mutex::new(init_db()),
        })
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![
            send_request,
            get_history
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
