package ma.ensate.pfa_manager.viewmodel.coordinateur_filiere;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ProfessorsWithStatsViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final long departmentId;

    public ProfessorsWithStatsViewModelFactory(Application application, long departmentId) {
        this.application = application;
        this.departmentId = departmentId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfessorsWithStatsViewModel.class)) {
            return (T) new ProfessorsWithStatsViewModel(application, departmentId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}