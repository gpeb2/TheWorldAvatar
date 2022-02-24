package com.cmclinnovations.ontochemexp.model.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;

import com.cmclinnovations.ontochemexp.model.converter.prime.PrimeConverter;
import com.cmclinnovations.ontochemexp.model.exception.OntoChemExpException;

import uk.ac.cam.cares.jps.base.query.SparqlOverHttpService.RDFStoreType;
import uk.ac.cam.cares.jps.base.upload.KnowledgeRepository;

/**
 * A utility class that supports the following functionalities:<p>
 * 1. Helps convert a file system path to a URL.
 * In particular, it supports the following string conversion features:</p>
 * a) the replacement of each single space (' ') of a path with an underscore ('_').</br>
 * b) the replacement of each backslash with a front slash.</br>
 * c) the addition of the protocol 'file:/' at the beginning of a path.</br>
 * d) the extraction of an experiment's name from a file path.</br>
 * e) the formation of a URL, suitable to be used in an OWL file, by combining
 * a file path and name.</p>
 * 2. Splits a space separated string and put each split part as a key and
 * the in
 * a Hashmap and 
 * 
 * 
 * @author Feroz Farazi (msff2@cam.ac.uk)
 *
 */
public class PrimeConverterUtils extends PrimeConverter{
	
	static Logger logger = org.slf4j.LoggerFactory.getLogger(PrimeConverterUtils.class);
	/**
	 * Replaces each space character of a path
	 * with an underscore.
	 * 
	 * @param path a path that is being processed to get
	 * spaces replaced by underscores.
	 * 
	 * @return a URL generated by replacing spaces with underscores
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors
	 */
	public static String convertToURLString(String path) throws OntoChemExpException{
		String urlString ="";
		if(path!=null){
			urlString = path.replace(' ', '_');
		} else{
			logger.error("The input path is null.");
			throw new OntoChemExpException("A null input path has been provided.");
		}
		return urlString;
	}
	
	/**
	 * Replaces each backslash of a path with a front slash to
	 * make it suitable to be used as a URL.
	 * 
	 * @param path a path that is being converted to a URL
	 * @return a URL after replacing the backslashes with the front.
	 * slashes.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formatToURLSlash(String path) throws OntoChemExpException{
		if(path==null){
			logger.error("The input path is null.");
			throw new OntoChemExpException("A null input path has been provided.");
		}
		if(path.contains("\\")){
			path = path.replace("\\", "/");
		}
		return path;
	}

	/**
	 * Adds the protocol 'file:/' at the beginning of a file path
	 * to form a URL that can be used in an OWL file as a URL.
	 * 
	 * @param path an absolute file path that needs to be converted
	 * to a URL that can be used in an OWL file.
	 * @return an OWL file formatted URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String addFileProtocol(String path) throws OntoChemExpException{
		if(path==null){
			logger.error("The input path is null.");
			throw new OntoChemExpException("A null input path has been provided.");
		}
		if(!path.contains("file:/")){
			path = "file:/"+path;
		}
		return path;
	}
	
	/**
	 * Forms a URL of a file based on the path where the file is stored plus
	 * the name of experiment, which is the name of the current PrIMe xml file.
	 * 
	 * @param primeFile The path to the PrIMe file being processed.
	 * @param owlFilePath The path to the file being processed.
	 * @return a string representing a URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formOwlUrl(String primeFile, String experimentABoxFilePath) throws OntoChemExpException {
		if (primeFile == null) {
			logger.error("Provided primeFile path is null.");
			throw new OntoChemExpException("Provided primeFile path is null.");
		}
		if (experimentABoxFilePath == null) {
			logger.error("Provided file path is null.");
			throw new OntoChemExpException("Provided file path is null.");
		}
		experimentABoxFilePath = ontoChemExpKB.getOntoChemExpKbURL();
		experimentABoxFilePath = ontoChemExpKB.getOntoChemExpKbURL()
				.concat(extractExperimentName(primeFile)).concat(opCtrl.getOwlFileExtension());
		experimentABoxFilePath = formatToURLSlash(experimentABoxFilePath);
		return experimentABoxFilePath;
	}
	
	/**
	 * Forms a URL of a file based on the path where the file is stored plus
	 * the name of experiment, which is the name of the current PrIMe xml file.
	 * 
	 * @param primeFile The path to the PrIMe file being processed.
	 * @param owlFilePath The path to the file being processed.
	 * @return a string representing a URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formOwlFileSaveUrl(String primeFile, String owlFilePath) throws OntoChemExpException {
		if (primeFile == null) {
			logger.error("Provided primeFile path is null.");
			throw new OntoChemExpException("Provided primeFile path is null.");
		}
		if (owlFilePath == null) {
			logger.error("Provided file path is null.");
			throw new OntoChemExpException("Provided file path is null.");
		}
		owlFilePath = owlFilePath.concat("/").concat(ontoChemExpKB.getOntoChemExpKbRootDirectory())
				.concat(extractExperimentName(primeFile)).concat(opCtrl.getOwlFileExtension());
		owlFilePath = formatToURLSlash(owlFilePath);
		owlFilePath = addFileProtocol(owlFilePath);
		return owlFilePath;
	}

	
	/**
	 * Forms the base URL for an OWL ontology.
	 * 
	 * @param primeFile The path to the PrIMe file being processed.
	 * @return a string representing the base URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formBaseURL(String primeFile) throws OntoChemExpException {
		if (primeFile == null) {
			logger.error("Provided primeFile path is null.");
			throw new OntoChemExpException("Provided primeFile path is null.");
		}
		return ontoChemExpKB.getOntoChemExpKbURL()
				.concat(extractExperimentName(primeFile)).concat(opCtrl.getOwlFileExtension());
	}
	
	/**
	 * Forms an OWL formatted URL of a file based on the path where the file is stored plus
	 * the name of the file.
	 * 
	 * @param owlFilePath The path to the file being processed
	 * @return a string representing a URL
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors
	 */
	public static String formOwlUrl(String owlFilePath) throws OntoChemExpException {
		if (owlFilePath == null) {
			logger.error("Provided owlFilePath path is null.");
			throw new OntoChemExpException("Provided owlFilePath path is null.");
		}
		owlFilePath = formatToURLSlash(owlFilePath);
		owlFilePath = addFileProtocol(owlFilePath);
		return owlFilePath;
	}
	
