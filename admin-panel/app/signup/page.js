'use client';

import { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { useRouter } from 'next/navigation';

export default function SignupPage() {
  const router = useRouter();
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [success, setSuccess] = useState(false);

  const API_BASE = 'http://localhost:8082';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    setLoading(true);
    try {
      const resp = await fetch(`${API_BASE}/api/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: form.username, email: form.email, password: form.password })
      });
      if (resp.ok) {
        setSuccess(true);
        setTimeout(() => router.push('/login'), 2500);
      } else {
        const text = await resp.text();
        setError(text || 'Registration failed. Please try again.');
      }
    } catch {
      setError('Could not connect to the backend server.');
    }
    setLoading(false);
  };

  const strength = form.password.length === 0 ? 0
    : form.password.length < 6 ? 1
    : form.password.length < 10 ? 2
    : 3;

  const strengthLabels = ['', 'Weak', 'Fair', 'Strong'];
  const strengthColors = ['', '#dc2626', '#d97706', '#059669'];

  return (
    <div className="auth-page" style={{ flexDirection: 'row-reverse' }}>
      {/* RIGHT DECORATIVE PANEL */}
      <div className="auth-left" style={{ background: 'linear-gradient(135deg, #064e3b 0%, #059669 40%, #0d9488 100%)' }}>
        <div className="auth-left-content">
          <Image src="/logo.png" alt="APIForge Logo" width={80} height={80} className="auth-left-logo" style={{ margin: '0 auto 1.5rem', display: 'block', filter: 'drop-shadow(0 4px 12px rgba(0,0,0,0.2))' }} />
          <h2>Join APIForge Studio</h2>
          <p>Create your account and start building, testing, and monitoring APIs like never before.</p>

          <div className="auth-feature-list">
            {[
              ['🚀', 'Instant access after registration'],
              ['🔒', 'Secure JWT-based authentication'],
              ['💡', 'AI-powered test generation'],
              ['📈', 'Load test up to 10K req/sec'],
              ['🌐', 'REST, GraphQL, gRPC & WebSocket'],
            ].map(([icon, label], i) => (
              <div className="auth-feature-item" key={i}>
                <div className="auth-feature-item-icon">{icon}</div>
                <span>{label}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* LEFT FORM PANEL */}
      <div className="auth-right">
        <div className="auth-form-container">
          {success ? (
            <div style={{ textAlign: 'center', padding: '2rem 0' }}>
              <div style={{ fontSize: '3.5rem', marginBottom: '1rem' }}>🎉</div>
              <h2 style={{ fontWeight: 800, color: 'var(--text-primary)', margin: '0 0 0.75rem' }}>Account Created!</h2>
              <p style={{ color: 'var(--text-secondary)' }}>Redirecting you to Sign In…</p>
            </div>
          ) : (
            <>
              <div className="auth-form-header">
                <h1>Create Account</h1>
                <p>Register a new APIForge Studio account to get started.</p>
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
                    placeholder="Choose a username"
                    required
                    value={form.username}
                    onChange={e => setForm({ ...form, username: e.target.value })}
                    autoComplete="username"
                    minLength={3}
                  />
                </div>
                <div className="field">
                  <label>Email Address</label>
                  <input
                    type="email"
                    placeholder="your@email.com"
                    required
                    value={form.email}
                    onChange={e => setForm({ ...form, email: e.target.value })}
                    autoComplete="email"
                  />
                </div>
                <div className="field">
                  <label>Password</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type={showPass ? 'text' : 'password'}
                      placeholder="Create a password"
                      required
                      value={form.password}
                      onChange={e => setForm({ ...form, password: e.target.value })}
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
                  {/* Password strength indicator */}
                  {form.password.length > 0 && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '6px' }}>
                      <div style={{ flex: 1, height: '4px', background: 'var(--border)', borderRadius: '99px', overflow: 'hidden' }}>
                        <div style={{ width: `${(strength / 3) * 100}%`, height: '100%', background: strengthColors[strength], borderRadius: '99px', transition: 'all 0.3s ease' }} />
                      </div>
                      <span style={{ fontSize: '0.75rem', fontWeight: 700, color: strengthColors[strength] }}>{strengthLabels[strength]}</span>
                    </div>
                  )}
                </div>
                <div className="field">
                  <label>Confirm Password</label>
                  <input
                    type="password"
                    placeholder="Repeat your password"
                    required
                    value={form.confirmPassword}
                    onChange={e => setForm({ ...form, confirmPassword: e.target.value })}
                  />
                  {form.confirmPassword.length > 0 && form.password !== form.confirmPassword && (
                    <span style={{ fontSize: '0.78rem', color: 'var(--danger)', fontWeight: 600 }}>Passwords do not match</span>
                  )}
                </div>

                <button type="submit" className="btn btn-primary" disabled={loading || (form.confirmPassword.length > 0 && form.password !== form.confirmPassword)}
                        style={{ width: '100%', padding: '13px', fontSize: '0.95rem', marginTop: '4px', background: 'linear-gradient(135deg, #059669 0%, #0d9488 100%)', boxShadow: '0 4px 14px rgba(5,150,105,0.35)' }}>
                  {loading ? '⏳ Creating Account…' : 'Create Account'}
                </button>
              </form>

              <div className="auth-footer-text">
                Already have an account?{' '}
                <Link href="/login">Sign In</Link>
              </div>

              <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
                <Link href="/" style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                  ← Back to Home
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
