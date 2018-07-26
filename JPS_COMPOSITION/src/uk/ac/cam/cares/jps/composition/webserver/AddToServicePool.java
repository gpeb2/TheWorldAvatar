package uk.ac.cam.cares.jps.composition.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.composition.util.ServicePoolTool;

/**
 * Servlet implementation class AddToServicePool
 */
@WebServlet("/AddToServicePool")
public class AddToServicePool extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String filename = AgentLocator.getAbsolutePath("./service_pool.txt", AddToServicePool.class);

	public AddToServicePool() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Receive the service as a JSON, APPEND it to the service pool file
		JSONObject service_pool_in_JSON = null;
		try {
			service_pool_in_JSON = ServicePoolTool.readTheServicePool();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = request.getReader().readLine()) != null) {
				sb.append(s);
			}
			JSONObject jsonObject = HTTP.toJSONObject(sb.toString());
			String AgentInString = jsonObject.getString("Method").toString();
			JSONObject newAgentInJSON = new JSONObject(AgentInString);
			service_pool_in_JSON.put(newAgentInJSON.getString("uri"), newAgentInJSON);
			ServicePoolTool.writeToTheServicePool(service_pool_in_JSON.toString());
			response.getWriter().write(service_pool_in_JSON.toString());
		} catch (Exception ex) {
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
