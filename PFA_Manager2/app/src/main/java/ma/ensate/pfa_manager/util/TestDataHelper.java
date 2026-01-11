package ma.ensate.pfa_manager.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.*;

public class TestDataHelper {

    public static void insertTestData(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            if (db.userDao().getAllUsers().size() > 0) {
                return;
            }

            String testFilesDir = createTestFiles(context);

            // --- 1. Création des Utilisateurs ---
            User admin = new User();
            admin.setFirst_name("Super");
            admin.setLast_name("Admin");
            admin.setEmail("admin@ensate.ma");
            admin.setPassword("123456");
            admin.setRole(Role.ADMIN);
            admin.setCreated_at(System.currentTimeMillis());
            db.userDao().insert(admin);

            User coord = new User();
            coord.setFirst_name("Hassan");
            coord.setLast_name("Amrani");
            coord.setEmail("coord@ensate.ma");
            coord.setPassword("123456");
            coord.setRole(Role.COORDINATOR);
            coord.setCreated_at(System.currentTimeMillis());
            db.userDao().insert(coord);

            User supervisor = new User();
            supervisor.setFirst_name("Abdeljebar");
            supervisor.setLast_name("Mansour");
            supervisor.setEmail("mansour@ensate.ma");
            supervisor.setPassword("123456");
            supervisor.setRole(Role.PROFESSOR);
            supervisor.setPhone_number("0612345678");
            supervisor.setCreated_at(System.currentTimeMillis());
            long supervisorId = db.userDao().insert(supervisor);

            User student1 = new User();
            student1.setFirst_name("Ahmed");
            student1.setLast_name("Alami");
            student1.setEmail("ahmed.alami@ensate.ma");
            student1.setPassword("123456");
            student1.setRole(Role.STUDENT);
            student1.setPhone_number("0611111111");
            student1.setCreated_at(System.currentTimeMillis());
            long student1Id = db.userDao().insert(student1);

            User student2 = new User();
            student2.setFirst_name("Fatima");
            student2.setLast_name("Zahra");
            student2.setEmail("fatima.zahra@ensate.ma");
            student2.setPassword("123456");
            student2.setRole(Role.STUDENT);
            student2.setPhone_number("0622222222");
            student2.setCreated_at(System.currentTimeMillis());
            long student2Id = db.userDao().insert(student2);

            User student3 = new User();
            student3.setFirst_name("Youssef");
            student3.setLast_name("Benkirane");
            student3.setEmail("youssef.benkirane@ensate.ma");
            student3.setPassword("123456");
            student3.setRole(Role.STUDENT);
            student3.setPhone_number("0633333333");
            student3.setCreated_at(System.currentTimeMillis());
            long student3Id = db.userDao().insert(student3);

            // --- 2. Ajout des Critères d'Évaluation (CODE AJOUTÉ) ---
            if (db.evaluationCriteriaDao().count() == 0) {
                EvaluationCriteria c1 = new EvaluationCriteria();
                c1.setLabel("Qualité technique");
                c1.setDescription("Code propre, architecture, bonnes pratiques");
                c1.setWeight(0.25);
                c1.setIs_active(true);
                db.evaluationCriteriaDao().insert(c1);

                EvaluationCriteria c2 = new EvaluationCriteria();
                c2.setLabel("Présentation orale");
                c2.setDescription("Clarté, structure, gestion du temps");
                c2.setWeight(0.20);
                c2.setIs_active(true);
                db.evaluationCriteriaDao().insert(c2);

                EvaluationCriteria c3 = new EvaluationCriteria();
                c3.setLabel("Rapport écrit");
                c3.setDescription("Qualité rédactionnelle, documentation");
                c3.setWeight(0.20);
                c3.setIs_active(true);
                db.evaluationCriteriaDao().insert(c3);

                EvaluationCriteria c4 = new EvaluationCriteria();
                c4.setLabel("Innovation");
                c4.setDescription("Originalité, créativité de la solution");
                c4.setWeight(0.15);
                c4.setIs_active(true);
                db.evaluationCriteriaDao().insert(c4);

                EvaluationCriteria c5 = new EvaluationCriteria();
                c5.setLabel("Réponses aux questions");
                c5.setDescription("Maîtrise du sujet, pertinence des réponses");
                c5.setWeight(0.20);
                c5.setIs_active(true);
                db.evaluationCriteriaDao().insert(c5);
            }

            // --- 3. Création des PFA ---
            PFADossier pfa1 = new PFADossier();
            pfa1.setStudent_id(student1Id);
            pfa1.setSupervisor_id(supervisorId);
            pfa1.setTitle("Application mobile de gestion universitaire");
            pfa1.setDescription("Développement d'une application Android pour la gestion des notes et emplois du temps des étudiants");
            pfa1.setCurrent_status(PFAStatus.IN_PROGRESS);
            pfa1.setUpdated_at(System.currentTimeMillis());
            long pfa1Id = db.pfaDossierDao().insert(pfa1);

            PFADossier pfa2 = new PFADossier();
            pfa2.setStudent_id(student2Id);
            pfa2.setSupervisor_id(supervisorId);
            pfa2.setTitle("Système de recommandation par Machine Learning");
            pfa2.setDescription("Développement d'un système de recommandation de livres utilisant les algorithmes de ML");
            pfa2.setCurrent_status(PFAStatus.CONVENTION_PENDING);
            pfa2.setUpdated_at(System.currentTimeMillis());
            long pfa2Id = db.pfaDossierDao().insert(pfa2);

            PFADossier pfa3 = new PFADossier();
            pfa3.setStudent_id(student3Id);
            pfa3.setSupervisor_id(supervisorId);
            pfa3.setTitle("Plateforme e-learning interactive");
            pfa3.setDescription("Plateforme d'apprentissage en ligne avec gamification et suivi de progression");
            pfa3.setCurrent_status(PFAStatus.IN_PROGRESS);
            pfa3.setUpdated_at(System.currentTimeMillis());
            long pfa3Id = db.pfaDossierDao().insert(pfa3);

            // --- 4. Création des Livrables ---
            Deliverable del1 = new Deliverable();
            del1.setPfa_id(pfa1Id);
            del1.setFile_title("Rapport d'avancement - Janvier 2025");
            del1.setFile_uri(testFilesDir + "/rapport_test.pdf");
            del1.setUploaded_at(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L);
            db.deliverableDao().insert(del1);

            Deliverable del2 = new Deliverable();
            del2.setPfa_id(pfa1Id);
            del2.setFile_title("Cahier des charges");
            del2.setFile_uri(testFilesDir + "/cahier_charges.pdf");
            del2.setUploaded_at(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L);
            db.deliverableDao().insert(del2);

            Deliverable del3 = new Deliverable();
            del3.setPfa_id(pfa1Id);
            del3.setFile_title("Maquettes UI/UX");
            del3.setFile_uri(testFilesDir + "/maquettes.pdf");
            del3.setUploaded_at(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L);
            db.deliverableDao().insert(del3);

            Deliverable del4 = new Deliverable();
            del4.setPfa_id(pfa2Id);
            del4.setFile_title("État de l'art - ML");
            del4.setFile_uri(testFilesDir + "/etat_art.pdf");
            del4.setUploaded_at(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L);
            db.deliverableDao().insert(del4);

            Deliverable del5 = new Deliverable();
            del5.setPfa_id(pfa3Id);
            del5.setFile_title("Spécifications techniques");
            del5.setFile_uri(testFilesDir + "/specs_techniques.pdf");
            del5.setUploaded_at(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L);
            db.deliverableDao().insert(del5);

            Deliverable del6 = new Deliverable();
            del6.setPfa_id(pfa3Id);
            del6.setFile_title("Diagrammes UML");
            del6.setFile_uri(testFilesDir + "/diagrammes_uml.pdf");
            del6.setUploaded_at(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
            db.deliverableDao().insert(del6);

            // --- 5. Création de la Convention ---
            Convention conv1 = new Convention();
            conv1.setPfa_id(pfa2Id);
            conv1.setCompany_name("TechCorp Morocco");
            conv1.setCompany_address("123 Rue Mohammed V, Casablanca");
            conv1.setCompany_supervisor_name("M. Hassan Bennani");
            conv1.setCompany_supervisor_email("h.bennani@techcorp.ma");
            conv1.setStart_date(System.currentTimeMillis());
            conv1.setEnd_date(System.currentTimeMillis() + 120L * 24 * 60 * 60 * 1000);
            conv1.setScanned_file_uri(testFilesDir + "/convention_test.pdf");
            conv1.setIs_validated(false);
            conv1.setState(ConventionState.UPLOADED);
            db.conventionDao().insert(conv1);

            // --- 6. Création des Soutenances ---
            Soutenance sout1 = new Soutenance();
            sout1.setPfa_id(pfa1Id);
            sout1.setLocation("Salle A101");
            sout1.setDate_soutenance(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
            sout1.setStatus(SoutenanceStatus.PLANNED);
            sout1.setCreated_at(System.currentTimeMillis());
            db.soutenanceDao().insert(sout1);

            Soutenance sout2 = new Soutenance();
            sout2.setPfa_id(pfa3Id);
            sout2.setLocation("Amphithéâtre B");
            sout2.setDate_soutenance(System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000);
            sout2.setStatus(SoutenanceStatus.PLANNED);
            sout2.setCreated_at(System.currentTimeMillis());
            db.soutenanceDao().insert(sout2);

            System.out.println("✅ Données de test insérées avec succès!");
        });
    }

