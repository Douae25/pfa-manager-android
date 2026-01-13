package com.ensate.pfa.repository;

import com.ensate.pfa.entity.PFADossier;
import com.ensate.pfa.entity.enums.PFAStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PFADossierRepository extends JpaRepository<PFADossier, Long> {
    List<PFADossier> findByStudentUserId(Long studentId);
    List<PFADossier> findBySupervisorUserId(Long supervisorId);
    List<PFADossier> findByCurrentStatus(PFAStatus status);
    List<PFADossier> findBySupervisorIsNull();
}
