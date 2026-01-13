package ma.ensate.pfa_manager.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.app.Application;
import ma.ensate.pfa_manager.repository.DepartmentCacheRepository;
import ma.ensate.pfa_manager.R;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();


    private static DepartmentCacheRepository departmentCacheRepository;

    public UserAdapter(List<User> users, Application application) {
        setUsers(users);
        if (departmentCacheRepository == null) {
            departmentCacheRepository = new DepartmentCacheRepository(application);
        }
    }

    public void setUsers(List<User> users) {
        this.allUsers = users == null ? new ArrayList<>() : new ArrayList<>(users);
        this.users = new ArrayList<>(this.allUsers);
        notifyDataSetChanged();
    }

    public void filter(String query, Role role) {
        String q = query == null ? "" : query.toLowerCase();
        users.clear();
        for (User u : allUsers) {
            boolean matchesQ = q.isEmpty() || (u.getFirst_name() != null && u.getFirst_name().toLowerCase().contains(q))
                    || (u.getLast_name() != null && u.getLast_name().toLowerCase().contains(q))
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q));
            boolean matchesRole = role == null || u.getRole() == role;
            if (matchesQ && matchesRole) users.add(u);
        }
        notifyDataSetChanged();
    }

    public void filterByQuery(String query) {
        String q = query == null ? "" : query.toLowerCase();
        users.clear();
        for (User u : allUsers) {
            boolean matches = q.isEmpty() || (u.getFirst_name() != null && u.getFirst_name().toLowerCase().contains(q))
                    || (u.getLast_name() != null && u.getLast_name().toLowerCase().contains(q))
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q));
            if (matches) users.add(u);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole, tvDepartment;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvDepartment = itemView.findViewById(R.id.tvUserDepartment);
        }

        void bind(User u) {
            String name = (u.getFirst_name() == null ? "" : u.getFirst_name()) + " " + (u.getLast_name() == null ? "" : u.getLast_name());
            tvName.setText(name.trim());
            tvEmail.setText(u.getEmail() == null ? "" : u.getEmail());
            tvRole.setText(u.getRole() == null ? "" : u.getRole().name());
            if (u.getDepartment_id() != null) {
                departmentCacheRepository.getDepartmentNameById(u.getDepartment_id(), deptName -> {
                    if (deptName != null) {
                        tvDepartment.setText(deptName);
                        tvDepartment.setVisibility(View.VISIBLE);
                    } else {
                        tvDepartment.setText("");
                        tvDepartment.setVisibility(View.GONE);
                    }
                });
            } else {
                tvDepartment.setText("");
                tvDepartment.setVisibility(View.GONE);
            }
        }
    }
}
