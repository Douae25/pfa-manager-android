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

    @SerializedName("pfa")
    private PFAInfoResponse pfa;

    @SerializedName("convention")
    private ConventionResponse convention;

    @SerializedName("deliverables")
    private List<DeliverableResponse> deliverables;

    @SerializedName("soutenance")
    private SoutenanceResponse soutenance;

    @SerializedName("evaluation")
    private EvaluationResponse evaluation;

    // Getters
    public Long getStudentId() { return studentId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public PFAInfoResponse getPfa() { return pfa; }
    public ConventionResponse getConvention() { return convention; }
    public List<DeliverableResponse> getDeliverables() { return deliverables; }
    public SoutenanceResponse getSoutenance() { return soutenance; }
    public EvaluationResponse getEvaluation() { return evaluation; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getInitials() {
        String first = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "";
        String last = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}