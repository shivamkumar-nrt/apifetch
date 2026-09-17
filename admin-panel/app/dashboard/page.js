'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';

// ==========================================
// REUSABLE UI COMPONENTS
// ==========================================

function ToastStack({ toasts }) {
  return (
    <div className="toast-stack">
      {toasts.map(t => (
        <div key={t.id} className={`toast toast-${t.type}`}>
          <span className="toast-icon">{t.type === 'success' ? '✅' : '❌'}</span>
          <span>{t.message}</span>
        </div>
      ))}
    </div>
  );
}

function DataTable({ headers, rows }) {
  if (!rows || rows.length === 0) {
    return (
      <div style={{ padding: '3.5rem 2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        <div style={{ fontSize: '2rem', marginBottom: '0.75rem' }}>📭</div>
        <div style={{ fontWeight: 600 }}>No records found</div>
      </div>
    );
  }
  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>{headers.map((h, i) => <th key={i}>{h}</th>)}</tr>
        </thead>
        <tbody>
          {rows}
        </tbody>
      </table>
    </div>
  );
}

function Field({ label, ...props }) {
  return (
    <div className="field">
      {label && <label>{label}</label>}
      <input {...props} />
    </div>
  );
}

function Select({ label, options, ...props }) {
  return (
    <div className="field">
      {label && <label>{label}</label>}
      <select {...props}>
        {options.map((o, i) => <option key={i} value={o.value}>{o.label}</option>)}
      </select>
    </div>
  );
}

function Paginator({ page, total, size, onChange }) {
  const pages = Math.max(1, Math.ceil(total / size));
  return (
    <div className="pagination-bar">
      <span className="pagination-info">
        Page <strong>{page + 1}</strong> of <strong>{pages}</strong> — {total} total records
      </span>
      <div className="pagination-controls">
        <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => onChange(page - 1)}>← Prev</button>
        <button className="btn btn-secondary btn-sm" disabled={page >= pages - 1} onClick={() => onChange(page + 1)}>Next →</button>
      </div>
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal-box">
        <div className="modal-header">
          <h3 className="modal-title">{title}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

// ==========================================
// DASHBOARD PAGE
// ==========================================

const API_BASE = 'http://localhost:8082';
const PAGE_SIZE = 8;

const NAV_ITEMS = [
  { id: 'history', label: 'Activity Logs', icon: '📜' },
  { id: 'users_roles', label: 'User Directory', icon: '👥' },
  { id: 'workspaces', label: 'Workspaces', icon: '🏢' },
  { id: 'licenses', label: 'License Keys', icon: '🔑' },
  { id: 'mocks', label: 'Mock Engine', icon: '🎭' },
  { id: 'load_tests', label: 'Load Tests', icon: '🚀' },
  { id: 'monitoring', label: 'Monitoring', icon: '📊' },
];

export default function DashboardPage() {
  const router = useRouter();

  const [userRole, setUserRole] = useState('');
  const [userName, setUserName] = useState('');
  const [activeTab, setActiveTab] = useState('history');
  const [toasts, setToasts] = useState([]);

  // Module data
  const [history, setHistory] = useState([]);
  const [historyTotal, setHistoryTotal] = useState(0);
  const [historyPage, setHistoryPage] = useState(0);
  const [historySearch, setHistorySearch] = useState('');

  const [users, setUsers] = useState([]);
  const [usersTotal, setUsersTotal] = useState(0);
  const [usersPage, setUsersPage] = useState(0);
  const [usersSearch, setUsersSearch] = useState('');

  const [workspaces, setWorkspaces] = useState([]);
  const [wsTotal, setWsTotal] = useState(0);
  const [wsPage, setWsPage] = useState(0);
  const [wsSearch, setWsSearch] = useState('');

  const [licenses, setLicenses] = useState([]);
  const [licTotal, setLicTotal] = useState(0);
  const [licPage, setLicPage] = useState(0);
  const [licSearch, setLicSearch] = useState('');

  const [mocks, setMocks] = useState([]);
  const [mocksTotal, setMocksTotal] = useState(0);
  const [mocksPage, setMocksPage] = useState(0);
  const [mocksSearch, setMocksSearch] = useState('');

  const [loadTests, setLoadTests] = useState([]);
  const [monitorJobs, setMonitorJobs] = useState([]);
  const [monitorStatus, setMonitorStatus] = useState([]);

  // Modal state
  const [modal, setModal] = useState(null); // 'user' | 'workspace' | 'license' | 'mock' | 'monitor'
  const [userForm, setUserForm] = useState({ username: '', email: '', password: '', role: 'DEVELOPER' });
  const [wsForm, setWsForm] = useState({ name: '', description: '', type: 'PERSONAL' });
  const [licForm, setLicForm] = useState({ tier: 'PRO', maxDevices: 3 });
  const [mockForm, setMockForm] = useState({ method: 'GET', path: '/mock/v1/custom', responseBody: '{"ok":true}', statusCode: 200, delayMs: 100 });
  const [monitorForm, setMonitorForm] = useState({ name: 'Health Check', method: 'GET', url: 'https://api.example.com/health', intervalSeconds: 60, isActive: true });

  // Auth check
  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn') === 'true';
    const role = localStorage.getItem('userRole');
    const name = localStorage.getItem('userName') || 'Operator';
    if (!loggedIn || !role) {
      router.push('/login');
      return;
    }
    setUserRole(role);
    setUserName(name);
  }, []);

  // Data fetching
  useEffect(() => {
    if (!userRole) return;
    if (activeTab === 'history') fetchHistory();
    if (activeTab === 'users_roles') fetchUsers();
    if (activeTab === 'workspaces') fetchWorkspaces();
    if (activeTab === 'licenses') fetchLicenses();
    if (activeTab === 'mocks') fetchMocks();
    if (activeTab === 'load_tests') fetchLoadTests();
    if (activeTab === 'monitoring') {
      fetchMonitorJobs();
      fetchMonitorStatus();
    }
  }, [userRole, activeTab, historyPage, historySearch, usersPage, usersSearch, wsPage, wsSearch, licPage, licSearch, mocksPage, mocksSearch]);

  const toast = (message, type = 'success') => {
    const id = Date.now() + Math.random();
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 3500);
  };

  const logout = () => {
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userName');
    router.push('/login');
  };

  const apiFetch = async (url, opts = {}) => {
    const r = await fetch(url, opts);
    return r;
  };

  async function fetchHistory() {
    try {
      const r = await apiFetch(`${API_BASE}/api/history?page=${historyPage}&size=${PAGE_SIZE}&search=${historySearch}`);
      const d = await r.json();
      setHistory(d.content || []); setHistoryTotal(d.totalElements || 0);
    } catch { toast('Could not load activity logs.', 'error'); }
  }

  async function fetchUsers() {
    try {
      const r = await apiFetch(`${API_BASE}/api/admin/users?page=${usersPage}&size=${PAGE_SIZE}&search=${usersSearch}`);
      const d = await r.json();
      setUsers(d.content || []); setUsersTotal(d.totalElements || 0);
    } catch { toast('Could not load user directory.', 'error'); }
  }

  async function fetchWorkspaces() {
    try {
      const r = await apiFetch(`${API_BASE}/api/workspaces?page=${wsPage}&size=${PAGE_SIZE}&search=${wsSearch}`);
      const d = await r.json();
      setWorkspaces(d.content || []); setWsTotal(d.totalElements || 0);
    } catch { toast('Could not load workspaces.', 'error'); }
  }

  async function fetchLicenses() {
    try {
      const r = await apiFetch(`${API_BASE}/api/admin/licenses?page=${licPage}&size=${PAGE_SIZE}&search=${licSearch}`);
      const d = await r.json();
      setLicenses(d.content || []); setLicTotal(d.totalElements || 0);
    } catch { toast('Could not load licenses.', 'error'); }
  }

  async function fetchMocks() {
    try {
      const r = await apiFetch(`${API_BASE}/api/mocks?page=${mocksPage}&size=${PAGE_SIZE}&search=${mocksSearch}`);
      const d = await r.json();
      setMocks(d.content || []); setMocksTotal(d.totalElements || 0);
    } catch { toast('Could not load mock routes.', 'error'); }
  }

  async function fetchLoadTests() {
    try {
      const r = await apiFetch(`${API_BASE}/api/v1/load-test-results`);
      if (r.ok) setLoadTests(await r.json());
    } catch { toast('Could not load load tests.', 'error'); }
  }

  async function fetchMonitorJobs() {
    try {
      const r = await apiFetch(`${API_BASE}/api/v1/monitor-jobs`);
      if (r.ok) setMonitorJobs(await r.json());
    } catch { toast('Could not load monitor jobs.', 'error'); }
  }

  async function fetchMonitorStatus() {
    try {
      const r = await apiFetch(`${API_BASE}/api/v1/monitor-jobs/status`);
      if (r.ok) setMonitorStatus(await r.json());
    } catch { toast('Could not load monitor status.', 'error'); }
  }

  const createUser = async (e) => {
    e.preventDefault();
    try {
      const r = await apiFetch(`${API_BASE}/api/admin/users`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(userForm) });
      if (r.ok) { toast('User account registered.'); setModal(null); setUserForm({ username: '', email: '', password: '', role: 'DEVELOPER' }); fetchUsers(); }
      else { const t = await r.text(); toast(t || 'Failed to create user.', 'error'); }
    } catch { toast('Network error.', 'error'); }
  };

  const createWorkspace = async (e) => {
    e.preventDefault();
    try {
      const r = await apiFetch(`${API_BASE}/api/workspaces`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(wsForm) });
      if (r.ok) { toast('Workspace created.'); setModal(null); setWsForm({ name: '', description: '', type: 'PERSONAL' }); fetchWorkspaces(); }
      else toast('Failed to create workspace.', 'error');
    } catch { toast('Network error.', 'error'); }
  };

  const createMonitorJob = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        workspaceId: '00000000-0000-0000-0000-000000000001',
        name: monitorForm.name,
        url: monitorForm.url,
        method: monitorForm.method,
        intervalSeconds: parseInt(monitorForm.intervalSeconds),
        active: monitorForm.isActive
      };
      const r = await apiFetch(`${API_BASE}/api/v1/monitor-jobs`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
      if (r.ok) { toast('Monitor Job created.'); setModal(null); fetchMonitorJobs(); }
      else toast('Failed to create monitor job.', 'error');
    } catch { toast('Network error.', 'error'); }
  };

  const createLicense = async (e) => {
    e.preventDefault();
    try {
      const r = await apiFetch(`${API_BASE}/api/admin/licenses`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(licForm) });
      if (r.ok) { toast('License key generated.'); setModal(null); fetchLicenses(); }
      else toast('Failed to generate license.', 'error');
    } catch { toast('Network error.', 'error'); }
  };

  const createMock = async (e) => {
    e.preventDefault();
    try {
      const r = await apiFetch(`${API_BASE}/api/mocks`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(mockForm) });
      if (r.ok) { toast('Mock route registered.'); setModal(null); fetchMocks(); }
      else toast('Failed to register mock.', 'error');
    } catch { toast('Network error.', 'error'); }
  };

  const toggleUserStatus = async (userId, current) => {
    const next = current === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    try {
      await apiFetch(`${API_BASE}/api/admin/users/status`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId, status: next }) });
      toast(`User ${next.toLowerCase()}.`); fetchUsers();
    } catch { toast('Failed.', 'error'); }
  };

  const changeRole = async (userId, role) => {
    try {
      const r = await apiFetch(`${API_BASE}/api/admin/users/role`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId, role }) });
      if (r.ok) { toast('Role updated.'); fetchUsers(); }
      else { const t = await r.text(); toast(t, 'error'); }
    } catch { toast('Failed.', 'error'); }
  };

  const archiveWs = async (id, status) => {
    const next = status === 'ARCHIVED' ? 'ACTIVE' : 'ARCHIVED';
    try {
      await apiFetch(`${API_BASE}/api/workspaces/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: next }) });
      toast(`Workspace ${next.toLowerCase()}.`); fetchWorkspaces();
    } catch { toast('Failed.', 'error'); }
  };

  const deleteWs = async (id) => {
    if (!window.confirm('Soft-delete this workspace? Data is retained for 30 days.')) return;
    try {
      await apiFetch(`${API_BASE}/api/workspaces/${id}`, { method: 'DELETE' });
      toast('Workspace deleted.'); fetchWorkspaces();
    } catch { toast('Failed.', 'error'); }
  };

  const deleteMock = async (id) => {
    if (!window.confirm('Delete this mock endpoint?')) return;
    try {
      await apiFetch(`${API_BASE}/api/mocks/${id}`, { method: 'DELETE' });
      toast('Mock deleted.'); fetchMocks();
    } catch { toast('Failed.', 'error'); }
  };

  const activeNav = NAV_ITEMS.find(n => n.id === activeTab);

  return (
    <div className="portal-layout">
      {/* SIDEBAR */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <Image src="/logo.png" alt="APIForge Logo" width={38} height={38} />
          <span className="sidebar-logo-text">APIForge Studio</span>
        </div>
        <nav className="sidebar-nav">
          {NAV_ITEMS.map(item => (
            <button
              key={item.id}
              className={`sidebar-item ${activeTab === item.id ? 'active' : ''}`}
              onClick={() => setActiveTab(item.id)}
            >
              <span className="sidebar-item-icon">{item.icon}</span>
              {item.label}
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button className="btn btn-danger" style={{ width: '100%' }} onClick={logout}>
            Sign Out
          </button>
        </div>
      </aside>

      {/* MAIN CONTENT */}
      <div className="portal-main">
        <div className="portal-topbar">
          <h1 className="portal-topbar-title">{activeNav?.icon} {activeNav?.label}</h1>
          <div className="portal-topbar-right">
            <span className="role-chip">
              👤 {userName} · {userRole}
            </span>
          </div>
        </div>

        <div className="portal-body">

          {/* ── ACTIVITY LOGS ── */}
          {activeTab === 'history' && (
            <div className="module-panel animate-in">
              <div className="module-panel-header">
                <h2 className="module-panel-title">Recent API Activity Logs</h2>
                <div className="module-panel-actions">
                  <div className="field" style={{ margin: 0 }}>
                    <input type="text" placeholder="Search URL or method…" value={historySearch}
                      onChange={e => { setHistorySearch(e.target.value); setHistoryPage(0); }}
                      style={{ width: '240px' }} />
                  </div>
                </div>
              </div>
              <DataTable
                headers={['Timestamp', 'Method', 'URL', 'Response Time', 'Status Code']}
                rows={history.map(h => (
                  <tr key={h.id}>
                    <td>{new Date(h.timestamp).toLocaleString()}</td>
                    <td><span className={`badge badge-${(h.method || 'get').toLowerCase()}`}>{h.method || 'GET'}</span></td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.83rem', color: 'var(--text-primary)' }}>{h.url}</td>
                    <td>{h.responseTimeMs} ms</td>
                    <td><span className={`badge ${h.statusCode >= 200 && h.statusCode < 300 ? 'badge-success' : 'badge-error'}`}>{h.statusCode}</span></td>
                  </tr>
                ))}
              />
              <Paginator page={historyPage} total={historyTotal} size={PAGE_SIZE} onChange={setHistoryPage} />
            </div>
          )}

          {/* ── USER DIRECTORY ── */}
          {activeTab === 'users_roles' && (
            <div className="module-panel animate-in">
              <div className="module-panel-header">
                <h2 className="module-panel-title">SaaS User Directory</h2>
                <div className="module-panel-actions">
                  <div className="field" style={{ margin: 0 }}>
                    <input type="text" placeholder="Search users…" value={usersSearch}
                      onChange={e => { setUsersSearch(e.target.value); setUsersPage(0); }}
                      style={{ width: '200px' }} />
                  </div>
                  <button className="btn btn-primary btn-sm" onClick={() => setModal('user')}>+ Register User</button>
                </div>
              </div>
              <DataTable
                headers={['Username', 'Email', 'Role', 'Change Role', 'Status', 'Action']}
                rows={users.map(u => (
                  <tr key={u.id}>
                    <td style={{ fontWeight: 700, color: 'var(--text-primary)' }}>{u.username}</td>
                    <td>{u.email}</td>
                    <td><span className={`badge ${u.role === 'MASTER_ADMIN' || u.role === 'ADMIN' ? 'badge-success' : 'badge-info'}`}>{u.role}</span></td>
                    <td>
                      <select value={u.role} disabled={userRole !== 'MASTER_ADMIN' || u.username === 'masteradmin'}
                        onChange={e => changeRole(u.id, e.target.value)}
                        style={{ fontSize: '0.82rem', padding: '5px 8px' }}>
                        <option value="DEVELOPER">DEVELOPER</option>
                        <option value="ADMIN">ADMIN</option>
                        <option value="MASTER_ADMIN">MASTER_ADMIN</option>
                      </select>
                    </td>
                    <td><span className={`badge ${u.status === 'ACTIVE' ? 'badge-success' : 'badge-error'}`}>{u.status}</span></td>
                    <td>
                      <button className={`btn btn-sm ${u.status === 'ACTIVE' ? 'btn-danger' : 'btn-primary'}`}
                        style={u.status !== 'ACTIVE' ? { background: 'var(--success)' } : {}}
                        disabled={userRole !== 'MASTER_ADMIN' || u.username === 'masteradmin'}
                        onClick={() => toggleUserStatus(u.id, u.status)}>
                        {u.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              />
              <Paginator page={usersPage} total={usersTotal} size={PAGE_SIZE} onChange={setUsersPage} />
            </div>
          )}

          {/* ── WORKSPACES ── */}
          {activeTab === 'workspaces' && (
            <div className="module-panel animate-in">
              <div className="module-panel-header">
                <h2 className="module-panel-title">SaaS Active Workspaces</h2>
                <div className="module-panel-actions">
                  <div className="field" style={{ margin: 0 }}>
                    <input type="text" placeholder="Search workspaces…" value={wsSearch}
                      onChange={e => { setWsSearch(e.target.value); setWsPage(0); }}
                      style={{ width: '200px' }} />
                  </div>
                  <button className="btn btn-primary btn-sm" onClick={() => setModal('workspace')}>+ New Workspace</button>
                </div>
              </div>
              <DataTable
                headers={['Name', 'Type', 'Status', 'Actions']}
                rows={workspaces.map(ws => (
                  <tr key={ws.id}>
                    <td style={{ fontWeight: 700, color: 'var(--text-primary)' }}>{ws.name}</td>
                    <td><span className="badge badge-info">{ws.type}</span></td>
                    <td><span className={`badge ${ws.status === 'ACTIVE' ? 'badge-success' : 'badge-error'}`}>{ws.status}</span></td>
                    <td style={{ display: 'flex', gap: '6px' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => archiveWs(ws.id, ws.status)}>
                        {ws.status === 'ARCHIVED' ? 'Unarchive' : 'Archive'}
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={() => deleteWs(ws.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              />
              <Paginator page={wsPage} total={wsTotal} size={PAGE_SIZE} onChange={setWsPage} />
            </div>
          )}

          {/* ── LICENSES ── */}
          {activeTab === 'licenses' && (
            <div className="module-panel animate-in">
              <div className="module-panel-header">
                <h2 className="module-panel-title">Software License Keys</h2>
                <div className="module-panel-actions">
                  <div className="field" style={{ margin: 0 }}>
                    <input type="text" placeholder="Search license codes…" value={licSearch}
                      onChange={e => { setLicSearch(e.target.value); setLicPage(0); }}
                      style={{ width: '200px' }} />
                  </div>
                  <button className="btn btn-primary btn-sm" onClick={() => setModal('license')}>+ Generate Key</button>
                </div>
              </div>
              <DataTable
                headers={['License Code', 'Tier', 'Activations', 'Expiry Date', 'Status']}
                rows={licenses.map(l => (
                  <tr key={l.id}>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.82rem', fontWeight: 700, color: 'var(--text-primary)' }}>{l.licenseCode}</td>
                    <td><span className="badge badge-purple">{l.tier}</span></td>
                    <td>{l.activatedDevices} / {l.maxDevices}</td>
                    <td>{l.expiryDate}</td>
                    <td><span className={`badge ${l.active ? 'badge-success' : 'badge-error'}`}>{l.active ? 'ACTIVE' : 'EXPIRED'}</span></td>
                  </tr>
                ))}
              />
              <Paginator page={licPage} total={licTotal} size={PAGE_SIZE} onChange={setLicPage} />
            </div>
          )}

          {/* ── MOCK SERVER ── */}
          {activeTab === 'mocks' && (
            <div className="module-panel animate-in">
              <div className="module-panel-header">
                <h2 className="module-panel-title">Mock Server Endpoints</h2>
                <div className="module-panel-actions">
                  <div className="field" style={{ margin: 0 }}>
                    <input type="text" placeholder="Search paths…" value={mocksSearch}
                      onChange={e => { setMocksSearch(e.target.value); setMocksPage(0); }}
                      style={{ width: '200px' }} />
                  </div>
                  <button className="btn btn-primary btn-sm" onClick={() => setModal('mock')}>+ Create Mock</button>
                </div>
              </div>
              <DataTable
                headers={['Method', 'Path', 'Status', 'Delay', 'Hits', 'Actions']}
                rows={mocks.map(m => (
                  <tr key={m.id}>
                    <td><span className={`badge badge-${(m.method || 'get').toLowerCase()}`}>{m.method}</span></td>
                    <td style={{ fontWeight: 700, fontFamily: 'monospace' }}>{m.path}</td>
                    <td>{m.statusCode}</td>
                    <td>{m.delayMs} ms</td>
                    <td><span className="badge badge-info">{m.hits || 0}</span></td>
                    <td>
                      <button className="btn btn-danger btn-sm" onClick={() => deleteMock(m.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              />
              <Paginator page={mocksPage} total={mocksTotal} size={PAGE_SIZE} onChange={setMocksPage} />
            </div>
          )}

          {/* ── LOAD TESTS ── */}
          {activeTab === 'load_tests' && (
            <div className="module-panel animate-in">
              <div className="module-panel-header">
                <h2 className="module-panel-title">Load Test Results</h2>
              </div>
              <DataTable
                headers={['Method', 'URL', 'Users', 'Requests', 'Success', 'Avg Latency', 'P99 Latency', 'TPS']}
                rows={loadTests.map(lt => (
                  <tr key={lt.id}>
                    <td><span className={`badge badge-${(lt.method || 'get').toLowerCase()}`}>{lt.method}</span></td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{lt.url}</td>
                    <td>{lt.concurrentUsers}</td>
                    <td>{lt.totalRequests}</td>
                    <td><span className="badge badge-success">{lt.successCount}</span></td>
                    <td>{lt.avgLatencyMs ? lt.avgLatencyMs.toFixed(2) : '0'} ms</td>
                    <td>{lt.p99Ms} ms</td>
                    <td style={{ fontWeight: 'bold' }}>{lt.tps ? lt.tps.toFixed(2) : '0'}</td>
                  </tr>
                ))}
              />
            </div>
          )}

          {/* ── MONITORING ── */}
          {activeTab === 'monitoring' && (
            <div className="module-panel animate-in">
              <div className="module-panel-header">
                <h2 className="module-panel-title">Endpoint Monitoring</h2>
                <div className="module-panel-actions">
                  <button className="btn btn-primary btn-sm" onClick={() => setModal('monitor')}>+ Create Job</button>
                </div>
              </div>
              
              <h3 style={{ margin: '1rem 0 0.5rem', fontSize: '1.2rem', color: 'var(--text-primary)' }}>Active Jobs</h3>
              <DataTable
                headers={['Name', 'Method', 'URL', 'Interval (s)', 'Status']}
                rows={monitorJobs.map(job => (
                  <tr key={job.id}>
                    <td style={{ fontWeight: 700 }}>{job.name}</td>
                    <td><span className={`badge badge-${(job.method || 'get').toLowerCase()}`}>{job.method}</span></td>
                    <td style={{ fontFamily: 'monospace' }}>{job.url}</td>
                    <td>{job.intervalSeconds} s</td>
                    <td><span className={`badge ${job.active ? 'badge-success' : 'badge-danger'}`}>{job.active ? 'ACTIVE' : 'DISABLED'}</span></td>
                  </tr>
                ))}
              />

              <h3 style={{ margin: '2rem 0 0.5rem', fontSize: '1.2rem', color: 'var(--text-primary)' }}>Recent Pings</h3>
              <DataTable
                headers={['Time', 'Job ID', 'Status', 'Code', 'Latency']}
                rows={monitorStatus.map(st => (
                  <tr key={st.id}>
                    <td>{new Date(st.checkedAt).toLocaleString()}</td>
                    <td style={{ fontSize: '0.8rem', opacity: 0.7 }}>{st.jobId}</td>
                    <td><span className={`badge ${st.up ? 'badge-success' : 'badge-error'}`}>{st.up ? 'UP' : 'DOWN'}</span></td>
                    <td>{st.statusCode}</td>
                    <td>{st.responseTimeMs} ms</td>
                  </tr>
                ))}
              />
            </div>
          )}

        </div>
      </div>

      {/* ── MODALS ── */}
      {modal === 'user' && (
        <Modal title="Register New User" onClose={() => setModal(null)}>
          <form onSubmit={createUser} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <Field label="Username" type="text" required value={userForm.username} onChange={e => setUserForm({ ...userForm, username: e.target.value })} />
            <Field label="Email Address" type="email" required value={userForm.email} onChange={e => setUserForm({ ...userForm, email: e.target.value })} />
            <Field label="Initial Password" type="password" required value={userForm.password} onChange={e => setUserForm({ ...userForm, password: e.target.value })} />
            <Select label="Assigned Role" value={userForm.role} onChange={e => setUserForm({ ...userForm, role: e.target.value })}
              options={[{ value: 'DEVELOPER', label: 'DEVELOPER' }, { value: 'ADMIN', label: 'ADMIN' }, { value: 'MASTER_ADMIN', label: 'MASTER_ADMIN' }]} />
            <button type="submit" className="btn btn-primary" style={{ marginTop: '6px' }}>Create User Account</button>
          </form>
        </Modal>
      )}

      {modal === 'workspace' && (
        <Modal title="Create New Workspace" onClose={() => setModal(null)}>
          <form onSubmit={createWorkspace} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <Field label="Workspace Name" type="text" required value={wsForm.name} onChange={e => setWsForm({ ...wsForm, name: e.target.value })} />
            <Field label="Description" type="text" value={wsForm.description} onChange={e => setWsForm({ ...wsForm, description: e.target.value })} />
            <Select label="Type" value={wsForm.type} onChange={e => setWsForm({ ...wsForm, type: e.target.value })}
              options={[{ value: 'PERSONAL', label: 'PERSONAL' }, { value: 'TEAM', label: 'TEAM' }, { value: 'ENTERPRISE', label: 'ENTERPRISE' }]} />
            <button type="submit" className="btn btn-primary" style={{ marginTop: '6px' }}>Create Workspace</button>
          </form>
        </Modal>
      )}

      {modal === 'license' && (
        <Modal title="Generate License Key" onClose={() => setModal(null)}>
          <form onSubmit={createLicense} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <Select label="Subscription Tier" value={licForm.tier} onChange={e => setLicForm({ ...licForm, tier: e.target.value })}
              options={[{ value: 'PRO', label: 'PRO Edition' }, { value: 'TEAM', label: 'TEAM Edition' }, { value: 'ENTERPRISE', label: 'ENTERPRISE Edition' }]} />
            <Field label="Max Device Activations" type="number" min={1} value={licForm.maxDevices} onChange={e => setLicForm({ ...licForm, maxDevices: parseInt(e.target.value) })} />
            <button type="submit" className="btn btn-primary" style={{ marginTop: '6px' }}>Generate License Key</button>
          </form>
        </Modal>
      )}

      {modal === 'mock' && (
        <Modal title="Register Mock Endpoint" onClose={() => setModal(null)}>
          <form onSubmit={createMock}>
            <Select label="HTTP Method" value={mockForm.method} onChange={e => setMockForm({ ...mockForm, method: e.target.value })} options={['GET', 'POST', 'PUT', 'DELETE']} />
            <Field label="Path" placeholder="/api/v1/mock" value={mockForm.path} onChange={e => setMockForm({ ...mockForm, path: e.target.value })} required />
            <div className="field">
              <label>Response Body (JSON)</label>
              <textarea rows={4} style={{ fontFamily: 'monospace', width: '100%' }} value={mockForm.responseBody} onChange={e => setMockForm({ ...mockForm, responseBody: e.target.value })} />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <Field label="Status Code" type="number" value={mockForm.statusCode} onChange={e => setMockForm({ ...mockForm, statusCode: e.target.value })} required />
              <Field label="Simulated Delay (ms)" type="number" value={mockForm.delayMs} onChange={e => setMockForm({ ...mockForm, delayMs: e.target.value })} required />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
              <button className="btn btn-primary" type="submit">Create Mock</button>
            </div>
          </form>
        </Modal>
      )}

      {/* Create Monitor Job Modal */}
      {modal === 'monitor' && (
        <Modal title="Create Monitor Job" onClose={() => setModal(null)}>
          <form onSubmit={createMonitorJob}>
            <Field label="Job Name" placeholder="Production Health Check" value={monitorForm.name} onChange={e => setMonitorForm({ ...monitorForm, name: e.target.value })} required />
            <Select label="HTTP Method" value={monitorForm.method} onChange={e => setMonitorForm({ ...monitorForm, method: e.target.value })} options={['GET', 'POST', 'PUT', 'DELETE', 'HEAD', 'OPTIONS']} />
            <Field label="Endpoint URL" placeholder="https://api.example.com/health" value={monitorForm.url} onChange={e => setMonitorForm({ ...monitorForm, url: e.target.value })} required />
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <Field label="Interval (Seconds)" type="number" value={monitorForm.intervalSeconds} onChange={e => setMonitorForm({ ...monitorForm, intervalSeconds: e.target.value })} min={10} required />
              <Select label="Status" value={monitorForm.isActive ? 'true' : 'false'} onChange={e => setMonitorForm({ ...monitorForm, isActive: e.target.value === 'true' })} options={['true', 'false']} />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
              <button className="btn btn-primary" type="submit">Create Job</button>
            </div>
          </form>
        </Modal>
      )}

      <ToastStack toasts={toasts} />
    </div>
  );
}
