package ma.ensate.pfa_manager.util;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.Department;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;

public class InitialDataLoader {

    private static final String TAG = "InitialDataLoader";

    public static void loadInitialData(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            try {
                int userCount = db.userDao().getAllUsers().size();

                if (userCount > 0) {
                    Log.d(TAG, "Les données initiales existent déjà. Pas de rechargement.");
                    return;
                }

                Log.d(TAG, "Chargement des données initiales...");
                
                insertUsers(db);

                Log.d(TAG, "Données initiales chargées avec succès !");

            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du chargement des données : " + e.getMessage(), e);
            }
        });
    }

    private static void insertUsers(AppDatabase db) {
        if (db.userDao().getAllUsers().isEmpty()) {
            User[] users = {
                    createUser("Nada", "Elmourabet", "nada.elmourabet@etu.uae.ac.ma",
                            "password", Role.COORDINATOR, 1L, "+212600000001"),

                    createUser("Lina", "Aitbrahim", "lina.aitbrahim@etu.uae.ac.ma",
                            "password", Role.ADMIN, null, "+212600000002"),

                    createUser("Douae", "Aazibou", "douae.aazibou@etu.uae.ac.ma",
                            "password", Role.STUDENT, 1L, "+212600000003"),

                    createUser("Youness", "Elkihal", "youness.elkihal@etu.uae.ac.ma",
                            "password", Role.STUDENT, 1L, "+212600000004"),

                    createUser("Youness", "Elkihal", "kihlyounes123@gmail.com",
                            "password", Role.PROFESSOR, 1L, "+212600000006"),        

                    createUser("Saad", "Barhrouj", "saad.barhrouj@etu.uae.ac.ma",
                            "password", Role.PROFESSOR, 1L, "+212600000005")
            };

            for (User user : users) {
                db.userDao().insert(user);
            }

            Log.d(TAG, users.length + " utilisateurs insérés");
        }
    }

    private static User createUser(String firstName, String lastName, String email,
                                   String password, Role role, Long deptId, String phoneNumber) {
        User user = new User();
        user.setFirst_name(firstName);
        user.setLast_name(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setPhone_number(phoneNumber);
        user.setCreated_at(System.currentTimeMillis());
        return user;
    }
}
