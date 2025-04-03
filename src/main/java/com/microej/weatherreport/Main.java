/*
 * Java
 *
 * Copyright 2025 HAJJI Amin
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.weatherreport;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import com.microej.weatherreport.ui.LoadingScreenWidget;
import com.microej.weatherreport.ui.SlideContainer;
import com.microej.weatherreport.ui.WeatherContainer;
import ej.library.service.location.Location;
import ej.microui.MicroUI;
import ej.microui.display.Colors;
import ej.microui.display.Font;
import ej.mwt.Desktop;
import ej.mwt.style.EditableStyle;
import ej.mwt.style.background.NoBackground;
import ej.mwt.style.background.RectangularBackground;
import ej.mwt.style.dimension.OptimalDimension;
import ej.mwt.style.outline.FlexibleOutline;
import ej.mwt.style.outline.UniformOutline;
import ej.mwt.style.outline.border.FlexibleRectangularBorder;
import ej.mwt.style.outline.border.RectangularBorder;
import ej.mwt.stylesheet.Stylesheet;
import ej.mwt.stylesheet.cascading.CascadingStylesheet;
import ej.mwt.stylesheet.selector.ClassSelector;
import ej.mwt.stylesheet.selector.StateSelector;
import ej.mwt.stylesheet.selector.TypeSelector;
import ej.mwt.stylesheet.selector.combinator.AndCombinator;
import ej.mwt.util.Alignment;
import ej.widget.basic.Button;
import ej.widget.basic.Label;
import ej.widget.container.Dock;
import ej.widget.container.LayoutOrientation;
import ej.widget.container.List;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import ej.net.PollerConnectivityManager;
import ej.net.util.NtpUtil;
import ej.rest.web.*;
import ej.service.ServiceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the application.
 * <p>
 * Sets up the UI and the necessary information.
 */
public class Main {

	public static final Logger LOGGER = java.util.logging.Logger.getLogger("Weather Report"); //$NON-NLS-1$

	// The server certificate file name
	private static final String SERVER_CERT_FILENAME = "open-meteo-com.pem"; //$NON-NLS-1$
	private static final String SERVER_CERT_PATH = "/certificates/"; //$NON-NLS-1$

	// X509 certificate type name
	private static final String CERT_TYPE = "X509"; //$NON-NLS-1$

	// TLS algorithm version 1.2
	private static final String TLS_VERSION_1_2 = "TLSv1.2"; //$NON-NLS-1$

	// Request information
	private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?"; //$NON-NLS-1$

	// Style (fonts, colors and boxes).
	public static final String ROBOTO_24_PX_500 = "/fonts/roboto_24px-500.ejf"; //$NON-NLS-1$
	private static final int CORAL = 0xee502e;
	private static final int CONCRETE_BLACK_75 = 0x262a2c;
	private static final int PADDING_MARGIN = 5;

	// Class selectors
	private static final int PAGE_CONTAINER = 1;
	private static final int PAGE_CONTAINER_ODD = 2;
	private static final int TITLE = 3;

	// Carousel style
	private static final int CLASS_CAROUSEL = 1501;
	private static final int CLASS_LAST_CLICKED_LABEL = 1502;
	private static final int CLASS_LAST_CLICKED_NAME = 1503;
	private static final int CLASS_LAST_CLICKED_LIST = 1504;

	private static final int CAROUSEL_PADDING_BOTTOM = 5;
	private static final int CAROUSEL_BORDER_BOTTOM_SIZE = 1;

	// Logger messages
	public static final String WAITING_FOR_CONNECTIVITY = "=========== WAITING FOR CONNECTIVITY ===========";
	public static final String GET_REQUEST_WITH_QUERY_PARAMETERS = "=========== GET REQUEST WITH QUERY PARAMETERS ===========";
	public static final String STOPPING_CONNECTIVITY_MANAGER = "=========== STOPPING CONNECTIVITY MANAGER ===========";
	public static final String UPDATING_TIME = "=========== UPDATING TIME ===========";

