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
 *    Kai Hudalla (Bosch Software Innovations GmbH) - add endpoints for all IP addresses
 ******************************************************************************/
package org.eclipse.californium.examples;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;


public class HelloWorldServer extends CoapServer {

	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
    /*
     * Application entry point.
     */
    public static void main(String[] args) {
        
        try {

            // create server
            HelloWorldServer server = new HelloWorldServer();
            // add endpoints on all IP addresses
            server.addEndpoints();
            server.start();

        } catch (SocketException e) {
            System.err.println("Failed to initialize server: " + e.getMessage());
        }
    }

    /**
     * Add individual endpoints listening on default CoAP port on all IPv4 addresses of all network interfaces.
     */
    private void addEndpoints() {
    	for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
    		// only binds to IPv4 addresses and localhost
			if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {
				InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
				addEndpoint(new CoapEndpoint(bindToAddress));
			}
		}
    }

    /*
     * Constructor for a new Hello-World server. Here, the resources
     * of the server are initialized.
     */
    public HelloWorldServer() throws SocketException {

        // provide an instance of a Hello-World resource
        //add(new HelloWorldResource());
        //add(new SimpleJSONRessource());

        Path dir = Paths.get("content");
        if (Files.exists(dir)) {
            try {
                Files.list(dir).forEach(file -> add(new SimpleFileRessource(file.toAbsolutePath())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Definition of the Hello-World Resource
     */
    class HelloWorldResource extends CoapResource {
        
        public HelloWorldResource() {
            
            // set resource identifier
            super("helloWorld");
            
            // set display name
            getAttributes().setTitle("Hello-World Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            
            // respond to the request
            exchange.respond("Hello World!");
        }
    }

    class SimpleJSONRessource extends CoapResource {

        public  SimpleJSONRessource() {
            super("simpleJSON");
            getAttributes().setTitle(this.getName());
        }

        public void handleGET(CoapExchange exchange) {
            exchange.respond("{name: \"simpleJSON\", status: \"up\", size: \"51 Byte\"}");
        }
    }

    class SimpleFileRessource extends CoapResource{

        byte[] payload;
        int mediaType;

        public SimpleFileRessource() {
            super("emptyFile");
            getAttributes().setTitle(this.getName());
        }

        public SimpleFileRessource(Path path) {
            super(path.getFileName().toString());
            try {
                this.payload = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] tmp = path.toString().split("\\.");
            switch (tmp[tmp.length -1]){
                case "jpg":
                case "jpeg":
                    mediaType = MediaTypeRegistry.IMAGE_JPEG;
                    break;
                case "png":
                    mediaType = MediaTypeRegistry.IMAGE_PNG;
                    break;
                default:
                    mediaType = MediaTypeRegistry.TEXT_PLAIN;
            }
            getAttributes().setTitle(this.getName());
        }

        public void handleGET(CoapExchange exchange) {
            if (payload == null) {
                exchange.respond("no content");
            }
            else {
                Response resp = new Response(CoAP.ResponseCode.CONTENT);
                resp.getOptions().setContentFormat(this.mediaType);
                resp.setPayload(this.payload);
                exchange.respond(resp);
            }
        }
    }
}
