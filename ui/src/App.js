import React, { useState } from 'react';
import axios from 'axios';

function App() {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchRules = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('http://localhost:8080/rules');
      setRules(response.data);
    } catch (err) {
      console.error('Error fetching rules:', err);
      setError(err.message || 'Failed to fetch rules');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>MCP Steward Dashboard</h1>
      <button 
        onClick={fetchRules} 
        disabled={loading}
        style={{ 
          padding: '10px 20px', 
          fontSize: '16px',
          backgroundColor: loading ? '#ccc' : '#007bff',
          color: 'white',
          border: 'none',
          borderRadius: '5px',
          cursor: loading ? 'not-allowed' : 'pointer'
        }}
      >
        {loading ? 'Loading...' : 'Load Rules'}
      </button>
      
      {error && (
        <div style={{ 
          color: 'red', 
          marginTop: '10px',
          padding: '10px',
          backgroundColor: '#ffe6e6',
          borderRadius: '5px'
        }}>
          Error: {error}
        </div>
      )}
      
      {rules.length > 0 && (
        <div style={{ marginTop: '20px' }}>
          <h2>Rules ({rules.length})</h2>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {rules.map(rule => (
              <li key={rule.ruleId} style={{ 
                padding: '10px', 
                margin: '5px 0', 
                backgroundColor: '#f8f9fa',
                borderRadius: '5px',
                border: '1px solid #dee2e6'
              }}>
                <strong>ID:</strong> {rule.ruleId} | 
                <strong>Domain:</strong> {rule.domain} | 
                <strong>Condition:</strong> {rule.condition} → 
                <strong>Action:</strong> {rule.action}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default App;