    private static String createTestFiles(Context context) {
        File testDir = new File(context.getFilesDir(), "test_deliverables");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }

        // Création des fichiers avec contenu réel
        createSimplePdf(testDir, "rapport_test.pdf", "RAPPORT D'AVANCEMENT", "Application mobile de gestion universitaire\n\nCe rapport présente l'état d'avancement du projet.\n\nDate: Janvier 2025\nAuteur: Ahmed Alami");
        createSimplePdf(testDir, "cahier_charges.pdf", "CAHIER DES CHARGES", "1. Introduction\n2. Objectifs\n3. Fonctionnalités\n4. Contraintes techniques\n\nCe document définit le périmètre du projet.");
        createSimplePdf(testDir, "maquettes.pdf", "MAQUETTES UI/UX", "Contenu:\n- Écran de connexion\n- Tableau de bord\n- Liste des projets\n\n(Simulation visuelle des interfaces)");
        createSimplePdf(testDir, "etat_art.pdf", "ÉTAT DE L'ART", "Étude des systèmes de recommandation existants:\n- Filtrage collaboratif\n- Filtrage basé sur le contenu\n- Approches hybrides");
        createSimplePdf(testDir, "specs_techniques.pdf", "SPÉCIFICATIONS TECHNIQUES", "Architecture technique de la plateforme e-learning:\n- Backend: Spring Boot\n- Frontend: React\n- Base de données: PostgreSQL");
        createSimplePdf(testDir, "diagrammes_uml.pdf", "DIAGRAMMES UML", "Diagrammes inclus:\n- Diagramme de cas d'utilisation\n- Diagramme de classes\n- Diagramme de séquence");
        createSimplePdf(testDir, "convention_test.pdf", "CONVENTION DE STAGE", "ENTRE: L'École Nationale des Sciences Appliquées\nET: TechCorp Morocco\n\nPOUR: Stage de fin d'études\nDurée: 4 mois");

