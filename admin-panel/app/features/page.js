'use client';

import Link from 'next/link';
import Image from 'next/image';

export default function FeaturesPage() {
  const features = [
    { icon: '🔥', title: 'REST API Client', desc: 'Full-featured HTTP client with environment variables, authentication helpers (OAuth2, API Key, Bearer), pre-request scripts, and test assertions.', tag: 'Core' },
    { icon: '🔌', title: 'WebSocket Client', desc: 'Real-time bidirectional communication testing with message history, binary frame support, and automatic reconnection handling.', tag: 'Protocol' },
    { icon: '📡', title: 'GraphQL Client', desc: 'Schema explorer, query autocomplete, variable editor, and subscription support for full GraphQL workflow coverage.', tag: 'Protocol' },
    { icon: '⚙️', title: 'gRPC Client', desc: 'Import .proto files, call unary and streaming methods, inspect response payloads with structured field views.', tag: 'Protocol' },
    { icon: '📁', title: 'Collections & Folders', desc: 'Organize endpoints into nested collections and folders. Sync across workspace members with real-time collaborative editing.', tag: 'Workspace' },
    { icon: '🌍', title: 'Environment Manager', desc: 'Define and switch between dev, staging, and production environment configs with variable scoping and secret masking.', tag: 'Workspace' },
    { icon: '🤖', title: 'AI-Powered Test Gen', desc: 'Auto-generate test suites from API responses. The AI engine analyzes payload schemas and creates edge case coverage.', tag: 'AI' },
    { icon: '📈', title: 'Load Testing Engine', desc: 'Simulate thousands of concurrent virtual users with configurable ramp-up curves, think times, and custom load profiles.', tag: 'Performance' },
    { icon: '📊', title: 'Analytics Dashboard', desc: 'Real-time latency charts, throughput graphs, error rate trends, and p95/p99 percentile breakdowns across all endpoints.', tag: 'Monitoring' },
    { icon: '🔔', title: 'Alert System', desc: 'Set SLA thresholds on latency, error rate, and availability. Get Slack, email, or webhook alerts when conditions breach.', tag: 'Monitoring' },
    { icon: '🧪', title: 'Mock Server Engine', desc: 'Define dynamic mock responses with custom status codes, latency delays, and conditional response logic for offline development.', tag: 'Testing' },
    { icon: '🔒', title: 'Enterprise RBAC', desc: 'Granular role-based access control: DEVELOPER, ADMIN, and MASTER_ADMIN roles with per-feature permission matrices.', tag: 'Enterprise' },
    { icon: '🏢', title: 'Multi-Workspace', desc: 'Isolate projects into PERSONAL, TEAM, or ENTERPRISE workspaces with separate member rosters and permission scoping.', tag: 'Enterprise' },
    { icon: '🔑', title: 'License Management', desc: 'Generate and distribute PRO/TEAM/ENTERPRISE license codes with device activation limits and expiry tracking.', tag: 'Enterprise' },
    { icon: '📤', title: 'Postman Migration', desc: 'One-click import of existing Postman v2.1 collections, environments, and Newman test scripts without any manual remapping.', tag: 'Migration' },
    { icon: '🗂️', title: 'API Documentation', desc: 'Auto-generate beautiful, interactive API docs from your collections. Publish to a shareable URL or export as OpenAPI spec.', tag: 'Docs' },
  ];

  const tagColors = {
    Core: 'badge-info', Protocol: 'badge-purple', Workspace: 'badge-success',
    AI: 'badge-post', Performance: 'badge-error', Monitoring: 'badge-get',
    Testing: 'badge-success', Enterprise: 'badge-purple', Migration: 'badge-post',
    Docs: 'badge-info',
  };

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-base)' }}>
      {/* NAVBAR */}
      <nav className="navbar">
        <Link href="/" className="navbar-brand">
          <Image src="/logo.png" alt="APIForge Logo" width={36} height={36} />
          <span className="navbar-brand-name">APIForge Studio</span>
        </Link>
        <div className="navbar-links">
          <Link href="/features" className="navbar-link active">Features</Link>
          <Link href="/login" className="navbar-link">Sign In</Link>
          <Link href="/signup" className="btn btn-primary btn-sm">Get Started Free</Link>
        </div>
      </nav>

      {/* HERO */}
      <section className="features-hero">
        <span className="hero-eyebrow" style={{ display: 'inline-flex' }}>
          🧩 Complete Feature Set
        </span>
        <h1 style={{ marginBottom: '1rem', marginTop: '1rem' }}>
          Everything you need to build{' '}
          <span className="hero-title-gradient">great APIs</span>
        </h1>
        <p>
          APIForge Studio ships with 16+ powerful modules across testing, performance, monitoring, and enterprise management — all included in one platform.
        </p>
      </section>

      {/* FEATURES GRID */}
      <section className="features-grid-section">
        {/* Filter pills */}
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '2.5rem' }}>
          {['All', 'Core', 'Protocol', 'Workspace', 'AI', 'Performance', 'Monitoring', 'Testing', 'Enterprise'].map(tag => (
            <span key={tag} className={`badge ${tag === 'All' ? 'badge-get' : tagColors[tag] || 'badge-info'}`}
                  style={{ padding: '6px 14px', fontSize: '0.78rem', cursor: 'pointer', userSelect: 'none' }}>
              {tag}
            </span>
          ))}
        </div>

        <div className="features-grid">
          {features.map((f, i) => (
            <div key={i} className="feature-card animate-in" style={{ animationDelay: `${i * 30}ms` }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
                <div className="feature-icon-box">{f.icon}</div>
                <span className={`badge ${tagColors[f.tag] || 'badge-info'}`}>{f.tag}</span>
              </div>
              <h3>{f.title}</h3>
              <p>{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* BOTTOM CTA */}
      <section style={{ background: 'var(--bg-surface)', borderTop: '1px solid var(--border)', padding: '5rem 5%', textAlign: 'center' }}>
        <h2 style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-primary)', margin: '0 0 1rem 0', letterSpacing: '-0.03em' }}>
          Ready to get started?
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>Download the Desktop Client or sign into the Admin Portal.</p>
        <div style={{ display: 'flex', gap: '12px', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link href="/login" className="btn btn-primary btn-lg">Access Admin Portal →</Link>
          <Link href="/" className="btn btn-secondary btn-lg">Download Desktop Client</Link>
        </div>
      </section>

      {/* FOOTER */}
      <footer style={{ background: 'var(--text-primary)', color: 'rgba(255,255,255,0.6)', padding: '2.5rem 5%', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.87rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Image src="/logo.png" alt="APIForge Logo" width={24} height={24} />
          <span style={{ color: 'white', fontWeight: 700 }}>APIForge Studio</span>
          <span>© 2026</span>
        </div>
        <div style={{ display: 'flex', gap: '24px' }}>
          <Link href="/" style={{ color: 'rgba(255,255,255,0.6)', textDecoration: 'none' }}>Home</Link>
          <Link href="/login" style={{ color: 'rgba(255,255,255,0.6)', textDecoration: 'none' }}>Admin Portal</Link>
        </div>
      </footer>
    </div>
  );
}
