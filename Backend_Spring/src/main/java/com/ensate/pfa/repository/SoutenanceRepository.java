package com.ensate.pfa.repository;

import com.ensate.pfa.entity.Soutenance;
import com.ensate.pfa.entity.enums.SoutenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {
    Optional<Soutenance> findByPfaDossierPfaId(Long pfaId);
    List<Soutenance> findByStatus(SoutenanceStatus status);
    List<Soutenance> findByDateSoutenanceBetween(Long startDate, Long endDate);
}
