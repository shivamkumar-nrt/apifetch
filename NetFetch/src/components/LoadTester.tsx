import { useState } from "react";
import { invoke } from "@tauri-apps/api/core";
import { Tab, ResponseData } from "../types";

export function LoadTester({ tab, envVarsJson, onClose }: { tab: Tab, envVarsJson: string | null, onClose: () => void }) {
  const [requests, setRequests] = useState(10);
  const [concurrency, setConcurrency] = useState(2);
  const [isRunning, setIsRunning] = useState(false);
  const [results, setResults] = useState<{success: number, fails: number, totalTime: number, responses: ResponseData[]}>({
    success: 0, fails: 0, totalTime: 0, responses: []
  });
  
  const runTest = async () => {
    setIsRunning(true);
    setResults({success: 0, fails: 0, totalTime: 0, responses: []});
    
    const startTime = Date.now();
    let currentReq = 0;
    
    // Simple concurrency implementation on the frontend
    const workers = Array(concurrency).fill(0).map(async () => {
      while (currentReq < requests) {
        currentReq++;
        try {
          const response = await invoke<ResponseData>("send_request", {
            method: tab.method,
            url: tab.url,
            headers: JSON.stringify(tab.headers.filter(h => h.enabled && h.key)),
            params: JSON.stringify(tab.params.filter(p => p.enabled && p.key)),
            body: JSON.stringify(tab.body),
            auth: JSON.stringify(tab.auth),
            settings: tab.settings,
            envVarsJson
          });
          
          setResults(r => ({
            ...r, 
            success: response.status >= 200 && response.status < 400 ? r.success + 1 : r.success,
            fails: response.status >= 400 || response.status === 0 ? r.fails + 1 : r.fails,
            responses: [...r.responses, response]
          }));
        } catch (e) {
          setResults(r => ({...r, fails: r.fails + 1}));
        }
      }
    });
    
    await Promise.all(workers);
    
    setResults(r => ({...r, totalTime: Date.now() - startTime}));
    setIsRunning(false);
  };
  
  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
      backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000,
      display: 'flex', alignItems: 'center', justifyContent: 'center'
    }}>
      <div style={{
        backgroundColor: 'var(--bg-light)', padding: 20, width: 600, height: 400,
        borderRadius: 8, display: 'flex', flexDirection: 'column'
      }}>
        <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: 16}}>
          <h2>Load Testing: {tab.name}</h2>
          <button onClick={onClose}>Close</button>
        </div>
        
        <div style={{display: 'flex', gap: 16, marginBottom: 16}}>
          <label>
            Number of Requests: <br/>
            <input type="number" value={requests} onChange={e => setRequests(Number(e.target.value))} disabled={isRunning} />
          </label>
          <label>
            Concurrency: <br/>
            <input type="number" value={concurrency} onChange={e => setConcurrency(Number(e.target.value))} disabled={isRunning} />
          </label>
          <div style={{display: 'flex', alignItems: 'flex-end'}}>
            <button className="send-btn" onClick={runTest} disabled={isRunning}>
              {isRunning ? 'Running...' : 'Run Test'}
            </button>
          </div>
        </div>
        
        <div style={{flex: 1, backgroundColor: 'var(--bg-gray)', padding: 16, borderRadius: 8}}>
          <h3>Results</h3>
          <p>Successful: <span style={{color: 'var(--get)', fontWeight: 'bold'}}>{results.success}</span></p>
          <p>Failed: <span style={{color: 'var(--delete)', fontWeight: 'bold'}}>{results.fails}</span></p>
          <p>Total Time: {results.totalTime} ms</p>
          {results.responses.length > 0 && (
            <p>Average Latency: {Math.round(results.responses.reduce((sum, r) => sum + r.time, 0) / results.responses.length)} ms</p>
          )}
          
          <div style={{marginTop: 16, border: '1px solid var(--border)', height: 20, borderRadius: 10, overflow: 'hidden', display: 'flex'}}>
            <div style={{width: `${(results.success / requests) * 100}%`, backgroundColor: 'var(--get)'}}></div>
            <div style={{width: `${(results.fails / requests) * 100}%`, backgroundColor: 'var(--delete)'}}></div>
          </div>
        </div>
      </div>
    </div>
  );
}
