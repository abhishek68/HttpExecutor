package com.ak.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponse.class);
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private HttpURLConnection connection;
	
	public HttpResponse(HttpURLConnection connection){
		this.connection = connection;
	}

	public int getHttpCode() throws IOException {
		return this.connection.getResponseCode();
	}

	
	public int getContentLength() {
		return this.connection.getContentLength();
	}

	public InputStream getResponseStream() throws IOException {
		return this.connection.getInputStream();
	}
	
	public InputStream getResponseErrorStream() throws IOException {
		return this.connection.getErrorStream();
	}

	public String readResponseStream() throws IOException {
		LOGGER.debug("Reading response stream");
		int contentLength = getContentLength();
		if (contentLength == 0) {
			return "";
		}
		if (contentLength > 0) {
			byte[] responseBytes = new byte[contentLength];
			IOUtils.readFully(getResponseStream(), responseBytes);
			return new String(responseBytes, UTF_8);
		}
		return IOUtils.toString(getResponseStream(), UTF_8);
	}
	
	public String readErrorResponseStream() throws IOException {
		LOGGER.debug("Reading error response stream");
		int contentLength = getContentLength();
		if (contentLength == 0) {
			return "";
		}
		if (contentLength > 0) {
			byte[] responseBytes = new byte[contentLength];
			IOUtils.readFully(getResponseErrorStream(), responseBytes);
			return new String(responseBytes, UTF_8);
		}
		return IOUtils.toString(getResponseErrorStream(), UTF_8);
	}

	public <T> T readResponseStream(Class<T> klass) throws IOException {
		String response = readResponseStream();
		ObjectMapper mapper = new ObjectMapper();
		T responseObject = mapper.readValue(response, klass);
		return responseObject;
	}
	
	public <T> T readErrorResponseStream(Class<T> klass) throws IOException {
		String response = readErrorResponseStream();
		ObjectMapper mapper = new ObjectMapper();
		T responseObject = mapper.readValue(response, klass);
		return responseObject;
	}

}