	/**
	 * Extracts the name of the experiment being processed from the 
	 * primeFile path.
	 * 
	 * @param primeFile The primeFile path.
	 * @return String returns a string that is the name of the current experiment
	 * being processed.
	 * @throws OntoChemExpException
	 */
	public static String extractExperimentName(String primeFile) throws OntoChemExpException {
		if(experimentName!=null && !experimentName.isEmpty()){
			return experimentName;
		}
		if (!primeFile.contains(FRONTSLASH)) {
			logger.error("Unexpected primeFile path.");
			throw new OntoChemExpException("Unexpected primeFile path.");
		}
		if(primeFile.endsWith(".xml")){
			primeFile = primeFile.substring(0, primeFile.lastIndexOf(".xml"));
		}
		String tokens[] = primeFile.split(FRONTSLASH.concat(FRONTSLASH));
		if(tokens.length<2){
			logger.error("The primeFile path is unexpectedly short.");
			throw new OntoChemExpException("The primeFile path is unexpectedly short.");
		}
		System.out.println(tokens[tokens.length-1]);
		return tokens[tokens.length-1];
	}

	/**
	 * Checks if the current ontology contains an IRI. If an IRI is available
	 * it is returned, otherwise the OntoException exception is thrown.
	 * 
	 * @param ontology
	 * @return IRI the IRI of the input ontology 
	 * @throws OntoChemExpException
	 */
	public static IRI readOntologyIRI(OWLOntology ontology) throws OntoChemExpException{
		if(ontology.getOntologyID().getOntologyIRI().isPresent()){
			return ontology.getOntologyID().getOntologyIRI().get();
		} else{
			logger.error("The OWL file does not contain an IRI.");
			throw new OntoChemExpException("The OWL file does not contain an IRI.");
		}
	}
	
