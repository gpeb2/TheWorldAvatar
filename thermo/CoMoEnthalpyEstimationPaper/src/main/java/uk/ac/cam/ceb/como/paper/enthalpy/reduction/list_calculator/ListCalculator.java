/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.paper.enthalpy.reduction.list_calculator;

import java.util.List;

/**
 *
 * @author pb556
 */
public interface ListCalculator<T> {
    
    public T calculate(List<T> values);
    
}
