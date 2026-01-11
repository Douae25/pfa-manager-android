package ma.ensate.pfa_manager.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ma.ensate.pfa_manager.R;

public class SoutenanceFormDialog extends BottomSheetDialogFragment {

    private static final String ARG_PFA_ID = "pfa_id";
    private static final String ARG_PFA_TITLE = "pfa_title";
    private static final String ARG_SOUTENANCE_ID = "soutenance_id";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_DATE = "date";

    private Long pfaId;
    private String pfaTitle;
    private Long soutenanceId;
    private String existingLocation;
    private Long existingDate;

    private TextInputEditText etLieu;
    private TextView tvDate, tvTime, tvTitle;
    private MaterialButton btnSelectDate, btnSelectTime, btnSave, btnCancel;

    private Calendar calendar = Calendar.getInstance();
    private OnSaveListener saveListener;

    public interface OnSaveListener {
        void onSave(Long pfaId, String lieu, long date);
    }

    public static SoutenanceFormDialog newInstance(Long pfaId, String pfaTitle,
                                                   Long soutenanceId, String location, Long date) {
        SoutenanceFormDialog dialog = new SoutenanceFormDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_PFA_ID, pfaId);
        args.putString(ARG_PFA_TITLE, pfaTitle);
        if (soutenanceId != null) args.putLong(ARG_SOUTENANCE_ID, soutenanceId);
        if (location != null) args.putString(ARG_LOCATION, location);
        if (date != null) args.putLong(ARG_DATE, date);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.saveListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        if (getArguments() != null) {
            pfaId = getArguments().getLong(ARG_PFA_ID);
            pfaTitle = getArguments().getString(ARG_PFA_TITLE);
            soutenanceId = getArguments().containsKey(ARG_SOUTENANCE_ID) ?
                    getArguments().getLong(ARG_SOUTENANCE_ID) : null;
            existingLocation = getArguments().getString(ARG_LOCATION);
            existingDate = getArguments().containsKey(ARG_DATE) ?
                    getArguments().getLong(ARG_DATE) : null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_soutenance_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvDialogTitle);
        etLieu = view.findViewById(R.id.etLieu);
        tvDate = view.findViewById(R.id.tvSelectedDate);
        tvTime = view.findViewById(R.id.tvSelectedTime);
        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Setup
        boolean isEdit = soutenanceId != null;
        tvTitle.setText(isEdit ? "Modifier la soutenance" : "Planifier une soutenance");

        TextView tvPfaTitle = view.findViewById(R.id.tvPfaTitle);
        tvPfaTitle.setText(pfaTitle);

        if (existingLocation != null) {
            etLieu.setText(existingLocation);
        }

        if (existingDate != null) {
            calendar.setTimeInMillis(existingDate);
        }

        updateDateTimeDisplay();

        // Listeners
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String lieu = etLieu.getText() != null ? etLieu.getText().toString() : "";
            if (lieu.trim().isEmpty()) {
                etLieu.setError("Le lieu est requis");
                return;
            }
            if (saveListener != null) {
                saveListener.onSave(pfaId, lieu, calendar.getTimeInMillis());
            }
            dismiss();
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (v, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateTimeDisplay();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (v, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            updateDateTimeDisplay();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvDate.setText(dateFormat.format(calendar.getTime()));
        tvTime.setText(timeFormat.format(calendar.getTime()));
    }
}