package com.ak.http;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface ConnectionFactory {
	public HttpURLConnection openConnection(String uri) throws IOException;
}