	/**
	 * Creates and returns an instance of the BufferedReader class.
	 * It takes the absolute file path including the file name as input.
	 * 
	 * @param filePathPlusName
	 *            the path plus name of the file being read
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader openSourceFile(String filePathPlusName)
			throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(
				filePathPlusName), "UTF-8"));
	}
	
	
	public static String retrieveSpeciesIRIFromPrimeID(String speciesFileIRI) throws OntoChemExpException {
		if (speciesFileIRI.trim().startsWith("<") || speciesFileIRI.trim().endsWith(">")) {
			speciesFileIRI = speciesFileIRI.replace("<", "").replace(">", "");
		}
		String queryString = formSpeciesIRIQueryFromPrimeID(speciesFileIRI);

		return retrieveSpeciesIRI(queryString);
	}
	
	public static String retrieveSpeciesIRIFromInChI(String inchi) throws OntoChemExpException {
		if (inchi.trim().toLowerCase().startsWith("1s/") || inchi.trim().toLowerCase().startsWith("1/")) {
			inchi = "InChI=".concat(inchi);
		}
		String queryString = formSpeciesIRIQueryFromInChI(inchi);
		return retrieveSpeciesIRI(queryString);
	}
	
	public static String retrieveSpeciesIRI(String queryString) throws OntoChemExpException {
		KnowledgeRepository kr = new KnowledgeRepository();
		String results = new String();
		try {
			if (ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBServerURL().toLowerCase().contains("rdf4j")) {
				results = kr.query(ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBServerURL(), ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBRepositoryID(), 
						RDFStoreType.RDF4J, queryString);
			} else if (ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBServerURL().toLowerCase().contains("blazegraph")) {
				results = kr.query(ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBServerURL(), ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBRepositoryID(), 
						RDFStoreType.BLAZEGRAPH, queryString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject(results);
		String uniqueSpeciesIRI = new String("");
		if (json.has("results")) {
			uniqueSpeciesIRI = json.getJSONObject("results").getJSONArray("bindings").getJSONObject(0).getJSONObject("speciesIRI").get("value").toString();
		}
		return uniqueSpeciesIRI;
	}
	
	private static String formSpeciesIRIQueryFromPrimeID(String partialSpeciesIRI) {
		String queryString = "PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#> \n";
		queryString = queryString.concat("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		queryString = queryString.concat("SELECT ?species \n");
		queryString = queryString.concat("WHERE { \n");
		queryString = queryString.concat("    ?species rdf:type OntoSpecies:Species . \n");
		queryString = queryString.concat("    FILTER regex(str(?species), \"").concat(partialSpeciesIRI).concat("\", \"i\") \n");
		queryString = queryString.concat("}");
		return queryString;
	}
	
	private static String formSpeciesIRIQueryFromInChI(String inchi) {
		String queryString = "PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#> \n";
		queryString = queryString.concat("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		queryString = queryString.concat("SELECT ?speciesIRI \n");
		queryString = queryString.concat("WHERE { \n");
		queryString = queryString.concat("    ?speciesIRI rdf:type OntoSpecies:Species . \n");
		queryString = queryString.concat("    ?speciesIRI OntoSpecies:inChI ?Inchi . \n");
		queryString = queryString.concat("    FILTER REGEX(REPLACE(str(?Inchi), \"InChI=1S\", \"InChI=1\"), REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(\"")
				.concat(inchi).concat("\", \"InChI=1S\",\"InChI=1\"), \"/t.+\", \"\"), \"/b.+\", \"\"), \"\\\\(\", \"\\\\\\\\(\"), \"\\\\)\", \"\\\\\\\\)\"), \"i\") \n");
		queryString = queryString.concat("}");
		return queryString;
	}
	
	/**
	 * 
	 * @param aboxFileName
	 * @param aboxFilePath
	 * @return
	 * @throws OntoChemExpException
	 */
	public static String uploadExperiment(String aboxFileName, String aboxFilePath) throws OntoChemExpException {
		loadOntology(ontoChemExpKB.getUploadTripleStoreServerURL(), aboxFileName, aboxFilePath, ontoChemExpKB.getOntoChemExpKbURL(), ontoChemExpKB.getUploadTripleStoreRepositoryOntoChemExp());
		return ontoChemExpKB.getOntoChemExpKbURL().concat(aboxFileName);
	}
	
