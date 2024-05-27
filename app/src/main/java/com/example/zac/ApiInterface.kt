package com.example.zac

import com.example.zac.modules.Films
import com.example.zac.modules.Homeworld
import com.example.zac.modules.Species
import com.example.zac.modules.StarWarsCharactersResponse
import com.example.zac.modules.Vehicle
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiInterface {
    @GET
    suspend fun getAllCharacters(@Url url: String = "people/"): Response<StarWarsCharactersResponse>

    @GET
    suspend fun getSpecies(@Url url: String): Response<Species>

    @GET
    suspend fun getVehicle(@Url url: String): Response<Vehicle>

    @GET
    suspend fun getHomeworld(@Url url: String): Response<Homeworld>

    @GET
    suspend fun getFilm(@Url url: String): Response <Films>

}
