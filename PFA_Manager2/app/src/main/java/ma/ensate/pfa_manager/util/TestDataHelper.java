package ma.ensate.pfa_manager.util;

import android.content.Context;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.*;

/**
 * Classe utilitaire pour insérer des données de test dans la base de données
 * À UTILISER UNIQUEMENT EN DÉVELOPPEMENT
 */
public class TestDataHelper {

    public static void insertTestData(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            // Vérifier si les données existent déjà


            // === CRÉER UN ENCADRANT ===
            User supervisor = new User();
            supervisor.setFirst_name("Abdeljebar");
            supervisor.setLast_name("Mansour");
            supervisor.setEmail("mansour@ensate.ma");
            supervisor.setRole(Role.PROFESSOR);
            supervisor.setPhone_number("0612345678");
            supervisor.setCreated_at(System.currentTimeMillis());
            long supervisorId = db.userDao().insert(supervisor);

            // === CRÉER DES ÉTUDIANTS ===
            User student1 = new User();
            student1.setFirst_name("Ahmed");
            student1.setLast_name("Alami");
            student1.setEmail("ahmed.alami@ensate.ma");
            student1.setRole(Role.STUDENT);
            student1.setCreated_at(System.currentTimeMillis());
            long student1Id = db.userDao().insert(student1);

            User student2 = new User();
            student2.setFirst_name("Fatima");
            student2.setLast_name("Zahra");
            student2.setEmail("fatima.zahra@ensate.ma");
            student2.setRole(Role.STUDENT);
            student2.setCreated_at(System.currentTimeMillis());
            long student2Id = db.userDao().insert(student2);

            User student3 = new User();
            student3.setFirst_name("Youssef");
            student3.setLast_name("Benkirane");
            student3.setEmail("youssef.benkirane@ensate.ma");
            student3.setRole(Role.STUDENT);
            student3.setCreated_at(System.currentTimeMillis());
            long student3Id = db.userDao().insert(student3);

            // === CRÉER DES PFAs ===
            PFADossier pfa1 = new PFADossier();
            pfa1.setStudent_id(student1Id);
            pfa1.setSupervisor_id(supervisorId);
            pfa1.setTitle("Application mobile de gestion universitaire");
            pfa1.setDescription("Développement d'une application Android pour la gestion des notes");
            pfa1.setCurrent_status(PFAStatus.IN_PROGRESS);
            pfa1.setUpdated_at(System.currentTimeMillis());
            long pfa1Id = db.pfaDossierDao().insert(pfa1);

            PFADossier pfa2 = new PFADossier();
            pfa2.setStudent_id(student2Id);
            pfa2.setSupervisor_id(supervisorId);
            pfa2.setTitle("Système de recommandation par Machine Learning");
            pfa2.setDescription("Développement d'un système de recommandation de livres");
            pfa2.setCurrent_status(PFAStatus.CONVENTION_PENDING);
            pfa2.setUpdated_at(System.currentTimeMillis());
            long pfa2Id = db.pfaDossierDao().insert(pfa2);

            PFADossier pfa3 = new PFADossier();
            pfa3.setStudent_id(student3Id);
            pfa3.setSupervisor_id(supervisorId);
            pfa3.setTitle("Plateforme e-learning interactive");
            pfa3.setDescription("Plateforme d'apprentissage en ligne avec gamification");
            pfa3.setCurrent_status(PFAStatus.IN_PROGRESS);
            pfa3.setUpdated_at(System.currentTimeMillis());
            long pfa3Id = db.pfaDossierDao().insert(pfa3);

            // === CRÉER DES LIVRABLES ===
            Deliverable del1 = new Deliverable();
            del1.setPfa_id(pfa1Id);
            del1.setFile_title("Rapport d'avancement - Janvier");
            del1.setFile_uri("content://documents/rapport_janvier.pdf");
            del1.setUploaded_at(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L); // Il y a 2 jours
            db.deliverableDao().insert(del1);

            Deliverable del2 = new Deliverable();
            del2.setPfa_id(pfa1Id);
            del2.setFile_title("Maquettes UI/UX");
            del2.setFile_uri("content://documents/maquettes.pdf");
            del2.setUploaded_at(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L); // Il y a 5 jours
            db.deliverableDao().insert(del2);

            Deliverable del3 = new Deliverable();
            del3.setPfa_id(pfa3Id);
            del3.setFile_title("Code source - Sprint 1");
            del3.setFile_uri("content://documents/code_sprint1.zip");
            del3.setUploaded_at(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L); // Il y a 1 jour
            db.deliverableDao().insert(del3);

            // === CRÉER DES CONVENTIONS ===
            Convention conv1 = new Convention();
            conv1.setPfa_id(pfa2Id);
            conv1.setCompany_name("TechCorp Morocco");
            conv1.setCompany_address("Casablanca, Maroc");
            conv1.setCompany_supervisor_name("M. Hassan Bennani");
            conv1.setCompany_supervisor_email("h.bennani@techcorp.ma");
            conv1.setStart_date(System.currentTimeMillis());
            conv1.setEnd_date(System.currentTimeMillis() + 120L * 24 * 60 * 60 * 1000); // +4 mois
            conv1.setScanned_file_uri("content://documents/convention_techcorp.pdf");
            conv1.setIs_validated(false);
            conv1.setState(ConventionState.UPLOADED);
            db.conventionDao().insert(conv1);

            // === CRÉER DES SOUTENANCES ===
            Soutenance sout1 = new Soutenance();
            sout1.setPfa_id(pfa1Id);
            sout1.setLocation("Salle A101");
            sout1.setDate_soutenance(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000); // Dans 30 jours
            sout1.setStatus(SoutenanceStatus.PLANNED);
            sout1.setCreated_at(System.currentTimeMillis());
            db.soutenanceDao().insert(sout1);

            Soutenance sout2 = new Soutenance();
            sout2.setPfa_id(pfa3Id);
            sout2.setLocation("Amphithéâtre B");
            sout2.setDate_soutenance(System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000); // Dans 45 jours
            sout2.setStatus(SoutenanceStatus.PLANNED);
            sout2.setCreated_at(System.currentTimeMillis());
            db.soutenanceDao().insert(sout2);

            System.out.println("✅ Données de test insérées avec succès!");
        });
    }
}