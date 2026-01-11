package ma.ensate.pfa_manager.model.dto;

import androidx.room.Embedded;
import androidx.room.Relation;

import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.Soutenance;

public class PFAWithSoutenance {
    @Embedded
    public PFADossier pfa;

    @Relation(
            parentColumn = "pfa_id",
            entityColumn = "pfa_id"
    )
    public Soutenance soutenance;

    public boolean isPlanned() {
        return soutenance != null;
    }
}