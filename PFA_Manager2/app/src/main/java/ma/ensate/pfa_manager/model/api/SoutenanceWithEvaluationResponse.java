// model/api/SoutenanceWithEvaluationResponse.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class SoutenanceWithEvaluationResponse {

    @SerializedName("soutenanceId")
    private Long soutenanceId;

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("pfaTitle")
    private String pfaTitle;

    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("studentFirstName")
    private String studentFirstName;

    @SerializedName("studentLastName")
    private String studentLastName;

    @SerializedName("dateSoutenance")
    private Long dateSoutenance;

    @SerializedName("location")
    private String location;

    @SerializedName("soutenanceStatus")
    private String soutenanceStatus;

    @SerializedName("totalScore")
    private Double totalScore;

    @SerializedName("evaluated")
    private boolean evaluated;

    // Getters
    public Long getSoutenanceId() { return soutenanceId; }
    public Long getPfaId() { return pfaId; }
    public String getPfaTitle() { return pfaTitle; }
    public Long getStudentId() { return studentId; }
    public String getStudentFirstName() { return studentFirstName; }
    public String getStudentLastName() { return studentLastName; }
    public Long getDateSoutenance() { return dateSoutenance; }
    public String getLocation() { return location; }
    public String getSoutenanceStatus() { return soutenanceStatus; }
    public Double getTotalScore() { return totalScore; }
    public boolean isEvaluated() { return evaluated; }

    public String getStudentFullName() {
        return studentFirstName + " " + studentLastName;
    }

    public String getStudentInitials() {
        String first = (studentFirstName != null && !studentFirstName.isEmpty()) ?
                studentFirstName.substring(0, 1) : "";
        String last = (studentLastName != null && !studentLastName.isEmpty()) ?
                studentLastName.substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}