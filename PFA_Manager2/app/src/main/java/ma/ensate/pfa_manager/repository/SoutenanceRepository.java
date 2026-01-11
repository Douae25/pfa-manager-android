package ma.ensate.pfa_manager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.SoutenanceDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.dto.PFAWithSoutenance;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.SoutenanceStatus;

public class SoutenanceRepository {

    private final SoutenanceDao soutenanceDao;
    private final PFADossierDao pfaDossierDao;
    private final ExecutorService executor;

    public SoutenanceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.soutenanceDao = db.soutenanceDao();
        this.pfaDossierDao = db.pfaDossierDao();
        this.executor = Executors.newSingleThreadExecutor();
    }


    public LiveData<List<PFADossier>> getPFAsEligibles(Long supervisorId) {
        return soutenanceDao.getPFAsNonPlanifies(supervisorId);
    }

    public LiveData<List<PFADossier>> getAllPFAsBySupervisor(Long supervisorId) {
        return soutenanceDao.getAllPFAsBySupervisor(supervisorId);
    }

    public LiveData<List<Soutenance>> getAllSoutenancesBySupervisor(Long supervisorId) {
        return soutenanceDao.getAllSoutenancesBySupervisor(supervisorId);
    }

    public LiveData<List<PFAWithSoutenance>> getPFAsWithSoutenances(Long supervisorId) {
        MediatorLiveData<List<PFAWithSoutenance>> result = new MediatorLiveData<>();

        LiveData<List<PFADossier>> pfasLive = pfaDossierDao.getPFAsBySupervisor(supervisorId);
        LiveData<List<Soutenance>> soutenancesLive = soutenanceDao.getAllSoutenancesBySupervisor(supervisorId);

        result.addSource(pfasLive, pfas -> {
            List<Soutenance> soutenances = soutenancesLive.getValue();
            if (pfas != null) {
                result.setValue(combine(pfas, soutenances));
            }
        });

        result.addSource(soutenancesLive, soutenances -> {
            List<PFADossier> pfas = pfasLive.getValue();
            if (pfas != null) {
                result.setValue(combine(pfas, soutenances));
            }
        });

        return result;
    }

    private List<PFAWithSoutenance> combine(List<PFADossier> pfas, List<Soutenance> soutenances) {
        List<PFAWithSoutenance> result = new ArrayList<>();
        for (PFADossier pfa : pfas) {
            PFAWithSoutenance item = new PFAWithSoutenance();
            item.pfa = pfa;
            item.soutenance = null;

            if (soutenances != null) {
                for (Soutenance s : soutenances) {
                    if (s.getPfa_id().equals(pfa.getPfa_id())) {
                        item.soutenance = s;
                        break;
                    }
                }
            }
            result.add(item);
        }
        return result;
    }

    // Méthodes spécifiques au PlanningViewModel (avec gestion d'erreur UI)
    public void planifierSoutenance(Soutenance soutenance, OnSoutenanceListener listener) {
        executor.execute(() -> {
            try {
                if (soutenanceDao.countSoutenanceByPfa(soutenance.getPfa_id()) > 0) {
                    listener.onError("Une soutenance existe déjà pour ce PFA.");
                    return;
                }
                soutenanceDao.insert(soutenance);
                listener.onSuccess("Soutenance planifiée avec succès !");
            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    public void modifierSoutenance(Soutenance soutenance, OnSoutenanceListener listener) {
        executor.execute(() -> {
            try {
                soutenanceDao.update(soutenance);
                listener.onSuccess("Soutenance modifiée avec succès !");
            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    public void supprimerSoutenance(long soutenanceId, OnSoutenanceListener listener) {
        executor.execute(() -> {
            try {
                soutenanceDao.deleteById(soutenanceId);
                listener.onSuccess("Soutenance supprimée !");
            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }



    public void insert(Soutenance soutenance, OnSoutenanceInsertedListener listener) {
        executor.execute(() -> {
            long id = soutenanceDao.insert(soutenance);
            soutenance.setSoutenance_id(id);
            if (listener != null) {
                listener.onSoutenanceInserted(soutenance);
            }
        });
    }

    public void update(Soutenance soutenance) {
        executor.execute(() -> soutenanceDao.update(soutenance));
    }

    public void delete(Soutenance soutenance) {
        executor.execute(() -> soutenanceDao.delete(soutenance));
    }

    public void getById(long id, OnSoutenanceFetchedListener listener) {
        executor.execute(() -> {
            Soutenance soutenance = soutenanceDao.getById(id);
            if (listener != null) {
                listener.onSoutenanceFetched(soutenance);
            }
        });
    }

    public void getByPfaId(long pfaId, OnSoutenanceFetchedListener listener) {
        executor.execute(() -> {
            Soutenance soutenance = soutenanceDao.getByPfaId(pfaId);
            if (listener != null) {
                listener.onSoutenanceFetched(soutenance);
            }
        });
    }

    public void getByStatus(SoutenanceStatus status, OnSoutenancesListFetchedListener listener) {
        executor.execute(() -> {
            List<Soutenance> soutenances = soutenanceDao.getByStatus(status);
            if (listener != null) {
                listener.onSoutenancesListFetched(soutenances);
            }
        });
    }

    public void getAll(OnSoutenancesListFetchedListener listener) {
        executor.execute(() -> {
            List<Soutenance> soutenances = soutenanceDao.getAll();
            if (listener != null) {
                listener.onSoutenancesListFetched(soutenances);
            }
        });
    }


    public interface OnSoutenanceListener {
        void onSuccess(String message);
        void onError(String message);
    }

    // Interfaces standards de la branche Main
    public interface OnSoutenanceInsertedListener {
        void onSoutenanceInserted(Soutenance soutenance);
    }

    public interface OnSoutenanceFetchedListener {
        void onSoutenanceFetched(Soutenance soutenance);
    }

    public interface OnSoutenancesListFetchedListener {
        void onSoutenancesListFetched(List<Soutenance> soutenances);
    }
}