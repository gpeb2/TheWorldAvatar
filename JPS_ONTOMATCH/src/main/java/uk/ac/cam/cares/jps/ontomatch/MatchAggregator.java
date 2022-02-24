package uk.ac.cam.cares.jps.ontomatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.herrmann.generator.Generator;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.annotate.MetaDataAnnotator;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
/***
 * 

Agent that processes all alignment files produced by single metric matching to a combination result
Include functions to choose: weighted sum, filtering ,cardinality filtering, class type penalizing
Must start with weighted sum and end with filtering
Input: IRI of alignments
Output: IRI of new alignmnent
 * @author shaocong
 *
 */
import uk.ac.cam.cares.jps.base.util.AsyncPythonHelper;
import uk.ac.cam.cares.jps.ontomatch.alignment.AlignmentIOHelper;
import uk.ac.cam.cares.jps.ontomatch.alignment.Measurement;
import uk.ac.cam.cares.jps.ontomatch.properties.OntomatchProperties;
import uk.ac.cam.cares.jps.ontomatch.streamRDFs.AlignmentGenerator;
import uk.ac.cam.cares.jps.paramsValidator.ParamsValidateHelper;
import uk.ac.cam.cares.jps.paramsValidator.ParamsValidateHelper.CUSTOMVALUETYPE;

/***
 * 
 * Aggregates element level alignment result(tmp alignment owl files). Contains
 * several function to be composed by choice. Input from KG: alignment owl IRIs.
 * Output to KG: result alignment owl IRI.
 * 
 * @author shaocong zhang
 * @version 1.0
 * @since 2020-09-08
 */
@WebServlet(urlPatterns = { "/matchAggregator" })

public class MatchAggregator extends JPSAgent {

	private static final long serialVersionUID = -1142445270131640156L;
	protected int BATCH_SIZE = 100;
	protected String srcOnto, tgtOnto;
	protected String thisAlignmentIRI;
	public List<Map> batchScoreList = new ArrayList<Map>();
	protected List<Double> weights = new ArrayList<Double>();
	protected double threshold;
	protected List<AGGREGATE_CHOICE> choices = new ArrayList<AGGREGATE_CHOICE>();
	protected String classAlignmentIRI;
	protected double pFactor, sameClassThreshold;
	protected List<Generator> MatcherResultGenerators = new ArrayList<Generator>();
	private String venvname = OntomatchProperties.getInstance().getProperty(OntomatchProperties.VENV_NAME);
	private AsyncPythonHelper pyHelper =  AsyncPythonHelper.getInstance(venvname);
	/*** types of optional steps in aggregator **/
	 enum AGGREGATE_CHOICE {
		PENALIZING, CARDINALITY
	}

