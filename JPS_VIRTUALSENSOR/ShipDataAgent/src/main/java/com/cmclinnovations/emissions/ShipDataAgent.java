package com.cmclinnovations.emissions;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.cam.cares.jps.base.agent.DerivationAgent;
import uk.ac.cam.cares.jps.base.derivation.DerivationClient;
import uk.ac.cam.cares.jps.base.derivation.DerivationInputs;
import uk.ac.cam.cares.jps.base.derivation.DerivationOutputs;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;

@WebServlet(urlPatterns = { "/" })
public class ShipDataAgent extends DerivationAgent {
    private static final Logger LOGGER = LogManager.getLogger(ShipDataAgent.class);

    @Override
    public void processRequestParameters(DerivationInputs derivationInputs, DerivationOutputs derivationOutputs) {
        LOGGER.info("Received request to update derivation <{}>", derivationInputs.getDerivationIRI());
        Map<String, List<String>> inputsMap = derivationInputs.getInputs();
        String shipIri = inputsMap.get(QueryClient.SHIP).get(0);
        addDataTriples(derivationOutputs, shipIri);
    }

    @Override
    public void init() throws ServletException {
        EndpointConfig endpointConfig = new EndpointConfig();
        RemoteStoreClient storeClient = new RemoteStoreClient(endpointConfig.getKgurl(),
                endpointConfig.getKgurl());
        super.devClient = new DerivationClient(storeClient, QueryClient.PREFIX);
    }

    /**
     * adds emissions triples to derivation outputs
     * 
     * @param chimney
     * @param derivationOutputs
     */
    void addDataTriples(DerivationOutputs derivationOutputs, String shipIri) {
        // energy efficiency
        String efficiencyProperty = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX,
                QueryClient.ENERGY_EFFICIENCY);
        String efficiencyMeasure = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX,
                QueryClient.MEASURE_STRING);

        derivationOutputs.addTriple(shipIri, QueryClient.HAS_PROPERTY_STRING, efficiencyProperty);
        derivationOutputs.addTriple(efficiencyProperty, QueryClient.HAS_VALUE_STRING, efficiencyMeasure);
        derivationOutputs.addLiteral(efficiencyMeasure, QueryClient.HAS_NUMERICALVALUE_STRING, 0.48);

        // fuel consumption, assume heavy fuel oil, LHV = 10.83 kWh/g, efficiency 48%
        String fuelConsumptionProperty = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX,
                QueryClient.FUEL_CONSUMPTION);
        String fuelConsumptionMeasure = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX,
                QueryClient.MEASURE_STRING);

        derivationOutputs.addTriple(shipIri, QueryClient.HAS_PROPERTY_STRING, fuelConsumptionProperty);
        derivationOutputs.addTriple(fuelConsumptionProperty, QueryClient.HAS_VALUE_STRING, fuelConsumptionMeasure);
        derivationOutputs.addLiteral(fuelConsumptionMeasure, QueryClient.HAS_NUMERICALVALUE_STRING, 192);

        // Specific CO2 emission (constant for each fuel type) heavy fuel oil, 3.114
        String specificCo2Property = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX,
                QueryClient.SPECIFIC_CO2_EMISSION);
        String specificCo2Measure = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX,
                QueryClient.MEASURE_STRING);

        derivationOutputs.addTriple(shipIri, QueryClient.HAS_PROPERTY_STRING, specificCo2Property);
        derivationOutputs.addTriple(specificCo2Property, QueryClient.HAS_VALUE_STRING, specificCo2Measure);
        // 3.114 * 1000 / 10.83 / 0.48
        derivationOutputs.addLiteral(specificCo2Measure, QueryClient.HAS_NUMERICALVALUE_STRING, 600);

        // CII
        String ciiProperty = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX, QueryClient.CII);
        String ciiMeasure = derivationOutputs.createNewEntityWithBaseUrl(QueryClient.PREFIX,
                QueryClient.MEASURE_STRING);

        derivationOutputs.addTriple(shipIri, QueryClient.HAS_PROPERTY_STRING, ciiProperty);
        derivationOutputs.addTriple(ciiProperty, QueryClient.HAS_PROPERTY_STRING, ciiMeasure);
        derivationOutputs.addLiteral(ciiMeasure, QueryClient.HAS_NUMERICALVALUE_STRING, 15);
    }
}
