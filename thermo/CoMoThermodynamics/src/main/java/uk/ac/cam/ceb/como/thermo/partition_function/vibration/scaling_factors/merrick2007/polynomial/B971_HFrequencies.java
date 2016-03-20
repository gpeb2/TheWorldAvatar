/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.thermo.partition_function.vibration.scaling_factors.merrick2007.polynomial;

import java.util.HashMap;
import uk.ac.cam.ceb.como.math.function.simple.PolynomialFunction;

/**
 *
 * @author pb556
 */
public class B971_HFrequencies extends PolynomialFunction {
    
    private HashMap<Integer, Double[]> coeff = new HashMap<Integer, Double[]>();
    
    public B971_HFrequencies(int degree) {
        super();
        initCoefficients();
        super.setCoefficients(coeff.get(degree));
    }
    
    private void initCoefficients() {
        coeff = new HashMap<Integer, Double[]>();
        
        coeff.put(2, new Double[] {0.9991927530804878, -2.467108541559233E-5, 4.539928587960903E-9});
        coeff.put(3, new Double[] {1.0253406150741533, -7.21315012765938E-5, 2.8463949566524543E-8, -3.546115384149823E-12});
        coeff.put(4, new Double[] {1.0407691619068675, -1.1367076228374932E-4, 6.38442695767665E-8, -1.5324498638137825E-11, 1.3351661641467695E-15});
        coeff.put(5, new Double[] {1.0323570195652012, -8.193389084712301E-5, 2.3651108460018772E-8, 6.9212226772393235E-12, -4.216708471249269E-15, 5.112607257169711E-19});
        coeff.put(6, new Double[] {1.0055110606938709, 5.0763781565427884E-5, -2.039990327777667E-7, 1.885489863041647E-10, -7.754207722236784E-14, 1.5030451970701893E-17, -1.1194712071397592E-21});
        coeff.put(7, new Double[] {0.9804748002988926, 2.00976126032093E-4, -5.280104897575123E-7, 5.291401203934773E-10, -2.713708982821174E-13, 7.5951128790418E-17, -1.105629754109895E-20, 6.561977145898797E-25});
        coeff.put(8, new Double[] {0.9945477342142212, 1.0340788357573492E-4, -2.753173564301328E-7, 1.976873383780398E-10, -2.4790575032488108E-14, -3.241454005266755E-17, 1.677608140620994E-20, -3.202354387908555E-24, 2.228660077632106E-28});
        coeff.put(9, new Double[] {1.0702191210178629, -4.898666013240218E-4, 1.5248469759155757E-6, -2.6558288131621003E-9, 2.6288128000634643E-12, -1.555278385519103E-15, 5.622333610840003E-19, -1.2186340032983312E-22, 1.45563478870644E-26, -7.370068504427917E-31});
        coeff.put(10, new Double[] {1.1645691363059136, -0.001317359273996129, 4.41720770332954E-6, -8.067713200263174E-9, 8.721925958414411E-12, -5.920709699427994E-15, 2.5980009387340463E-18, -7.3710921197997E-22, 1.3068193440217603E-25, -1.317027457095554E-29, 5.762563180726755E-34});
        coeff.put(11, new Double[] {1.1775328691246285, -0.0014429255435937636, 4.913248537793467E-6, -9.137671630107387E-9, 1.0139126300804858E-11, -7.14360713354785E-15, 3.3056975594186825E-18, -1.0138439122706726E-21, 2.027996548489033E-25, -2.5164170042433482E-29, 1.7272825324156908E-33, -4.848198363439611E-38});
        coeff.put(12, new Double[] {1.0124688199067604, 2.9568708059058053E-4, -2.6970329661278072E-6, 9.367010516945659E-9, -1.7979935179395888E-11, 2.12387525004499E-14, -1.6358144625415908E-17, 8.471264192421164E-21, -2.9769843809687113E-24, 7.009612479531992E-28, -1.059505422114536E-31, 9.29657286308397E-36, -3.6013180388206746E-40});
        coeff.put(13, new Double[] {0.7330615330996527, 0.0034565297615557736, -1.7796768370433723E-5, 5.005853628218003E-8, -8.758158024267238E-11, 1.016397213791392E-13, -8.131958825589676E-17, 4.585955594591332E-20, -1.838422234151981E-23, 5.210960459999093E-27, -1.0210443438653801E-30, 1.3159737212404328E-34, -1.0039354400510355E-38, 3.435208458459968E-43});
        coeff.put(14, new Double[] {0.8206367456439709, 0.0024007214932570088, -1.2346179298794299E-5, 3.3967568477538393E-8, -5.7017801780149686E-11, 6.1873203586305E-14, -4.4561632770610234E-17, 2.1213498393503523E-20, -6.284486711741698E-24, 8.652815354400068E-28, 1.0692226874817162E-31, -7.422835328021963E-35, 1.4990852776336273E-38, -1.4768220542397628E-42, 5.986554440034027E-47});
        coeff.put(15, new Double[] {2.187481337958167, -0.015072693653053412, 8.44712660015965E-5, -2.764619751516587E-7, 5.90989292969015E-10, -8.760255912374565E-13, 9.32523609592864E-16, -7.281355917264249E-19, 4.2195477851751967E-22, -1.8214830383790853E-25, 5.830436224019551E-29, -1.3636354201757432E-32, 2.2627728323940936E-36, -2.5219881413405275E-40, 1.6924572933319152E-44, -5.1667597091678264E-49});
    }
}