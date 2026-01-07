package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "evaluation_criteria")
public class EvaluationCriteria {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "criteria_id")
    private Long criteria_id;
    
    @ColumnInfo(name = "label")
    private String label;
    
    @ColumnInfo(name = "weight")
    private Double weight;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "is_active")
    private Boolean is_active;
    
    public EvaluationCriteria() {}
    
    public Long getCriteria_id() { return criteria_id; }
    public void setCriteria_id(Long criteria_id) { this.criteria_id = criteria_id; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getIs_active() { return is_active; }
    public void setIs_active(Boolean is_active) { this.is_active = is_active; }
}
