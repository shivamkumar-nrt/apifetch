import { useState } from "react";
import { Tab } from "../types";
import { KeyValueTable } from "./KeyValueTable";
import { AuthPanel } from "./AuthPanel";

export function RequestPanel({ tab, updateTab }: { tab: Tab, updateTab: (t: Partial<Tab>) => void }) {
  const [activeInnerTab, setActiveInnerTab] = useState<'params'|'auth'|'headers'|'body'|'settings'|'pre-request'|'tests'|'ai-assistant'>('params');

  return (
    <div className="request-panel">
      <div className="inner-tabs">
        {['Params', 'Auth', 'Headers', 'Body', 'Pre-request', 'Tests', 'Settings', 'AI Assistant'].map(t => (
          <div 
            key={t}
            className={`inner-tab ${activeInnerTab === t.toLowerCase().replace(' ', '-') ? 'active' : ''}`}
            onClick={() => setActiveInnerTab(t.toLowerCase().replace(' ', '-') as any)}
          >
            {t}
          </div>
        ))}
      </div>
      <div className="panel-content">
        {activeInnerTab === 'params' && (
          <KeyValueTable 
            items={tab.params} 
            onChange={(params) => updateTab({ params })} 
          />
        )}
        
        {activeInnerTab === 'headers' && (
          <KeyValueTable 
            items={tab.headers} 
            onChange={(headers) => updateTab({ headers })} 
          />
        )}
        
        {activeInnerTab === 'auth' && (
          <AuthPanel auth={tab.auth} onChange={(auth) => updateTab({ auth })} />
        )}
        
        {activeInnerTab === 'body' && (
          <div>
            <div style={{marginBottom: 12, display: 'flex', gap: 12}}>
              {['none', 'formdata', 'urlencoded', 'raw', 'binary'].map(m => (
                <label key={m}>
                  <input 
                    type="radio" 
                    name="bodyMode" 
                    checked={tab.body.mode === m}
                    onChange={() => updateTab({ body: { ...tab.body, mode: m as any } })}
                  /> {m}
                </label>
              ))}
            </div>
            {tab.body.mode === 'raw' && (
              <textarea 
                value={tab.body.raw || ""} 
                onChange={(e) => updateTab({ body: { ...tab.body, raw: e.target.value }})}
                placeholder="Raw body data..."
              />
            )}
            {tab.body.mode === 'urlencoded' && (
              <KeyValueTable 
                items={tab.body.urlencoded || []} 
                onChange={(urlencoded) => updateTab({ body: { ...tab.body, urlencoded }})} 
              />
            )}
          </div>
        )}
        
        {activeInnerTab === 'settings' && (
          <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
            <div style={{ display: 'flex', gap: 16, borderBottom: '1px solid var(--border)', marginBottom: 16, paddingBottom: 8 }}>
              <span style={{ color: 'var(--primary)', borderBottom: '2px solid var(--primary)', paddingBottom: 8, cursor: 'pointer', fontWeight: 'bold' }}>Request Settings</span>
              <span style={{ color: 'var(--text-light)', cursor: 'pointer', paddingBottom: 8 }}>Load Test</span>
              <span style={{ color: 'var(--text-light)', cursor: 'pointer', paddingBottom: 8 }}>Mock Config</span>
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 24, padding: '0 8px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 'bold', marginBottom: 4 }}>Enable SSL certificate verification</div>
                  <div style={{ color: 'var(--text-light)', fontSize: 13 }}>Verify SSL certs when sending requests.</div>
                </div>
                <label className="toggle-switch">
                  <input type="checkbox" checked={tab.settings.ssl_verify} onChange={e => updateTab({ settings: { ...tab.settings, ssl_verify: e.target.checked } })} />
                  <span className="slider round"></span>
                </label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 'bold', marginBottom: 4 }}>Automatically follow redirects</div>
                  <div style={{ color: 'var(--text-light)', fontSize: 13 }}>Follow HTTP 3xx responses.</div>
                </div>
                <label className="toggle-switch">
                  <input type="checkbox" checked={tab.settings.follow_redirects} onChange={e => updateTab({ settings: { ...tab.settings, follow_redirects: e.target.checked } })} />
                  <span className="slider round"></span>
                </label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 'bold', marginBottom: 4 }}>Follow original HTTP Method</div>
                  <div style={{ color: 'var(--text-light)', fontSize: 13 }}>Redirect with the original HTTP method.</div>
                </div>
                <label className="toggle-switch">
                  <input type="checkbox" />
                  <span className="slider round"></span>
                </label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 'bold', marginBottom: 4 }}>Follow Authorization header</div>
                  <div style={{ color: 'var(--text-light)', fontSize: 13 }}>Retain auth header on redirect.</div>
                </div>
                <label className="toggle-switch">
                  <input type="checkbox" />
                  <span className="slider round"></span>
                </label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 'bold', marginBottom: 4 }}>Remove referer header on redirect</div>
                  <div style={{ color: 'var(--text-light)', fontSize: 13 }}>Remove referer when redirect happens.</div>
                </div>
                <label className="toggle-switch">
                  <input type="checkbox" defaultChecked />
                  <span className="slider round"></span>
                </label>
              </div>
            </div>
          </div>
        )}

        {activeInnerTab === 'pre-request' && (
          <div style={{height: '100%'}}>
            <textarea
              style={{height: '100%'}}
              value={tab.preRequestScript || ""}
              onChange={(e) => updateTab({ preRequestScript: e.target.value })}
              placeholder="// Write JavaScript code to execute before the request. Example: env.set('timestamp', Date.now());"
            />
          </div>
        )}

        {activeInnerTab === 'tests' && (
          <div style={{height: '100%'}}>
            <textarea
              style={{height: '100%'}}
              value={tab.testScript || ""}
              onChange={(e) => updateTab({ testScript: e.target.value })}
              placeholder="// Write JavaScript tests to validate the response. Example: if(response.status !== 200) throw new Error('Failed');"
            />
          </div>
        )}

        {activeInnerTab === 'ai-assistant' && (
          <div style={{height: '100%', display: 'flex', flexDirection: 'column', gap: 12}}>
            <div style={{display: 'flex', gap: 8}}>
              <button style={{padding: '6px 12px', border: '1px solid var(--border)', borderRadius: 4, background: 'var(--bg-gray)', cursor: 'pointer'}}>Generate Tests</button>
              <button style={{padding: '6px 12px', border: '1px solid var(--border)', borderRadius: 4, background: 'var(--bg-gray)', cursor: 'pointer'}}>Mock Payload</button>
              <button style={{padding: '6px 12px', border: '1px solid var(--border)', borderRadius: 4, background: 'var(--bg-gray)', cursor: 'pointer'}}>Explain Method</button>
            </div>
            <textarea
              style={{flex: 1, height: '100%'}}
              placeholder="AI response will appear here..."
              readOnly
            />
          </div>
        )}
      </div>
    </div>
  );
}
