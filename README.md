# Build Maven projects
mvn clean package -DskipTests

# Start all services
docker-compose up --build

## Entity Merge & Drools-based Entity Merging API

The MCP server now supports Reltio-style entity matching and merging using Drools rules. The following endpoints are available:

### Load Sample Data

Load 3 sample PERSON entities (2 are likely matches, 1 is unique):

```
curl -X POST http://localhost:8080/entity-merge/sample-data
```

### Find Match Candidates

Find potential duplicate pairs based on Drools rules:

```
curl http://localhost:8080/entity-merge/match-candidates
```

### Bulk Merge All Entities

Run the merge engine on all entities and return merge results:

```
curl -X POST http://localhost:8080/entity-merge/merge-all
```

### Merge Two Entities by ID

Merge two specific entities and return the result:

```
curl -X POST "http://localhost:8080/entity-merge/merge?entityId1=E1&entityId2=E2"
```

### API Summary

- `POST /entity-merge/sample-data` — Loads sample data for testing
- `GET /entity-merge/match-candidates` — Returns match candidates
- `POST /entity-merge/merge-all` — Bulk merge
- `POST /entity-merge/merge?entityId1=E1&entityId2=E2` — Merge two entities

### Testing

1. Load sample data
2. Find match candidates
3. Merge all or merge two entities

The Drools rules are in `mcp-server/src/main/resources/rules/` and can be customized for your business logic.

### Testing Tools

#### Postman Collection

A Postman collection is provided for easy API testing:

1. Import `entity_merge_postman_collection.json` into Postman
2. The collection includes all entity merge endpoints
3. Set the `baseUrl` variable to `http://localhost:8080` in your environment
4. Run the requests in sequence: Load Sample Data → Find Match Candidates → Merge All Entities

#### React UI

A React-based UI is available for testing the entity merge functionality:

1. Navigate to the `ui` directory
2. Install dependencies: `npm install`
3. Start the development server: `npm start`
4. Open `http://localhost:3000` in your browser
5. Use the "Entity Merge" tab to test the API endpoints
6. The UI provides buttons for all operations and displays results in a formatted JSON view

The React UI includes:
- Load Sample Data button
- Find Match Candidates button  
- Merge All Entities button
- Merge Specific Entities with input fields for entity IDs
- Real-time result display with JSON formatting
- Error handling and loading states