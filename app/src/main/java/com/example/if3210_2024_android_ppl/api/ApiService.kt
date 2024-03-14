import com.example.if3210_2024_android_ppl.api.LoginRequest
import com.example.if3210_2024_android_ppl.api.LoginResponse
import retrofit2.Call

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
