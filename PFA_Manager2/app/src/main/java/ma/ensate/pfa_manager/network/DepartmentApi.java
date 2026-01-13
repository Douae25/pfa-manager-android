package ma.ensate.pfa_manager.network;

import java.util.List;
import ma.ensate.pfa_manager.model.Department;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DepartmentApi {
    @GET("/api/departments")
    Call<List<Department>> getAllDepartments();

    @GET("/api/departments/{id}")
    Call<Department> getDepartmentById(@Path("id") long id);

    @POST("/api/departments")
    Call<Department> createDepartment(@Body Department department);

    @PUT("/api/departments/{id}")
    Call<Department> updateDepartment(@Path("id") long id, @Body Department department);

    @DELETE("/api/departments/{id}")
    Call<Void> deleteDepartment(@Path("id") long id);
}
