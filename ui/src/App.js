import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';
import EntityMergeUI from './components/EntityMergeUI';
import AgentMergeUI from './components/AgentMergeUI';

function App() {
  const [activeTab, setActiveTab] = useState('rules');
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchRules = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('http://localhost:8080/api/rules');
      setRules(response.data);
    } catch (err) {
      console.error('Error fetching rules:', err);
      setError(err.message || 'Failed to fetch rules');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  return (
    <div className="App">
      <header className="App-header">
        <h1>MDM MCP POC - Master Data Management</h1>
        <p>Entity Management and Rule Engine System</p>
      </header>

      <nav className="tab-navigation">
        <button 
          className={`tab-button ${activeTab === 'rules' ? 'active' : ''}`}
          onClick={() => setActiveTab('rules')}
        >
          📋 Rules Management
        </button>
        <button 
          className={`tab-button ${activeTab === 'entity-merge' ? 'active' : ''}`}
          onClick={() => setActiveTab('entity-merge')}
        >
          🔄 Entity Merge
        </button>
        <button 
          className={`tab-button ${activeTab === 'agent-merge' ? 'active' : ''}`}
          onClick={() => setActiveTab('agent-merge')}
        >
          🤖 Agent-Based Merge
        </button>
      </nav>

      <main className="tab-content">
        {activeTab === 'rules' && (
          <div className="rules-management">
            <h2>Rules Management</h2>
            <div className="rules-controls">
              <button onClick={fetchRules} disabled={loading} className="load-rules-btn">
                {loading ? 'Loading...' : 'Load Rules'}
              </button>
            </div>
            
            {error && (
              <div className="error-message">
                <h3>Error</h3>
                <p>{error}</p>
              </div>
            )}
            
            {loading && <div className="loading">Loading rules...</div>}
            
            {!loading && !error && (
              <div className="rules-list">
                <h3>Current Rules ({rules.length})</h3>
                {rules.length === 0 ? (
                  <p>No rules found. Use the "Load Rules" button to fetch rules from the server.</p>
                ) : (
                  <ul>
                    {rules.map((rule, index) => (
                      <li key={index} className="rule-item">
                        <strong>Rule ID:</strong> {rule.ruleId}<br />
                        <strong>Domain:</strong> {rule.domain}<br />
                        <strong>Condition:</strong> {rule.condition}<br />
                        <strong>Action:</strong> {rule.action}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>
        )}

        {activeTab === 'entity-merge' && (
          <EntityMergeUI />
        )}

        {activeTab === 'agent-merge' && (
          <AgentMergeUI />
        )}
      </main>

      <footer className="App-footer">
        <p>MDM MCP POC - Master Data Management System</p>
        <p>Built with Spring Boot, React, and Drools</p>
      </footer>
    </div>
  );
}

export default App;