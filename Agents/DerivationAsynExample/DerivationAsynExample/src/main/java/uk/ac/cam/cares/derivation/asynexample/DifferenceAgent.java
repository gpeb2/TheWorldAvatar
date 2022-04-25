package uk.ac.cam.cares.derivation.asynexample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.cam.cares.jps.base.agent.DerivationAgent;
import uk.ac.cam.cares.jps.base.derivation.DerivationInputs;
import uk.ac.cam.cares.jps.base.derivation.DerivationOutputs;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.interfaces.StoreClientInterface;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;

/**
 * This difference agent takes two inputs as maximum value and minimum value and compute their difference.
 * @author Jiaru Bai (jb2197@cam.ac.uk)
 *
 */
@WebServlet(urlPatterns = {DifferenceAgent.API_PATTERN})
public class DifferenceAgent extends DerivationAgent {
	
	private static final Logger LOGGER = LogManager.getLogger(DifferenceAgent.class);
	
	private static final long serialVersionUID = 1L;
	
	static final String API_PATTERN = "/DifferenceAgent";
	
	StoreClientInterface kbClient;
	SparqlClient sparqlClient;
	
	public DifferenceAgent() {
		LOGGER.info("DifferenceAgent is initialised.");
	}
	
	public DifferenceAgent(StoreClientInterface storeClient, String derivationInstanceBaseURL) {
		super(storeClient, derivationInstanceBaseURL);
		this.kbClient = storeClient;
		this.sparqlClient = new SparqlClient(storeClient);
	}
	
	@Override
	public DerivationOutputs processRequestParameters(DerivationInputs derivationInputs) {
		LOGGER.debug("DifferenceAgent received derivationInputs: " + derivationInputs.toString());

		// get the input from the KG
		String maxvalue_iri = derivationInputs.getIris(SparqlClient.getRdfTypeString(SparqlClient.MaxValue)).get(0);
		String minvalue_iri = derivationInputs.getIris(SparqlClient.getRdfTypeString(SparqlClient.MinValue)).get(0);
		
		// compute difference
		Integer diff = sparqlClient.getValue(maxvalue_iri) - sparqlClient.getValue(minvalue_iri);
		
		// create new instances in KG
		String createdDifference = sparqlClient.createDifference();
		sparqlClient.addValueInstance(createdDifference, diff);

		// create DerivationOutputs instance
		DerivationOutputs derivationOutputs = new DerivationOutputs(
				SparqlClient.getRdfTypeString(SparqlClient.Difference), createdDifference);

		return derivationOutputs;
	}
	
	@Override
	public void init() throws ServletException {
		LOGGER.info("\n---------------------- Difference Agent has started ----------------------\n");
		System.out.println("\n---------------------- Difference Agent has started ----------------------\n");
		ScheduledExecutorService exeService = Executors.newSingleThreadScheduledExecutor();
		
		Config.initProperties();
		
		if (this.kbClient == null) {
			this.kbClient = new RemoteStoreClient(Config.sparqlEndpoint, Config.sparqlEndpoint, Config.kgUser, Config.kgPassword);
			this.sparqlClient = new SparqlClient(this.kbClient);
		}
		DifferenceAgent diffAgent = new DifferenceAgent(this.kbClient, Config.derivationInstanceBaseURL);
		
		exeService.scheduleAtFixedRate(() -> {
			try {
				diffAgent.monitorAsyncDerivations(Config.agentIriDifference);
			} catch (JPSRuntimeException e) {
				e.printStackTrace();
			}
		}, Config.initDelayAgentDifference, Config.periodAgentDifference, TimeUnit.SECONDS);
		LOGGER.info("\n---------------------- Difference Agent is monitoring derivation instance ----------------------\n");
		System.out.println("\n---------------------- Difference Agent is monitoring derivation instance ----------------------\n");
	}
}
