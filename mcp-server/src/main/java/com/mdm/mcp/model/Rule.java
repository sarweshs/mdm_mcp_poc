package com.mdm.mcp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Rule {
    @Id
    private String ruleId;
    private String domain;
    private String condition;
    private String action;
}