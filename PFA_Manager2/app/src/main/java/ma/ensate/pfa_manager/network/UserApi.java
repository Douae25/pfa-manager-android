package ma.ensate.pfa_manager.network;

import java.util.List;
import ma.ensate.pfa_manager.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {
    @GET("/api/users")
    Call<List<User>> getAllUsers();

    @GET("/api/users/{id}")
    Call<User> getUserById(@Path("id") long id);

    @POST("/api/users")
    Call<User> createUser(@Body User user);

    @PUT("/api/users/{id}")
    Call<User> updateUser(@Path("id") long id, @Body User user);

    @DELETE("/api/users/{id}")
    Call<Void> deleteUser(@Path("id") long id);
}
