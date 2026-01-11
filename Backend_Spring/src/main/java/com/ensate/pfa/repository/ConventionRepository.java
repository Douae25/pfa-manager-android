package com.ensate.pfa.repository;

import com.ensate.pfa.entity.Convention;
import com.ensate.pfa.entity.enums.ConventionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConventionRepository extends JpaRepository<Convention, Long> {
    Optional<Convention> findByPfaDossierPfaId(Long pfaId);
    List<Convention> findByState(ConventionState state);
    List<Convention> findByIsValidatedTrue();
    List<Convention> findByStateIn(List<ConventionState> states);
}
