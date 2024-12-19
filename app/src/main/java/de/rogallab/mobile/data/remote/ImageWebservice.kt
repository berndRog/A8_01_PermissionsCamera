package de.rogallab.mobile.data.remote

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ImageWebservice {

   @GET("peopleapi/v1/images/{fileName}")
   suspend fun download(
      @Path("fileName") fileName: String,
   ): Response<ResponseBody>

   @Multipart
   @POST("peopleapi/v1/images")
   suspend fun upload(
      @Part file: MultipartBody.Part
   ): Response<String>

   @DELETE("peopleapi/v1/images/{fileName}")
   suspend fun delete(
      @Path("fileName") fileName: String
   ): Response<Boolean>

}