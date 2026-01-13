package ma.ensate.pfa_manager.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO pour la synchronisation avec le backend.
 * Ce modèle reflète exactement la structure retournée par le backend.
 */
public class PFADossierSync {
    @SerializedName("pfaId")
    public Long pfaId;
    
    @SerializedName("studentId")
    public Long studentId;
    
    @SerializedName("title")
    public String title;
    
    @SerializedName("description")
    public String description;
    
    @SerializedName("currentStatus")
    public PFAStatus currentStatus;
    
    @SerializedName("updatedAt")
    public Long updatedAt;
    
    // Autres champs optionnels du backend
    @SerializedName("studentName")
    public String studentName;
    
    @SerializedName("supervisorId")
    public Long supervisorId;
    
    @SerializedName("supervisorName")
    public String supervisorName;
    
    @SerializedName("convention")
    public Object convention;
    
    /**
     * Convertir ce DTO en entité Room PFADossier
     */
    public PFADossier toRoomEntity() {
        PFADossier dossier = new PFADossier();
        dossier.setPfa_id(this.pfaId);
        dossier.setStudent_id(this.studentId);
        dossier.setSupervisor_id(this.supervisorId);
        dossier.setTitle(this.title);
        dossier.setDescription(this.description);
        dossier.setCurrent_status(this.currentStatus);
        dossier.setUpdated_at(this.updatedAt);
        return dossier;
    }
}
