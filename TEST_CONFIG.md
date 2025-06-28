# 1. Add a Merge Rule
```bash
curl -X POST http://localhost:8080/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "LS_DRUG_MERGE",
    "domain": "LifeSciences",
    "condition": "drugName similarity > 0.9",
    "action": "AUTO_MERGE"
  }'
```

# Trigger a BoT
```bash
curl -X POST http://localhost:8080/bot/execute \
  -H "Content-Type: application/json" \
  -d '{
    "botType": "LifeSciencesBot",
    "input": "Aspirin vs Ibuprofen"
  }'

```
# 3. Slack Bot (Local Testing with Ngrok)

Install Ngrok:

```bash
ngrok http 3000
Update Slack App’s Event Subscriptions to use the Ngrok URL (e.g., https://abc123.ngrok.io/slack/events).
```

# Step 5: Sample Outputs
1. MCP Server Logs
POST /rules - 200 OK
GET /bot/execute - 200 OK
  Response: "Checked interactions for Aspirin vs Ibuprofen. Context: HIGH_RISK"

# 2. PostgreSQL Audit Logs
SELECT * FROM audit_logs;
id	action	user_id	timestamp
1	RULE_ADDED	SYSTEM	2024-02-20 10:00:00


# Troubleshooting
Elasticsearch not responding?

Wait 60s after docker-compose up (Elasticsearch takes time to boot).

Check logs: docker logs reltio-mcp-poc-elasticsearch-1.

Slack bot not receiving events?

Verify Ngrok URL in Slack App settings.

Use app.command for slash commands, app.event for mentions.