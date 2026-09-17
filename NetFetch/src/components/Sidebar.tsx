import { useState } from "react";
import { HistoryEntry, CollectionItem } from "../types";

function CollectionNode({ item, onAddFolder, onAddRequest, toggleExpand }: { item: CollectionItem, onAddFolder: (id: string) => void, onAddRequest: (id: string) => void, toggleExpand: (id: string) => void }) {
  const [showMenu, setShowMenu] = useState(false);

  return (
    <div style={{ marginLeft: item.type === 'collection' ? 0 : 16, marginBottom: 4 }}>
      <div 
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', padding: '4px 0', fontSize: 13 }}
        onMouseEnter={() => setShowMenu(true)}
        onMouseLeave={() => setShowMenu(false)}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }} onClick={() => toggleExpand(item.id)}>
          {item.type !== 'request' && (
            <span style={{ fontSize: 10, width: 12, display: 'inline-block' }}>{item.expanded ? '▼' : '▶'}</span>
          )}
          {item.type === 'request' && item.method && (
            <span className={`method-${item.method}`} style={{ fontSize: 10, minWidth: 35 }}>{item.method}</span>
          )}
          <span style={{ fontWeight: item.type === 'collection' ? 'bold' : 'normal' }}>
            {item.name}
          </span>
        </div>
        {showMenu && item.type !== 'request' && (
          <div style={{ display: 'flex', gap: 4 }}>
            <span style={{ cursor: 'pointer', fontSize: 14, color: 'var(--primary)' }} title="New Folder" onClick={(e) => { e.stopPropagation(); onAddFolder(item.id); }}>📁</span>
            <span style={{ cursor: 'pointer', fontSize: 14, color: 'var(--primary)' }} title="New Request" onClick={(e) => { e.stopPropagation(); onAddRequest(item.id); }}>+</span>
          </div>
        )}
      </div>
      {item.expanded && item.children && item.children.map(child => (
        <CollectionNode key={child.id} item={child} onAddFolder={onAddFolder} onAddRequest={onAddRequest} toggleExpand={toggleExpand} />
      ))}
    </div>
  );
}

export function Sidebar({ history, collections, setCollections, onSelectHistory }: { history: HistoryEntry[], collections: CollectionItem[], setCollections: (c: CollectionItem[]) => void, onSelectHistory: (h: HistoryEntry) => void }) {
  const [tab, setTab] = useState<'collections' | 'history'>('collections');
  const [search, setSearch] = useState('');

  const handleNewCollection = () => {
    const name = window.prompt("Collection Name:");
    if (name) {
      setCollections([...collections, { id: Math.random().toString(), name, type: 'collection', children: [], expanded: true }]);
    }
  };

  const updateCollectionTree = (nodes: CollectionItem[], targetId: string, updater: (node: CollectionItem) => CollectionItem): CollectionItem[] => {
    return nodes.map(node => {
      if (node.id === targetId) {
        return updater(node);
      }
      if (node.children) {
        return { ...node, children: updateCollectionTree(node.children, targetId, updater) };
      }
      return node;
    });
  };

  const onAddFolder = (parentId: string) => {
    const name = window.prompt("Folder Name:");
    if (name) {
      setCollections(updateCollectionTree(collections, parentId, node => ({
        ...node,
        expanded: true,
        children: [...(node.children || []), { id: Math.random().toString(), name, type: 'folder', children: [], expanded: true }]
      })));
    }
  };

  const onAddRequest = (parentId: string) => {
    const name = window.prompt("Request Name:");
    if (name) {
      setCollections(updateCollectionTree(collections, parentId, node => ({
        ...node,
        expanded: true,
        children: [...(node.children || []), { id: Math.random().toString(), name, type: 'request', method: 'GET' }]
      })));
    }
  };

  const toggleExpand = (id: string) => {
    setCollections(updateCollectionTree(collections, id, node => ({ ...node, expanded: !node.expanded })));
  };

  return (
    <div className="sidebar">
      {tab === 'collections' && (
        <div style={{ display: 'flex', gap: 8, padding: '8px 16px', borderBottom: '1px solid var(--border)' }}>
          <button onClick={handleNewCollection} style={{ flex: 1, padding: '4px', border: '1px solid var(--border)', borderRadius: 4, background: 'none', cursor: 'pointer' }}>New</button>
          <button style={{ flex: 1, padding: '4px', border: '1px solid var(--border)', borderRadius: 4, background: 'none', cursor: 'pointer' }}>Import</button>
        </div>
      )}
      
      <div className="sidebar-tabs">
        <div 
          className={`sidebar-tab ${tab === 'collections' ? 'active' : ''}`}
          onClick={() => setTab('collections')}
        >
          Collections
        </div>
        <div 
          className={`sidebar-tab ${tab === 'history' ? 'active' : ''}`}
          onClick={() => setTab('history')}
        >
          History
        </div>
      </div>
      
      {tab === 'collections' && (
        <div style={{ padding: '8px 16px', borderBottom: '1px solid var(--border)' }}>
          <input 
            type="text" 
            placeholder="Search collections..." 
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ width: '100%', padding: '6px', border: '1px solid var(--border)', borderRadius: 4, background: 'var(--bg-gray)' }}
          />
        </div>
      )}

      <div className="sidebar-content">
        {tab === 'collections' && (
          <div>
            {collections.length === 0 ? (
              <div style={{fontSize: 13, color: 'var(--text-light)', textAlign: 'center', marginTop: 20}}>No collections yet.</div>
            ) : (
              collections.map(c => (
                <CollectionNode 
                  key={c.id} 
                  item={c} 
                  onAddFolder={onAddFolder} 
                  onAddRequest={onAddRequest} 
                  toggleExpand={toggleExpand} 
                />
              ))
            )}
          </div>
        )}
        {tab === 'history' && (
          <div>
            {history.map(h => (
              <div key={h.id} className="history-item" onClick={() => onSelectHistory(h)}>
                <span className={`history-method method-${h.method}`}>{h.method}</span>
                {h.url}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
