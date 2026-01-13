package ma.ensate.pfa_manager.network;

import java.util.concurrent.TimeUnit;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Interceptor;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ma.ensate.pfa_manager.model.api.DeliverableRequest;
import android.util.Log;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static final String TAG = "ApiClient";
    private static ApiService apiService = null;

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Retry interceptor for transient failures
            Interceptor retryInterceptor = chain -> {
                int maxRetries = 3;
                IOException lastException = null;
                
                for (int attempt = 0; attempt < maxRetries; attempt++) {
                    try {
                        return chain.proceed(chain.request());
                    } catch (IOException e) {
                        lastException = e;
                        Log.w(TAG, "Request failed (attempt " + (attempt + 1) + "/" + maxRetries + "): " + e.getMessage());
                        if (attempt < maxRetries - 1) {
                            try {
                                // Exponential backoff: 1s, 2s, 4s
                                long delayMs = 1000L * (long) Math.pow(2, attempt);
                                Thread.sleep(delayMs);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Retry interrupted", ie);
                            }
                        }
                    }
                }
                throw lastException;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(retryInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(90, TimeUnit.SECONDS)
                    .build();

            // Create custom Gson with DeliverableRequest serializer
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(DeliverableRequest.class, new DeliverableRequestSerializer())
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}