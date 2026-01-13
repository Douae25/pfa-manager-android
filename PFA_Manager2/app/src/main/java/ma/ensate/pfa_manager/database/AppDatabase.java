package ma.ensate.pfa_manager.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import ma.ensate.pfa_manager.model.*;

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
}, version = 3, exportSchema = false) 
@TypeConverters({RoleConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

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
                    .fallbackToDestructiveMigration()
                    .enableMultiInstanceInvalidation()
                    .build();
            
            // Enable WAL mode for better concurrent access
            instance.getOpenHelper().getWritableDatabase().enableWriteAheadLogging();
        }
        return instance;
    }
}