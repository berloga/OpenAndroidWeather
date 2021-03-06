package no.openandroidweather.weatherproxy;

import java.io.IOException;

import no.openandroidweather.misc.IProgressItem;
import android.location.Location;
import android.net.Uri;

public interface WeatherProxy {

	/**
	 * Gets the newest forecast, if successful it returns the rowId where the
	 * forecast is added in the meta table. If the provider is delayed, all
	 * forecasts is updated with this new time.
	 * 
	 * @param location
	 *            of the forecast
	 * @param lastForecastGenerated
	 *            time for when the last forecast was updated. If this time is
	 *            the same as the previously or greater, it will return null and
	 *            update the database with new expected time for next forecast.
	 *            Time is given in milliseconds since 01.01.1970
	 * 
	 * @return Uri in the content provider or null if there is no new data
	 * @throws IOException
	 *             If the device does not have Internet.
	 * @throws Exception
	 *             Something else went wrong: parsing, etc.
	 */
	Uri getWeatherForecast(Location location, long lastForecastGenerated,
			IProgressItem progressItem) throws IOException, Exception;

}
