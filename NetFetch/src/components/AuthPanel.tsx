import { AuthData } from "../types";

export function AuthPanel({ auth, onChange }: { auth: AuthData, onChange: (a: AuthData) => void }) {
  return (
    <div style={{display: 'flex', gap: 24, height: '100%'}}>
      <div style={{width: 200, borderRight: '1px solid var(--border)', paddingRight: 16}}>
        <div style={{marginBottom: 8, fontWeight: 'bold', color: 'var(--text-light)'}}>Type</div>
        <select 
          style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}}
          value={auth.type}
          onChange={e => onChange({ ...auth, type: e.target.value as any })}
        >
          <option value="none">No Auth</option>
          <option value="apikey">API Key</option>
          <option value="bearer">Bearer Token</option>
          <option value="jwt">JWT Bearer</option>
          <option value="basic">Basic Auth</option>
          <option value="digest">Digest Auth</option>
          <option value="oauth1">OAuth 1.0</option>
          <option value="oauth2">OAuth 2.0</option>
          <option value="hawk">Hawk Authentication</option>
          <option value="aws">AWS Signature</option>
          <option value="ntlm">NTLM Authentication [Beta]</option>
          <option value="akamai">Akamai EdgeGrid</option>
        </select>
      </div>
      
      <div style={{flex: 1}}>
        {auth.type === 'none' && (
          <div style={{color: 'var(--text-light)'}}>This request does not use any authorization.</div>
        )}
        
        {auth.type === 'bearer' && (
          <div>
            <div style={{marginBottom: 16}}>
              <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>Token</label>
              <input 
                type="text" 
                style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}}
                value={auth.token || ''}
                onChange={e => onChange({ ...auth, token: e.target.value })}
                placeholder="Bearer Token"
              />
            </div>
          </div>
        )}

        {auth.type === 'basic' && (
          <div>
            <div style={{marginBottom: 16}}>
              <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>Username</label>
              <input 
                type="text" 
                style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}}
                value={auth.username || ''}
                onChange={e => onChange({ ...auth, username: e.target.value })}
              />
            </div>
            <div style={{marginBottom: 16}}>
              <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>Password</label>
              <input 
                type="password" 
                style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}}
                value={auth.password || ''}
                onChange={e => onChange({ ...auth, password: e.target.value })}
              />
            </div>
          </div>
        )}

        {auth.type === 'apikey' && (
          <div>
            <div style={{marginBottom: 16}}>
              <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>Key</label>
              <input 
                type="text" 
                style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}}
                value={auth.key || ''}
                onChange={e => onChange({ ...auth, key: e.target.value })}
              />
            </div>
            <div style={{marginBottom: 16}}>
              <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>Value</label>
              <input 
                type="text" 
                style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}}
                value={auth.value || ''}
                onChange={e => onChange({ ...auth, value: e.target.value })}
              />
            </div>
            <div style={{marginBottom: 16}}>
              <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>Add to</label>
              <select 
                style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}}
                value={auth.in || 'header'}
                onChange={e => onChange({ ...auth, in: e.target.value as any })}
              >
                <option value="header">Header</option>
                <option value="query">Query Params</option>
              </select>
            </div>
          </div>
        )}

                {auth.type === 'jwt' && (
          <div style={{ display: 'flex', gap: 24 }}>
            <div style={{ flex: 1 }}>
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', marginBottom: 4, fontWeight: 'bold' }}>Algorithm</label>
                <select style={{ width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4, background: 'var(--bg-gray)' }}>
                  <option>HS256</option>
                  <option>HS384</option>
                  <option>HS512</option>
                  <option>RS256</option>
                </select>
              </div>
              <div style={{ marginBottom: 16 }}>
                <input type="checkbox" style={{ marginRight: 8 }} />
                <span>Secret Base64 encoded</span>
              </div>
            </div>
            <div style={{ flex: 2 }}>
              <textarea 
                style={{ width: '100%', height: 200, padding: 8, border: '1px solid var(--border)', borderRadius: 4, fontFamily: 'monospace' }}
                placeholder="{
  &quot;sub&quot;: &quot;1234567890&quot;,
  &quot;name&quot;: &quot;John Doe&quot;,
  &quot;iat&quot;: 1516239022
}"
              ></textarea>
            </div>
          </div>
        )}

        {auth.type === 'oauth2' && (
          <div>
            {['token', 'tokenUrl', 'clientId', 'clientSecret', 'scope'].map(field => (
              <div key={field} style={{marginBottom: 16}}>
                <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>
                  {field === 'token' ? 'Access Token' : field === 'tokenUrl' ? 'Token URL' : field === 'clientId' ? 'Client ID' : field === 'clientSecret' ? 'Client Secret' : 'Scope'}
                </label>
                <input type={field === 'clientSecret' ? 'password' : 'text'} style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}} value={(auth as any)[field] || ''} onChange={e => onChange({ ...auth, [field]: e.target.value })} />
              </div>
            ))}
          </div>
        )}

        {auth.type === 'oauth1' && (
          <div>
            {['consumerKey', 'consumerSecret', 'token', 'tokenSecret', 'signatureMethod', 'timestamp', 'nonce', 'version', 'realm'].map(field => (
              <div key={field} style={{marginBottom: 16}}>
                <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>{field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</label>
                <input type={field.toLowerCase().includes('secret') ? 'password' : 'text'} style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}} value={(auth as any)[field] || ''} onChange={e => onChange({ ...auth, [field]: e.target.value })} />
              </div>
            ))}
          </div>
        )}

        {auth.type === 'digest' && (
          <div>
            {['username', 'password', 'realm', 'nonce', 'algorithm', 'qop', 'nonceCount', 'clientNonce', 'opaque'].map(field => (
              <div key={field} style={{marginBottom: 16}}>
                <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>{field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</label>
                <input type={field === 'password' ? 'password' : 'text'} style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}} value={(auth as any)[field] || ''} onChange={e => onChange({ ...auth, [field]: e.target.value })} />
              </div>
            ))}
          </div>
        )}

        {auth.type === 'hawk' && (
          <div>
            {['hawkId', 'hawkKey', 'algorithm', 'user', 'nonce', 'ext', 'app', 'dlg', 'timestamp'].map(field => (
              <div key={field} style={{marginBottom: 16}}>
                <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>{field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</label>
                <input type={field === 'hawkKey' ? 'password' : 'text'} style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}} value={(auth as any)[field] || ''} onChange={e => onChange({ ...auth, [field]: e.target.value })} />
              </div>
            ))}
          </div>
        )}

        {auth.type === 'aws' && (
          <div>
            {['accessKey', 'secretKey', 'awsRegion', 'serviceName', 'sessionToken'].map(field => (
              <div key={field} style={{marginBottom: 16}}>
                <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>{field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</label>
                <input type={field === 'secretKey' ? 'password' : 'text'} style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}} value={(auth as any)[field] || ''} onChange={e => onChange({ ...auth, [field]: e.target.value })} />
              </div>
            ))}
          </div>
        )}

        {auth.type === 'ntlm' && (
          <div>
            {['username', 'password', 'domain', 'workstation'].map(field => (
              <div key={field} style={{marginBottom: 16}}>
                <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>{field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</label>
                <input type={field === 'password' ? 'password' : 'text'} style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}} value={(auth as any)[field] || ''} onChange={e => onChange({ ...auth, [field]: e.target.value })} />
              </div>
            ))}
          </div>
        )}

        {auth.type === 'akamai' && (
          <div>
            {['accessToken', 'clientToken', 'clientSecret', 'baseUrl'].map(field => (
              <div key={field} style={{marginBottom: 16}}>
                <label style={{display: 'block', marginBottom: 4, fontWeight: 'bold'}}>{field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</label>
                <input type={field === 'clientSecret' ? 'password' : 'text'} style={{width: '100%', padding: 8, border: '1px solid var(--border)', borderRadius: 4}} value={(auth as any)[field] || ''} onChange={e => onChange({ ...auth, [field]: e.target.value })} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
