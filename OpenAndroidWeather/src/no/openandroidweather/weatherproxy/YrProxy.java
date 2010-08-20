/*
	Copyright 2010 Torstein Ingebrigtsen Bø

    This file is part of OpenAndroidWeather.

    OpenAndroidWeather is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenAndroidWeather is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenAndroidWeather.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package no.openandroidweather.weatherproxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.openandroidweather.weatherproxy.yr.YrLocationForecastParser;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class YrProxy implements WeatherProxy {
	private static final String TAG = "YrProxy";
	public static final String PROVIDER = "met.no";
	private ContentResolver mContentResolver;

	public YrProxy(ContentResolver contentResolver) {
		super();
		mContentResolver = contentResolver;
	}

	/* (non-Javadoc)
	 * @see no.openandroidweather.weatherproxy.WeatherProxy#getWeatherForecast(android.location.Location, long)
	 */
	@Override
	public Uri getWeatherForecast(Location location, long lastForecastGenerated)
			throws IOException, Exception {
		return locationForecast(location, lastForecastGenerated);
	}

	/**
	 * See http://api.met.no/weatherapi/locationforecast/1.8/documentation for
	 * more information
	 * 
	 * @param location
	 *            of the forecast
	 * @param lastForecastGenerated
	 * @return Uri to the data in WeatherContentProvider
	 * @throws UnknownHostException When no Internet connection
	 * @throws SAXException parsing exception
	 * @throws ParserConfigurationException
	 * @throws IOException Internet trouble
	 */
	private Uri locationForecast(Location location, long lastForecastGenerated)
			throws UnknownHostException, IOException,
			ParserConfigurationException, SAXException {
		// Makes the uri
		Uri.Builder uri = new Uri.Builder();
		uri.authority("api.met.no");
		uri.path("/weatherapi/locationforecast/1.8/");
		uri.scheme("http");
		String lat = new Double(location.getLatitude()).toString();
		String lon = new Double(location.getLongitude()).toString();
		uri.appendQueryParameter("lat", lat);
		uri.appendQueryParameter("lon", lon);
		if (location.hasAltitude()) {
			Double altD = location.getAltitude();
			String alt = Integer.toString(altD.intValue());
			uri.appendQueryParameter("msl", alt);
		}
		Log.d(TAG, uri.toString());

		HttpRequest httpRequest = new HttpGet(uri.toString());
		httpRequest.addHeader("Accept-Encoding", "gzip");

		HttpClient httpClient = new DefaultHttpClient();
		HttpHost httpHost = new HttpHost("api.met.no");
		HttpResponse httpResponse = null;
		httpResponse = httpClient.execute(httpHost, httpRequest);

		if (httpResponse.getStatusLine().getStatusCode() != 200) {
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			throw new HttpResponseException(responseCode,
					"Trouble with response from api.yr.no: Response code: "
							+ responseCode);
		}

		InputStream inputStream = httpResponse.getEntity().getContent();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser parser = spf.newSAXParser();

		// Parse it
		YrLocationForecastParser yrLocationForecastParser = new YrLocationForecastParser(
				mContentResolver, lastForecastGenerated);
		try {
			parser.parse(inputStream, yrLocationForecastParser);
		} catch (SAXException e) {
			if (e.getMessage().equals(
					YrLocationForecastParser.NO_NEW_DATA_EXCEPTION))
				return null;
			else
				throw e;
		}

		return yrLocationForecastParser.getContentUri();
	}
}
