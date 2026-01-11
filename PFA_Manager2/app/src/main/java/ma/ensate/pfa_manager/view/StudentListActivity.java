package ma.ensate.pfa_manager.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.dto.StudentWithPFA;
import ma.ensate.pfa_manager.viewmodel.StudentListViewModel;

public class StudentListActivity extends AppCompatActivity
        implements StudentAdapter.OnStudentClickListener {

    private RecyclerView recyclerView;
    private FrameLayout loadingOverlay;
    private LinearLayout emptyStateLayout;
    private TextView tvStudentCount;
    private ImageView btnBack;

    private StudentListViewModel viewModel;
    private StudentAdapter adapter;

    private Long currentSupervisorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentSupervisorId = getIntent().getLongExtra("USER_ID", -1L);

        if (currentSupervisorId == -1L) {
            Toast.makeText(this, "Erreur d'identifiant prof", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_student_list);

        initViews();
        setupRecyclerView();
        initViewModel();
        observeData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewStudents);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new StudentAdapter(this);
        adapter.setOnStudentClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(StudentListViewModel.class);
        viewModel.setSupervisorId(currentSupervisorId);
    }

    private void observeData() {
        viewModel.getStudentsWithPFA().observe(this, students -> {
            hideLoading();
            if (students != null && !students.isEmpty()) {
                adapter.setStudents(students);
                showStudentsList();
            } else {
                showEmptyState();
            }
        });

        viewModel.getStudentCount().observe(this, count -> {
            tvStudentCount.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    private void showStudentsList() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStudentClick(StudentWithPFA student) {
        Intent intent = new Intent(this, StudentDetailActivity.class);
        intent.putExtra("STUDENT_ID", student.getStudentId());
        intent.putExtra("USER_ID", currentSupervisorId);
        startActivity(intent);
    }
}