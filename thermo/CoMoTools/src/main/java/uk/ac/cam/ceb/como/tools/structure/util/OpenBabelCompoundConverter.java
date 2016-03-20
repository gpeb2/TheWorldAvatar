/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.tools.structure.util;

import uk.ac.cam.ceb.como.chem.structure.util.CompoundConverter;
import java.util.HashMap;
import java.util.Map;
import org.openbabel.OBAtom;
import org.openbabel.OBBond;
import org.openbabel.OBMol;
import org.xmlcml.cml.element.CMLMolecule;
import uk.ac.cam.ceb.como.chem.structure.Atom;
import uk.ac.cam.ceb.como.chem.structure.Bond;
import uk.ac.cam.ceb.como.chem.structure.BondType;
import uk.ac.cam.ceb.como.chem.structure.Compound;

/**
 *
 * @author pb556
 */
public class OpenBabelCompoundConverter {

    public static OBMol convertToOBMol(CMLMolecule jDoc) {
        return convertToOBMol(CompoundConverter.convert(jDoc));
    }

    public static OBMol convertToOBMol(Compound jDoc) {
        OBMol obmol = new OBMol();
        Map<Atom, Integer> atomMap = new HashMap<Atom, Integer>(jDoc.getAtomCount());
        for (int i = 0; i < jDoc.getAtomCount(); i++) {
            OBAtom atom = new OBAtom();
            atom.SetAtomicNum(jDoc.getAtom(i).getElement().getAtomicNumber());
            atom.SetIdx(i + 1);
            atom.SetVector(jDoc.getAtom(i).getXInA(), jDoc.getAtom(i).getYInA(), jDoc.getAtom(i).getZInA());
            obmol.AddAtom(atom);
            atomMap.put(jDoc.getAtom(i), i + 1);
        }

        for (int i = 0; i < jDoc.getBondCount(); i++) {
            OBBond bond = new OBBond();
            Bond mybond = jDoc.getBond(i);
            OBAtom atomA = obmol.GetAtom(atomMap.get(mybond.getAtomA()));
            OBAtom atomB = obmol.GetAtom(atomMap.get(mybond.getAtomB()));
            bond.Set(i, atomA, atomB, BondType.convertToOBBondType(mybond.getBondType()), 0);
            obmol.AddBond(bond);
        }
        return obmol;
    }
}