	 @Override
	public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		JSONObject jo = requestParams;
		JSONObject resultObj = new JSONObject();
		if (validateInput(requestParams)) {
		try {
			threshold = jo.getFloat("threshold");
			srcOnto = jo.getString("srcOnto");
			tgtOnto = jo.getString("tgtOnto");
			thisAlignmentIRI = jo.getString("addr");

			/*** get weights *****/
			JSONArray jweight = jo.getJSONArray("weights");
			for (int i = 0; i < jweight.length(); i++) {
				weights.add(jweight.getDouble(i));
			}
			/** get alignment IRIS generated by single matchers ****/
			JSONArray jalignments = jo.getJSONArray("alignments");
			for (int i = 0; i < jalignments.length(); i++) {
				String aIRI = jalignments.getString(i);
				System.out.println(aIRI);
				MatcherResultGenerators.add(AlignmentIOHelper.getAlignmentListAsStream(aIRI));
			}
			/** get optional steps chosen by caller agent ****/
			if (jo.has("choices")) {
				JSONArray functionChoice = jo.getJSONArray("choices");
				for (int i = 0; i < functionChoice.length(); i++) {
					choices.add(AGGREGATE_CHOICE.valueOf(functionChoice.getString(i)));
				}
			}
		} catch (Exception e1) {
			throw new JPSRuntimeException(e1);

		}


		/*** reading params for optional step, if any ****/
		try {
			classAlignmentIRI = jo.getString("classAlign");
			pFactor = jo.getDouble("pFactor");
			sameClassThreshold = jo.getInt("sameClassThreshold");
		} catch (Exception e) {
			// do nothing as these are optional params
		}
		;
		/*** execute aggregating procedure ****/
		try {
			// run aggregation according to choices of steps
			AlignmentIOHelper.writeAlignmentFileHeader(srcOnto, tgtOnto, thisAlignmentIRI);
			handleChoiceWithBatch();
			// write result to KG
			AlignmentIOHelper.writeAlignment2File(batchScoreList, srcOnto, tgtOnto,
			 thisAlignmentIRI);
			String successFlag = OntomatchProperties.getInstance().getProperty(OntomatchProperties.SUCCESS_FLAG);
			resultObj.put(successFlag, true);
		} catch (Exception e) {
			throw new JPSRuntimeException(e);
		}}
		return resultObj;
	}

	/***
	 * run aggregation according to choices of steps by BATCH
	 * @throws Exception
	 */
	public void handleChoiceWithBatch() throws Exception {
		/***** weighted sum **************/
		int matcherNum = MatcherResultGenerators.size();
		Iterator[] scoreIters = new Iterator[matcherNum];
		// initiate generators
		for (int i = 0; i < matcherNum; i++) {
			scoreIters[i] = MatcherResultGenerators.get(i).iterator();
		}

		// loop thru triples and update aggregated alignment file batch by batch
		while (scoreIters[0].hasNext()) {
			if (batchScoreList.size() == BATCH_SIZE) {// write and reset batch score list
				AlignmentIOHelper.updateAlignmentFile(thisAlignmentIRI, batchScoreList);
				System.out.println("one batch");
				batchScoreList.clear();
			}
			weightingWithGenerator(scoreIters, threshold);
		}
		//process remaining items in buffer
		if (batchScoreList.size()>0) {
			AlignmentIOHelper.updateAlignmentFile(thisAlignmentIRI, batchScoreList);
		    batchScoreList.clear();
		}
		
		// read the result
	    batchScoreList.clear();
		batchScoreList = AlignmentIOHelper.readAlignmentFileAsMapList(thisAlignmentIRI);
		System.out.println("file read, prepare to penalize");
		/******** class penalizing filtering(optional) ****************/
		if (choices != null && choices.contains(AGGREGATE_CHOICE.PENALIZING)) {
			 penalizing(classAlignmentIRI, sameClassThreshold, pFactor);
		}
		System.out.println("penalizing sucssesss");

		/******** cardinality filtering(optional) ****************/
		if (choices != null && choices.contains(AGGREGATE_CHOICE.CARDINALITY)) {
			one2oneCardinalityFiltering( AlignmentIOHelper.IRI2local(thisAlignmentIRI));
		}
		/******** filtering by measure value ****************/
	     filtering(threshold);

	}



	/**
	 * extract double value from rdf style value definition
	 * @param valueString
	 * @return
	 */
	public double getDoubleSparqlResult(String valueString) {
		try {
			double value = Double.parseDouble(valueString);
			return value;
		} catch (NumberFormatException e) {
			Pattern pattern = Pattern.compile("\"([0-9\\.]+)\"\\^\\^http:\\/\\/www.w3.org\\/2001\\/XMLSchema#float");
			Matcher m = pattern.matcher(valueString);
			if (m.find()) {
			}
			return Double.parseDouble(m.group(1));
		}
	}

	/***
	 * weighting by looping matcher value generator
	 * @param scoreIters
	 * @param threshold
	 */
	protected void weightingWithGenerator(Iterator[] scoreIters, double threshold) {
		Map<String, Object> acell = new HashMap<>();
		String[] values = (String[]) scoreIters[0].next();
		acell.put("entity1", values[0]);
		acell.put("entity2", values[1]);
		double measure = weights.get(0) * getDoubleSparqlResult(values[2]);
		for (int idxMatcher = 1; idxMatcher < scoreIters.length; idxMatcher++) {
			String[] scoreValues = (String[]) scoreIters[idxMatcher].next();
			if (!scoreValues[0].equals(values[0]) || !scoreValues[1].contentEquals(values[1])) {
				System.out.println("err sequence");
				System.out.println(values[0]);
				System.out.println(values[1]);

			}
			measure += weights.get(idxMatcher) * getDoubleSparqlResult(scoreValues[2]);
		}
		acell.put("measure", measure);
		if (measure >= threshold) {
			batchScoreList.add(acell);
		}
	}


	/***
	 * filtering by measure
	 * 
	 * @param threshold of measure, smaller than this will be discard
	 */
	protected void filtering(double threshold) {// remove cellmaps with measure<threshold
		int elementNum = batchScoreList.size();
		// loop thru cells to filter out based on measurement
		Iterator<Map> it = batchScoreList.iterator();
		while (it.hasNext()) {
			Map mcell = it.next();
			// Do something
			if ((double) mcell.get("measure") - threshold < 0) {
				// TODO:ERR HERE
				it.remove();
			}
		}
	}

	/***
	 * Penalizing measurement based on if two entities belong to same
	 * Class(equivalent class also counts)
	 * 
	 * @param classAlignmentIRI  IRI of the T-BOX matching
	 * @param sameClassThreshold threshold of measurement to determine if two terms
	 *                           in T-BOX is equivalent
	 * @param pFactor            penalizing factor(0<r<1), if not same class,
	 *                           current measure score multiply this factor
	 * @throws Exception
	 */
	public void penalizing(String classAlignmentIRI, double sameClassThreshold, double pFactor) throws Exception {
		OntModel srcModel = ModelFactory.createOntologyModel();
		srcModel.read(srcOnto);
		OntModel tgtModel = ModelFactory.createOntologyModel();
		tgtModel.read(tgtOnto);
		Map ICMap1 = constructICMap(srcModel);
		Map ICMap2 = constructICMap(tgtModel);
		int elementNum = batchScoreList.size();
		JSONArray joca = AlignmentIOHelper.readAlignmentFileAsJSONArray(classAlignmentIRI, sameClassThreshold);
		List classAlign = AlignmentIOHelper.Json2ScoreList(joca);
		for (int idxElement = 0; idxElement < elementNum; idxElement++) {
			Map mcell = batchScoreList.get(idxElement);
			String indi1 = (String) mcell.get("entity1");
			String indi2 = (String) mcell.get("entity2");

			if (!sameClass(classAlign, ICMap1, ICMap2, indi1, indi2)) {// does not belong to same class
			   System.out.println("not same class");
				mcell.put("measure", (double) mcell.get("measure") * pFactor);// penalize
			}
		}

	}

	/**
	 * Determine if two individual entities from two A-Boxs belongs to equivalent
	 * class
	 * 
	 * @param classAlign List of the class aligned pairs
	 * @param icmap1     individual-class map of ontology1
	 * @param icmap2     individual-class map of ontology2
	 * @param indiIri1   IRI of entity1
	 * @param indiIri2   IRI of entity2
	 * @return boolean belongs to equivalent class or not
	 */
	protected boolean sameClass(List<Map> classAlign, Map icmap1, Map icmap2, String indiIri1, String indiIri2) {
		List classt1 = (List) icmap1.get(indiIri1);
		List classt2 = (List) icmap2.get(indiIri2);
		if(classt1 == null || classt2 ==null || classt1.size()==0 ||classt2.size()==0) {
			return false;
		}
		if (sameClass(classAlign, classt1, classt2)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Determine if two classes from two T-Boxs are equivalent
	 * 
	 * @param classAlign List of the class aligned pairs
	 * @param class1  IRI of class1
	 * @param class2  IRI of class2
	 * @return boolean same class or not
	 */
	
	protected boolean sameClass(List<Map> classAlign, List<String> classtree1, List<String> classtree2) {

		for (Map matchedPair : classAlign) {
			String matchedClass1 = (String) matchedPair.get("entity1");
			String matchedClass2= (String) matchedPair.get("entity2");
            
			if (classtree1.contains(matchedClass1) && classtree2.contains(matchedClass2)
					) {
				return true;
			}
		}
		return false;
	}

	/***
	 * Construct a Individual-class map by querying a ontology model
	 * 
	 * @param model        jena model of the ontology
	 * @return
	 */
	public Map constructICMap(OntModel model) {
		Map ICMap = new HashMap();
		ExtendedIterator classes = model.listClasses();
		while (classes.hasNext()) {
			OntClass thisClass = (OntClass) classes.next();
			if(thisClass.getURI()==null || thisClass.isProperty()||thisClass.getURI().contentEquals("http://www.w3.org/2002/07/owl#Thing")) {
				continue;
			}
			ExtendedIterator instances = thisClass.listInstances();
			while (instances.hasNext()) {
				Individual thisInstance = (Individual) instances.next();
				
				if(ICMap.get(thisInstance.getURI())==null) {
					List classl = new ArrayList<String>();
					classl.add(thisClass.getURI());
					ICMap.put(thisInstance.getURI(), classl);

				} else {
				List classl  = (List) ICMap.get((thisInstance.getURI()));
				classl.add(thisClass.getURI());
				ICMap.put(thisInstance.getURI(), classl);

				}
				}
		}
		return ICMap;
	}

	/***
	 * one-to-one cardinality filtering
	 * 
	 * @throws IOException
	 */
	public void one2oneCardinalityFiltering(String alignmentFileAddr) throws IOException {
		// need to call the python for now
		// input: map rendered as json
		//String tmpAddress = OntomatchProperties.getInstance()
		//		.getProperty(OntomatchProperties.CARDINALITYFILTERING_TMP_ALIGNMENT_PATH);
		String[] paras = {  alignmentFileAddr };
		String pyName = OntomatchProperties.getInstance().getProperty(OntomatchProperties.PY_NAME_ONETOONECARDI);
		String[] results = pyHelper.callPython(pyName, paras, MatchAggregator.class);//
		System.out.println(results[0]);
		System.out.println(results[0]);

		JSONArray scoreListNew = new JSONArray(results[0]);
		batchScoreList = AlignmentIOHelper.Json2ScoreList(scoreListNew);
	}

	@Override
	public boolean validateInput(JSONObject requestParams) throws BadRequestException {
		if (requestParams.isEmpty() || !requestParams.has("threshold") || !requestParams.has("srcOnto")
				|| !requestParams.has("tgtOnto") || !requestParams.has("alignments") || !requestParams.has("addr")|| !requestParams.has("weights")) {
			throw new BadRequestException();
		}
		/**
		Map<String, CUSTOMVALUETYPE> paramTypes = new HashMap<String, CUSTOMVALUETYPE>();
	     paramTypes.put("threshold",CUSTOMVALUETYPE.THRESHOLD);
	     paramTypes.put("srcOnto",CUSTOMVALUETYPE.PATH);
	     paramTypes.put("tgtOnto",CUSTOMVALUETYPE.PATH);
	     paramTypes.put("alignments",CUSTOMVALUETYPE.PATHLIST);
	     paramTypes.put("addr",CUSTOMVALUETYPE.URL);
	     paramTypes.put("weights",CUSTOMVALUETYPE.WEIGHTS);
			if (requestParams.has("classAlignment")) {
				paramTypes.put("classAlignment", CUSTOMVALUETYPE.URL);
			}
			if (requestParams.has("pFactor")) {
				paramTypes.put("pFactor", CUSTOMVALUETYPE.THRESHOLD);
			}
			if (requestParams.has("sameClassThrehold")) {
				paramTypes.put("sameClassThrehold", CUSTOMVALUETYPE.THRESHOLD);
			}
			if (requestParams.has("modelAddress")) {
				paramTypes.put("modelAddress", CUSTOMVALUETYPE.PATH);
			}
			if (requestParams.has("dictAddress")) {
				paramTypes.put("dictAddress", CUSTOMVALUETYPE.PATH);
			}
			if (!ParamsValidateHelper.validateALLParams(requestParams, paramTypes)) {
				throw new BadRequestException();
			}
			***/
		return true;	     

			}
}
