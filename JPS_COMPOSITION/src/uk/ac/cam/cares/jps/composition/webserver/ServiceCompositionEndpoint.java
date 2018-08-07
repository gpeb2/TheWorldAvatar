package uk.ac.cam.cares.jps.composition.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.HTTP;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.composition.compositionengine.ServiceCompositionEngine;
import uk.ac.cam.cares.jps.composition.servicemodel.Service;
import uk.ac.cam.cares.jps.composition.util.ConnectionBuilder;
import uk.ac.cam.cares.jps.composition.util.FormatTranslator;

/**
 * Servlet implementation class ServiceCompositionEndpoint
 */
@WebServlet("/ServiceCompositionEndpoint")
public class ServiceCompositionEndpoint extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ServiceCompositionEndpoint() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = request.getReader().readLine()) != null) {
				sb.append(s);
			}
			JSONObject jsonObject = HTTP.toJSONObject(sb.toString());
			String AgentInString = jsonObject.getString("Method").toString();
			Service agent = FormatTranslator.convertJSONTOJavaClass(AgentInString);

			ServiceCompositionEngine myCompositionEngine = new ServiceCompositionEngine(agent,
					"http://" + request.getServerName() + ":" + request.getServerPort());

			boolean met = false;
			int index = 0;
			while (!met) {
				index++;
				met = myCompositionEngine.appendLayerToGraph(index);
			}
			int size = 1;
			while (size != 0) {
				size = myCompositionEngine.eliminateRedundantAgent();
			}

			ConnectionBuilder connectionBuilder = new ConnectionBuilder();
			connectionBuilder.buildEdge(myCompositionEngine.getGraph()); // build the connection between services
			connectionBuilder.connectEdges(myCompositionEngine.getGraph());
			connectionBuilder.rearrangeEdges(myCompositionEngine.getGraph());

			JSONObject graphInJSON = FormatTranslator.convertGraphJavaClassTOJSON(myCompositionEngine.getGraph());
			response.getWriter().write(graphInJSON.toString());

		} catch (Exception ex) {

		}
	}

}
