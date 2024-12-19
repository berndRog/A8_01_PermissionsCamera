package de.rogallab.mobile.data.remote.network

import de.rogallab.mobile.domain.utilities.logDebug
import retrofit2.Retrofit

inline fun <reified T> createWebservice(
   retrofit: Retrofit,
   webserviceName: String
): T {
   logDebug("<-createWebService", "create $webserviceName")
   return retrofit.create(T::class.java)
}
