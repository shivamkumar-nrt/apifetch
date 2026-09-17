'use client';

import Link from 'next/link';
import Image from 'next/image';

export default function HomePage() {
  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-base)' }}>
      {/* NAVBAR */}
      <nav className="navbar">
        <Link href="/" className="navbar-brand">
          <Image src="/logo.png" alt="APIForge Logo" width={36} height={36} />
          <span className="navbar-brand-name">APIForge Studio</span>
        </Link>
        <div className="navbar-links">
          <Link href="/features" className="navbar-link">Features</Link>
          <Link href="/login" className="navbar-link">Sign In</Link>
          <Link href="/signup" className="btn btn-primary btn-sm">Get Started Free</Link>
        </div>
      </nav>

      {/* HERO SECTION */}
      <section className="home-hero">
        <div className="hero-content animate-in">
          <div className="hero-eyebrow">
            <span>🚀</span> Enterprise API Platform v1.0
          </div>
          <Image src="/logo.png" alt="APIForge" width={100} height={100} className="hero-logo" style={{ display: 'block', margin: '0 auto 2rem' }} />
          <h1 className="hero-title">
            The API Platform That <br />
            <span className="hero-title-gradient">Does It All</span>
          </h1>
          <p className="hero-subtitle">
            Replace Postman, JMeter, Swagger UI, and your monitoring stack with one powerful platform. Build, test, document, and monitor APIs — all from a single workspace.
          </p>
          <div className="hero-actions">
            <Link href="/login" className="btn btn-primary btn-lg">
              Access Operator Portal →
            </Link>
            <Link href="/features" className="btn btn-secondary btn-lg">
              Explore Features
            </Link>
          </div>

          <div className="hero-stats">
            <div>
              <div className="hero-stat-value">50+</div>
              <div className="hero-stat-label">API Protocols</div>
            </div>
            <div style={{ borderLeft: '1px solid var(--border)', paddingLeft: '3rem' }}>
              <div className="hero-stat-value">10K+</div>
              <div className="hero-stat-label">Requests/sec</div>
            </div>
            <div style={{ borderLeft: '1px solid var(--border)', paddingLeft: '3rem' }}>
              <div className="hero-stat-value">99.9%</div>
              <div className="hero-stat-label">Uptime SLA</div>
            </div>
          </div>
        </div>
      </section>

      {/* WHY APIFORGE SECTION */}
      <section style={{ padding: '6rem 5%', background: 'var(--bg-surface)', borderTop: '1px solid var(--border)' }}>
        <div style={{ maxWidth: '1100px', margin: '0 auto' }}>
          <div style={{ textAlign: 'center', marginBottom: '4rem' }}>
            <h2 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.03em', color: 'var(--text-primary)', margin: '0 0 1rem 0' }}>
              Why teams choose APIForge Studio
            </h2>
            <p style={{ fontSize: '1rem', color: 'var(--text-secondary)', maxWidth: '540px', margin: '0 auto' }}>
              A unified workspace that your entire engineering team can collaborate in.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '2rem' }}>
            {[
              { icon: '⚡', title: 'Blazing Fast Execution', text: 'Execute thousands of API requests per second with our optimized engine built on Spring Boot and JavaFX.' },
              { icon: '🔒', title: 'Enterprise-Grade Security', text: 'JWT authentication, RBAC roles, workspace-level access control, and audit trails for full compliance.' },
              { icon: '📊', title: 'Real-time Analytics', text: 'Monitor live API health, latency percentiles, error rates, and developer activity from a central dashboard.' },
              { icon: '🤖', title: 'AI-Powered Testing', text: 'Generate test suites automatically, detect anomalies in response patterns, and get fix suggestions instantly.' },
              { icon: '🌐', title: 'Multi-Protocol Support', text: 'REST, GraphQL, gRPC, WebSocket, SSE — handle every API protocol from one unified client.' },
              { icon: '🧩', title: 'Postman Import', text: 'Migrate your entire Postman collections, environments and test scripts with a single import click.' },
            ].map((item, i) => (
              <div key={i} className="feature-card">
                <div className="feature-icon-box">{item.icon}</div>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA SECTION */}
      <section style={{ padding: '6rem 5%', background: 'linear-gradient(135deg, #312e81 0%, #4f46e5 50%, #7c3aed 100%)', textAlign: 'center' }}>
        <div style={{ maxWidth: '700px', margin: '0 auto' }}>
          <h2 style={{ fontSize: '2.2rem', fontWeight: 800, color: 'white', letterSpacing: '-0.03em', margin: '0 0 1rem 0' }}>
            Ready to unify your API workflow?
          </h2>
          <p style={{ fontSize: '1.05rem', color: 'rgba(255,255,255,0.8)', marginBottom: '2.5rem' }}>
            Start with the Desktop Client for free. No credit card required.
          </p>
          <div style={{ display: 'flex', gap: '14px', justifyContent: 'center', flexWrap: 'wrap' }}>
            <button className="btn btn-lg" style={{ background: 'white', color: 'var(--primary)', fontWeight: 800 }}>
              Download for Windows
            </button>
            <button className="btn btn-lg" style={{ background: 'rgba(255,255,255,0.15)', color: 'white', border: '1.5px solid rgba(255,255,255,0.3)' }}>
              Download for macOS
            </button>
          </div>
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
          <Link href="/features" style={{ color: 'rgba(255,255,255,0.6)', textDecoration: 'none' }}>Features</Link>
          <Link href="/login" style={{ color: 'rgba(255,255,255,0.6)', textDecoration: 'none' }}>Admin Portal</Link>
        </div>
      </footer>
    </div>
  );
}
