import React, { useState } from 'react';
import axios from 'axios';

function App() {
  const [rules, setRules] = useState([]);

  const fetchRules = async () => {
    const response = await axios.get('http://localhost:8080/rules/LifeSciences');
    setRules(response.data);
  };

  return (
    <div>
      <h1>MCP Steward Dashboard</h1>
      <button onClick={fetchRules}>Load Rules</button>
      <ul>
        {rules.map(rule => (
          <li key={rule.ruleId}>{rule.condition} → {rule.action}</li>
        ))}
      </ul>
    </div>
  );
}

export default App;