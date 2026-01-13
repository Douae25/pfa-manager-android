package ma.ensate.pfa_manager;

import android.app.Application;
import ma.ensate.pfa_manager.sync.SyncManager;

public class PFAManagerApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialiser le SyncManager au lancement de l'app
        // Il écoutera les changements réseau et synchronisera automatiquement
        SyncManager.getInstance(this);
    }
}
