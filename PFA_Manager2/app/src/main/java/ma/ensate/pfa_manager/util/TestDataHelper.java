package ma.ensate.pfa_manager.util;

import android.content.Context;
import java.io.File;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.*;

public class TestDataHelper {

    /**
     * Cette méthode est appelée par HomeActivity.
     * Elle vide la base, reset les IDs, et insère seulement Mansour (ID 1).
     */
    public static void resetAndReload(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            // 1. NETTOYAGE COMPLET
            db.clearAllTables();

            // 2. RESET DU COMPTEUR AUTO-INCREMENT (IMPORTANT POUR AVOIR ID=1)
            db.getOpenHelper().getWritableDatabase().execSQL("DELETE FROM sqlite_sequence");

            // 3. SUPPRESSION DES FICHIERS DE TEST (Optionnel, pour faire propre)
            File testDir = new File(context.getFilesDir(), "test_deliverables");
            if (testDir.exists()) {
                for (File file : testDir.listFiles()) {
                    file.delete();
                }
            }

            // 4. INSERTION DE L'ENCADRANT MANSOUR UNIQUEMENT
            insertMansourOnly(db);

            System.out.println("🔄 resetAndReload terminé : Base nettoyée et Mansour (ID 1) créé.");
        });
    }

    // Méthode interne pour insérer Mansour
    private static void insertMansourOnly(AppDatabase db) {
        User supervisor = new User();
        supervisor.setFirst_name("Abdeljebar");
        supervisor.setLast_name("Mansour");
        supervisor.setEmail("mansour@ensate.uae.ma");
        supervisor.setPassword("123456");
        supervisor.setRole(Role.PROFESSOR);
        supervisor.setPhone_number("0612345678");
        supervisor.setCreated_at(System.currentTimeMillis());

        // Comme la table est vide et la sequence reset, ceci DOIT retourner 1
        long supervisorId = db.userDao().insert(supervisor);

        if (supervisorId == 1) {
            System.out.println("✅ SUCCÈS : Mansour créé avec l'ID local " + supervisorId + ". Prêt pour synchro !");
        } else {
            System.out.println("⚠️ ATTENTION : Mansour a l'ID " + supervisorId + " (On attendait 1).");
        }
    }

    public static void insertTestData(Context context) {
        resetAndReload(context);
    }
}