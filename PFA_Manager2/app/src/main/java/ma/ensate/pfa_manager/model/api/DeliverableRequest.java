package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

import ma.ensate.pfa_manager.model.DeliverableFileType;
import ma.ensate.pfa_manager.model.DeliverableType;

public class DeliverableRequest {
    
    @SerializedName("pfaId")
    private Long pfaId;
    
    @SerializedName("fileTitle")
    private String fileTitle;
    
    @SerializedName("filePath")
    private String filePath;
    
    @SerializedName("fileType")
    private DeliverableFileType fileType;
    
    @SerializedName("deliverableType")
    private DeliverableType deliverableType;
    
    public DeliverableRequest() {}
    
    public DeliverableRequest(Long pfaId, String fileTitle, String filePath, 
                             DeliverableFileType fileType, DeliverableType deliverableType) {
        this.pfaId = pfaId;
        this.fileTitle = fileTitle;
        this.filePath = filePath;
        this.fileType = fileType;
        this.deliverableType = deliverableType;
    }
    
    // Getters & Setters
    public Long getPfaId() { return pfaId; }
    public void setPfaId(Long pfaId) { this.pfaId = pfaId; }
    
    public String getFileTitle() { return fileTitle; }
    public void setFileTitle(String fileTitle) { this.fileTitle = fileTitle; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public DeliverableFileType getFileType() { return fileType; }
    public void setFileType(DeliverableFileType fileType) { this.fileType = fileType; }
    
    public DeliverableType getDeliverableType() { return deliverableType; }
    public void setDeliverableType(DeliverableType deliverableType) { this.deliverableType = deliverableType; }
    
    // Helper method to map client enum to backend string format
    public String getBackendFileType() {
        if (fileType == null) return null;
        switch (fileType) {
            case RAPPORT_AVANCEMENT:
                return "PROGRESS_REPORT";
            case PRESENTATION:
                return "PRESENTATION";
            case RAPPORT_FINAL:
                return "FINAL_REPORT";
            default:
                return fileType.name();
        }
    }
}
