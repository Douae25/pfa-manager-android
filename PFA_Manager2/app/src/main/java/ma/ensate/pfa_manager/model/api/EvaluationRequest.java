// model/api/EvaluationRequest.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import ma.ensate.pfa_manager.model.dto.CriteriaWithScore;

public class EvaluationRequest {

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("scores")
    private List<CriteriaScoreItem> scores;

    public EvaluationRequest(Long pfaId, List<CriteriaWithScore> criteriaScores) {
        this.pfaId = pfaId;
        this.scores = new ArrayList<>();
        for (CriteriaWithScore cs : criteriaScores) {
            this.scores.add(new CriteriaScoreItem(cs.getCriteriaId(), cs.score));
        }
    }

    public static class CriteriaScoreItem {
        @SerializedName("criteriaId")
        private Long criteriaId;

        @SerializedName("score")
        private Double score;

        public CriteriaScoreItem(Long criteriaId, Double score) {
            this.criteriaId = criteriaId;
            this.score = score;
        }
    }
}