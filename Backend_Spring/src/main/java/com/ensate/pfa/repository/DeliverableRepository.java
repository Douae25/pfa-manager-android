package com.ensate.pfa.repository;

import com.ensate.pfa.entity.Deliverable;
import com.ensate.pfa.entity.enums.DeliverableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliverableRepository extends JpaRepository<Deliverable, Long> {
    List<Deliverable> findByPfaDossierPfaId(Long pfaId);
    List<Deliverable> findByPfaDossierPfaIdAndDeliverableType(Long pfaId, DeliverableType type);

    List<Deliverable> findByIsValidatedFalse();
        
    @Query("SELECT d FROM Deliverable d " +
           "JOIN d.pfaDossier p " +
           "WHERE p.supervisor.userId = :supervisorId " +
           "ORDER BY d.uploadedAt DESC")
    List<Deliverable> findBySupervisorId(Long supervisorId);
}
