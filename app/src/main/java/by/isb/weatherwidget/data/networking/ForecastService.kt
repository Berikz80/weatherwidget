package by.isb.weatherwidget.data.networking

import by.isb.weatherwidget.data.dto.forecast.ForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

const val appid = "066684c224288ec83f079c8017eb1057"

interface ForecastService {

    @GET("/data/2.5/onecall?appid=${appid}")
    suspend fun loadForecast(
        @Query("lat")
        lat: Double,
        @Query("sort_dir")
        lon: Double,

    ): Response<ForecastResponse>
}