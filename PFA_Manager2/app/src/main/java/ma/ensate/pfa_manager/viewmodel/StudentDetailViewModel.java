package ma.ensate.pfa_manager.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import ma.ensate.pfa_manager.model.*;
import ma.ensate.pfa_manager.repository.StudentDetailRepository;

public class StudentDetailViewModel extends AndroidViewModel {

    private final StudentDetailRepository repository;
    private final MutableLiveData<Long> studentIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> pfaIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> supervisorIdLiveData = new MutableLiveData<>();

    private final LiveData<User> student;
    private final LiveData<List<PFADossier>> studentPFAs;
    private final LiveData<List<Deliverable>> deliverables;
    private final LiveData<Soutenance> soutenance;
    private final LiveData<Convention> convention;
    private final LiveData<Evaluation> evaluation;
    private final LiveData<Integer> deliverablesCount;

    public StudentDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new StudentDetailRepository(application);

        student = Transformations.switchMap(studentIdLiveData,
                id -> repository.getStudent(id));

        studentPFAs = Transformations.switchMap(studentIdLiveData,
                id -> repository.getStudentPFAs(id));

        deliverables = Transformations.switchMap(pfaIdLiveData,
                id -> repository.getPFADeliverables(id));

        soutenance = Transformations.switchMap(pfaIdLiveData,
                id -> repository.getPFASoutenance(id));

        convention = Transformations.switchMap(pfaIdLiveData,
                id -> repository.getPFAConvention(id));

        evaluation = Transformations.switchMap(pfaIdLiveData,
                id -> repository.getPFAEvaluation(id));

        deliverablesCount = Transformations.switchMap(pfaIdLiveData,
                id -> repository.countDeliverables(id));
    }

    public void setStudentId(Long studentId) {
        studentIdLiveData.setValue(studentId);
    }

    public void setPfaId(Long pfaId) {
        pfaIdLiveData.setValue(pfaId);
    }

    public void setSupervisorId(Long supervisorId) {
        supervisorIdLiveData.setValue(supervisorId);
    }

    public void syncFromApi() {
        Long supervisorId = supervisorIdLiveData.getValue();
        Long studentId = studentIdLiveData.getValue();

        if (supervisorId != null && studentId != null) {
            repository.syncStudentDetail(supervisorId, studentId);
        }
    }


    public LiveData<User> getStudent() { return student; }
    public LiveData<List<PFADossier>> getStudentPFAs() { return studentPFAs; }
    public LiveData<List<Deliverable>> getDeliverables() { return deliverables; }
    public LiveData<Soutenance> getSoutenance() { return soutenance; }
    public LiveData<Convention> getConvention() { return convention; }
    public LiveData<Integer> getDeliverablesCount() { return deliverablesCount; }
    public LiveData<Evaluation> getEvaluation() { return evaluation; }

    public LiveData<Boolean> getIsSyncing() {
        return repository.getIsSyncing();
    }
}