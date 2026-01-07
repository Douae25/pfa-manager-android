package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "evaluations",
    foreignKeys = {
        @ForeignKey(entity = PFADossier.class, parentColumns = "pfa_id", childColumns = "pfa_id"),
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "evaluator_id")
    },
    indices = {@Index("pfa_id"), @Index("evaluator_id")})
public class Evaluation {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "evaluation_id")
    private Long evaluation_id;
    
    @ColumnInfo(name = "pfa_id")
    private Long pfa_id;
    
    @ColumnInfo(name = "evaluator_id")
    private Long evaluator_id;
    
    @ColumnInfo(name = "date_evaluation")
    private Long date_evaluation;
    
    @ColumnInfo(name = "total_score")
    private Double total_score;
    
    public Evaluation() {}
    
    public Long getEvaluation_id() { return evaluation_id; }
    public void setEvaluation_id(Long evaluation_id) { this.evaluation_id = evaluation_id; }
    
    public Long getPfa_id() { return pfa_id; }
    public void setPfa_id(Long pfa_id) { this.pfa_id = pfa_id; }
    
    public Long getEvaluator_id() { return evaluator_id; }
    public void setEvaluator_id(Long evaluator_id) { this.evaluator_id = evaluator_id; }
    
    public Long getDate_evaluation() { return date_evaluation; }
    public void setDate_evaluation(Long date_evaluation) { this.date_evaluation = date_evaluation; }
    
    public Double getTotal_score() { return total_score; }
    public void setTotal_score(Double total_score) { this.total_score = total_score; }
}
