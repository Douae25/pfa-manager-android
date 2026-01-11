package ma.ensate.pfa_manager.model.dto;

import ma.ensate.pfa_manager.model.EvaluationCriteria;

public class CriteriaWithScore {
    public EvaluationCriteria criteria;
    public Double score;

    public CriteriaWithScore(EvaluationCriteria criteria) {
        this.criteria = criteria;
        this.score = 0.0;
    }

    public Long getCriteriaId() {
        return criteria != null ? criteria.getCriteria_id() : null;
    }

    public String getLabel() {
        return criteria != null ? criteria.getLabel() : "";
    }

    public String getDescription() {
        return criteria != null ? criteria.getDescription() : "";
    }

    public Double getWeight() {
        return criteria != null ? criteria.getWeight() : 0.0;
    }
}