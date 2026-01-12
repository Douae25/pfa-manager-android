// model/api/EvaluationCriteriaResponse.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class EvaluationCriteriaResponse {

    @SerializedName("criteriaId")
    private Long criteriaId;

    @SerializedName("label")
    private String label;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("description")
    private String description;

    @SerializedName("isActive")
    private Boolean isActive;

    // Getters
    public Long getCriteriaId() { return criteriaId; }
    public String getLabel() { return label; }
    public Double getWeight() { return weight; }
    public String getDescription() { return description; }
    public Boolean getIsActive() { return isActive; }
}