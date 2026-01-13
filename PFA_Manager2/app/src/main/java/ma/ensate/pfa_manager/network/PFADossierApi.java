package ma.ensate.pfa_manager.network;

import java.util.List;
import ma.ensate.pfa_manager.model.PFADossier;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PFADossierApi {
    @GET("/api/pfa-dossiers")
    Call<List<PFADossier>> getAllPFADossiers();

    @GET("/api/pfa-dossiers/{id}")
    Call<PFADossier> getPFADossierById(@Path("id") long id);

    @POST("/api/pfa-dossiers")
    Call<PFADossier> createPFADossier(@Body PFADossier dossier);

    @PUT("/api/pfa-dossiers/{id}")
    Call<PFADossier> updatePFADossier(@Path("id") long id, @Body PFADossier dossier);

    @DELETE("/api/pfa-dossiers/{id}")
    Call<Void> deletePFADossier(@Path("id") long id);
}
