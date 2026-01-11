package ma.ensate.pfa_manager.model;

import androidx.room.Embedded;
import androidx.room.Relation;

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