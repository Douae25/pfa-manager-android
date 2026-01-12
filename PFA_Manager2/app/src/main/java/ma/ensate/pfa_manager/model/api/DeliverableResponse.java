package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class DeliverableResponse {
    @SerializedName("deliverableId")
    private Long deliverableId;

    @SerializedName("fileTitle")
    private String fileTitle;

    @SerializedName("fileUri")
    private String fileUri;

    @SerializedName("uploadedAt")
    private Long uploadedAt;

    @SerializedName("deliverableType")
    private String deliverableType;

    // Getters
    public Long getDeliverableId() { return deliverableId; }
    public String getFileTitle() { return fileTitle; }
    public String getFileUri() { return fileUri; }
    public Long getUploadedAt() { return uploadedAt; }
    public String getDeliverableType() { return deliverableType; }
}