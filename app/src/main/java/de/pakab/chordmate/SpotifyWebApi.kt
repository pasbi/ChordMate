package de.pakab.chordmate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

@Serializable
class AccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String = "",
    @SerialName("token_type")
    val tokenType: String = "",
    @SerialName("expires_in")
    val expiresIn: Int = 0,
)

interface SpotifyWebApi {
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("token")
    fun getToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials",
    ): Call<AccessTokenResponse>
}
