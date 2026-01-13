package ma.ensate.pfa_manager.sync;

import android.content.Context;
import android.util.Log;
import java.util.List;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.Department;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.network.UserApi;
import ma.ensate.pfa_manager.network.DepartmentApi;
import ma.ensate.pfa_manager.network.PFADossierApi;
import ma.ensate.pfa_manager.network.ConventionApi;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;

public class SyncManager {
            private static void log(String msg) {
                Log.d("SyncManager", msg);
            }
        // Upload local changes to backend for all entities
        public static void uploadAll(Context context) {
            log("--- uploadAll START ---");
            uploadDepartments(context);
            uploadUsers(context);
            uploadPFADossiers(context);
            uploadConventions(context);
            log("--- uploadAll END ---");
        }

        public static void uploadDepartments(Context context) {
            log("uploadDepartments: called");
            DepartmentApi api = retrofit.create(DepartmentApi.class);
            AppDatabase db = AppDatabase.getInstance(context);
            List<Department> departments = db.departmentDao().getAll();
            log("uploadDepartments: count=" + departments.size());
            for (Department dept : departments) {
                try {
                    if (dept.getDepartment_id() == null || dept.getDepartment_id() == 0) {
                        log("uploadDepartments: createDepartment " + dept.getName());
                        api.createDepartment(dept).execute();
                    } else {
                        log("uploadDepartments: updateDepartment id=" + dept.getDepartment_id());
                        api.updateDepartment(dept.getDepartment_id(), dept).execute();
                    }
                } catch (Exception e) {
                    Log.e("SyncManager", "Department upload failed", e);
                }
            }
        }

        public static void uploadUsers(Context context) {
            log("uploadUsers: called");
            UserApi api = retrofit.create(UserApi.class);
            AppDatabase db = AppDatabase.getInstance(context);
            List<User> users = db.userDao().getAllUsers();
            log("uploadUsers: count=" + users.size());
            for (User user : users) {
                try {
                    if (user.getUser_id() == null || user.getUser_id() == 0) {
                        log("uploadUsers: createUser " + user.getEmail());
                        api.createUser(user).execute();
                    } else {
                        log("uploadUsers: updateUser id=" + user.getUser_id());
                        api.updateUser(user.getUser_id(), user).execute();
                    }
                } catch (Exception e) {
                    Log.e("SyncManager", "User upload failed", e);
                }
            }
        }

        public static void uploadPFADossiers(Context context) {
            log("uploadPFADossiers: called");
            PFADossierApi api = retrofit.create(PFADossierApi.class);
            AppDatabase db = AppDatabase.getInstance(context);
            List<PFADossier> dossiers = db.pfaDossierDao().getAll();
            log("uploadPFADossiers: count=" + dossiers.size());
            for (PFADossier dossier : dossiers) {
                try {
                    if (dossier.getPfa_id() == null || dossier.getPfa_id() == 0) {
                        log("uploadPFADossiers: createPFADossier " + dossier.getTitle());
                        api.createPFADossier(dossier).execute();
                    } else {
                        log("uploadPFADossiers: updatePFADossier id=" + dossier.getPfa_id());
                        api.updatePFADossier(dossier.getPfa_id(), dossier).execute();
                    }
                } catch (Exception e) {
                    Log.e("SyncManager", "PFADossier upload failed", e);
                }
            }
        }

