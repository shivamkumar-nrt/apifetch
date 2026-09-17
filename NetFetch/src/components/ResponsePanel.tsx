import { useState } from "react";
import { ResponseData } from "../types";
import { CloudUpload } from "lucide-react";

export function ResponsePanel({ response }: { response?: ResponseData }) {
  const [tab, setTab] = useState<'body'|'headers'|'cookies'>('body');

  if (!response || response.status === 0) {
    return (
      <div className="response-panel" style={{alignItems: 'center', justifyContent: 'center', color: '#888', display: 'flex', flexDirection: 'column', gap: 12, flex: 1}}>
        <CloudUpload size={48} style={{ color: '#ccc' }} />
        <div style={{ fontSize: 18, fontWeight: 500, color: 'var(--text)' }}>Hit Send to get a response</div>
        <div style={{ fontSize: 14 }}>Waiting for you to make a request...</div>
      </div>
    );
  }

  const statusClass = `status-${String(response.status).charAt(0)}xx`;
  
  let formattedBody = response.body;
  try {
    if (response.body.trim().startsWith('{') || response.body.trim().startsWith('[')) {
      formattedBody = JSON.stringify(JSON.parse(response.body), null, 2);
    }
  } catch (e) {}

  return (
    <div className="response-panel">
      <div style={{padding: '12px 16px', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
        <div style={{display: 'flex', gap: 16}}>
          <div 
            className={`inner-tab ${tab === 'body' ? 'active' : ''}`}
            onClick={() => setTab('body')}
            style={{padding: '0 0 8px 0', borderBottomWidth: tab === 'body' ? 2 : 0, cursor: 'pointer', color: tab === 'body' ? 'var(--text)' : 'var(--text-light)'}}
          >
            Body
          </div>
          <div 
            className={`inner-tab ${tab === 'headers' ? 'active' : ''}`}
            onClick={() => setTab('headers')}
            style={{padding: '0 0 8px 0', borderBottomWidth: tab === 'headers' ? 2 : 0, cursor: 'pointer', color: tab === 'headers' ? 'var(--text)' : 'var(--text-light)'}}
          >
            Headers
          </div>
          <div 
            className={`inner-tab ${tab === 'cookies' ? 'active' : ''}`}
            onClick={() => setTab('cookies')}
            style={{padding: '0 0 8px 0', borderBottomWidth: tab === 'cookies' ? 2 : 0, cursor: 'pointer', color: tab === 'cookies' ? 'var(--text)' : 'var(--text-light)'}}
          >
            Cookies
          </div>
        </div>
        <div className="meta-info" style={{marginBottom: 0}}>
          <div>Status: <span className={`status-badge ${statusClass}`}>{response.status} {response.statusText}</span></div>
          <div>Time: <span style={{color: 'var(--get)'}}>{response.time} ms</span></div>
          <div>Size: <span style={{color: 'var(--get)'}}>{(response.size / 1024).toFixed(2)} KB</span></div>
        </div>
      </div>
      
      <div className="panel-content" style={{ overflowY: 'auto', height: '100%' }}>
        {tab === 'body' && (
          <pre>{formattedBody}</pre>
        )}
        
        {tab === 'headers' && (
          <table className="kv-table">
            <tbody>
              {Object.entries(response.headers).map(([k, v]) => (
                <tr key={k}>
                  <td style={{fontWeight: 'bold', width: '30%'}}>{k}</td>
                  <td style={{wordBreak: 'break-all'}}>{v}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        
        {tab === 'cookies' && (
          <div style={{color: 'var(--text-light)'}}>No cookies</div>
        )}
      </div>
    </div>
  );
}
