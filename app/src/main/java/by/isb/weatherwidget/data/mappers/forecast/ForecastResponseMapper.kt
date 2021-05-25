package by.isb.weatherwidget.data.mappers.forecast

import by.isb.weatherwidget.data.dto.forecast.ForecastResponse
import by.isb.weatherwidget.data.entities.forecast.Forecast
import by.isb.weatherwidget.data.mappers.Mapper

class ForecastResponseMapper : Mapper<ForecastResponse.Daily, Forecast> {

    override fun map(from: ForecastResponse.Daily): Forecast {
        return Forecast(
            date = from.dt?:0,
            min = from.temp?.min?:0.0,
            max = from.temp?.max?:0.0,
            icon = from.weather?.get(0)?.icon.orEmpty()
        )
    }
}