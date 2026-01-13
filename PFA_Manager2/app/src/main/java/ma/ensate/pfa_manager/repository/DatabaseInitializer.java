package ma.ensate.pfa_manager.repository;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.Role;

public class DatabaseInitializer {
    
    private static final String DB_INIT_KEY = "database_initialized";
    
    public static void initializeDatabase(Application application) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        
        // Vérifier si la base a déjà été initialisée
        if (!prefs.getBoolean(DB_INIT_KEY, false)) {
            AppDatabase database = AppDatabase.getInstance(application);
            
            // Ajouter l'utilisateur admin par défaut
            User adminUser = new User();
            adminUser.setEmail("admin@ensate.ma");
            adminUser.setPassword("admin123");
            adminUser.setFirst_name("Admin");
            adminUser.setLast_name("System");
            adminUser.setRole(Role.ADMIN);
            adminUser.setPhone_number("0601234567");
            adminUser.setCreated_at(System.currentTimeMillis());
            
            // Insérer l'admin en arrière-plan
            new Thread(() -> {
                long adminId = database.userDao().insert(adminUser);
                // Marquer la base comme initialisée
                prefs.edit().putBoolean(DB_INIT_KEY, true).apply();
            }).start();
        }
    }
}
