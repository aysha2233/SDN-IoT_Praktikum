/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Achim Kraus (Bosch Software Innovations GmbH) - add saving payload
 ******************************************************************************/
package org.eclipse.californium.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.jndi.toolkit.url.Uri;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.coap.*;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import sun.net.www.http.HttpClient;

import javax.net.ssl.HttpsURLConnection;


public class GETClient {

	/*
	 * Application entry point.
	 * 
	 */	
	public static void main(String args[]) {
		List<TimeMeasure> lst = new ArrayList<>();

		try {

			//lst.add(new CoapMeasure("coap:1mb", new URI("coap://172.27.128.1:5683/1024kb.txt"), 512));
			//lst.add(new CoapMeasure("coap:2kb", new URI("coap://172.27.128.1:5683/2048b.txt"), 512));

			lst.add(new HttpMeasure("http:32b", new URL("http://localhost/32b.txt"), 1));
			lst.add(new CoapMeasure("coap:32b", new URI("coap://localhost:5683/32b.txt"), 1));

			lst.add(new HttpMeasure("http:2kb", new URL("http://localhost/2048b.txt"), 1));
			lst.add(new CoapMeasure("coap:2kb", new URI("coap://localhost:5683/1024b.txt"), 2));

			lst.add(new HttpMeasure("http:8kb", new URL("http://localhost/8192b.txt"), 1));
			lst.add(new CoapMeasure("coap:8kb", new URI("coap://localhost:5683/1024b.txt"), 8));

			lst.add(new HttpMeasure("http:1mb", new URL("http://localhost/1024kb.txt"), 1));
			lst.add(new CoapMeasure("coap:1mb", new URI("coap://localhost:5683/1024b.txt"), 1024));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("#########################");
		System.out.println("#########################");
		System.out.println();

		System.out.print("run");
		System.out.print("\t");
		for (TimeMeasure m: lst) {
			System.out.print(m.getName() + "(ms)");
			System.out.print("\t");
		}
		System.out.println();

		for (int i = 0; i < 50; i++) {
			System.out.print(i);
			System.out.print("\t");
			for (TimeMeasure m: lst) {
				System.out.print(m.measure());
				System.out.print("\t");
			}
			System.out.println();
		}
	}

	private static interface TimeMeasure {
		public String getName();
		public long measure();
	}

	private static class CoapMeasure implements TimeMeasure {
		private URI uri;
		private CoapClient client;
		private int loop;
		private String name;

		public CoapMeasure(String name, URI uri, int loop) {
			this.uri = uri;
			this.loop = loop;
			this.name = name;

			client = new CoapClient(this.uri);
			client.useEarlyNegotiation(1024);
			client.get();
		}

		public long measure() {
			long start = System.currentTimeMillis();
			boolean isSuccess = true;
			CoapResponse response = null;

			for (int i = 0; i < loop; i++) {
				String content = "";
				response = client.get();
				if (response != null) {
					content += response.getResponseText();
				}
				isSuccess &= response != null && response.isSuccess();
				/*int sequence = 0;
				//do {
					Request req = new Request(CoAP.Code.GET);
					req.getOptions().setBlock2(1, true, sequence++);
					response = client.advanced(req);
					if (response != null) {
						content += response.getResponseText();
					}
					isSuccess &= response != null && response.isSuccess();
				//} while (response != null && !response.getOptions().getBlock2().isM());
				*/
			}
			long duration = System.currentTimeMillis() - start;
			//if (response != null)
				//System.out.println(Utils.prettyPrint(response));
			if (isSuccess)
				return duration;
			else
				return -1;
		}

		public String getName() {
			return this.name;
		}
	}

	private static class HttpMeasure implements TimeMeasure {
		private URL url;
		private int loop;
		private String name;

		public HttpMeasure(String name, URL url, int loop) {
			this.url = url;
			this.loop = loop;
			this.name = name;
		}

		public long measure() {
			try {
				long start = System.currentTimeMillis();
				for (int i = 0; i < loop; i++) {
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					InputStream is = con.getInputStream();
					String content = "";
					try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
						content = buffer.lines().collect(Collectors.joining("\n"));
					}
				}
				long duration = System.currentTimeMillis() - start;

				return duration;
			}
			catch (Exception e)
			{
				//e.printStackTrace();
				return -1;
			}
		}

		public String getName() {
			return this.name;
		}
	}

}
