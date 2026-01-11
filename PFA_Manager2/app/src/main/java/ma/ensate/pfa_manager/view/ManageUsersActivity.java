package ma.ensate.pfa_manager.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.repository.ConventionRepository;
import ma.ensate.pfa_manager.repository.UserRepository;
import ma.ensate.pfa_manager.repository.LanguageRepository;
import ma.ensate.pfa_manager.view.adapter.UserAdapter;
import ma.ensate.pfa_manager.viewmodel.AdminViewModel;
import ma.ensate.pfa_manager.viewmodel.AdminViewModelFactory;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModel;
import ma.ensate.pfa_manager.viewmodel.SettingsViewModelFactory;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private AdminViewModel adminViewModel;
    private SettingsViewModel settingsViewModel;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup language first
        LanguageRepository languageRepository = new LanguageRepository(this);
        SettingsViewModelFactory settingsFactory = new SettingsViewModelFactory(languageRepository);
        settingsViewModel = new ViewModelProvider(this, settingsFactory).get(SettingsViewModel.class);
        settingsViewModel.applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        setupLanguageToggle();

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        etSearch = findViewById(R.id.etSearchUser);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ConventionRepository conventionRepository = new ConventionRepository(getApplication());
        UserRepository userRepository = new UserRepository(getApplication());
        AdminViewModelFactory factory = new AdminViewModelFactory(conventionRepository, userRepository);
        adminViewModel = new ViewModelProvider(this, factory).get(AdminViewModel.class);

        userAdapter = new UserAdapter(new ArrayList<>());
        usersRecyclerView.setAdapter(userAdapter);

        adminViewModel.getAllUsers().observe(this, users -> {
            // Filter only STUDENT users
            if (users != null) {
                List<User> students = new ArrayList<>();
                for (User u : users) {
                    if (u.getRole() == Role.STUDENT) {
                        students.add(u);
                    }
                }
                userAdapter.setUsers(students);
            }
        });
        adminViewModel.loadAllUsers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupLanguageToggle() {
        TextView langFr = findViewById(R.id.langFr);
        TextView langEn = findViewById(R.id.langEn);
        if (langFr != null) {
            langFr.setOnClickListener(v -> {
                settingsViewModel.changeLanguage("fr");
                recreate();
            });
        }
        if (langEn != null) {
            langEn.setOnClickListener(v -> {
                settingsViewModel.changeLanguage("en");
                recreate();
            });
        }
    }

    private void applyFilter() {
        String query = etSearch.getText() == null ? "" : etSearch.getText().toString().toLowerCase();
        userAdapter.filterByQuery(query);
    }
}


