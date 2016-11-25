package com.ak.http;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.activemq.broker.SslContext;
import org.apache.activemq.transport.tcp.SslTransportFactory;
import org.apache.activemq.util.JMSExceptionSupport;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslSocketFactories {
	private static final Logger LOGGER = LoggerFactory.getLogger(SslSocketFactories.class);
	private static final String FILE_PROTOCOL = "file://";
	

	public static SSLSocketFactory getSSLSocketFactory(String keyStore, String ketStorePassword, String trustStore,
			String trustStorePassword) throws Exception {
		try {
			TrustManager[] trustManager = createTrustManager(trustStore, trustStorePassword);
			KeyManager[] keyManager = createKeyManager(keyStore, ketStorePassword);

			SslTransportFactory e = new SslTransportFactory();
			SslContext ctx = new SslContext(keyManager, trustManager, null);
			SslContext.setCurrentSslContext(ctx);
			return ctx.getSSLContext().getSocketFactory();
		} catch (Exception var3) {
			throw JMSExceptionSupport.create("Could not create Transport. Reason: " + var3, var3);
		}
	}

	private static TrustManager[] createTrustManager(String trustStore, String trustStorePassword) throws Exception {
		TrustManager[] trustStoreManagers = null;
		KeyStore trustedCertStore = KeyStore.getInstance("jks");
		InputStream tsStream = loadFile(trustStore);
		trustedCertStore.load(tsStream, trustStorePassword.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustedCertStore);
		trustStoreManagers = tmf.getTrustManagers();
		return trustStoreManagers;
	}

	private static KeyManager[] createKeyManager(String keyStore, String ketStorePassword) throws Exception {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore ks = KeyStore.getInstance("jks");
		KeyManager[] keystoreManagers = null;
		byte[] sslCert = IOUtils.toByteArray(loadFile(keyStore));
		if (sslCert != null && sslCert.length > 0) {
			ByteArrayInputStream bin = new ByteArrayInputStream(sslCert);
			ks.load(bin, ketStorePassword.toCharArray());
			kmf.init(ks, ketStorePassword.toCharArray());
			keystoreManagers = kmf.getKeyManagers();
		}

		return keystoreManagers;
	}
	
	private static InputStream loadFile(String filePath) throws FileNotFoundException {
		return loadFile(SslSocketFactories.class.getClassLoader(), filePath);
	}

	private static InputStream loadFile(ClassLoader classLoader, String filePath) throws FileNotFoundException {
		if (filePath.startsWith(FILE_PROTOCOL)) {
			return new FileInputStream(filePath.substring(FILE_PROTOCOL.length()));
		}
		return classLoader.getResourceAsStream(filePath);
	}


}
