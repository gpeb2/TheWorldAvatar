/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cam.ceb.como.chem.filemgmt.filter;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author pb556
 */
public class CMLFileFilter extends FileFilter {
        
    public CMLFileFilter() {
        super();
    }
    
    public CMLFileFilter(boolean recursive) {
        super(recursive);
    }
    
    public CMLFileFilter(File source) {
        super(source);
    }
    
    public CMLFileFilter(File source, boolean recursive) {
        super(source, recursive);
    }
    
    @Override
    public String[] getValidExtensions() {
        return new String[] {"cml"};
    }

    @Override
    public Collection<File> getValidFiles(File source, Map properties) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
