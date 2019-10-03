/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.species.Species;

/**
 *
 * @author pb556
 * 
 */

public class VariableSet {
    
    private VariableFactory factory;
    
    /**
     * @author nk510 (caresssd@hermes.cam.ac.uk)
     * Line below is commented from original source code.
     */
    
//  private BiMap<Species, Variable> speciesToVariables = HashBiMap.create();
    
    private HashMap<Species, Variable> speciesToVariables = new HashMap<Species, Variable>();
    
   
    
    
    public VariableSet(VariableFactory factory) {
    	
        this.factory = factory;
    }
    
    /**
     * 
	 * The method returns the same data on each run.
	 * 
	 */
    public Variable getVariableOf(Species species) {
    	
    System.out.println("Variable getVariableOf(Species " +species.getRef() + " )");
    	
    if (!speciesToVariables.containsKey(species) && species != null) {
        	
        speciesToVariables.put(species, factory.newVariable());
            
        }
    
    
    
        
        for(Map.Entry<Species, Variable> speciesToVar : speciesToVariables.entrySet()) {
        	
        System.out.println("species name: " + speciesToVar.getKey().getRef() + " var name: " + speciesToVar.getValue().name + " var value: " + speciesToVar.getValue().absName);        
        
        }
        
        /**
         * @author nk510 (caresssd@hermes.cam.ac.uk)
         * Line below is commented from original code.
         */
        //return speciesToVariables.get(species);
        
        return speciesToVariables.get(species);
    }

    public Species findSpeciesByVariableName(String varName) {
    	/**
         * @author nk510 (caresssd@hermes.cam.ac.uk)
         * Line below is commented from original code.
         */
//      for (Map.Entry<Species, Variable> entry : speciesToVariables.entrySet()) {
    	/**
    	 * Line is added instead of commented line above.
    	 */
    	for (Map.Entry<Species, Variable> entry :speciesToVariables.entrySet()) {	
            Species iSDSpecies = entry.getKey();
            Variable variable = entry.getValue();
            
            if (varName.equals(variable.name) || varName.equals(variable.absName)) {
            	
            return iSDSpecies;
            
            }
        }
        
        return null;
    }
    
    public Set<Variable> getSet() {
    	
        HashSet<Variable> vars = new HashSet<Variable>();
        
         /**
          * 
          * @author nk510 (caresssd@hermes.cam.ac.uk)
          * Three lines below are commneted from original source code.
          * 
          */
//        for (Species s : speciesToVariables.keySet()) {
//        	
//            vars.add(speciesToVariables.get(s));
//            
//        }
        
        /**
         * Three lines below are added instead of three lines above that are commneted.
         */
      for (Species s : speciesToVariables.keySet()) {
    	
        vars.add(speciesToVariables.get(s));
        
    }
        
        return vars;
    }
}