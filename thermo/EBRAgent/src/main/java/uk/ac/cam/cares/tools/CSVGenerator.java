package uk.ac.cam.cares.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.opencsv.CSVWriter;

import uk.ac.cam.ceb.como.nist.info.NISTSpeciesId;

/**
 * 
 * @author NK510 (caresssd@hermes.cam.ac.uk)
 * 
 * Generates a csv file by using data that are results of federated sparql query. It stores in csv file CAS Reg ID, species element list, and bond connectivities. 
 *
 */
public class CSVGenerator {
	
	private SpeciesElementGenerator seg = new SpeciesElementGenerator();
	private SpeciesBondGenerator sbg = new SpeciesBondGenerator();
	/**
	 * @author NK520 (caresssd@hermes.cam.ac.uk)
	 * 
	 * @param nistSpeciesLinkedList the list of species beans.
	 * @param csvFilePath the csv file path that stores CAS reg id, species elements, and bond connectivities.
	 * 
	 */
	public void generateCSVFile(LinkedList<NISTSpeciesId> nistSpeciesLinkedList, String csvFilePath)  {
		
		File file = new File(csvFilePath);
		
		try {
		
		 FileWriter output = new FileWriter(file);
		 
         CSVWriter writer = new CSVWriter(output);
		
		for(NISTSpeciesId species: nistSpeciesLinkedList) {
			
			LinkedHashMap<Integer,String> speciesHashMap = new LinkedHashMap<Integer,String>();
			
			speciesHashMap.putAll(seg.getSpeciesHashMap(species));
			
			LinkedList<String> speciesNameList= new LinkedList<String>();
			
			for(Map.Entry<Integer, String> map : speciesHashMap.entrySet()) {
				
			speciesNameList.add(map.getValue());
			
			}
			
			LinkedHashMap<Integer,String> updatedSpeciesHashMap = new LinkedHashMap<Integer,String>();
			
			LinkedHashMap<String, Integer> speciesFrequencyMap = new LinkedHashMap<String, Integer>();
			
			updatedSpeciesHashMap = seg.getUpdatedSpeciesHashMap(speciesNameList,speciesHashMap);
			
			speciesFrequencyMap = seg.gestSpeciesFrequencyHashMap(speciesNameList, speciesHashMap);
			
			String[] line = { species.getCASRegNr(), "", "", seg.generateSpeciesElements(speciesFrequencyMap).toString(),  sbg.generateBondConnectivity(species, updatedSpeciesHashMap).toString() };
			
			writer.writeNext(line);			
		}
		
		writer.close();
		
		}catch(IOException e) {
		
		e.printStackTrace();
			
		}
	}
}