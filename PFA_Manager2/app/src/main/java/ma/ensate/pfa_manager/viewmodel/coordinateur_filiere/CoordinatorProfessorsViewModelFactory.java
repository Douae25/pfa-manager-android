package ma.ensate.pfa_manager.viewmodel.coordinateur_filiere;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CoordinatorProfessorsViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final long departmentId;

    public CoordinatorProfessorsViewModelFactory(Application application, long departmentId) {
        this.application = application;
        this.departmentId = departmentId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CoordinatorProfessorsViewModel.class)) {
            return (T) new CoordinatorProfessorsViewModel(application, departmentId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}