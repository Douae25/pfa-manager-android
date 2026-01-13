package ma.ensate.pfa_manager.network;

import java.util.List;
import ma.ensate.pfa_manager.model.Convention;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ConventionApi {
    @GET("/api/conventions")
    Call<List<Convention>> getAllConventions();

    @GET("/api/conventions/{id}")
    Call<Convention> getConventionById(@Path("id") long id);

    @POST("/api/conventions")
    Call<Convention> createConvention(@Body Convention convention);

    @PUT("/api/conventions/{id}")
    Call<Convention> updateConvention(@Path("id") long id, @Body Convention convention);

    @DELETE("/api/conventions/{id}")
    Call<Void> deleteConvention(@Path("id") long id);
}
