package com.crm.enhance_wellness.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Short-lived HTTP caching for clinic *catalogue* endpoints only.
 *
 * Switching bottom-nav tabs rebuilds the destination's ViewModel, and each one refetches
 * in `init`. A single session was observed issuing `services` 8 times, `my-memberships`
 * 7 times and `membership-plans` 5 times. These responses change rarely, so a brief cache
 * removes the duplicate round-trips without any change to feature code.
 *
 * ## Why an allowlist
 *
 * OkHttp's cache writes plaintext response bodies to the app's cache directory. Patient
 * data — wallet balances, prescriptions, loyalty, visits, appointments — must not be
 * persisted that way, so only endpoints that return clinic-wide catalogue content are
 * cacheable here. Anything not on this list is passed through untouched and continues to
 * hit the network every time.
 *
 * Add a path here only if its response is identical for every patient of the tenant.
 */
class CatalogueCacheInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (request.method != "GET") return response
        if (!response.isSuccessful) return response
        if (!isCacheable(request.url.encodedPath)) return response

        // The API sends no-store/no-cache, so the cache is opt-in from this side.
        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", "public, max-age=$MAX_AGE_SECONDS")
            .build()
    }

    private fun isCacheable(path: String): Boolean =
        CACHEABLE_SUFFIXES.any { path.endsWith(it) }

    private companion object {
        /**
         * Long enough to collapse the burst of duplicate calls from tab switching, short
         * enough that a clinic's catalogue edit shows up within a minute.
         */
        const val MAX_AGE_SECONDS = 60

        val CACHEABLE_SUFFIXES = listOf(
            "/services",
            "/service-categories",
            "/membership-plans",
            "/giftcards/storefront",
        )
    }
}
