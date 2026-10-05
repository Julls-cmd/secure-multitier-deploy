import { useEffect, useState } from 'react';
/**
 * Main application component.
 * Checks the backend health endpoint on mount and displays its status.
 * @returns {JSX.Element} The infrastructure demo page with the health badge.
 */
export default function App() {
  const [status, setStatus] = useState('checking...');
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch('/api/health')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data) => setStatus(data.status))
      .catch((err) => setError(err.message));
  }, []);

  return (
    <main className="app">
      <h1>Infrastructure Demo</h1>
      <section className="card">
        <span className="label">Backend health</span>
        {error ? (
          <span className="badge badge-down">DOWN ({error})</span>
        ) : (
          <span className={`badge ${status === 'UP' ? 'badge-up' : ''}`}>{status}</span>
        )}
      </section>
    </main>
  );
}
