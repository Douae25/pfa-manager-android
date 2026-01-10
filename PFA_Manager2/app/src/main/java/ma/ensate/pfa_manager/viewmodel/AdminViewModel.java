package ma.ensate.pfa_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.UserRepository;

public class AdminViewModel extends ViewModel {
    private MutableLiveData<List<Convention>> pendingConventions = new MutableLiveData<>();
    private MutableLiveData<List<Convention>> signedConventions = new MutableLiveData<>();
    private MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private MutableLiveData<String> actionResult = new MutableLiveData<>();
    private MutableLiveData<Convention> selectedConvention = new MutableLiveData<>();

    private ConventionRepository conventionRepository;
    private UserRepository userRepository;

    public AdminViewModel(ConventionRepository conventionRepository, UserRepository userRepository) {
        this.conventionRepository = conventionRepository;
        this.userRepository = userRepository;
    }

    public LiveData<List<Convention>> getPendingConventions() {
        return pendingConventions;
    }

    public LiveData<List<Convention>> getSignedConventions() {
        return signedConventions;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<String> getActionResult() {
        return actionResult;
    }

    public LiveData<Convention> getSelectedConvention() {
        return selectedConvention;
    }

    // Load pending convention requests
    public void loadPendingConventions() {
        conventionRepository.getConventionsByState(ConventionState.PENDING, conventions -> {
            pendingConventions.postValue(conventions);
        });
    }

    // Load signed conventions
    public void loadSignedConventions() {
        conventionRepository.getConventionsByState(ConventionState.UPLOADED, conventions -> {
            signedConventions.postValue(conventions);
        });
    }

    // Load all users
    public void loadAllUsers() {
        userRepository.getAllUsers(users -> {
            allUsers.postValue(users);
        });
    }

    // Approve pending convention request (PENDING -> GENERATED)
    public void approveConvention(Convention convention) {
        convention.setState(ConventionState.GENERATED);
        convention.setIs_validated(true);
        conventionRepository.updateConvention(convention);
        actionResult.postValue("Convention approuvée avec succès");
    }

    // Reject pending convention request (PENDING -> REFUSED)
    public void rejectConvention(Convention convention, String comment) {
        convention.setState(ConventionState.REFUSED);
        convention.setIs_validated(false);
        convention.setAdmin_comment(comment);
        conventionRepository.updateConvention(convention);
        actionResult.postValue("Convention refusée: " + comment);
    }

    // Validate uploaded convention (UPLOADED -> VALIDATED)
    public void validateUploadedConvention(Convention convention) {
        convention.setState(ConventionState.VALIDATED);
        convention.setIs_validated(true);
        conventionRepository.updateConvention(convention);
        actionResult.postValue("Convention validée avec succès");
    }

    // Reject uploaded convention (UPLOADED -> REJECTED)
    public void rejectUploadedConvention(Convention convention, String comment) {
        convention.setState(ConventionState.REJECTED);
        convention.setIs_validated(false);
        convention.setAdmin_comment(comment);
        conventionRepository.updateConvention(convention);
        actionResult.postValue("Convention rejetée: " + comment);
    }

    // Update user
    public void updateUser(User user) {
        userRepository.update(user);
        actionResult.postValue("Utilisateur mis à jour");
    }

    // Delete user
    public void deleteUser(User user) {
        userRepository.delete(user);
        actionResult.postValue("Utilisateur supprimé");
    }

    // Set selected convention for detail view
    public void selectConvention(Convention convention) {
        selectedConvention.postValue(convention);
    }
}

