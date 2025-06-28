import React, { useState } from 'react';
import './AgentMergeUI.css';

const AgentMergeUI = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [agentStatus, setAgentStatus] = useState(null);

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
      const data = await response.json();

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      setResult(data);
      return data;
    } catch (err) {
      console.error('Error:', err);
      setError(err.message || 'Failed to make request');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const loadSampleData = async () => {
    try {
      await makeRequest('/api/agent-merge/load-sample-data', 'POST');
    } catch (err) {
      // Error already handled in makeRequest
    }
  };

  const getAgentStatus = async () => {
    try {
      const data = await makeRequest('/api/agent-merge/agent-status');
      setAgentStatus(data);
    } catch (err) {
      // Error already handled in makeRequest
    }
  };

  const findMatches = async () => {
    try {
      await makeRequest('/api/agent-merge/find-matches');
    } catch (err) {
      // Error already handled in makeRequest
    }
  };

  const bulkMerge = async () => {
    try {
      await makeRequest('/api/agent-merge/bulk-merge', 'POST');
    } catch (err) {
      // Error already handled in makeRequest
    }
  };

  const executeCompleteWorkflow = async () => {
    try {
      await makeRequest('/api/agent-merge/complete-workflow', 'POST');
    } catch (err) {
      // Error already handled in makeRequest
    }
  };

  const getAuditLogs = async () => {
    try {
      const data = await makeRequest('/api/agent-merge/audit-logs?limit=20');
      setAuditLogs(data.auditLogs || []);
    } catch (err) {
      // Error already handled in makeRequest
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleString();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'SUCCESS':
        return 'green';
      case 'FAILED':
        return 'red';
      case 'PENDING':
        return 'orange';
      default:
        return 'gray';
    }
  };

  return (
    <div className="agent-merge-ui">
      <h2>🤖 Agent-Based Entity Merge System</h2>
      <p className="description">
        Intelligent agents automatically find merge candidates and perform entity merging with comprehensive audit logging.
      </p>

      <div className="button-grid">
        <button onClick={loadSampleData} disabled={loading} className="btn btn-primary">
          📊 Load Sample Data
        </button>
        
        <button onClick={getAgentStatus} disabled={loading} className="btn btn-info">
          🔍 Check Agent Status
        </button>
        
        <button onClick={findMatches} disabled={loading} className="btn btn-success">
          🎯 Find Matches
        </button>
        
        <button onClick={bulkMerge} disabled={loading} className="btn btn-warning">
          🔄 Bulk Merge
        </button>
        
        <button onClick={executeCompleteWorkflow} disabled={loading} className="btn btn-danger">
          🚀 Complete Workflow
        </button>
        
        <button onClick={getAuditLogs} disabled={loading} className="btn btn-secondary">
          📋 View Audit Logs
        </button>
      </div>

      {loading && (
        <div className="loading">
          <div className="spinner"></div>
          <p>Processing...</p>
        </div>
      )}

      {error && (
        <div className="error">
          <h3>❌ Error</h3>
          <p>{error}</p>
        </div>
      )}

      {agentStatus && (
        <div className="result-section">
          <h3>🤖 Agent Status</h3>
          <div className="agent-status-grid">
            <div className="agent-category">
              <h4>Matching Agents</h4>
              <p>Total: {agentStatus.matchingAgents?.totalAgents || 0}</p>
              <p>Enabled: {agentStatus.matchingAgents?.enabledAgents || 0}</p>
              {agentStatus.matchingAgents?.agents?.map((agent, index) => (
                <div key={index} className="agent-info">
                  <strong>{agent.agentId}</strong> - {agent.enabled ? '✅' : '❌'}
                  <br />
                  <small>Threshold: {agent.confidenceThreshold}</small>
                </div>
              ))}
            </div>
            
            <div className="agent-category">
              <h4>Merge Agents</h4>
              <p>Total: {agentStatus.mergeAgents?.totalAgents || 0}</p>
              <p>Enabled: {agentStatus.mergeAgents?.enabledAgents || 0}</p>
              {agentStatus.mergeAgents?.agents?.map((agent, index) => (
                <div key={index} className="agent-info">
                  <strong>{agent.agentId}</strong> - {agent.enabled ? '✅' : '❌'}
                  <br />
                  <small>Strategy: {agent.mergeStrategy}</small>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {result && (
        <div className="result-section">
          <h3>📊 Results</h3>
          <div className="result-content">
            <pre>{JSON.stringify(result, null, 2)}</pre>
          </div>
        </div>
      )}

      {auditLogs.length > 0 && (
        <div className="result-section">
          <h3>📋 Recent Audit Logs</h3>
          <div className="audit-logs">
            {auditLogs.map((log, index) => (
              <div key={index} className="audit-log-entry">
                <div className="log-header">
                  <span className={`status-badge status-${getStatusColor(log.status)}`}>
                    {log.status}
                  </span>
                  <span className="timestamp">{formatTimestamp(log.createdAt)}</span>
                </div>
                <div className="log-details">
                  <p><strong>Operation:</strong> {log.operationType}</p>
                  <p><strong>Agent:</strong> {log.agentId} ({log.agentType})</p>
                  <p><strong>Entities:</strong> {log.entityIds || 'N/A'}</p>
                  <p><strong>Reason:</strong> {log.decisionReason || 'N/A'}</p>
                  {log.confidenceScore && (
                    <p><strong>Confidence:</strong> {log.confidenceScore.toFixed(2)}</p>
                  )}
                  {log.executionTimeMs && (
                    <p><strong>Execution Time:</strong> {log.executionTimeMs}ms</p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="api-info">
        <h3>🔗 Available API Endpoints</h3>
        <ul>
          <li><code>GET /api/agent-merge/health</code> - Service health check</li>
          <li><code>GET /api/agent-merge/agent-status</code> - Get agent status</li>
          <li><code>POST /api/agent-merge/load-sample-data</code> - Load sample entities</li>
          <li><code>GET /api/agent-merge/find-matches</code> - Find matches using agents</li>
          <li><code>POST /api/agent-merge/bulk-merge</code> - Perform bulk merge</li>
          <li><code>POST /api/agent-merge/complete-workflow</code> - Execute complete workflow</li>
          <li><code>GET /api/agent-merge/audit-logs</code> - Get recent audit logs</li>
          <li><code>GET /api/agent-merge/audit-logs/entity/{entityId}</code> - Get entity audit logs</li>
          <li><code>GET /api/agent-merge/audit-logs/agent/{agentId}</code> - Get agent audit logs</li>
        </ul>
      </div>
    </div>
  );
};

export default AgentMergeUI; 