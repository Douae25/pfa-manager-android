// model/api/EvaluationResponse.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class EvaluationResponse {
    @SerializedName("evaluationId")
    private Long evaluationId;

    @SerializedName("totalScore")
    private Double totalScore;

    @SerializedName("comments")
    private String comments;

    // Getters
    public Long getEvaluationId() { return evaluationId; }
    public Double getTotalScore() { return totalScore; }
    public String getComments() { return comments; }
}