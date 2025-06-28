import React, { useState } from 'react';
import './EntityMergeUI.css';

const EntityMergeUI = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [entityId1, setEntityId1] = useState('CRM_001');
  const [entityId2, setEntityId2] = useState('ERP_001');

  const API_BASE = 'http://localhost:8080';

  const makeRequest = async (endpoint, method = 'GET', body = null) => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const options = {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
      };

      if (body) {
        options.body = JSON.stringify(body);
      }

      const response = await fetch(`${API_BASE}${endpoint}`, options);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadSampleData = () => {
    makeRequest('/api/entity-merge/load-sample-data', 'POST');
  };

  const findMatchCandidates = () => {
    makeRequest('/api/entity-merge/find-match-candidates');
  };

  const mergeAllEntities = () => {
    makeRequest('/api/entity-merge/bulk-merge', 'POST');
  };

  const mergeTwoEntities = () => {
    makeRequest(`/api/entity-merge/merge-entities?entityId1=${entityId1}&entityId2=${entityId2}`, 'POST');
  };

  const formatResult = (data) => {
    if (Array.isArray(data)) {
      return data.map((item, index) => (
        <div key={index} className="result-item">
          <pre>{JSON.stringify(item, null, 2)}</pre>
        </div>
      ));
    }
    return <pre>{JSON.stringify(data, null, 2)}</pre>;
  };

  return (
    <div className="entity-merge-ui">
      <h2>MDM Entity Merge Testing</h2>
      <p>Test the Reltio-style entity matching and merging using Drools rules</p>

      <div className="controls">
        <div className="button-group">
          <button 
            onClick={loadSampleData} 
            disabled={loading}
            className="btn btn-primary"
          >
            {loading ? 'Loading...' : 'Load Sample Data'}
          </button>
          
          <button 
            onClick={findMatchCandidates} 
            disabled={loading}
            className="btn btn-secondary"
          >
            {loading ? 'Finding...' : 'Find Match Candidates'}
          </button>
          
          <button 
            onClick={mergeAllEntities} 
            disabled={loading}
            className="btn btn-success"
          >
            {loading ? 'Merging...' : 'Merge All Entities'}
          </button>
        </div>

        <div className="merge-specific">
          <h3>Merge Specific Entities</h3>
          <div className="input-group">
            <input
              type="text"
              value={entityId1}
              onChange={(e) => setEntityId1(e.target.value)}
              placeholder="Entity ID 1"
              className="input"
            />
            <input
              type="text"
              value={entityId2}
              onChange={(e) => setEntityId2(e.target.value)}
              placeholder="Entity ID 2"
              className="input"
            />
            <button 
              onClick={mergeTwoEntities} 
              disabled={loading}
              className="btn btn-warning"
            >
              {loading ? 'Merging...' : 'Merge Entities'}
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div className="error">
          <h3>Error</h3>
          <p>{error}</p>
        </div>
      )}

      {result && (
        <div className="result">
          <h3>Result</h3>
          <div className="result-content">
            {formatResult(result)}
          </div>
        </div>
      )}

      <div className="info">
        <h3>API Endpoints</h3>
        <ul>
          <li><strong>POST /api/entity-merge/load-sample-data</strong> - Load sample data</li>
          <li><strong>GET /api/entity-merge/find-match-candidates</strong> - Find match candidates</li>
          <li><strong>POST /api/entity-merge/bulk-merge</strong> - Bulk merge all entities</li>
          <li><strong>POST /api/entity-merge/merge-entities?entityId1=X&entityId2=Y</strong> - Merge specific entities</li>
        </ul>
      </div>
    </div>
  );
};

export default EntityMergeUI; 