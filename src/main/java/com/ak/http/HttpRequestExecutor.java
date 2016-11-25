package com.ak.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestExecutor.class);

	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private ConnectionFactory connectionFactory;
	private SSLSocketFactory sslSocketFactory;

	public HttpRequestExecutor() {
		this.connectionFactory = new UrlConnectionFactory(null, null);
		this.sslSocketFactory = null;
	}

	public HttpRequestExecutor(ConnectionFactory connectionFactory, SSLSocketFactory sslSocketFactory) {
		this.connectionFactory = connectionFactory;
		this.sslSocketFactory = sslSocketFactory;
	}

	public HttpRequestExecutor(String proxyHost, String proxyPort) throws Exception {
		this.connectionFactory = new UrlConnectionFactory(proxyHost, proxyPort);
		this.sslSocketFactory = null;
	}

	public HttpRequestExecutor(String proxyHost, String proxyPort, String trustStore, String trustStorePassword,
			String keyStore, String keyStorePassword) throws Exception {
		this.connectionFactory = new UrlConnectionFactory(proxyHost, proxyPort);
		this.sslSocketFactory = SslSocketFactories.getSSLSocketFactory(keyStore, keyStorePassword, trustStore,
				trustStorePassword);
	}

	public HttpResponse executeRequest(HttpRequest request) throws Exception {
		try {
			String url = request.getUrl();
			LOGGER.debug("Executing request, url={}", url);
			HttpURLConnection connection = connectionFactory.openConnection(url);

			if (connection instanceof HttpsURLConnection) {
				HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) connection;

				if (sslSocketFactory != null) {
					httpsUrlConnection.setSSLSocketFactory(sslSocketFactory);
				}
			}

			connection.setRequestMethod(request.getHttpMethod().toString());
			connection.setRequestProperty("Content-Type", request.getContentType());
			connection.setRequestProperty("Accept", request.getAcceptType());
			connection.setRequestProperty("Accept-Charset", "utf-8");

			String body = request.getBody();
			if (StringUtils.isNotBlank(body)) {
				sendRequestBody(body, connection);
			}

			return response(connection);
		} catch (ProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	private void sendRequestBody(String body, HttpURLConnection connection) throws IOException {
		byte[] bodyUtf8Bytes = body.getBytes(UTF_8);
		int length = bodyUtf8Bytes.length;
		connection.setDoOutput(true);
		connection.setFixedLengthStreamingMode(length);
		connection.setRequestProperty("Content-Length", Integer.toString(length));
		try (OutputStream requestStream = connection.getOutputStream()) {
			requestStream.write(bodyUtf8Bytes);
		}
	}

	private HttpResponse response(HttpURLConnection connection) throws Exception {
		HttpResponse response = new HttpResponse(connection);
		return response;
	}

	private static class UrlConnectionFactory implements ConnectionFactory {
		private String proxyHost;
		private String proxyPort;
		private boolean usePorxy;

		public UrlConnectionFactory(String proxyHost, String proxyPort) {
			this.proxyHost = proxyHost;
			this.proxyPort = proxyPort;
			this.usePorxy = StringUtils.isNotEmpty(proxyHost) && StringUtils.isNotEmpty(proxyPort);
		}

		@Override
		public HttpURLConnection openConnection(String uri) throws IOException {
			URL url = new URL(uri);
			if (this.usePorxy) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
				HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
				return connection;
			} else {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				return connection;
			}
		}
	}

}
