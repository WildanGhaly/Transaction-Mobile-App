import com.example.if3210_2024_android_ppl.api.BillResponse
import com.example.if3210_2024_android_ppl.api.LoginRequest
import com.example.if3210_2024_android_ppl.api.LoginResponse
import com.example.if3210_2024_android_ppl.api.TokenResponse
import okhttp3.MultipartBody
import retrofit2.Call

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/token")
    fun token(@Header("Authorization") authorization: String): Call<TokenResponse>

    @Multipart
    @POST("api/bill/upload")
    fun uploadBill(
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Call<BillResponse>

}
