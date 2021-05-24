package by.isb.weatherwidget.data.repository.forecast

import by.isb.an07.hw8.data.mappers.crypto.CryptoResponseMapper
import by.isb.an07.hw8.data.entities.crypto.Crypto
import by.isb.an07.hw8.data.networking.crypto.CryptoApi
import by.isb.weatherwidget.data.entities.forecast.Forecast
import by.isb.weatherwidget.data.mappers.forecast.ForecastResponseMapper
import by.isb.weatherwidget.data.networking.forecast.ForecastApi

class ForecastRepository {

    private val api = ForecastApi.provideRetrofit()
    private val forecastResponseMapper = ForecastResponseMapper()

    suspend fun loadForecast(lat: Double, lon: Double, units: String): List<Forecast> {
        val response = api.loadForecast(lat,lon,units)

        return if (response.isSuccessful) {
            response.body()?.dataIn?.map {
                cryptoResponseMapper.map(it)
            }.orEmpty()
        } else {
            throw Throwable(response.errorBody().toString())
        }
    }

}