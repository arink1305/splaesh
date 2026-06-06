package no.uio.ifi.in2000.aryanma.splaesh.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val MET_BASE_URL = "https://in2000.api.met.no/"
    private const val USER_AGENT = "Splaesh/1.0, team-41, kontakt: arinkk@uio.no"

    private val metHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .build()

            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: MetWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(MET_BASE_URL)
            .client(metHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MetWeatherApi::class.java)
    }

    val oceanApi: OceanForecastApi by lazy {
        Retrofit.Builder()
            .baseUrl(MET_BASE_URL)
            .client(metHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OceanForecastApi::class.java)
    }

    val metAlertsApi: MetAlertsApi by lazy {
        Retrofit.Builder()
            .baseUrl(MET_BASE_URL)
            .client(metHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MetAlertsApi::class.java)
    }

    private const val BASE_URL_UV = "https://api.open-meteo.com/"

    val uvApi: UvApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_UV)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UvApi::class.java)
    }
}
