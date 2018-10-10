package uk.ac.cam.cares.jps.base.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;


public class AgentCaller {
	
	private static final String JSON_PARAMETER_KEY = "query";
	private static Logger logger = LoggerFactory.getLogger(AgentCaller.class);
	private static String hostPort = null;
	
	private static synchronized String getHostPort() {
		if (hostPort == null) {
			hostPort = AgentLocator.getProperty("host") + ":" + AgentLocator.getProperty("port");
//			hostPort = AgentLocator.getProperty("host");
		}
		return hostPort;
	}
	
	public static String executeGet(String path) {
		URIBuilder builder = new URIBuilder().setScheme("http").setHost(getHostPort())
				.setPath(path);
		return executeGet(builder);
	}
	
	public static String executeGet(String path, String... keyOrvalue) {
		// TODO-AE maybe use directly class java.net.URI
		// TODO-AE refactor get hostname
		URIBuilder builder = new URIBuilder().setScheme("http").setHost(getHostPort())
				.setPath(path);
		
		for (int i=0; i<keyOrvalue.length; i=i+2) {
			String key = keyOrvalue[i];
			String value = keyOrvalue[i+1];
			builder.setParameter(key, value);
		}
				
		try {
			return executeGet(builder);
		} catch (Exception e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		} 
	}	
	
	public static String executeGetWithJsonParameter(String path, String json) {
		URIBuilder builder = new URIBuilder().setScheme("http").setHost(getHostPort())
				.setPath(path);
		
		builder.setParameter(JSON_PARAMETER_KEY, json);
				
		try {
			return executeGet(builder);
		} catch (Exception e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		} 
	}	
	
	public static JSONObject getJsonParameter(HttpServletRequest request) {
			
		try {
			
			String json = request.getParameter(JSON_PARAMETER_KEY);
			if (json != null) {
				return new JSONObject(json);
			} 
			
			JSONObject jsonobject = new JSONObject();
			Enumeration<String> keys = request.getParameterNames();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				String value = request.getParameter(key);
				jsonobject.put(key, value);
			}
			return jsonobject;	
			
		} catch (JSONException e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		}
	}
		
	// TODO-AE turn from public to private
	public static String executeGet(URIBuilder builder) {
		try {
			HttpGet request = new HttpGet(builder.build());
						
			logger.debug(request.toString());
			HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
			
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				throw new JPSRuntimeException("HTTP response with error = " + httpResponse.getStatusLine());
			}
		
			String body =  EntityUtils.toString(httpResponse.getEntity());
			logger.debug(body);
			return body;
		} catch (Exception e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		} 
	}
	
	// TODO-AE this method seems not to be required. 
	public static String getRequestBody(final HttpServletRequest req) {
	    final StringBuilder builder = new StringBuilder();
	    try (final BufferedReader reader = req.getReader()) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            builder.append(line);
	        }
	        return builder.toString();
	    } catch (final Exception e) {
	        return null;
	    }
	}
	
	@Deprecated
	public static AgentResponse callAgent(String contextPath, AgentRequest agentRequest)  {
		
		Gson gson = new Gson();
		
		logger.info("callAgent start ");
		
		String serializedAgentRequest = gson.toJson(agentRequest);
		
		logger.info("SerAgRequ " + serializedAgentRequest);
		
		try {
			String serializedAgentResponse = executeGet(contextPath, "agentrequest", serializedAgentRequest);
			
			logger.info("SerAgResp " + serializedAgentResponse);
						
			return gson.fromJson(serializedAgentResponse, AgentResponse.class);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new JPSRuntimeException(e.getMessage(), e);
		}
	}
	
	@Deprecated
	public static AgentRequest getAgentRequest(HttpServletRequest req) {
		String serializedAgentRequest = req.getParameter("agentrequest");
		return new Gson().fromJson(serializedAgentRequest, AgentRequest.class);
	}
	
	public static void printToResponse(Object object, HttpServletResponse resp) {
		
		String message = new Gson().toJson(object);
		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");
		try {
			resp.getWriter().print(message);
		} catch (IOException e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		}	
	}
}