	/**
	 * Loads an abox to the ontology KB repository. It also creates</br>
	 * a context, which is a necessary feature to delete the abox</br>
	 * if user wants.
	 * 
	 * @param serverURL
	 * @param aboxFileName
	 * @param aboxFilePath
	 * @param baseURI
	 * @param repositoryID
	 * @throws OntoChemExpException
	 */
	public static void loadOntology(String serverURL, String aboxFileName, String aboxFilePath, String baseURI, 
			String repositoryID) throws OntoChemExpException {
		checkUploadParameterValidity(serverURL, aboxFileName, aboxFilePath, baseURI, repositoryID);
		try {
			Repository repo = new HTTPRepository(serverURL, repositoryID);
			repo.initialize();
			RepositoryConnection con = repo.getConnection();
			ValueFactory f = repo.getValueFactory();
			org.eclipse.rdf4j.model.IRI context = f.createIRI(baseURI.concat(aboxFileName));
			try {
				URL url = new URL("file:/".concat(aboxFilePath).concat(aboxFileName));
				con.add(url, url.toString(), RDFFormat.RDFXML, context);
			} finally {
				con.close();
			}
		} catch (RDF4JException e) {
			logger.error("RDF4JException occurred.");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException occurred.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks the validity of the following parameters:</br>
	 * 1. The Server URL.</br>
	 * 2. The abox file name.</br>
	 * 3. The abox file path.</br>
	 * 4. The base URL.</br>
	 * 5. The Knowledge Base repository ID.
	 * 
	 * @param serverURL
	 * @param aboxFileName
	 * @param aboxFilePath
	 * @param baseURI
	 * @param repositoryID
	 * @throws OntoChemExpException
	 */
	private static void checkUploadParameterValidity(String serverURL, String aboxFileName, String aboxFilePath,
			String baseURI, String repositoryID) throws OntoChemExpException {
		checkURLValidity(serverURL, "The server URL");
		checkStringValidity(aboxFileName, "The abox file name");
		checkFilePathValidity(aboxFilePath, "The abox file path");
		checkURLValidity(baseURI, "The base IRI");
		checkStringValidity(repositoryID, "The repository ID");
	}
	
	/**
	 * Checks the validity of a URL.
	 * 
	 * @param url
	 * @param message
	 * @throws OntoException
	 */
	private static void checkURLValidity(String url, String message) throws OntoChemExpException {
		if (url == null) {
			if (message != null) {
				throw new OntoChemExpException(message.concat("is null."));
			}
		}
		if (url.isEmpty()) {
			throw new OntoChemExpException(message.concat(" is empty."));
		}
		if (!IRI.create(url).isIRI()) {
			throw new OntoChemExpException(message.concat(" is not valid."));
		}
	}

	/**
	 * Checks the validity of a string value.</br>
	 * It checks whether the string value is null or empty.
	 * 
	 * @param string
	 * @param message
	 * @throws OntoException
	 */
	private static void checkStringValidity(String string, String message) throws OntoChemExpException {
		if (string == null) {
			if (message != null) {
				throw new OntoChemExpException(message.concat(" is null."));
			}
		}
		if (string.isEmpty()) {
			throw new OntoChemExpException(message.concat(" is empty."));
		}
	}

	/**
	 * Checks the validity of a file system file path.</br>
	 * It checks whether the path is valid file path, null or empty.
	 * 
	 * @param path
	 * @param message
	 * @throws OntoException
	 */
	private static void checkFilePathValidity(String path, String message) throws OntoChemExpException {
		File file = new File(path);
		if (path == null) {
			if (message != null) {
				throw new OntoChemExpException(message.concat(" is null."));
			}
		}
		if (path.isEmpty()) {
			throw new OntoChemExpException(message.concat(" is empty."));
		}
		if (!file.exists()) {
//			throw new OntoException("The following file does not exist:"+path);
		}
	}
	
	public static OWLIndividual createIndividualFromOtherOntology(String tboxPath, String clasName, String instance) throws OntoChemExpException {
		OWLClass claz = createOWLClass(dataFactory, tboxPath, clasName);
		OWLIndividual individual = createOWLIndividual(dataFactory, basePathABox, instance);
		// Adds to the ontology the instance of the class
		manager.applyChange(new AddAxiom(ontology, dataFactory.getOWLClassAssertionAxiom(claz, individual)));
		return individual;
	}
	
	public static void addObjectPropertyFromOtherOntology(String objectPropertyPath, String domainInstanceName, String rangeInstanceName) throws OntoChemExpException {
		OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(objectPropertyPath);
		OWLIndividual domainIndividual = createOWLIndividual(dataFactory, basePathABox, domainInstanceName);
		OWLIndividual rangeIndividual = createOWLIndividual(dataFactory, basePathABox, rangeInstanceName);
		manager.applyChange(new AddAxiom(ontology, 
				dataFactory.getOWLObjectPropertyAssertionAxiom(objectProperty, domainIndividual, rangeIndividual)));
	}
	
	public static void addObjectPropertyFromOtherOntology(String basePath, String objectPropertyName, String domainInstanceName, String rangeInstanceName) throws OntoChemExpException {
		OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(basePath.concat(HASH).concat(objectPropertyName));
		OWLIndividual domainIndividual = createOWLIndividual(dataFactory, basePathABox, domainInstanceName);
		OWLIndividual rangeIndividual = createOWLIndividual(dataFactory, basePathABox, rangeInstanceName);
		manager.applyChange(new AddAxiom(ontology, 
				dataFactory.getOWLObjectPropertyAssertionAxiom(objectProperty, domainIndividual, rangeIndividual)));
	}
	
	public static void addDataPropertyFromOtherOntology(String basePath, String instance, String dataPropertyName, String dataPropertyValue, String propertyType) throws OntoChemExpException {
		OWLIndividual individual = createOWLIndividual(dataFactory, basePathABox, instance);
		OWLLiteral literal = createOWLLiteral(dataFactory, dataPropertyValue, propertyType);
		OWLDataProperty dataPropertyCreated = createOWLDataProperty(dataFactory, basePath, dataPropertyName, HASH);
		manager.applyChange(new AddAxiom(ontology, 
				dataFactory.getOWLDataPropertyAssertionAxiom(dataPropertyCreated, individual, literal)));
	}
	
	/**
	 * Create ontology class. 
	 * 
	 * @param ontoFactory
	 * @param owlFilePath baseTBoxPath
	 * @param className
	 * @return
	 */
	private static OWLClass createOWLClass(OWLDataFactory ontoFactory, String owlFilePath, String className) {
		if (className != null && (className.trim().startsWith("http://") || className.trim().startsWith("https://"))) {
			return ontoFactory.getOWLClass(className.trim());
		}
		return ontoFactory.getOWLClass(owlFilePath.concat("#").concat(className));
	}
	
	/**
	 * 
	 * @param ontoFactory
	 * @param owlFilePath
	 * @param individualName
	 * @return
	 */
	private static OWLIndividual createOWLIndividual(OWLDataFactory ontoFactory, String owlFilePath, String individualName) {
		if (individualName != null && (individualName.trim().startsWith("http://") || individualName.trim().startsWith("https://"))) {
			return ontoFactory.getOWLNamedIndividual(individualName.trim());
		}
		return ontoFactory.getOWLNamedIndividual(owlFilePath.concat(BACKSLASH).concat(individualName));
	}
	
	/**
	 * 
	 * @param dataFactory
	 * @param iri
	 * @param propertyName
	 * @param separator
	 * @return
	 */
	private static OWLDataProperty createOWLDataProperty(OWLDataFactory dataFactory, String iri, String propertyName, String separator) {
		if (propertyName != null && (propertyName.trim().startsWith("http://") || propertyName.trim().startsWith("https://"))) {
			return dataFactory.getOWLDataProperty(propertyName.trim());
		}
		return dataFactory.getOWLDataProperty(iri.concat(separator).concat(propertyName));
	}
	
	/**
	 * 
	 * @param ontoFactory
	 * @param literal
	 * @param propertyType
	 * @return
	 * @throws OntoChemExpException
	 */
	private static OWLLiteral createOWLLiteral(OWLDataFactory ontoFactory, String literal, String propertyType) throws OntoChemExpException {
		if (propertyType.equalsIgnoreCase("string")) {
			return ontoFactory.getOWLLiteral(literal);
		} else if (propertyType.equalsIgnoreCase("integer")) {
			try {
				return ontoFactory.getOWLLiteral(Integer.parseInt(literal));
			} catch (NumberFormatException e) {
				throw new OntoChemExpException("The following value is not an integer:"+literal);
			}
		} else if (propertyType.equalsIgnoreCase("float")) {
			try {
				return ontoFactory.getOWLLiteral(Float.parseFloat(literal));
			} catch (NumberFormatException e) {
				throw new OntoChemExpException("The following value is not a float:"+literal);
			}
		}
		return ontoFactory.getOWLLiteral(literal);
	}
}
