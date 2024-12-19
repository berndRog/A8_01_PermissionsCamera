package de.rogallab.mobile.domain


interface ImageRepository {

    // @GET("/images/{fileName}")
    suspend fun get(fileName: String): ResultData<String?>

    // @POST("/images")
    suspend fun post(localImage: String): ResultData<String>

    //@DELETE("/images/{fileName}")
    suspend fun delete(fileName: String): ResultData<Boolean>

}