        public static void uploadConventions(Context context) {
            log("uploadConventions: called");
            ConventionApi api = retrofit.create(ConventionApi.class);
            AppDatabase db = AppDatabase.getInstance(context);
            List<Convention> conventions = db.conventionDao().getAll();
            log("uploadConventions: count=" + conventions.size());
            for (Convention conv : conventions) {
                try {
                    if (conv.getConvention_id() == null || conv.getConvention_id() == 0) {
                        log("uploadConventions: createConvention");
                        api.createConvention(conv).execute();
                    } else {
                        log("uploadConventions: updateConvention id=" + conv.getConvention_id());
                        api.updateConvention(conv.getConvention_id(), conv).execute();
                    }
                } catch (Exception e) {
                    Log.e("SyncManager", "Convention upload failed", e);
                }
            }
        }
        private static final String BASE_URL = "http://10.0.2.2:8080"; // Adapter selon ton backend
        private static final com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
            .setLenient()
            .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.IDENTITY)
            .create();
        private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    public static void syncAll(Context context) {
        // Synchroniser d'abord les enfants, puis les parents
        syncConventions(context);
        syncPFADossiers(context);
        syncUsers(context);
        syncDepartments(context);
        log("--- syncAll END ---");
    }

    public static void syncDepartments(Context context) {
        DepartmentApi api = retrofit.create(DepartmentApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<Department>> response = api.getAllDepartments().execute();
            if (response.isSuccessful()) {
                String rawJson = response.errorBody() != null ? response.errorBody().string() : null;
                if (rawJson != null) Log.d("SyncManager", "Department raw JSON: " + rawJson);
                if (response.body() != null) {
                    List<Department> remoteDepartments = response.body();
                    Log.d("SyncManager", "Departments reçus: " + remoteDepartments.size());
                    for (Department dep : remoteDepartments) {
                        Log.d("SyncManager", "Department reçu: id=" + dep.getDepartment_id() + ", name=" + dep.getName());
                        if (dep.getDepartment_id() == null) continue;
                        Department local = db.departmentDao().getById(dep.getDepartment_id());
                        if (local != null) {
                            db.departmentDao().update(dep);
                        } else {
                            db.departmentDao().insert(dep);
                        }
                    }
                    List<Long> remoteIds = new java.util.ArrayList<>();
                    for (Department dep : remoteDepartments) {
                        if (dep.getDepartment_id() != null) remoteIds.add(dep.getDepartment_id());
                    }
                    Log.d("SyncManager", "Department IDs conservés: " + remoteIds);
                    db.departmentDao().deleteNotInIds(remoteIds);
                } else {
                    Log.d("SyncManager", "Department: body null");
                }
            } else {
                Log.d("SyncManager", "Department: response not successful");
            }
        } catch (Exception e) {
            Log.e("SyncManager", "Department sync failed", e);
        }
    }

    public static void syncUsers(Context context) {
        UserApi api = retrofit.create(UserApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<User>> response = api.getAllUsers().execute();
            if (response.isSuccessful() && response.body() != null) {
                List<User> remoteUsers = response.body();
                Log.d("SyncManager", "Users reçus: " + remoteUsers.size());
                for (User user : remoteUsers) {
                    Log.d("SyncManager", "User reçu: id=" + user.getUser_id() + ", email=" + user.getEmail() + ", firstName=" + user.getFirst_name());
                    if (user.getUser_id() == null) continue;
                    User local = db.userDao().getUserById(user.getUser_id());
                    if (local != null) {
                        // Merger: ne pas écraser avec null
                        if (user.getFirst_name() != null) local.setFirst_name(user.getFirst_name());
                        if (user.getLast_name() != null) local.setLast_name(user.getLast_name());
                        if (user.getEmail() != null) local.setEmail(user.getEmail());
                        if (user.getPassword() != null) local.setPassword(user.getPassword());
                        if (user.getRole() != null) local.setRole(user.getRole());
                        if (user.getPhone_number() != null) local.setPhone_number(user.getPhone_number());
                        if (user.getCreated_at() != null) local.setCreated_at(user.getCreated_at());
                        if (user.getDepartment_id() != null) local.setDepartment_id(user.getDepartment_id());
                        db.userDao().update(local);
                    } else {
                        db.userDao().insert(user);
                    }
                }
                List<Long> remoteIds = new java.util.ArrayList<>();
                for (User user : remoteUsers) {
                    if (user.getUser_id() != null) remoteIds.add(user.getUser_id());
                }
                Log.d("SyncManager", "User IDs conservés: " + remoteIds);
                db.userDao().deleteNotInIds(remoteIds);
            } else {
                Log.d("SyncManager", "User: rien reçu");
            }
        } catch (Exception e) {
            Log.e("SyncManager", "User sync failed", e);
        }
    }

    public static void syncPFADossiers(Context context) {
        PFADossierApi api = retrofit.create(PFADossierApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<PFADossier>> response = api.getAllPFADossiers().execute();
            if (response.isSuccessful() && response.body() != null) {
                List<PFADossier> remoteDossiers = response.body();
                Log.d("SyncManager", "PFADossiers reçus: " + remoteDossiers.size());
                for (PFADossier dossier : remoteDossiers) {
                    Log.d("SyncManager", "PFADossier reçu: id=" + dossier.getPfa_id() + ", title=" + dossier.getTitle() + ", status=" + dossier.getCurrent_status());
                    if (dossier.getPfa_id() == null) continue;
                    PFADossier local = db.pfaDossierDao().getById(dossier.getPfa_id());
                    if (local != null) {
                        Log.d("SyncManager", "PFADossier local avant update: id=" + local.getPfa_id() + ", status=" + local.getCurrent_status());
                        // Merger: ne pas écraser avec null
                        if (dossier.getStudent_id() != null) local.setStudent_id(dossier.getStudent_id());
                        if (dossier.getSupervisor_id() != null) local.setSupervisor_id(dossier.getSupervisor_id());
                        if (dossier.getTitle() != null) local.setTitle(dossier.getTitle());
                        if (dossier.getDescription() != null) local.setDescription(dossier.getDescription());
                        if (dossier.getCurrent_status() != null) local.setCurrent_status(dossier.getCurrent_status());
                        if (dossier.getUpdated_at() != null) local.setUpdated_at(dossier.getUpdated_at());
                        db.pfaDossierDao().update(local);
                        PFADossier updated = db.pfaDossierDao().getById(dossier.getPfa_id());
                        Log.d("SyncManager", "PFADossier local après update: id=" + updated.getPfa_id() + ", status=" + updated.getCurrent_status());
                    } else {
                        db.pfaDossierDao().insert(dossier);
                    }
                }
                List<Long> remoteIds = new java.util.ArrayList<>();
                for (PFADossier dossier : remoteDossiers) {
                    if (dossier.getPfa_id() != null) remoteIds.add(dossier.getPfa_id());
                }
                Log.d("SyncManager", "PFADossier IDs conservés: " + remoteIds);
                db.pfaDossierDao().deleteNotInIds(remoteIds);
            } else {
                Log.d("SyncManager", "PFADossier: rien reçu");
            }
        } catch (Exception e) {
            Log.e("SyncManager", "PFADossier sync failed", e);
        }
    }

    public static void syncConventions(Context context) {
        ConventionApi api = retrofit.create(ConventionApi.class);
        AppDatabase db = AppDatabase.getInstance(context);
        try {
            Response<List<Convention>> response = api.getAllConventions().execute();
            if (response.isSuccessful() && response.body() != null) {
                List<Convention> remoteConventions = response.body();
                Log.d("SyncManager", "Conventions reçues: " + remoteConventions.size());
                for (Convention conv : remoteConventions) {
                    Log.d("SyncManager", "Convention reçue: id=" + conv.getConvention_id() + ", company=" + conv.getCompany_name());
                    if (conv.getConvention_id() == null) continue;
                    Convention local = db.conventionDao().getById(conv.getConvention_id());
                    if (local != null) {
                        // Merger: ne pas écraser avec null
                        if (conv.getPfa_id() != null) local.setPfa_id(conv.getPfa_id());
                        if (conv.getCompany_name() != null) local.setCompany_name(conv.getCompany_name());
                        if (conv.getCompany_address() != null) local.setCompany_address(conv.getCompany_address());
                        if (conv.getCompany_supervisor_name() != null) local.setCompany_supervisor_name(conv.getCompany_supervisor_name());
                        if (conv.getCompany_supervisor_email() != null) local.setCompany_supervisor_email(conv.getCompany_supervisor_email());
                        if (conv.getStart_date() != null) local.setStart_date(conv.getStart_date());
                        if (conv.getEnd_date() != null) local.setEnd_date(conv.getEnd_date());
                        if (conv.getState() != null) local.setState(conv.getState());
                        if (conv.getScanned_file_uri() != null) local.setScanned_file_uri(conv.getScanned_file_uri());
                        if (conv.getIs_validated() != null) local.setIs_validated(conv.getIs_validated());
                        if (conv.getAdmin_comment() != null) local.setAdmin_comment(conv.getAdmin_comment());
                        db.conventionDao().update(local);
                    } else {
                        db.conventionDao().insert(conv);
                    }
                }
                List<Long> remoteIds = new java.util.ArrayList<>();
                for (Convention conv : remoteConventions) {
                    if (conv.getConvention_id() != null) remoteIds.add(conv.getConvention_id());
                }
                Log.d("SyncManager", "Convention IDs conservés: " + remoteIds);
                db.conventionDao().deleteNotInIds(remoteIds);
            } else {
                Log.d("SyncManager", "Convention: rien reçu");
            }
        } catch (Exception e) {
            Log.e("SyncManager", "Convention sync failed", e);
        }
    }
}
