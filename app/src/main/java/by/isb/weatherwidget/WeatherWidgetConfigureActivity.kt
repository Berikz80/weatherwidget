package by.isb.weatherwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import by.isb.weatherwidget.databinding.WeatherWidgetConfigureBinding

/**
 * The configuration screen for the [WeatherWidget] AppWidget.
 */
class WeatherWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var appWidgetLat: EditText
    private lateinit var appWidgetLon: EditText
    private lateinit var appWidgetUnits: EditText

    private var onClickListener = View.OnClickListener {
        val context = this@WeatherWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val lat = appWidgetLat.text.toString().toLong()
        val lon = appWidgetLon.text.toString().toLong()
        val units = appWidgetUnits.text.toString()

        saveTitlePref(context, appWidgetId, lat, lon, "metric")

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAppWidget(context, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
    private lateinit var binding: WeatherWidgetConfigureBinding

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = WeatherWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appWidgetLat = binding.textLat as EditText
        appWidgetLon = binding.textLon as EditText
        binding.addButton.setOnClickListener(onClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        appWidgetLat.setText(loadTitlePref(this@WeatherWidgetConfigureActivity, appWidgetId, "lat"))
        appWidgetLon.setText(loadTitlePref(this@WeatherWidgetConfigureActivity, appWidgetId, "lon"))
    }

}

private const val PREFS_NAME = "by.isb.weatherwidget.WeatherWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(
    context: Context,
    appWidgetId: Int,
    lat: Long,
    lon: Long,
    units: String
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + "lat_" + appWidgetId, lat.toString())
    prefs.putString(PREF_PREFIX_KEY + "lon_" + appWidgetId, lon.toString())
    prefs.putString(PREF_PREFIX_KEY + "units_" + appWidgetId, units)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int, pref: String): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + pref + "_" + appWidgetId, null)
    return titleValue.orEmpty()
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + "lat_" + appWidgetId)
    prefs.remove(PREF_PREFIX_KEY + "lon_" + appWidgetId)
    prefs.remove(PREF_PREFIX_KEY + "units_" + appWidgetId)
    prefs.apply()
}