/*
 * Java
 *
 * Copyright 2025 HAJJI Amin
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

package com.microej.weatherreport.ui;

import ej.library.service.location.Location;
import ej.microui.display.Image;
import ej.mwt.Container;
import ej.mwt.Widget;
import ej.mwt.util.Size;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import java.util.Calendar;

public class WeatherContainer extends Container {

	/**
	 * Allows the creation of a container that contains a WeatherWidget and a
	 * Slider.
	 * 
	 * @param weatherData     the json data of the weather forecast
	 * @param currentChild    the index of this container given by the slide
	 *                        container
	 * @param currentLocation the current location of the board
	 */
	public WeatherContainer(JSONObject weatherData, int currentChild, Location currentLocation) throws JSONException {
		int jsonHour = currentChild * 24;
		JSONArray weatherCodes = weatherData.getJSONArray("weather_code");
		JSONArray temperatures = weatherData.getJSONArray("temperature_2m");

		CarouselEntry[] weatherEntries = new CarouselEntry[24];

		for (int i = 0; i < 24; i++) {
			int currentHour = i + jsonHour;

			String imagePath = getWeatherIcon(weatherCodes.getInt(currentHour), i);
			String tempString = Math.round(temperatures.getDouble(currentHour)) + "°C";

			weatherEntries[i] = new CarouselEntry(i, Image.getImage(imagePath), tempString, i + "h");
		}

		Carousel carousel = new Carousel(weatherEntries, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), 128, 128);

		addChild(carousel);
	}

	/**
	 * Returns the appropriate image path for the current weather and the focused
	 * hour.
	 *
	 * @param currentWeatherCode the current weather code
	 * @param hour               the hour of the weather
	 * @return String the path to the Image
	 */
	public String getWeatherIcon(int currentWeatherCode, int hour) {
		if (currentWeatherCode <= 1) {
			if (hour < 8 || hour > 18) {
				return "/images/clear-night.png";
			} else {
				return "/images/clear-day.png";
			}
		} else if (currentWeatherCode <= 19) {
			return "/images/cloudy.png";
		} else if (currentWeatherCode >= 40 && currentWeatherCode <= 49) {
			return "/images/fog.png";
		} else if (currentWeatherCode <= 59) {
			return "/images/drizzle.png";
		} else if (currentWeatherCode <= 69) {
			return "/images/rainy.png";
		} else if (currentWeatherCode <= 79) {
			return "/images/snowy.png";
		} else if (currentWeatherCode <= 99) {
			return "/images/thunderstorm.png";
		}

		return "/images/clear-day.png";
	}

	@Override
	protected void layOutChildren(int contentWidth, int contentHeight) {
		for (Widget child : getChildren()) {
			assert (child != null);
			layOutChild(child, 0, 0, contentWidth, contentHeight);
		}
	}

	@Override
	protected void computeContentOptimalSize(Size size) {
		size.setSize(450, 200);
	}

}
