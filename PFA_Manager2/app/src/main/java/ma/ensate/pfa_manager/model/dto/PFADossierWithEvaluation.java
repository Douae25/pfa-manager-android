package ma.ensate.pfa_manager.model.dto;

import androidx.room.Embedded;
import androidx.room.Relation;
import ma.ensate.pfa_manager.model.Evaluation;
import ma.ensate.pfa_manager.model.PFADossier;

public class PFADossierWithEvaluation {
    @Embedded
    public PFADossier pfaDossier;

    @Relation(
            parentColumn = "pfa_id",
            entityColumn = "pfa_id"
    )
    public Evaluation evaluation;
}