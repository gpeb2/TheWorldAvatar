package org.cam.ceb.como.nist.webbook.parser;

import java.util.HashMap;
import java.util.Map;

import org.cam.ceb.como.nist.webbook.DownloadHTML;
import org.cam.ceb.como.nist.webbook.info.NISTSpeciesInfo;
import org.cam.ceb.como.nist.webbook.thermochem.NISTEnthalpy;

public class NISTWebBookParser {
	/**
	 * A map created to store the type of parsed file vs information extracted from the file. 
	 */
	Map<String, NISTSpeciesInfo> data = new HashMap<String, NISTSpeciesInfo>();
	
	static NISTWebBookParser nistWebBookParser;
	static NISTSDFParser nistSDFParser;
	/**
	 * The default constructor
	 */
	public NISTWebBookParser() {
		data = new HashMap<String, NISTSpeciesInfo>();
		nistSDFParser = new NISTSDFParser();
	}
	
	
	public static void main(String[] args) {
		nistWebBookParser = new NISTWebBookParser();
		try{
			nistWebBookParser.parseHTML("D:\\msff2\\Documents\\Data\\NIST\\ChemSpecies\\html\\");
			nistWebBookParser.parseSDF("D:\\msff2\\Documents\\Data\\NIST\\download\\", "D:\\msff2\\Documents\\Data\\NIST\\list-of-atoms\\atom.csv");
		}catch(Exception e){
			e.printStackTrace();
		}
		nistWebBookParser.display();

	}
	
	private void display(){
		for(String key:data.keySet()){
			NISTSpeciesInfo speciesInfo = data.get(key);
			DownloadHTML.display(speciesInfo);
			if(speciesInfo.getEnergy()!=null){
				System.out.println("Energy:"+speciesInfo.getEnergy());
			}
			System.out.println("Paired Electrons:"+speciesInfo.getPairedElectrons());
			System.out.println("Unpaired Electrons:"+speciesInfo.getUnpairedElectrons());
			System.out.println("Total number of Electrons:"+speciesInfo.getElectrons());
			if(speciesInfo.getName()!=null && !speciesInfo.getName().isEmpty()){
				System.out.println("name:"+speciesInfo.getName());
			}
			if(speciesInfo.gettBoil()!=null){
				System.out.println("Boiling point temperature:"+speciesInfo.gettBoil().getValue());
				System.out.println("Boiling point temperature units:"+speciesInfo.gettBoil().getUnits());
			}
			if(speciesInfo.gettCritical()!=null){
				System.out.println("Critical point temperature:"+speciesInfo.gettCritical().getValue());
				System.out.println("Critical point temperature units:"+speciesInfo.gettCritical().getUnits());
			}
			if(speciesInfo.getpTriple()!=null){
				System.out.println("Triple point pressure:"+speciesInfo.getpTriple().getValue());
				System.out.println("Triple point pressure units:"+speciesInfo.getpTriple().getUnits());
			}
			if(speciesInfo.gettFusion()!=null){
				System.out.println("Fusion (or melting) temperature:"+speciesInfo.gettFusion().getValue());
				System.out.println("Fusion (or melting) temperature units:"+speciesInfo.gettFusion().getUnits());
			}
			if(speciesInfo.getEnthalpy()!=null && speciesInfo.getEnthalpy().size()>0){
				for(NISTEnthalpy enthalpy: speciesInfo.getEnthalpy()){
					System.out.println("Enthalpy value:"+enthalpy.getValue());
					System.out.println("Enthalpy units:"+enthalpy.getUnits());
					System.out.println("Enthalpy reference:"+enthalpy.getReference());
				}
			}
			if(speciesInfo.getPhase()!=null && !speciesInfo.getPhase().trim().isEmpty()){
				System.out.println("Phase:"+speciesInfo.getPhase());
			}			
			System.out.println(" - - -  - - - -  - - - - - -  - - - - - - - -");
		}
	}
	
	/**
	 * Scans the current HTML file to extract information of interest. 
	 * 
	 * @param htmlFolderPath
	 * @throws Exception
	 */
	public void parseHTML(String htmlFolderPath) throws Exception{
		if(htmlFolderPath!=null && !htmlFolderPath.isEmpty())
		{
			DownloadHTML.parsingHTML(htmlFolderPath, data);
		}
	}
	
	/**
	 * Scans the current SDF file to extract information of interest. 
	 * 
	 * @param sdfFolderPath
	 * @throws Exception
	 */
	public void parseSDF(String sdfFolderPath, String pathToAtoms) throws Exception{
		if(sdfFolderPath!=null && !sdfFolderPath.isEmpty())
		{
			nistSDFParser.setPathToAtoms(pathToAtoms);
			nistSDFParser.parseSDF(sdfFolderPath, data);
		}
	}

}
