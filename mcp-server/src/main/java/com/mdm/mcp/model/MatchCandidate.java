package com.mdm.mcp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchCandidate {
    
    private DataEntity entity1;
    private DataEntity entity2;
    private Double confidenceScore;
    private String matchReason;
    private String ruleName;
    
    public MatchCandidate(DataEntity entity1, DataEntity entity2, Double confidenceScore, String matchReason) {
        this.entity1 = entity1;
        this.entity2 = entity2;
        this.confidenceScore = confidenceScore;
        this.matchReason = matchReason;
    }
} 