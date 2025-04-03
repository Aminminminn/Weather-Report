/*
 * Copyright 2025 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
package com.microej.weatherreport;

import ej.rest.web.Resty.Option;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.URLConnection;

/**
 * Custom Resty Option used to set the SSL Context to the URLConnection before
 * the actual connection is made. This option is required when using a custom
 * SSL Context that is not set globally.
 */
public class CustomSSLContextOption extends Option {
	private final SSLContext sslContext;

	/**
	 * Provide the SSL Context that should be used by Resty.
	 *
	 * @param sslContext the SSL Context to use
	 */
	public CustomSSLContextOption(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	/**
	 * Set the SSL Context only if required.
	 */
	@Override
	public void apply(URLConnection urlConnection) {
		if (urlConnection instanceof HttpsURLConnection) {
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
			httpsURLConnection.setSSLSocketFactory(this.sslContext.getSocketFactory());
		}
	}
}