	public static void main(String[] args) {
		MicroUI.start();

		Desktop desktop = new Desktop();
		SlideContainer container = new SlideContainer();

		desktop.setWidget(new LoadingScreenWidget());
		desktop.requestShow();

		waitForConnectivity();
		updateTime();

		try {
			SSLContext sslContext = createCustomSSLContext();

			// Hardcoded Location due to the LocationProvider not working anymore (see
			// CHANGELOG.md).
			final Location currentLocation = new Location() {
				@Override
				public float getLatitude() {
					return 47.2172f;
				}

				@Override
				public float getLongitude() {
					return -1.5534f;
				}

				@Override
				public String getTimeZone() {
					return "GMT+1";
				}

				@Override
				public String getZipCode() {
					return "";
				}

				@Override
				public String getCity() {
					return "Nantes";
				}

				@Override
				public String getRegionName() {
					return "";
				}

				@Override
				public String getRegionCode() {
					return "";
				}

				@Override
				public String getCountryName() {
					return "France";
				}

				@Override
				public String getCountryCode() {
					return "";
				}
			};

			final JSONObject weatherData = requestWeatherForecast(currentLocation.getLatitude(),
					currentLocation.getLongitude(), sslContext).getJSONObject("hourly");

			createPage(container, weatherData, currentLocation);

			desktop = new Desktop();
			desktop.setStylesheet(createStylesheet());
			desktop.setWidget(container);
			desktop.requestShow();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		stopConnectivityManager();
	}

	/**
	 * Requests the weather forecast of the next 7 days at the given location.
	 * 
	 * @param latitude
	 *            the latitude of the place
	 * @param longitude
	 *            the longitude of the place
	 * @param sslContext
	 *            custom SSL Context to use.
	 */
	private static JSONObject requestWeatherForecast(float latitude, float longitude, SSLContext sslContext)
			throws IOException, JSONException {
		LOGGER.info(GET_REQUEST_WITH_QUERY_PARAMETERS);

		Resty.Option sslContextOption = new CustomSSLContextOption(sslContext);
		Resty restClient = new Resty(sslContextOption);

		String requestURL = WEATHER_API_URL + "latitude=" + latitude + "&longitude=" + longitude //$NON-NLS-1$
				+ "&hourly=temperature_2m,weather_code&timezone=auto";

		// do GET request
		TextResource resource = restClient.text(requestURL);

		return new JSONObject(resource.toString());
	}

	private static void waitForConnectivity() {
		LOGGER.info(WAITING_FOR_CONNECTIVITY);
		final Object mutex = new Object();
		final ConnectivityManager service = ServiceFactory.getServiceLoader().getService(ConnectivityManager.class);
		if (service != null) {
			ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
				@Override
				public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
					if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
						synchronized (mutex) {
							mutex.notify(); // NOSONAR Not my code
						}
					}
				}
			};
			service.registerDefaultNetworkCallback(callback);
			NetworkCapabilities capabilities = service.getNetworkCapabilities(service.getActiveNetwork());
			if (capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
				synchronized (mutex) {
					try {
						mutex.wait(); // NOSONAR Not my code
					} catch (InterruptedException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
				}
			}
			service.unregisterNetworkCallback(callback);
			LOGGER.info("Connected");
		} else {
			LOGGER.info("No connectivity manager found.");
		}
	}

	private static void stopConnectivityManager() {
		LOGGER.info(STOPPING_CONNECTIVITY_MANAGER);
		final ConnectivityManager service = ServiceFactory.getServiceLoader().getService(ConnectivityManager.class);
		if (service instanceof PollerConnectivityManager) {
			((PollerConnectivityManager) service).cancel();
		} else {
			LOGGER.info("No connectivity manager found.");
		}
	}

	private static void updateTime() {
		LOGGER.info(UPDATING_TIME);
		try {
			NtpUtil.updateLocalTime();
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "Could not update time.", e);
		}
		LOGGER.info("Time updated");
	}

	/**
	 * Create and initialize the SSLContext which will be used to connect to the secure Server. The followings steps
	 * show how to create and set up the SSLContext for Resty Https connection.
	 * 
	 * @return SSLContext returns the custom SSL context
	 */
	private static SSLContext createCustomSSLContext() throws KeyStoreException, IOException, CertificateException,
			NoSuchAlgorithmException, KeyManagementException {

		/*
		 * Step 1 : Create an input stream with the server certificate file
		 */
		try (InputStream in = Main.class.getResourceAsStream(SERVER_CERT_PATH + SERVER_CERT_FILENAME)) {
			LOGGER.info(SERVER_CERT_PATH + SERVER_CERT_FILENAME + " - OK !");

			/*
			 * Step 2 : Generate the server certificate
			 */
			CertificateFactory certificateFactory = CertificateFactory.getInstance(CERT_TYPE);
			Certificate openMeteoCert = certificateFactory.generateCertificate(in);

			/*
			 * Step 3 : Create and set up the KeyStore with the server certificate
			 */

			// create a default KeyStore
			KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
			// our default KeyStore can not be loaded from an InputStream; so just load as
			// empty KeyStore with null
			// parameters
			store.load(null, null);
			// add the server certificate to our created KeyStore
			store.setCertificateEntry("OpenMeteo", openMeteoCert);

			/*
			 * Step 4: Create and initialize the trust manager with our KeyStore
			 */
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(CERT_TYPE);
			trustManagerFactory.init(store);
			TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

			/*
			 * Step 5 : Create and initialize the SSLContext with our trust managers
			 */
			SSLContext sslContext = SSLContext.getInstance(TLS_VERSION_1_2);
			sslContext.init(null, trustManagers, null);

			return sslContext;
		}
	}

	/**
	 * Recursive method that sets up all the possible pages.
	 *
	 * @param container
	 *            the slide container
	 * @param weatherData
	 *            the weather data needed for the next day
	 */
	private static void createPage(final SlideContainer container, final JSONObject weatherData,
			final Location currentLocation) throws JSONException {
		final int index = container.getChildrenCount() + 1;

		// We can only see the weather in a week
		if (index > 7)
			return;

		Dock dock = new Dock();
		if (index % 2 == 0) {
			dock.addClassSelector(PAGE_CONTAINER);
		} else {
			dock.addClassSelector(PAGE_CONTAINER_ODD);
		}

		int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + container.getChildrenCount();
		String[] weekDays = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
		Label title = new Label(weekDays[(dayOfWeek - 1) % 7]);
		title.addClassSelector(TITLE);
		dock.addChildOnTop(title);

		List list = new List(LayoutOrientation.VERTICAL);

		Button goToNext = new Button(">");
		list.addChild(goToNext);
		goToNext.setOnClickListener(() -> {
			try {
				createPage(container, weatherData, currentLocation);
			} catch (JSONException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		});

		Button goToPrevious = new Button("<");
		list.addChild(goToPrevious);
		goToPrevious.setOnClickListener(() -> {
			if (container.getChildrenCount() != 1) {
				container.removeLast();
			}
		});

		dock.setCenterChild(list);

		WeatherContainer weatherContainer = new WeatherContainer(weatherData, container.getChildrenCount(),
				currentLocation);
		dock.addChildOnLeft(weatherContainer);

		container.addChild(dock);
	}

	private static Stylesheet createStylesheet() {
		CascadingStylesheet stylesheet = new CascadingStylesheet();

		EditableStyle style = stylesheet.getDefaultStyle();
		style.setColor(CONCRETE_BLACK_75);
		style.setFont(Font.getFont(ROBOTO_24_PX_500));
		style.setBackground(NoBackground.NO_BACKGROUND);

		style = stylesheet.getSelectorStyle(new TypeSelector(SlideContainer.class));
		style.setBackground(NoBackground.NO_BACKGROUND);
		style.setBorder(new RectangularBorder(CONCRETE_BLACK_75, PADDING_MARGIN));

		style = stylesheet.getSelectorStyle(new ClassSelector(PAGE_CONTAINER));
		style.setBackground(new RectangularBackground(Colors.WHITE));

		style = stylesheet.getSelectorStyle(new ClassSelector(PAGE_CONTAINER_ODD));
		style.setBackground(new RectangularBackground(Colors.WHITE));

		style = stylesheet.getSelectorStyle(new ClassSelector(TITLE));
		style.setHorizontalAlignment(Alignment.HCENTER);
		style.setBorder(new FlexibleRectangularBorder(Colors.BLACK, 0, 0, 2, 0));

		// BUTTON STYLE
		style = stylesheet.getSelectorStyle(new TypeSelector(Button.class));
		style.setPadding(new UniformOutline(PADDING_MARGIN));
		style.setDimension(OptimalDimension.OPTIMAL_DIMENSION_XY);
		style.setHorizontalAlignment(Alignment.RIGHT);
		style.setVerticalAlignment(Alignment.VCENTER);

		style = stylesheet
				.getSelectorStyle(new AndCombinator(new TypeSelector(Button.class), new StateSelector(Button.ACTIVE)));
		style.setColor(CORAL);

		// CAROUSEL STYLE
		style = stylesheet.getSelectorStyle(new ClassSelector(CLASS_CAROUSEL));
		style.setDimension(OptimalDimension.OPTIMAL_DIMENSION_XY);
		style.setHorizontalAlignment(Alignment.HCENTER);
		style.setVerticalAlignment(Alignment.VCENTER);
		style.setColor(Colors.WHITE);
		style.setPadding(new FlexibleOutline(0, 0, CAROUSEL_PADDING_BOTTOM, 0));
		style = stylesheet.getSelectorStyle(new ClassSelector(CLASS_LAST_CLICKED_LIST));
		style.setBorder(new FlexibleRectangularBorder(0x97a7af, CAROUSEL_BORDER_BOTTOM_SIZE, 0, 0, 0));

		style = stylesheet.getSelectorStyle(new ClassSelector(CLASS_LAST_CLICKED_LABEL));
		style.setHorizontalAlignment(Alignment.LEFT);

		style = stylesheet.getSelectorStyle(new ClassSelector(CLASS_LAST_CLICKED_NAME));
		style.setHorizontalAlignment(Alignment.RIGHT);

		return stylesheet;
	}

}