package ma.ensate.pfa_manager.viewmodel.coordinateur_filiere;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class StudentsWithEvaluationsViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final long departmentId;

    public StudentsWithEvaluationsViewModelFactory(Application application, long departmentId) {
        this.application = application;
        this.departmentId = departmentId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StudentsWithEvaluationsViewModel.class)) {
            return (T) new StudentsWithEvaluationsViewModel(application, departmentId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}