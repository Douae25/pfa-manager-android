package ma.ensate.pfa_manager.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import ma.ensate.pfa_manager.model.*;
import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {
    User.class,
    Department.class,
    PFADossier.class,
    Convention.class,
    Deliverable.class,
    Soutenance.class,
    Evaluation.class,
    EvaluationCriteria.class,
    EvaluationDetail.class
}, version = 6, exportSchema = false)
@TypeConverters({RoleConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static AppDatabase instance;

    // Migration pour convertir department_id de TEXT à INTEGER
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 1. Ajouter une nouvelle colonne temporaire de type INTEGER
            database.execSQL("ALTER TABLE users ADD COLUMN department_id_temp INTEGER");

            // 2. Copier les valeurs converties (si possible)
            database.execSQL("UPDATE users SET department_id_temp = CAST(department_id AS INTEGER)");

            // 3. Créer une nouvelle table users_new avec le bon schéma
            database.execSQL(
                "CREATE TABLE users_new (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT, " +
                "password TEXT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "role TEXT, " +
                "phone_number TEXT, " +
                "created_at INTEGER, " +
                "department_id INTEGER, " +
                "FOREIGN KEY(department_id) REFERENCES departments(department_id))"
            );
            database.execSQL(
                "INSERT INTO users_new (user_id, email, password, first_name, last_name, role, phone_number, created_at, department_id) " +
                "SELECT user_id, email, password, first_name, last_name, role, phone_number, created_at, department_id_temp FROM users"
            );
            database.execSQL("DROP TABLE users");
            database.execSQL("ALTER TABLE users_new RENAME TO users");

            // 4. Supprimer l’ancienne table
            database.execSQL("DROP TABLE users");

            // 5. Renommer la nouvelle table
            database.execSQL("ALTER TABLE users_new RENAME TO users");
        }
    };

    // Migration pour ajouter ON DELETE CASCADE sur department_id (users)
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 1. Créer une nouvelle table users_new avec la contrainte ON DELETE CASCADE
            database.execSQL(
                "CREATE TABLE users_new (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT, " +
                "password TEXT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "role TEXT, " +
                "phone_number TEXT, " +
                "created_at INTEGER, " +
                "department_id INTEGER, " +
                "FOREIGN KEY(department_id) REFERENCES departments(department_id) ON DELETE CASCADE)"
            );
            // 2. Copier les données existantes
            database.execSQL(
                "INSERT INTO users_new (user_id, email, password, first_name, last_name, role, phone_number, created_at, department_id) " +
                "SELECT user_id, email, password, first_name, last_name, role, phone_number, created_at, department_id FROM users"
            );
            // 3. Supprimer l’ancienne table
            database.execSQL("DROP TABLE users");
            // 4. Renommer la nouvelle table
            database.execSQL("ALTER TABLE users_new RENAME TO users");
        }
    };
    
    public abstract UserDao userDao();
    public abstract DepartmentDao departmentDao();
    public abstract PFADossierDao pfaDossierDao();
    public abstract ConventionDao conventionDao();
    public abstract DeliverableDao deliverableDao();
    public abstract SoutenanceDao soutenanceDao();
    public abstract EvaluationDao evaluationDao();
    public abstract EvaluationCriteriaDao evaluationCriteriaDao();
    public abstract EvaluationDetailDao evaluationDetailDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "pfa_manager_database"
            )
            .addMigrations(MIGRATION_2_3, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}
