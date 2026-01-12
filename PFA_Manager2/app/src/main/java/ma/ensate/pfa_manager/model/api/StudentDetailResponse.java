package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentDetailResponse {

    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("pfaTitle")
    private String pfaTitle;

    @SerializedName("pfaDescription")
    private String pfaDescription;

    @SerializedName("pfaStatus")
    private String pfaStatus;

    @SerializedName("pfaUpdatedAt")
    private Long pfaUpdatedAt;

    @SerializedName("convention")
    private ConventionResponse convention;

    @SerializedName("deliverables")
    private List<DeliverableResponse> deliverables;

    @SerializedName("soutenance")
    private SoutenanceResponse soutenance;

    @SerializedName("totalScore")
    private Double totalScore;

    @SerializedName("isEvaluated")
    private Boolean isEvaluated;

    // Getters
    public Long getStudentId() { return studentId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Long getPfaId() { return pfaId; }
    public String getPfaTitle() { return pfaTitle; }
    public String getPfaDescription() { return pfaDescription; }
    public String getPfaStatus() { return pfaStatus; }
    public Long getPfaUpdatedAt() { return pfaUpdatedAt; }
    public ConventionResponse getConvention() { return convention; }
    public List<DeliverableResponse> getDeliverables() { return deliverables; }
    public SoutenanceResponse getSoutenance() { return soutenance; }
    public Double getTotalScore() { return totalScore; }
    public Boolean getIsEvaluated() { return isEvaluated; }
}