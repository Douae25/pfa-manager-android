package ma.ensate.pfa_manager.network;

import java.util.List;
import ma.ensate.pfa_manager.model.PFADossierSync;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Interface API pour récupérer les PFA Dossiers avec la structure du backend
 */
public interface PFADossierSyncApi {
    @GET("/api/pfa-dossiers")
    Call<List<PFADossierSync>> getAllPFADossiers();
}
