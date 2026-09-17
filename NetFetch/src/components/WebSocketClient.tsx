import { useState, useRef } from "react";

export function WebSocketClient() {
  const [url, setUrl] = useState("ws://localhost:8080");
  const [messages, setMessages] = useState<{type: 'sent'|'received'|'info', text: string}[]>([]);
  const [input, setInput] = useState("");
  const [isConnected, setIsConnected] = useState(false);
  
  const wsRef = useRef<WebSocket | null>(null);

  const connect = () => {
    try {
      const ws = new WebSocket(url);
      wsRef.current = ws;
      
      ws.onopen = () => {
        setIsConnected(true);
        setMessages(m => [...m, {type: 'info', text: 'Connected to ' + url}]);
      };
      
      ws.onmessage = (e) => {
        setMessages(m => [...m, {type: 'received', text: e.data}]);
      };
      
      ws.onclose = () => {
        setIsConnected(false);
        setMessages(m => [...m, {type: 'info', text: 'Disconnected'}]);
      };
      
      ws.onerror = () => {
        setMessages(m => [...m, {type: 'info', text: 'Error connecting'}]);
      };
    } catch(e) {
      setMessages(m => [...m, {type: 'info', text: String(e)}]);
    }
  };

  const disconnect = () => {
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
  };

  const send = () => {
    if (wsRef.current && isConnected) {
      wsRef.current.send(input);
      setMessages(m => [...m, {type: 'sent', text: input}]);
      setInput("");
    }
  };

  return (
    <div style={{display: 'flex', flexDirection: 'column', height: '100%', padding: 16}}>
      <div style={{display: 'flex', gap: 8, marginBottom: 16}}>
        <input 
          type="text" 
          className="url-input" 
          value={url} 
          onChange={e => setUrl(e.target.value)} 
          placeholder="ws://..."
          disabled={isConnected}
        />
        {isConnected ? (
          <button className="send-btn" style={{backgroundColor: '#e74c3c'}} onClick={disconnect}>Disconnect</button>
        ) : (
          <button className="send-btn" onClick={connect}>Connect</button>
        )}
      </div>
      
      <div style={{flex: 1, border: '1px solid var(--border)', overflowY: 'auto', marginBottom: 16, padding: 8, display: 'flex', flexDirection: 'column', gap: 8}}>
        {messages.map((msg, i) => (
          <div key={i} style={{
            alignSelf: msg.type === 'sent' ? 'flex-end' : (msg.type === 'received' ? 'flex-start' : 'center'),
            backgroundColor: msg.type === 'sent' ? 'var(--primary)' : (msg.type === 'received' ? '#444' : 'transparent'),
            color: msg.type === 'info' ? 'var(--text-light)' : 'white',
            padding: '8px 12px',
            borderRadius: 8,
            maxWidth: '80%'
          }}>
            {msg.text}
          </div>
        ))}
      </div>
      
      <div style={{display: 'flex', gap: 8}}>
        <input 
          type="text" 
          className="url-input" 
          value={input} 
          onChange={e => setInput(e.target.value)} 
          onKeyDown={e => e.key === 'Enter' && send()}
          disabled={!isConnected}
          placeholder="Message to send..."
        />
        <button className="send-btn" onClick={send} disabled={!isConnected}>Send</button>
      </div>
    </div>
  );
}
