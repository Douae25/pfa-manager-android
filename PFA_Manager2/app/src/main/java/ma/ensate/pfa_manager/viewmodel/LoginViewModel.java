package ma.ensate.pfa_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<String> loginResult = new MutableLiveData<>();

    public LiveData<String> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            loginResult.setValue("Champs vides !");
            return;
        }

        if (email.equals("admin@emsi.ma") && password.equals("admin123")) {
            loginResult.setValue("Success");
        } else {
            loginResult.setValue("Identifiants incorrects");
        }
    }
}