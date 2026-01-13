package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class DeliverableResponse {

    @SerializedName("deliverableId")
    private Long deliverableId;

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("fileTitle")
    private String fileTitle;

    @SerializedName("fileUri")
    private String fileUri;

    @SerializedName("deliverableType")
    private String deliverableType;

    @SerializedName("fileType")
    private String deliverableFileType;

    @SerializedName("uploadedAt")
    private Long uploadedAt;

    @SerializedName("isValidated")
    private Boolean isValidated;

    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("studentFirstName")
    private String studentFirstName;

    @SerializedName("studentLastName")
    private String studentLastName;

    @SerializedName("pfaTitle")
    private String pfaTitle;

    // Getters
    public Long getDeliverableId() { return deliverableId; }
    public Long getPfaId() { return pfaId; }
    public String getFileTitle() { return fileTitle; }
    public String getFileUri() { return fileUri; }
    public String getDeliverableType() { return deliverableType; }
    public String getDeliverableFileType() { return deliverableFileType; }
    public Long getUploadedAt() { return uploadedAt; }
    public Boolean getIsValidated() { return isValidated; }
    public Long getStudentId() { return studentId; }
    public String getStudentFirstName() { return studentFirstName; }
    public String getStudentLastName() { return studentLastName; }
    public String getPfaTitle() { return pfaTitle; }
}