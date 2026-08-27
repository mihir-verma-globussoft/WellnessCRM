package com.crm.enhance_wellness.core.di

import android.content.Context
import com.crm.enhance_wellness.BuildConfig
import com.crm.enhance_wellness.core.network.AuthInterceptor
import com.crm.enhance_wellness.core.network.CatalogueCacheInterceptor
import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.feature.health.data.remote.dto.FlexibleDrugsAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * SPKI SHA-256 pins for the Google Trust Services chain that issues the API
     * certificate. Mirrors `res/xml/network_security_config.xml` — keep both in sync.
     *
     * We pin the issuing intermediate and root rather than the leaf: GTS rotates leaf
     * certificates roughly every 90 days, so a leaf pin would break the app on renewal.
     */
    private val CERT_PINS = listOf(
        "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // GTS WE1 intermediate
        "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=", // GTS Root R4 (backup)
    )

    private const val CACHE_SIZE_BYTES = 5L * 1024 * 1024

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(FlexibleDrugsAdapterFactory())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * HEADERS, never BODY. Response bodies carry PHI — prescriptions, drug lists,
     * diagnoses — and Logcat is readable by `adb logcat` on any debug device. Header
     * level keeps method/URL/status/timing, which is what is actually useful when
     * debugging, without writing patient data to the log. `Authorization` and `Cookie`
     * are redacted so bearer tokens never land in a bug report.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
            else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }

    /**
     * Certificate pinning as a second, independent layer alongside
     * `network_security_config.xml`. Applied to release builds only so that debug
     * builds remain inspectable through a proxy (the debug flavour ships a config
     * without a pin-set for the same reason).
     */
    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        if (BuildConfig.DEBUG) return CertificatePinner.DEFAULT
        val host = BuildConfig.BASE_URL.toHttpUrlOrNull()?.host
            ?: return CertificatePinner.DEFAULT
        return CertificatePinner.Builder()
            .apply { CERT_PINS.forEach { add(host, it) } }
            .build()
    }

    /**
     * Backing store for [CatalogueCacheInterceptor]. Only clinic catalogue responses are
     * ever written here — see that class for why patient data is excluded.
     */
    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache =
        Cache(File(context.cacheDir, "http_catalogue_cache"), CACHE_SIZE_BYTES)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        catalogueCacheInterceptor: CatalogueCacheInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinner: CertificatePinner,
        cache: Cache,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        // Network interceptor: sees the real response and rewrites its cache headers
        // before OkHttp decides whether to store it.
        .addNetworkInterceptor(catalogueCacheInterceptor)
        .addInterceptor(loggingInterceptor)
        .certificatePinner(certificatePinner)
        .cache(cache)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideWellnessApiService(retrofit: Retrofit): WellnessApiService =
        retrofit.create(WellnessApiService::class.java)
}
