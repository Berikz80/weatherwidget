package by.isb.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import by.isb.weatherwidget.data.entities.forecast.Forecast
import by.isb.weatherwidget.data.repository.forecast.ForecastRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [WeatherWidgetConfigureActivity]
 */
class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deletePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {

        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

private val forecastRepository = ForecastRepository()
private val ioScope = CoroutineScope(Dispatchers.IO)

var lat = 0.0
var lon = 0.0
var units = "metric"

var forecasts = listOf<Forecast>()

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    lat = loadPref(context, appWidgetId, "lat")?.toDouble() ?: 0.0
    lon = loadPref(context, appWidgetId, "lon")?.toDouble() ?: 0.0
    units = loadPref(context, appWidgetId, "units") ?: "metric"
    // Construct the RemoteViews object

    val arrayDates = arrayOf(
        R.id.day_1_number,
        R.id.day_2_number,
        R.id.day_3_number,
        R.id.day_4_number
    )

    val arrayTemperatures = arrayOf(
        R.id.day_1_temperature,
        R.id.day_2_temperature,
        R.id.day_3_temperature,
        R.id.day_4_temperature
    )

    val arrayImages = arrayOf(
        R.id.day_1_image,
        R.id.day_2_image,
        R.id.day_3_image,
        R.id.day_4_image
    )

    val views = RemoteViews(context.packageName, R.layout.weather_widget)

    ioScope.launch {
        for (i in 0..3) {
            views.setTextViewText(arrayDates[i], "-")
            views.setTextViewText(arrayTemperatures[i], "-")

        }
        loadForecast(lat, lon, units)
    }



    if (forecasts.isNotEmpty()) {

        views.setTextViewText(R.id.location, "$lat / $lon")

        for (i in 0..3) {
            val dateMillis = (forecasts[i]?.date?.times(1000))
            val dateTime = SimpleDateFormat("dd.MM").format(dateMillis).toString()
            views.setTextViewText(arrayDates[i], dateTime)

            val temp = "${forecasts[i]?.min.toInt()} / ${forecasts[i]?.max.toInt()}"

            views.setTextViewText(arrayTemperatures[i], temp)

//            val icon = forecasts[i]?.icon
//            views.setImageViewBitmap(arrayImages[i], getBitmap("http://openweathermap.org/img/wn/$icon.png"))
        }
    }

    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, WeatherWidget::class.java))

    val intent = Intent(context, WeatherWidget::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    }

    val updateIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )


    views.setOnClickPendingIntent(R.id.item_refresh, updateIntent)

    val settingsIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        Intent(context, WeatherWidgetConfigureActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    views.setOnClickPendingIntent(R.id.item_setting, settingsIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

suspend fun loadForecast(lat: Double, lon: Double, units: String) {
    withContext(ioScope.coroutineContext) {
        forecasts = forecastRepository.loadForecast(
            lat,
            lon,
            units
        )
    }
}