package ma.ensate.pfa_manager.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;

public class RejectionDialogHelper {
    
    public interface RejectionListener {
        void onRejectionConfirmed(String reason);
        void onRejectionCancelled();
    }
    
    public static void showRejectionDialog(Context context, RejectionListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Justifier le refus");
        builder.setMessage("Veuillez fournir une raison pour le refus de cette convention:");
        
        // Créer un EditText pour la raison
        LinearLayout container = new LinearLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        container.setPadding(50, 40, 50, 40);
        
        EditText input = new EditText(context);
        input.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        input.setMinLines(3);
        input.setHint("Ex: Modules non validés, Informations manquantes...");
        container.addView(input);
        
        builder.setView(container);
        
        builder.setPositiveButton("Confirmer", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                listener.onRejectionCancelled();
            } else {
                listener.onRejectionConfirmed(reason);
            }
        });
        
        builder.setNegativeButton("Annuler", (dialog, which) -> {
            listener.onRejectionCancelled();
            dialog.cancel();
        });
        
        builder.show();
    }
}