        return testDir.getAbsolutePath();
    }

    // ✅ MÉTHODE CORRIGÉE POUR GÉNÉRER UN VRAI PDF LISIBLE
    private static void createSimplePdf(File dir, String fileName, String title, String content) {
        File file = new File(dir, fileName);
        if (file.exists()) {
            // Optionnel : on peut supprimer return ici si on veut forcer la régénération
            return;
        }

        // 1. Création du document PDF
        PdfDocument document = new PdfDocument();

        // 2. Définition de la page A4
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // 3. Préparation du dessin
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        // 4. Dessin du TITRE
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText(title, 50, 60, paint);

        // Ligne de séparation sous le titre
        paint.setStrokeWidth(2);
        canvas.drawLine(50, 70, 545, 70, paint);

        // 5. Dessin du CONTENU (Ligne par ligne)
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        paint.setStrokeWidth(0);

        int x = 50;
        int y = 110;

        // Découpage du texte pour gérer les sauts de ligne
        String[] lines = content.split("\n");
        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += 25; // Espace entre les lignes
        }

        // 6. Fin de la page et sauvegarde
        document.finishPage(page);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearAllData(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            db.clearAllTables();

            File testDir = new File(context.getFilesDir(), "test_deliverables");
            if (testDir.exists()) {
                for (File file : testDir.listFiles()) {
                    file.delete();
                }
                testDir.delete();
            }

            System.out.println("🗑️ Toutes les données ont été supprimées!");
        });
    }

    public static void resetAndReload(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            db.clearAllTables();

            File testDir = new File(context.getFilesDir(), "test_deliverables");
            if (testDir.exists()) {
                for (File file : testDir.listFiles()) {
                    file.delete();
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            insertTestData(context);
            System.out.println("🔄 Données réinitialisées avec succès!");
        });
    }
}