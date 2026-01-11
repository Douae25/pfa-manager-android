package ma.ensate.pfa_manager.model;

import androidx.room.TypeConverter;

public class DepartmentConverter {
    @TypeConverter
    public static Department fromCode(String code) {
        if (code == null) return null;
        switch (code) {
            case "ADM": return new Department("ADM", "Administration");
            case "GI2": return new Department("GI2", "2ème année Génie informatique");
            case "SCM2": return new Department("SCM2", "2ème année Supply Chain Management");
            case "GM2": return new Department("GM2", "2ème année Génie Mécatronique");
            case "GSTR2": return new Department("GSTR2", "2ème année Génie des Systèmes de Télécommunications et Réseaux");
            case "GC2": return new Department("GC2", "2ème année Génie Civil");
            default: return new Department(code, "");
        }
    }
    @TypeConverter
    public static String toCode(Department department) {
        return department != null ? department.getCode() : null;
    }
}
