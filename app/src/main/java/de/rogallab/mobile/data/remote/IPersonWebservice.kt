package de.rogallab.mobile.data.remote

import de.rogallab.mobile.data.dtos.PersonDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface IPersonWebservice {
   @GET("peopleapi/v1/people")
   suspend fun getAll(
   ): Response<List<PersonDto>>

   @GET("peopleapi/v1/people/{id}")
   suspend fun getById(
      @Path("id") id: String
   ): Response<PersonDto?>

   @POST("peopleapi/v1/people")
   suspend fun post(
      @Body personDto: PersonDto
   ): Response<Unit>

   @PUT("peopleapi/v1/people/{id}")
   suspend fun put(
      @Path("id") id:String,
      @Body personDto: PersonDto
   ): Response<Unit>

   @DELETE("peopleapi/v1/people/{id}")
   suspend fun delete(
      @Path("id") id: String
   ): Response<Unit>
}