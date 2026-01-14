package ma.ensate.pfa_manager.model;

import androidx.room.TypeConverter;

public class RoleConverter {

    // ------------------ Role ------------------
    @TypeConverter
    public String fromRole(Role role) {
        return role == null ? null : role.name();
    }

    @TypeConverter
    public Role toRole(String roleString) {
        return roleString == null ? null : Role.valueOf(roleString);
    }

    // ------------------ PFAStatus ------------------
    @TypeConverter
    public String fromPFAStatus(PFAStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public PFAStatus toPFAStatus(String statusString) {
        return statusString == null ? null : PFAStatus.valueOf(statusString);
    }

    // ------------------ ConventionState ------------------
    @TypeConverter
    public String fromConventionState(ConventionState state) {
        return state == null ? null : state.name();
    }

    @TypeConverter
    public ConventionState toConventionState(String stateString) {
        return stateString == null ? null : ConventionState.valueOf(stateString);
    }

    // ------------------ SoutenanceStatus ------------------
    @TypeConverter
    public String fromSoutenanceStatus(SoutenanceStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public SoutenanceStatus toSoutenanceStatus(String statusString) {
        return statusString == null ? null : SoutenanceStatus.valueOf(statusString);
    }

    // ------------------ DeliverableType ------------------
    @TypeConverter
    public String fromDeliverableType(DeliverableType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public DeliverableType toDeliverableType(String typeString) {
        return typeString == null ? null : DeliverableType.valueOf(typeString);
    }

    // ------------------ DeliverableFileType ------------------
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
            return null;
        }
    }

    // ------------------ DepartmentId pour coordinateur ------------------
    @TypeConverter
    public static String fromDepartmentId(Long departmentId) {
        return departmentId == null ? null : String.valueOf(departmentId);
    }

    @TypeConverter
    public static Long toDepartmentId(String departmentIdString) {
        return departmentIdString == null ? null : Long.parseLong(departmentIdString);
    }
}
