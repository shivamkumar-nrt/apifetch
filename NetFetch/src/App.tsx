import { useState, useEffect } from "react";
import { invoke } from "@tauri-apps/api/core";
import { Sidebar } from "./components/Sidebar";
import { RequestPanel } from "./components/RequestPanel";
import { ResponsePanel } from "./components/ResponsePanel";
import { EnvironmentManager, Environment } from "./components/EnvironmentManager";
import { WebSocketClient } from "./components/WebSocketClient";
import { LoadTester } from "./components/LoadTester";
import { Tab, HistoryEntry } from "./types";

function App() {
  const [tabs, setTabs] = useState<Tab[]>([createNewTab()]);
  const [activeTabId, setActiveTabId] = useState<string>(tabs[0].id);
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [activeEnvId, setActiveEnvId] = useState<string | null>(null);
  const [showEnvManager, setShowEnvManager] = useState(false);
  const [showLoadTester, setShowLoadTester] = useState(false);
  
  const [workspaces, setWorkspaces] = useState([{id: 'personal', name: 'My Personal Workspace'}, {id: 'team', name: 'Team Workspace'}]);
  const [activeWorkspaceId, setActiveWorkspaceId] = useState('personal');

  const [collections, setCollections] = useState<any[]>([]);

  const [activeMode, setActiveMode] = useState<'http'|'websocket'>('http');
  const [topPanelHeight, setTopPanelHeight] = useState(300);

  const handleMouseDown = (e: React.MouseEvent) => {
    e.preventDefault();
    const startY = e.clientY;
    const startHeight = topPanelHeight;

    const handleMouseMove = (moveEvent: MouseEvent) => {
      const newHeight = startHeight + (moveEvent.clientY - startY);
      if (newHeight > 100 && newHeight < window.innerHeight - 200) {
        setTopPanelHeight(newHeight);
      }
    };

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  };

  const activeTab = tabs.find(t => t.id === activeTabId) || tabs[0];
  const activeEnv = environments.find(e => e.id === activeEnvId);
  const activeEnvVars = activeEnv?.variables.filter(v => v.enabled).reduce((acc, curr) => ({...acc, [curr.key]: curr.value}), {}) || {};
  const activeEnvVarsJson = Object.keys(activeEnvVars).length > 0 ? JSON.stringify(activeEnvVars) : null;

  const [showSplash, setShowSplash] = useState(true);

  useEffect(() => {
    loadHistory();
    const savedEnvs = localStorage.getItem('netfetch_envs');
    if (savedEnvs) {
      setEnvironments(JSON.parse(savedEnvs));
    }
    const timer = setTimeout(() => setShowSplash(false), 2000);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    localStorage.setItem('netfetch_envs', JSON.stringify(environments));
  }, [environments]);

  function loadHistory() {
    invoke<HistoryEntry[]>("get_history")
      .then(setHistory)
      .catch(console.error);
  }

  function createNewTab(): Tab {
    return {
      id: Math.random().toString(36).substr(2, 9),
      name: "Untitled Request",
      method: "GET",
      url: "",
      headers: [],
      params: [],
      body: { mode: "none" },
      auth: { type: "none" },
      settings: { ssl_verify: true, follow_redirects: true, timeout: 30000 }
    };
  }

  const [showSync, setShowSync] = useState(false);
  const [showImport, setShowImport] = useState(false);
  const [importCurl, setImportCurl] = useState('');
  
  const handleImportSubmit = () => {
    if (importCurl.trim()) {
      parseCurlCommand(importCurl);
      setShowImport(false);
      setImportCurl('');
    }
  };
  const [syncStatus, setSyncStatus] = useState('');

  const handleCloudSync = async () => {
    try {
      setSyncStatus('Logging into Spring Boot (http://localhost:8082)...');
      
      const loginRes = await fetch('http://localhost:8082/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'admin', password: 'password' })
      });
      
      if (!loginRes.ok) throw new Error('Login failed');
      const authData = await loginRes.json();
      
      setSyncStatus('Logged in as ' + authData.username + '. Pushing collections to cloud...');
      
      const pushRes = await fetch('http://localhost:8082/api/v1/sync/push', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': Bearer 
        },
        body: JSON.stringify(collections)
      });
      
      if (!pushRes.ok) throw new Error('Push failed');
      const pushData = await pushRes.text();
      
      setSyncStatus('Success: ' + pushData);
      setTimeout(() => setShowSync(false), 2000);
      
    } catch (e: any) {
      setSyncStatus('Error: ' + String(e.message || e));
    }
  };
  const addTab = () => {
    const nt = createNewTab();
    setTabs([...tabs, nt]);
    setActiveTabId(nt.id);
    setActiveMode('http');
  };

  const closeTab = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (tabs.length === 1) return;
    const newTabs = tabs.filter(t => t.id !== id);
    setTabs(newTabs);
    if (activeTabId === id) setActiveTabId(newTabs[0].id);
  };

  const updateActiveTab = (updates: Partial<Tab>) => {
    setTabs(prev => prev.map(t => t.id === activeTabId ? { ...t, ...updates } : t));
  };

  const executeScript = (script: string, env: any, response?: any) => {
    try {
      const fn = new Function('env', 'response', script);
      fn(
        {
          set: (k: string, v: string) => {
            if (activeEnvId) {
              setEnvironments(envs => envs.map(e => e.id === activeEnvId ? 
                {...e, variables: [...e.variables.filter(x => x.key !== k), {id: Math.random().toString(), key: k, value: v, enabled: true}]} : e
              ));
            }
          },
          get: (k: string) => env[k]
        },
        response
      );
    } catch(e) {
      console.error("Script error:", e);
    }
  };

  const sendRequest = async () => {
    try {
      if (activeTab.preRequestScript) {
        executeScript(activeTab.preRequestScript, activeEnvVars);
      }


      let finalHeaders = activeTab.headers.filter(h => h.enabled && h.key);
      if (activeTab.body.mode === 'raw' && activeTab.body.raw) {
        try {
          JSON.parse(activeTab.body.raw);
          const hasContentType = finalHeaders.some(h => h.key.toLowerCase() === 'content-type');
          if (!hasContentType) {
            finalHeaders.push({ id: 'auto', key: 'Content-Type', value: 'application/json', enabled: true });
          }
        } catch(e) {}
      }

      const response = await invoke("send_request", {
        method: activeTab.method,
        url: activeTab.url,
        headers: JSON.stringify(finalHeaders),

        params: JSON.stringify(activeTab.params.filter(p => p.enabled && p.key)),
        body: JSON.stringify(activeTab.body),
        auth: JSON.stringify(activeTab.auth),
        settings: activeTab.settings,
        envVarsJson: activeEnvVarsJson
      });
      
      if (activeTab.testScript) {
        executeScript(activeTab.testScript, activeEnvVars, response);
      }

      updateActiveTab({ response: response as any });
      loadHistory();
    } catch (e: any) {
      updateActiveTab({ 
        response: { 
          status: 0, statusText: "Error", headers: {}, body: String(e), time: 0, size: 0 
        } 
      });
    }
  };

  const handleWorkspaceChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    if (e.target.value === 'create_new') {
      const name = window.prompt("Enter new workspace name:");
      if (name) {
        const id = name.toLowerCase().replace(/\s+/g, '_');
        setWorkspaces([...workspaces, {id, name}]);
        setActiveWorkspaceId(id);
      }
    } else {
      setActiveWorkspaceId(e.target.value);
    }
  };

  const handleEnvChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    if (e.target.value === 'create_new') {
      setShowEnvManager(true);
    } else {
      setActiveEnvId(e.target.value || null);
    }
  };

  
  const parseCurlCommand = (curlStr: string) => {
    curlStr = curlStr.replace(/\\\s*\r?\n/g, ' '); // Replace line continuation slashes + newlines with space
    curlStr = curlStr.replace(/\r?\n/g, ' ');     // Replace any remaining newlines with space
    let method = 'GET';
    let url = '';
    const headers: any[] = [];
    let bodyMode: 'none' | 'raw' = 'none';
    let rawBody = '';

    const methodMatch = curlStr.match(/(?:-X|--request)\s+([A-Z]+)/);
    if (methodMatch) method = methodMatch[1];

    const urlMatch = curlStr.match(/['"]?(https?:\/\/[^\s'"]+)['"]?/);
    if (urlMatch) url = urlMatch[1];

    const headerRegex = /(?:-H|--header)\s+(['"])(.*?)\1/g;
    let hMatch;
    while ((hMatch = headerRegex.exec(curlStr)) !== null) {
      const parts = hMatch[2].split(':');
      if (parts.length >= 2) {
        headers.push({ id: Math.random().toString(), key: parts[0].trim(), value: parts.slice(1).join(':').trim(), enabled: true });
      }
    }

    const dataRegex = /(?:-d|--data|--data-raw)\s+(['"])([\s\S]*?)\1/;
    const dataMatch = curlStr.match(dataRegex);
    if (dataMatch) {
      if (method === 'GET') method = 'POST';
      bodyMode = 'raw';
      rawBody = dataMatch[2].replace(/\\"/g, '"');
    }

    updateActiveTab({
      method,
      url,
      headers: [...activeTab.headers.filter(h => h.key), ...headers],
      body: { ...activeTab.body, mode: bodyMode, raw: rawBody }
    });
  };

  const handleSave = () => {
    if (collections.length > 0) {
      const newCollections = [...collections];
      if (!newCollections[0].children) newCollections[0].children = [];
      newCollections[0].children.push({
        id: Math.random().toString(36).substr(2, 9),
        name: activeTab.name,
        type: 'request',
        method: activeTab.method,
        tabData: activeTab
      });
      setCollections(newCollections);
      window.alert("Saved to first collection!");
    } else {
      window.alert("Please create a collection first to save this request.");
    }
  };

  return (
    <>
      {showSplash && (
        <div className="splash-screen">
          <img src="/NetFectch.png" alt="NetFetch Logo" className="splash-logo" />
        </div>
      )}
      <div className="top-bar" style={{justifyContent: 'space-between', display: 'flex'}}>
        <div style={{display: 'flex', alignItems: 'center'}}>
          <img src="/NetFectch.png" alt="NetFetch" style={{height: 24, marginRight: 8}} />
          NetFetch
          <span style={{marginLeft: 16, cursor: 'pointer', color: activeMode === 'http' ? 'var(--primary)' : 'inherit'}} onClick={() => setActiveMode('http')}>HTTP</span>
          <span style={{marginLeft: 16, cursor: 'pointer', color: activeMode === 'websocket' ? 'var(--primary)' : 'inherit'}} onClick={() => setActiveMode('websocket')}>WebSocket</span>
        </div>
        <div style={{display: 'flex', alignItems: 'center', gap: 8}}>
          <button onClick={handleSave} style={{background: 'none', border: '1px solid var(--border)', padding: '4px 12px', borderRadius: 4, cursor: 'pointer', fontSize: 13}}>Save</button>
          <button onClick={() => setShowSync(true)} style={{background: 'var(--primary)', color: 'white', border: 'none', padding: '4px 12px', borderRadius: 4, cursor: 'pointer', fontSize: 13}}>?? Cloud Sync</button>
          <button onClick={() => setHistory([])} style={{background: 'none', border: '1px solid var(--border)', padding: '4px 12px', borderRadius: 4, cursor: 'pointer', fontSize: 13}}>Clear History</button>
          
          <select value={activeWorkspaceId} onChange={handleWorkspaceChange} style={{padding: '4px', border: '1px solid var(--border)', borderRadius: 4, marginLeft: 8}}>
            {workspaces.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
            <option value="create_new">+ Create Workspace</option>
          </select>
          
          <select value={activeEnvId || ''} onChange={handleEnvChange} style={{padding: '4px', border: '1px solid var(--border)', borderRadius: 4}}>
            <option value="">No Environment</option>
            {environments.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
            <option value="create_new">+ Create Environment</option>
          </select>
          <button onClick={() => setShowEnvManager(true)} style={{background: 'none', border: '1px solid var(--border)', padding: '2px 8px', borderRadius: 4, cursor: 'pointer'}}>⚙️</button>
        </div>
      </div>
      
      
      
      {showImport && (
        <div className="modal-overlay" style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
          <div style={{background: 'var(--bg)', padding: 24, borderRadius: 8, width: 600}}>
            <h3 style={{marginTop: 0, marginBottom: 16}}>Import cURL</h3>
            <p style={{fontSize: 14, color: 'var(--text-light)', marginBottom: 16}}>Paste your full cURL command below:</p>
            <textarea 
              value={importCurl}
              onChange={(e) => setImportCurl(e.target.value)}
              style={{width: '100%', height: 200, padding: 12, background: 'var(--bg-gray)', border: '1px solid var(--border)', borderRadius: 4, fontFamily: 'monospace', color: 'var(--text)', marginBottom: 24}}
              placeholder="curl -X POST ..."
            />
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: 12}}>
              <button onClick={() => setShowImport(false)} style={{padding: '6px 16px', borderRadius: 4, border: '1px solid var(--border)', background: 'transparent', cursor: 'pointer'}}>Cancel</button>
              <button onClick={handleImportSubmit} style={{padding: '6px 16px', borderRadius: 4, border: 'none', background: 'var(--primary)', color: 'white', cursor: 'pointer'}}>Import</button>
            </div>
          </div>
        </div>
      )}

      {showSync && (
        <div className="modal-overlay" style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
          <div style={{background: 'var(--bg)', padding: 24, borderRadius: 8, width: 400}}>
            <h3 style={{marginTop: 0, marginBottom: 16}}>Sync with APIForge Cloud</h3>
            <p style={{fontSize: 14, color: 'var(--text-light)', marginBottom: 24}}>Connecting to Spring Boot backend at http://localhost:8082</p>
            
            <div style={{padding: 12, background: 'var(--bg-gray)', borderRadius: 4, marginBottom: 24, fontFamily: 'monospace', fontSize: 12}}>
              {syncStatus || 'Ready to sync.'}
            </div>
            
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: 12}}>
              <button onClick={() => setShowSync(false)} style={{padding: '6px 16px', borderRadius: 4, border: '1px solid var(--border)', background: 'transparent', cursor: 'pointer'}}>Cancel</button>
              <button onClick={handleCloudSync} style={{padding: '6px 16px', borderRadius: 4, border: 'none', background: 'var(--primary)', color: 'white', cursor: 'pointer'}}>Start Sync</button>
            </div>
          </div>
        </div>
      )}

      {showEnvManager && (
        <EnvironmentManager 
          environments={environments} 
          activeEnvId={activeEnvId}
          onEnvironmentsChange={setEnvironments}
          onActiveEnvChange={setActiveEnvId}
          onClose={() => setShowEnvManager(false)}
        />
      )}
      
      {showLoadTester && (
        <LoadTester 
          tab={activeTab}
          envVarsJson={activeEnvVarsJson}
          onClose={() => setShowLoadTester(false)}
        />
      )}

      <div className="main-content">
        <Sidebar onImportClick={() => setShowImport(true)} history={history} collections={collections} setCollections={setCollections} onSelectHistory={(h) => {
          const nt = createNewTab();
          nt.method = h.method;
          nt.url = h.url;
          setTabs([...tabs, nt]);
          setActiveTabId(nt.id);
          setActiveMode('http');
        }} />
        
        <div className="workspace">
          {activeMode === 'http' ? (
            <>
              <div className="tabs-bar">
                {tabs.map(tab => (
                  <div 
                    key={tab.id} 
                    className={`tab ${activeTabId === tab.id ? 'active' : ''}`}
                    onClick={() => setActiveTabId(tab.id)}
                  >
                    <span className={`method-${tab.method}`} style={{marginRight: 8}}>{tab.method}</span>
                    {tab.name}
                    <span className="tab-close" onClick={(e) => closeTab(tab.id, e)}>×</span>
                  </div>
                ))}
                <div className="add-tab" onClick={addTab}>+</div>
              </div>
              
              <div className="request-bar">
                <select className="protocol-select" style={{padding: '8px', border: '1px solid var(--border)', borderRadius: '4px 0 0 4px', borderRight: 'none', background: 'var(--bg-gray)'}}>
                  <option>REST</option>
                  <option>GraphQL</option>
                  <option>WebSocket</option>
                  <option>gRPC</option>
                  <option>SOAP</option>
                  <option>SSE</option>
                </select>
                <select 
                  className={`method-select method-${activeTab.method}`} 
                  value={activeTab.method}
                  onChange={(e) => updateActiveTab({ method: e.target.value })}
                  style={{borderRadius: '0 4px 4px 0', borderLeft: '1px solid var(--border)'}}
                >
                  {['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'].map(m => (
                    <option key={m} value={m}>{m}</option>
                  ))}
                </select>
                <input 
                  type="text" 
                  className="url-input" 
                  placeholder="Enter URL or paste text"
                  value={activeTab.url}
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val.trim().startsWith('curl ')) {
                      parseCurlCommand(val);
                    } else {
                      updateActiveTab({ url: val });
                    }
                  }}
                  onKeyDown={(e) => e.key === 'Enter' && sendRequest()}
                />
                <button className="send-btn" onClick={sendRequest}>Send</button>
                <button className="send-btn" style={{backgroundColor: '#4a4a4a', marginLeft: 8}} onClick={() => {
                  navigator.clipboard.writeText(`curl -X ${activeTab.method} "${activeTab.url}"`);
                }}>Copy cURL</button>
                <button className="send-btn" style={{backgroundColor: 'var(--text)', marginLeft: 8}} onClick={() => setShowLoadTester(true)}>Load Test</button>
              </div>

              <div className="panels-container" style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
                <div style={{ height: topPanelHeight, flexShrink: 0, overflow: 'hidden' }}>
                  <RequestPanel tab={activeTab} updateTab={updateActiveTab} />
                </div>
                <div 
                  className="resizer"
                  style={{ height: 8, cursor: 'ns-resize', background: 'var(--border)', flexShrink: 0 }}
                  onMouseDown={handleMouseDown}
                ></div>
                <div style={{ flex: 1, overflow: 'hidden' }}>
                  <ResponsePanel response={activeTab.response} />
                </div>
              </div>
            </>
          ) : (
            <WebSocketClient />
          )}
        </div>
      </div>
    </>
  );
}

export default App;
