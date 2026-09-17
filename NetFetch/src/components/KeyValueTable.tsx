import { KeyVal } from "../types";

export function KeyValueTable({ 
  items, 
  onChange 
}: { 
  items: KeyVal[], 
  onChange: (items: KeyVal[]) => void 
}) {
  
  const updateItem = (index: number, updates: Partial<KeyVal>) => {
    const newItems = [...items];
    newItems[index] = { ...newItems[index], ...updates };
    
    // Auto add row if last row is being typed in
    if (index === items.length - 1 && (updates.key || updates.value)) {
      newItems.push({ id: Math.random().toString(), key: "", value: "", enabled: true });
    }
    
    onChange(newItems);
  };

  const removeItem = (index: number) => {
    if (index === items.length - 1) return; // don't remove last empty row
    const newItems = [...items];
    newItems.splice(index, 1);
    onChange(newItems);
  };

  // Ensure there's always at least one empty row
  if (items.length === 0 || items[items.length - 1].key !== "" || items[items.length - 1].value !== "") {
    items = [...items, { id: Math.random().toString(), key: "", value: "", enabled: true }];
  }

  return (
    <table className="kv-table">
      <thead>
        <tr>
          <th style={{width: 40}}></th>
          <th>Key</th>
          <th>Value</th>
          <th style={{width: 80}}>
            <button style={{fontSize: 11, padding: '2px 6px', background: 'var(--bg-gray)', border: '1px solid var(--border)', borderRadius: 3, cursor: 'pointer'}}>Bulk Edit</button>
          </th>
        </tr>
      </thead>
      <tbody>
        {items.map((item, i) => (
          <tr key={item.id} className="kv-row">
            <td style={{textAlign: 'center'}}>
              <input 
                type="checkbox" 
                checked={item.enabled} 
                onChange={(e) => updateItem(i, { enabled: e.target.checked })}
              />
            </td>
            <td>
              <input 
                type="text" 
                placeholder="Key" 
                value={item.key} 
                onChange={(e) => updateItem(i, { key: e.target.value })}
              />
            </td>
            <td>
              <input 
                type="text" 
                placeholder="Value" 
                value={item.value} 
                onChange={(e) => updateItem(i, { value: e.target.value })}
              />
            </td>
            <td>
              {i !== items.length - 1 && (
                <button className="delete-btn" onClick={() => removeItem(i)}>×</button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
