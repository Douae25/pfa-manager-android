package ma.ensate.pfa_manager.model;

import androidx.room.TypeConverter;

public class RoleConverter {
    
    @TypeConverter
    public String fromRole(Role role) {
        return role == null ? null : role.name();
    }
    
    @TypeConverter
    public Role toRole(String roleString) {
        return roleString == null ? null : Role.valueOf(roleString);
    }
    
    @TypeConverter
    public String fromPFAStatus(PFAStatus status) {
        return status == null ? null : status.name();
    }
    
    @TypeConverter
    public PFAStatus toPFAStatus(String statusString) {
        return statusString == null ? null : PFAStatus.valueOf(statusString);
    }
    
    @TypeConverter
    public String fromConventionState(ConventionState state) {
        return state == null ? null : state.name();
    }
    
    @TypeConverter
    public ConventionState toConventionState(String stateString) {
        return stateString == null ? null : ConventionState.valueOf(stateString);
    }
    
    @TypeConverter
    public String fromSoutenanceStatus(SoutenanceStatus status) {
        return status == null ? null : status.name();
    }
    
    @TypeConverter
    public SoutenanceStatus toSoutenanceStatus(String statusString) {
        return statusString == null ? null : SoutenanceStatus.valueOf(statusString);
    }
    
    @TypeConverter
    public String fromDeliverableType(DeliverableType type) {
        return type == null ? null : type.name();
    }
    
    @TypeConverter
    public DeliverableType toDeliverableType(String typeString) {
        return typeString == null ? null : DeliverableType.valueOf(typeString);
    }
    
    @TypeConverter
    public String fromDeliverableFileType(DeliverableFileType fileType) {
        return fileType == null ? null : fileType.name();
    }
    
    @TypeConverter
    public DeliverableFileType toDeliverableFileType(String fileTypeString) {
        if (fileTypeString == null || fileTypeString.trim().isEmpty()) {
            return null;
        }
        try {
            return DeliverableFileType.valueOf(fileTypeString);
        } catch (IllegalArgumentException e) {
            // Handle invalid enum values gracefully
            return null;
        }
    }
}
