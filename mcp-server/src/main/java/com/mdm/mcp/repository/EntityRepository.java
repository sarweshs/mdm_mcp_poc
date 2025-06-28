package com.mdm.mcp.repository;

import com.mdm.mcp.model.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityRepository extends JpaRepository<DataEntity, Long> {
    
    Optional<DataEntity> findByEntityId(String entityId);
    
    Optional<DataEntity> findByEntityIdAndEntityType(String entityId, String entityType);
    
    Optional<DataEntity> findByEntityIdAndSourceSystem(String entityId, String sourceSystem);
    
    List<DataEntity> findByEntityType(String entityType);
    
    List<DataEntity> findByStatus(String status);
    
    List<DataEntity> findByEntityTypeAndStatus(String entityType, String status);
    
    @Query("SELECT e FROM DataEntity e WHERE e.attributes['email'] = :email")
    List<DataEntity> findByEmail(@Param("email") String email);
    
    @Query("SELECT e FROM DataEntity e WHERE e.attributes['phoneNumber'] = :phoneNumber")
    List<DataEntity> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT e FROM DataEntity e WHERE e.attributes['ssn'] = :ssn")
    List<DataEntity> findBySSN(@Param("ssn") String ssn);
    
    @Query("SELECT e FROM DataEntity e WHERE e.attributes['companyName'] = :companyName")
    List<DataEntity> findByCompanyName(@Param("companyName") String companyName);
    
    @Query("SELECT e FROM DataEntity e WHERE e.confidenceScore >= :minConfidence")
    List<DataEntity> findByMinConfidenceScore(@Param("minConfidence") Double minConfidence);
    
    @Query("SELECT e FROM DataEntity e WHERE e.sourceSystem = :sourceSystem")
    List<DataEntity> findBySourceSystem(@Param("sourceSystem") String sourceSystem);
} 