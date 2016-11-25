package com.ak.http;

import org.junit.Test;

public class HttpRequestExecutorTest {
	
	@Test
	public void testGetExecuteRequest() throws Exception{
		HttpRequestExecutor executor = new HttpRequestExecutor();
		HttpRequest request = new HttpRequest();
		request.setHttpMethod(HttpMethod.GET);
		request.setUrl("http://google.com");
		HttpResponse response = executor.executeRequest(request);
		System.out.println(response.readResponseStream());
	}

}
