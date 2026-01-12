package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class StudentWithPFAResponse {

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

    @SerializedName("totalScore")
    private Double totalScore;

    @SerializedName("evaluated")
    private boolean evaluated;

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
    public Double getTotalScore() { return totalScore; }
    public boolean isEvaluated() { return evaluated; }
}