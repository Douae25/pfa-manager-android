package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "evaluation_details",
    foreignKeys = {
        @ForeignKey(entity = Evaluation.class, parentColumns = "evaluation_id", childColumns = "evaluation_id"),
        @ForeignKey(entity = EvaluationCriteria.class, parentColumns = "criteria_id", childColumns = "criteria_id")
    },
    indices = {@Index("evaluation_id"), @Index("criteria_id")})
public class EvaluationDetail {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "detail_id")
    private Long detail_id;
    
    @ColumnInfo(name = "evaluation_id")
    private Long evaluation_id;
    
    @ColumnInfo(name = "criteria_id")
    private Long criteria_id;
    
    @ColumnInfo(name = "score_given")
    private Double score_given;
    
    public EvaluationDetail() {}
    
    public Long getDetail_id() { return detail_id; }
    public void setDetail_id(Long detail_id) { this.detail_id = detail_id; }
    
    public Long getEvaluation_id() { return evaluation_id; }
    public void setEvaluation_id(Long evaluation_id) { this.evaluation_id = evaluation_id; }
    
    public Long getCriteria_id() { return criteria_id; }
    public void setCriteria_id(Long criteria_id) { this.criteria_id = criteria_id; }
    
    public Double getScore_given() { return score_given; }
    public void setScore_given(Double score_given) { this.score_given = score_given; }
}
