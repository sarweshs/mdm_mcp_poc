package com.mdm.mcp.agent.impl;

import com.mdm.mcp.agent.MatchingAgent;
import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class IntelligentMatchingAgent implements MatchingAgent {
    
    private static final String AGENT_ID = "INTELLIGENT_MATCHING_AGENT";
    private static final String AGENT_TYPE = "MATCHING_AGENT";
    private static final double CONFIDENCE_THRESHOLD = 0.7;
    
    @Override
    public String getAgentId() {
        return AGENT_ID;
    }
    
    @Override
    public String getAgentType() {
        return AGENT_TYPE;
    }
    
    @Override
    public double getConfidenceThreshold() {
        return CONFIDENCE_THRESHOLD;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public List<MatchCandidate> findMatches(DataEntity entity, List<DataEntity> candidateEntities) {
        List<MatchCandidate> matches = new ArrayList<>();
        
        for (DataEntity candidate : candidateEntities) {
            if (entity.getId().equals(candidate.getId())) {
                continue; // Skip self
            }
            
            double confidence = calculateConfidence(entity, candidate);
            if (confidence >= CONFIDENCE_THRESHOLD) {
                MatchCandidate match = MatchCandidate.builder()
                    .entity1(entity)
                    .entity2(candidate)
                    .confidenceScore(confidence)
                    .matchReason(generateMatchReason(entity, candidate, confidence))
                    .ruleName("INTELLIGENT_MATCHING_RULE")
                    .build();
                matches.add(match);
            }
        }
        
        log.info("Agent {} found {} matches for entity {}", AGENT_ID, matches.size(), entity.getEntityId());
        return matches;
    }
    
    @Override
    public List<MatchCandidate> findAllMatches(List<DataEntity> entities) {
        List<MatchCandidate> allMatches = new ArrayList<>();
        
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                DataEntity entity1 = entities.get(i);
                DataEntity entity2 = entities.get(j);
                
                double confidence = calculateConfidence(entity1, entity2);
                if (confidence >= CONFIDENCE_THRESHOLD) {
                    MatchCandidate match = MatchCandidate.builder()
                        .entity1(entity1)
                        .entity2(entity2)
                        .confidenceScore(confidence)
                        .matchReason(generateMatchReason(entity1, entity2, confidence))
                        .ruleName("INTELLIGENT_MATCHING_RULE")
                        .build();
                    allMatches.add(match);
                }
            }
        }
        
        log.info("Agent {} found {} total matches among {} entities", AGENT_ID, allMatches.size(), entities.size());
        return allMatches;
    }
    
    @Override
    public double calculateConfidence(DataEntity entity1, DataEntity entity2) {
        double totalScore = 0.0;
        int criteriaCount = 0;
        
        // Email matching (highest weight)
        double emailScore = calculateEmailConfidence(entity1, entity2);
        if (emailScore > 0) {
            totalScore += emailScore * 0.4; // 40% weight
            criteriaCount++;
        }
        
        // Name matching
        double nameScore = calculateNameConfidence(entity1, entity2);
        if (nameScore > 0) {
            totalScore += nameScore * 0.3; // 30% weight
            criteriaCount++;
        }
        
        // Phone matching
        double phoneScore = calculatePhoneConfidence(entity1, entity2);
        if (phoneScore > 0) {
            totalScore += phoneScore * 0.2; // 20% weight
            criteriaCount++;
        }
        
        // Address matching
        double addressScore = calculateAddressConfidence(entity1, entity2);
        if (addressScore > 0) {
            totalScore += addressScore * 0.1; // 10% weight
            criteriaCount++;
        }
        
        return criteriaCount > 0 ? totalScore : 0.0;
    }
    
    private double calculateEmailConfidence(DataEntity entity1, DataEntity entity2) {
        String email1 = getAttributeValue(entity1, "email");
        String email2 = getAttributeValue(entity2, "email");
        
        if (email1 == null || email2 == null) {
            return 0.0;
        }
        
        email1 = email1.toLowerCase().trim();
        email2 = email2.toLowerCase().trim();
        
        if (email1.equals(email2)) {
            return 1.0;
        }
        
        // Check for similar emails (e.g., john.doe@email.com vs john.doe@email.com)
        String[] parts1 = email1.split("@");
        String[] parts2 = email2.split("@");
        
        if (parts1.length == 2 && parts2.length == 2) {
            if (parts1[0].equals(parts2[0]) && parts1[1].equals(parts2[1])) {
                return 0.95;
            }
        }
        
        return 0.0;
    }
    
    private double calculateNameConfidence(DataEntity entity1, DataEntity entity2) {
        String firstName1 = getAttributeValue(entity1, "firstName");
        String lastName1 = getAttributeValue(entity1, "lastName");
        String firstName2 = getAttributeValue(entity2, "firstName");
        String lastName2 = getAttributeValue(entity2, "lastName");
        
        double firstNameScore = 0.0;
        double lastNameScore = 0.0;
        
        if (firstName1 != null && firstName2 != null) {
            firstName1 = firstName1.toLowerCase().trim();
            firstName2 = firstName2.toLowerCase().trim();
            
            if (firstName1.equals(firstName2)) {
                firstNameScore = 1.0;
            } else if (firstName1.contains(firstName2) || firstName2.contains(firstName1)) {
                firstNameScore = 0.8;
            }
        }
        
        if (lastName1 != null && lastName2 != null) {
            lastName1 = lastName1.toLowerCase().trim();
            lastName2 = lastName2.toLowerCase().trim();
            
            if (lastName1.equals(lastName2)) {
                lastNameScore = 1.0;
            } else if (lastName1.contains(lastName2) || lastName2.contains(lastName1)) {
                lastNameScore = 0.8;
            }
        }
        
        return (firstNameScore + lastNameScore) / 2.0;
    }
    
    private double calculatePhoneConfidence(DataEntity entity1, DataEntity entity2) {
        String phone1 = getAttributeValue(entity1, "phone");
        String phone2 = getAttributeValue(entity2, "phone");
        
        if (phone1 == null || phone2 == null) {
            return 0.0;
        }
        
        // Normalize phone numbers (remove spaces, dashes, parentheses)
        phone1 = phone1.replaceAll("[\\s\\-\\(\\)]", "");
        phone2 = phone2.replaceAll("[\\s\\-\\(\\)]", "");
        
        if (phone1.equals(phone2)) {
            return 1.0;
        }
        
        // Check if one is a substring of the other (e.g., country code differences)
        if (phone1.contains(phone2) || phone2.contains(phone1)) {
            return 0.9;
        }
        
        return 0.0;
    }
    
    private double calculateAddressConfidence(DataEntity entity1, DataEntity entity2) {
        String address1 = getAttributeValue(entity1, "address");
        String address2 = getAttributeValue(entity2, "address");
        
        if (address1 == null || address2 == null) {
            return 0.0;
        }
        
        address1 = address1.toLowerCase().trim();
        address2 = address2.toLowerCase().trim();
        
        if (address1.equals(address2)) {
            return 1.0;
        }
        
        // Simple similarity check
        String[] words1 = address1.split("\\s+");
        String[] words2 = address2.split("\\s+");
        
        int commonWords = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2) && word1.length() > 2) {
                    commonWords++;
                }
            }
        }
        
        if (words1.length > 0 && words2.length > 0) {
            return (double) commonWords / Math.max(words1.length, words2.length);
        }
        
        return 0.0;
    }
    
    private String getAttributeValue(DataEntity entity, String attributeName) {
        if (entity.getAttributes() != null) {
            return (String) entity.getAttributes().get(attributeName);
        }
        return null;
    }
    
    private String generateMatchReason(DataEntity entity1, DataEntity entity2, double confidence) {
        StringBuilder reason = new StringBuilder();
        
        if (calculateEmailConfidence(entity1, entity2) > 0.8) {
            reason.append("Email match; ");
        }
        if (calculateNameConfidence(entity1, entity2) > 0.8) {
            reason.append("Name match; ");
        }
        if (calculatePhoneConfidence(entity1, entity2) > 0.8) {
            reason.append("Phone match; ");
        }
        if (calculateAddressConfidence(entity1, entity2) > 0.8) {
            reason.append("Address match; ");
        }
        
        reason.append("Confidence: ").append(String.format("%.2f", confidence));
        return reason.toString();
    }
    
    @Override
    public String getMatchingCriteria() {
        return "Email, Name, Phone, Address with weighted scoring";
    }
} 