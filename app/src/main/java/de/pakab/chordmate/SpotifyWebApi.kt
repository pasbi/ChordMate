package de.pakab.chordmate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap
import kotlin.collections.joinToString

@Serializable
class AccessTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
)

@Serializable
class AlbumCover(
    @SerialName("url") val url: String,
    @SerialName("height") val height: Int,
    @SerialName("width") val width: Int,
)

@Serializable
class Album(
    @SerialName("album_type") val albumType: String,
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("images") val images: ArrayList<AlbumCover>,
)

@Serializable
class SimplifiedArtist(
    @SerialName("name") val name: String,
    @SerialName("id") val id: String,
)

@Serializable
class Track(
    @SerialName("album") val album: Album,
    @SerialName("artists") val artists: ArrayList<SimplifiedArtist>,
    @SerialName("id") val id: String,
    @SerialName("is_playable") val isPlayable: Boolean = false,
    @SerialName("name") val name: String,
) {
    override fun toString(): String = "Track[$name by ${artists.map { it.name }.joinToString(", ")}]"
}

@Serializable
class Tracks(
    @SerialName("offset") val offset: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("total") val total: Int,
    @SerialName("items") val items: ArrayList<Track>,
)

@Serializable
class SearchResponse(
    @SerialName("tracks") val tracks: Tracks,
) {
    override fun toString(): String = tracks.items.toString()
}

interface SpotifyWebApi {
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("token")
    fun getToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials",
    ): Call<AccessTokenResponse>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @GET("search")
    fun search(
        @QueryMap query: HashMap<String, String>,
    ): Call<SearchResponse>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @GET("tracks/{id}")
    fun track(
        @Path("id") id: String,
    ): Call<Track>
}
