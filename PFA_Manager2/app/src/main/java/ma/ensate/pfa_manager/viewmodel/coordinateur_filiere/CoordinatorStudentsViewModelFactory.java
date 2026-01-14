package ma.ensate.pfa_manager.viewmodel.coordinateur_filiere;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CoordinatorStudentsViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final long coordinatorId;

    public CoordinatorStudentsViewModelFactory(Application application, long coordinatorId) {
        this.application = application;
        this.coordinatorId = coordinatorId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CoordinatorStudentsViewModel.class)) {
            return (T) new CoordinatorStudentsViewModel(application, coordinatorId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}