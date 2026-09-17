'use client';

import { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const router = useRouter();
  const [form, setForm] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPass, setShowPass] = useState(false);

  const API_BASE = 'http://localhost:8082';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const resp = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: form.username, password: form.password })
      });
      if (resp.ok) {
        const data = await resp.json();
        if (data.role === 'DEVELOPER') {
          setError('Access denied. Only ADMIN or MASTER_ADMIN roles can access this portal.');
          setLoading(false);
          return;
        }
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('userRole', data.role);
        localStorage.setItem('userName', data.username);
        router.push('/dashboard');
      } else {
        setError('Invalid username or password. Please try again.');
      }
    } catch {
      setError('Could not connect to the backend server. Make sure Spring Boot is running on port 8080.');
    }
    setLoading(false);
  };

  return (
    <div className="auth-page">
      {/* LEFT PANEL */}
      <div className="auth-left">
        <div className="auth-left-content">
          <Image src="/logo.png" alt="APIForge Logo" width={80} height={80} className="auth-left-logo" style={{ margin: '0 auto 1.5rem', display: 'block', filter: 'drop-shadow(0 4px 12px rgba(0,0,0,0.2))' }} />
          <h2>APIForge Studio</h2>
          <p>The enterprise API development platform that replaces all your disconnected tools.</p>

          <div className="auth-feature-list">
            {[
              ['📊', 'Real-time API Analytics Dashboard'],
              ['👥', 'User & Role Management System'],
              ['💼', 'Multi-Workspace Collaboration'],
              ['🔑', 'License & Subscription Control'],
              ['⚙️', 'Dynamic Mock Server Engine'],
            ].map(([icon, label], i) => (
              <div className="auth-feature-item" key={i}>
                <div className="auth-feature-item-icon">{icon}</div>
                <span>{label}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* RIGHT PANEL */}
      <div className="auth-right">
        <div className="auth-form-container">
          <div className="auth-form-header">
            <h1>Operator Sign In</h1>
            <p>Sign in to access the APIForge Operator Portal.</p>
          </div>

          {error && (
            <div style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', borderRadius: 'var(--radius-md)', padding: '12px 16px', fontSize: '0.88rem', marginBottom: '1rem', fontWeight: 500 }}>
              ⚠ {error}
            </div>
          )}

          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="field">
              <label>Username</label>
              <input
                type="text"
                placeholder="Enter your username"
                required
                value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })}
                autoComplete="username"
              />
            </div>
            <div className="field">
              <label>Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type={showPass ? 'text' : 'password'}
                  placeholder="Enter your password"
                  required
                  value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  autoComplete="current-password"
                  style={{ width: '100%', paddingRight: '48px' }}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', padding: '4px', fontSize: '1rem' }}
                >
                  {showPass ? '🙈' : '👁'}
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn-primary" disabled={loading}
                    style={{ width: '100%', padding: '13px', fontSize: '0.95rem', marginTop: '4px' }}>
              {loading ? '⏳ Signing in…' : 'Sign In to Portal'}
            </button>
          </form>

          <div className="auth-footer-text">
            Don't have an account?{' '}
            <Link href="/signup">Create Account</Link>
          </div>

          <div style={{ marginTop: '2rem', padding: '1rem', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border)' }}>
            <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'center', fontWeight: 600 }}>
              🔒 Admin Portal access requires ADMIN or MASTER_ADMIN role
            </p>
          </div>

          <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
            <Link href="/" style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
              ← Back to Home
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
