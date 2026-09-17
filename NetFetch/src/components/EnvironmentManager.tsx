import { useState } from "react";
import { KeyVal } from "../types";

export interface Environment {
  id: string;
  name: string;
  variables: KeyVal[];
}

interface EnvManagerProps {
  environments: Environment[];
  activeEnvId: string | null;
  onEnvironmentsChange: (envs: Environment[]) => void;
  onActiveEnvChange: (id: string | null) => void;
  onClose: () => void;
}

export function EnvironmentManager({ environments, activeEnvId, onEnvironmentsChange, onActiveEnvChange, onClose }: EnvManagerProps) {
  const [selectedId, setSelectedId] = useState<string | null>(environments.length > 0 ? environments[0].id : null);
  
  const selectedEnv = environments.find(e => e.id === selectedId);

  const addEnv = () => {
    const newEnv: Environment = {
      id: Math.random().toString(36).substr(2, 9),
      name: "New Environment",
      variables: []
    };
    onEnvironmentsChange([...environments, newEnv]);
    setSelectedId(newEnv.id);
  };

  const updateSelectedEnv = (updates: Partial<Environment>) => {
    if (!selectedId) return;
    onEnvironmentsChange(environments.map(e => e.id === selectedId ? { ...e, ...updates } : e));
  };

  const deleteEnv = (id: string) => {
    onEnvironmentsChange(environments.filter(e => e.id !== id));
    if (selectedId === id) setSelectedId(null);
    if (activeEnvId === id) onActiveEnvChange(null);
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
      backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000,
      display: 'flex', alignItems: 'center', justifyContent: 'center'
    }}>
      <div style={{
        backgroundColor: 'var(--bg-light)', padding: 20, width: 800, height: 600,
        borderRadius: 8, display: 'flex', flexDirection: 'column'
      }}>
        <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: 16}}>
          <h2>Manage Environments</h2>
          <button onClick={onClose}>Close</button>
        </div>
        
        <div style={{display: 'flex', flex: 1, gap: 16, overflow: 'hidden'}}>
          <div style={{width: 250, display: 'flex', flexDirection: 'column', borderRight: '1px solid var(--border)', paddingRight: 16}}>
            {environments.map(env => (
              <div 
                key={env.id} 
                onClick={() => setSelectedId(env.id)}
                style={{
                  padding: 8, 
                  backgroundColor: selectedId === env.id ? 'var(--bg-gray)' : 'transparent',
                  cursor: 'pointer',
                  display: 'flex', justifyContent: 'space-between'
                }}
              >
                <span>{env.name}</span>
                <button onClick={(e) => { e.stopPropagation(); deleteEnv(env.id); }}>x</button>
              </div>
            ))}
            <button onClick={addEnv} style={{marginTop: 8}}>+ Add Environment</button>
          </div>
          
          <div style={{flex: 1, overflowY: 'auto'}}>
            {selectedEnv ? (
              <div>
                <input 
                  type="text" 
                  value={selectedEnv.name}
                  onChange={e => updateSelectedEnv({name: e.target.value})}
                  style={{fontSize: 18, marginBottom: 16, padding: 8, width: '100%'}}
                />
                
                <table className="kv-table">
                  <thead>
                    <tr>
                      <th style={{width: 40}}></th>
                      <th>Key</th>
                      <th>Value</th>
                      <th style={{width: 40}}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedEnv.variables.map(kv => (
                      <tr key={kv.id} className="kv-row">
                        <td style={{textAlign: 'center'}}>
                          <input 
                            type="checkbox" 
                            checked={kv.enabled} 
                            onChange={e => {
                              const newVars = selectedEnv.variables.map(v => v.id === kv.id ? {...v, enabled: e.target.checked} : v);
                              updateSelectedEnv({variables: newVars});
                            }}
                          />
                        </td>
                        <td>
                          <input 
                            type="text" 
                            value={kv.key}
                            onChange={e => {
                              const newVars = selectedEnv.variables.map(v => v.id === kv.id ? {...v, key: e.target.value} : v);
                              updateSelectedEnv({variables: newVars});
                            }}
                            placeholder="Key"
                          />
                        </td>
                        <td>
                          <input 
                            type="text" 
                            value={kv.value}
                            onChange={e => {
                              const newVars = selectedEnv.variables.map(v => v.id === kv.id ? {...v, value: e.target.value} : v);
                              updateSelectedEnv({variables: newVars});
                            }}
                            placeholder="Value"
                          />
                        </td>
                        <td style={{textAlign: 'center'}}>
                          <button className="delete-btn" onClick={() => {
                            updateSelectedEnv({variables: selectedEnv.variables.filter(v => v.id !== kv.id)});
                          }}>🗑</button>
                        </td>
                      </tr>
                    ))}
                    <tr className="kv-row">
                      <td style={{textAlign: 'center'}}><input type="checkbox" disabled /></td>
                      <td>
                        <input 
                          type="text" 
                          placeholder="New Key"
                          onBlur={e => {
                            if (e.target.value) {
                              updateSelectedEnv({
                                variables: [...selectedEnv.variables, {id: Math.random().toString(), key: e.target.value, value: '', enabled: true}]
                              });
                              e.target.value = '';
                            }
                          }}
                        />
                      </td>
                      <td><input type="text" placeholder="Value" readOnly /></td>
                      <td></td>
                    </tr>
                  </tbody>
                </table>
              </div>
            ) : (
              <div>Select an environment to edit</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
