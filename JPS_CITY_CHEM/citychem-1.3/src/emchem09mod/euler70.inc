! Generated by GenChem.py - DO NOT EDIT

!-> OP

  P = RC(2) * X(OD,2)  &
    + DJ(2) * X(O3,2)  &
    + DJ(3) * X(NO2,2)  &
    + DJ(14) * X(NO3,2)
  
  L = RC(1)  &
    + RC(3) * X(NO,2)
  
  X(OP,2) = P/L


!-> OD

  P = DJ(1) * X(O3,2)
  
  L = RC(2)  &
    + RC(4)
  
  X(OD,2) = P/L


!-> OH

  P = 2.* RC(4) * X(OD,2)  &
    + RC(8) * X(O3,2) * X(HO2,2)  &
    + RC(10) * X(NO,2) * X(HO2,2)  &
    + 3.5e-12 * X(NO3,2) * X(HO2,2)  &
    + RC(28) * X(CH3O2H,2) * X(OH,2)  &
    + 8.01e-12 * X(C2H5OOH,2) * X(OH,2)  &
    + 0.44* RC(42) * X(CH3COO2,2) * X(HO2,2)  &
    + 2.15e-11 * X(BURO2H,2) * X(OH,2)  &
    + 0.13* RC(51) * X(C2H4,2) * X(O3,2)  &
    + 1.38e-11 * X(ETRO2H,2) * X(OH,2)  &
    + 0.36* RC(53) * X(O3,2) * X(C3H6,2)  &
    + 2.44e-11 * X(PRRO2H,2) * X(OH,2)  &
    + 7.5e-11 * X(ISRO2H,2) * X(OH,2)  &
    + 0.67* 2.0e-16 * X(O3,2) * X(Limonene,2)  &
    + 0.46* RC(67) * X(O3,2) * X(Apinene,2)  &
    + 2* DJ(4) * X(H2O2,2)  &
    + DJ(5) * X(HNO3,2)  &
    + DJ(16) * X(CH3O2H,2)  &
    + DJ(17) * X(HONO,2)  &
    + DJ(18) * X(C2H5OOH,2)  &
    + DJ(19) * X(ETRO2H,2)  &
    + DJ(20) * X(BURO2H,2)  &
    + DJ(22) * X(MEKO2H,2)  &
    + DJ(23) * X(CH3COO2H,2)  &
    + DJ(24) * X(OXYO2H,2)  &
    + DJ(25) * X(MemalO2H,2)
  
  L = RC(7) * X(O3,2)  &
    + RC(12) * X(NO2,2)  &
    + RC(14) * X(H2,2)  &
    + RC(15) * X(HO2,2)  &
    + RC(16) * X(H2O2,2)  &
    + RC(19) * X(HNO3,2)  &
    + RC(20) * X(SO2,2)  &
    + RC(21) * X(HONO,2)  &
    + RC(22) * X(NO,2)  &
    + RC(24) * X(CH4,2)  &
    + RC(26) * X(CH3OH,2)  &
    + RC(28) * X(CH3O2H,2)  &
    + RC(29) * X(CH3O2H,2)  &
    + RC(30) * X(HCHO,2)  &
    + RC(32) * X(CO,2)  &
    + RC(35) * X(C2H6,2)  &
    + RC(37) * X(CH3CHO,2)  &
    + 8.01e-12 * X(C2H5OOH,2)  &
    + RC(29) * X(C2H5OOH,2)  &
    + RC(29) * X(CH3COO2H,2)  &
    + RC(45) * X(C2H5OH,2)  &
    + RC(46) * X(nC4H10,2)  &
    + RC(48) * X(CH3COC2H5,2)  &
    + RC(29) * X(MEKO2H,2)  &
    + RC(29) * X(BURO2H,2)  &
    + 2.15e-11 * X(BURO2H,2)  &
    + RC(50) * X(C2H4,2)  &
    + 1.38e-11 * X(ETRO2H,2)  &
    + RC(29) * X(ETRO2H,2)  &
    + RC(52) * X(C3H6,2)  &
    + 2.44e-11 * X(PRRO2H,2)  &
    + RC(55) * X(CH3COCH2OH,2)  &
    + RC(29) * X(PRRO2H,2)  &
    + 1.36e-11 * X(oXylene,2)  &
    + 5.58e-11 * X(Memaldial,2)  &
    + RC(56) * X(CH3COCHO,2)  &
    + RC(57) * X(HCOCHO,2)  &
    + 4.2e-11 * X(OXYO2H,2)  &
    + RC(29) * X(MemalO2H,2)  &
    + RC(60) * X(Isoprene,2)  &
    + RC(61) * X(MVKetone,2)  &
    + 7.5e-11 * X(ISRO2H,2)  &
    + 2.2e-11 * X(MVKO2H,2)  &
    + RC(63) * X(Apinene,2)  &
    + 1.7e-10 * X(Limonene,2)
  
  X(OH,2) = P/L


!-> HO2

  P = RC(7) * X(O3,2) * X(OH,2)  &
    + RC(14) * X(OH,2) * X(H2,2)  &
    + RC(16) * X(OH,2) * X(H2O2,2)  &
    + RC(20) * X(SO2,2) * X(OH,2)  &
    + 4.0e-17 * X(SO2,2) * X(CH3O2,2)  &
    + RC(25) * X(CH3O2,2) * X(NO,2)  &
    + 1.3e-12 * X(CH3O2,2) * X(NO3,2)  &
    + RC(26) * X(OH,2) * X(CH3OH,2)  &
    + RC(30) * X(OH,2) * X(HCHO,2)  &
    + RC(31) * X(NO3,2) * X(HCHO,2)  &
    + RC(32) * X(OH,2) * X(CO,2)  &
    + 2.* RC(33) * X(CH3O2,2) * X(CH3O2,2)  &
    + RC(36) * X(C2H5O2,2) * X(NO,2)  &
    + 2.3e-12 * X(C2H5O2,2) * X(NO3,2)  &
    + 0.9* RC(43) * X(CH3O2,2) * X(CH3COO2,2)  &
    + RC(45) * X(OH,2) * X(C2H5OH,2)  &
    + 0.65* RC(47) * X(NO,2) * X(secC4H9O2,2)  &
    + RC(47) * X(CH2O2CH2OH,2) * X(NO,2)  &
    + 0.13* RC(51) * X(C2H4,2) * X(O3,2)  &
    + RC(47) * X(NO,2) * X(CH3CHO2CH2OH,2)  &
    + 0.28* RC(53) * X(O3,2) * X(C3H6,2)  &
    + RC(55) * X(CH3COCH2OH,2) * X(OH,2)  &
    + RC(47) * X(oXylOHO2,2) * X(NO,2)  &
    + RC(47) * X(MemalO2,2) * X(NO,2)  &
    + RC(57) * X(OH,2) * X(HCOCHO,2)  &
    + RC(47) * X(IsopO2,2) * X(NO,2)  &
    + RC(62) * X(MVKO2,2) * X(NO,2)  &
    + RC(47) * X(MTO2,2) * X(NO,2)  &
    + 2* DJ(6) * X(HCHO,2)  &
    + DJ(8) * X(CH3CHO,2)  &
    + DJ(11) * X(CH3COCHO,2)  &
    + 0.5* DJ(12) * X(HCOCHO,2)  &
    + DJ(16) * X(CH3O2H,2)  &
    + DJ(18) * X(C2H5OOH,2)  &
    + DJ(19) * X(ETRO2H,2)  &
    + 0.65* DJ(20) * X(BURO2H,2)  &
    + DJ(21) * X(PRRO2H,2)  &
    + DJ(24) * X(OXYO2H,2)  &
    + DJ(25) * X(MemalO2H,2)
  
  L = RC(8) * X(O3,2)  &
    + RC(10) * X(NO,2)  &
    + 3.5e-12 * X(NO3,2)  &
    + RC(15) * X(OH,2)  &
    + RC(17) * X(HO2,2)  &
    + RC(17) * X(HO2,2)  &
    + RC(18) * X(HO2,2)  &
    + RC(18) * X(HO2,2)  &
    + RC(27) * X(CH3O2,2)  &
    + RC(41) * X(C2H5O2,2)  &
    + RC(42) * X(CH3COO2,2)  &
    + RC(49) * X(secC4H9O2,2)  &
    + RC(49) * X(CH3COCHO2CH3,2)  &
    + 1.2e-11 * X(CH2O2CH2OH,2)  &
    + RC(54) * X(CH3CHO2CH2OH,2)  &
    + RC(58) * X(oXylOHO2,2)  &
    + RC(59) * X(Memaldial,2)  &
    + RC(59) * X(IsopO2,2)  &
    + RC(49) * X(MVKO2,2)  &
    + RC(65) * X(MTO2,2)
  
  X(HO2,2) = (X(HO2,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3O2

  P = RC(24) * X(OH,2) * X(CH4,2)  &
    + RC(29) * X(CH3O2H,2) * X(OH,2)  &
    + 0.05* RC(37) * X(OH,2) * X(CH3CHO,2)  &
    + RC(40) * X(CH3COO2,2) * X(NO,2)  &
    + 0.44* RC(42) * X(CH3COO2,2) * X(HO2,2)  &
    + 0.9* RC(43) * X(CH3O2,2) * X(CH3COO2,2)  &
    + RC(44) * X(CH3COO2,2) * X(CH3COO2,2)  &
    + RC(44) * X(CH3COO2,2) * X(CH3COO2,2)  &
    + 0.28* RC(53) * X(O3,2) * X(C3H6,2)  &
    + DJ(8) * X(CH3CHO,2)  &
    + DJ(23) * X(CH3COO2H,2)
  
  L = 4.0e-17 * X(SO2,2)  &
    + RC(25) * X(NO,2)  &
    + 1.3e-12 * X(NO3,2)  &
    + RC(27) * X(HO2,2)  &
    + RC(33) * X(CH3O2,2)  &
    + RC(33) * X(CH3O2,2)  &
    + RC(34) * X(CH3O2,2)  &
    + RC(34) * X(CH3O2,2)  &
    + RC(43) * X(CH3COO2,2)  &
    + RC(66) * X(MTO2,2)
  
  X(CH3O2,2) = (X(CH3O2,1) + Ldt * P) / (1. + Ldt * L)


!-> C2H5O2

  P = RC(35) * X(OH,2) * X(C2H6,2)  &
    + RC(29) * X(C2H5OOH,2) * X(OH,2)  &
    + 0.35* RC(47) * X(NO,2) * X(secC4H9O2,2)  &
    + DJ(9) * X(CH3COC2H5,2)  &
    + 0.35* DJ(20) * X(BURO2H,2)
  
  L = RC(36) * X(NO,2)  &
    + 2.3e-12 * X(NO3,2)  &
    + RC(41) * X(HO2,2)  &
    + RC(66) * X(MTO2,2)
  
  X(C2H5O2,2) = (X(C2H5O2,1) + Ldt * P) / (1. + Ldt * L)


!-> secC4H9O2

  P = RC(46) * X(OH,2) * X(nC4H10,2)  &
    + RC(29) * X(BURO2H,2) * X(OH,2)
  
  L = RC(47) * X(NO,2)  &
    + RC(49) * X(HO2,2)
  
  X(secC4H9O2,2) = (X(secC4H9O2,1) + Ldt * P) / (1. + Ldt * L)


!-> IsopO2

  P = RC(60) * X(Isoprene,2) * X(OH,2)  &
    + 0.12* RC(47) * X(IsopO2,2) * X(NO,2)  &
    + 7.5e-11 * X(ISRO2H,2) * X(OH,2)
  
  L = RC(47) * X(NO,2)  &
    + RC(59) * X(HO2,2)
  
  X(IsopO2,2) = (X(IsopO2,1) + Ldt * P) / (1. + Ldt * L)


!-> CH2O2CH2OH

  P = RC(50) * X(C2H4,2) * X(OH,2)  &
    + RC(29) * X(ETRO2H,2) * X(OH,2)
  
  L = RC(47) * X(NO,2)  &
    + 1.2e-11 * X(HO2,2)
  
  X(CH2O2CH2OH,2) = (X(CH2O2CH2OH,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3CHO2CH2OH

  P = RC(52) * X(OH,2) * X(C3H6,2)  &
    + RC(29) * X(PRRO2H,2) * X(OH,2)
  
  L = RC(47) * X(NO,2)  &
    + RC(54) * X(HO2,2)
  
  X(CH3CHO2CH2OH,2) = (X(CH3CHO2CH2OH,1) + Ldt * P) / (1. + Ldt * L)


!-> oXylOHO2

  P = 1.36e-11 * X(oXylene,2) * X(OH,2)  &
    + 4.2e-11 * X(OXYO2H,2) * X(OH,2)  &
    + 1.0e-30 * X(OH,2) * X(ALOC,2)
  
  L = RC(47) * X(NO,2)  &
    + RC(58) * X(HO2,2)
  
  X(oXylOHO2,2) = (X(oXylOHO2,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3COCHO2CH3

  P = RC(48) * X(OH,2) * X(CH3COC2H5,2)  &
    + RC(29) * X(MEKO2H,2) * X(OH,2)
  
  L = RC(47) * X(NO,2)  &
    + RC(49) * X(HO2,2)
  
  X(CH3COCHO2CH3,2) = (X(CH3COCHO2CH3,1) + Ldt * P) / (1. + Ldt * L)


!-> MemalO2

  P = 5.58e-11 * X(Memaldial,2) * X(OH,2)  &
    + RC(29) * X(MemalO2H,2) * X(OH,2)
  
  L = RC(47) * X(NO,2)
  
  X(MemalO2,2) = (X(MemalO2,1) + Ldt * P) / (1. + Ldt * L)


!-> MVKO2

  P = RC(61) * X(MVKetone,2) * X(OH,2)  &
    + 2.2e-11 * X(MVKO2H,2) * X(OH,2)
  
  L = RC(62) * X(NO,2)  &
    + RC(49) * X(HO2,2)
  
  X(MVKO2,2) = (X(MVKO2,1) + Ldt * P) / (1. + Ldt * L)


!-> MTO2

  P = RC(63) * X(OH,2) * X(Apinene,2)  &
    + RC(64) * X(NO3,2) * X(Apinene,2)  &
    + 1.7e-10 * X(OH,2) * X(Limonene,2)  &
    + 1.3e-11 * X(NO3,2) * X(Limonene,2)  &
    + 2.0e-16 * X(O3,2) * X(Limonene,2)  &
    + 1.0e-30 * X(OH,2) * X(ProdApinOH,2)  &
    + 1.0e-30 * X(OH,2) * X(ProdApinNO3,2)  &
    + 1.0e-30 * X(OH,2) * X(ProdApinO3,2)  &
    + 1.0e-30 * X(OH,2) * X(ProdLimoOH,2)  &
    + 1.0e-30 * X(OH,2) * X(ProdLimoO3,2)  &
    + 1.0e-30 * X(OH,2) * X(ProdLimoNO3,2)  &
    + 1.0e-30 * X(OH,2) * X(MTKetone,2)  &
    + 1.0e-30 * X(OH,2) * X(MTO2H,2)  &
    + 0.80* RC(67) * X(O3,2) * X(Apinene,2)  &
    + 1.0e-30 * X(OH,2) * X(BLOC,2)
  
  L = RC(47) * X(NO,2)  &
    + RC(65) * X(HO2,2)  &
    + RC(66) * X(CH3O2,2)  &
    + RC(66) * X(C2H5O2,2)
  
  X(MTO2,2) = (X(MTO2,1) + Ldt * P) / (1. + Ldt * L)


!-> O3

  P = RC(1) * X(OP,2)  &
    + 0.15* RC(42) * X(CH3COO2,2) * X(HO2,2)
  
  L = RC(5) * X(NO,2)  &
    + RC(6) * X(NO2,2)  &
    + RC(7) * X(OH,2)  &
    + RC(8) * X(HO2,2)  &
    + RC(51) * X(C2H4,2)  &
    + RC(53) * X(C3H6,2)  &
    + 2.0e-16 * X(Limonene,2)  &
    + RC(67) * X(Apinene,2)  &
    + DJ(1)  &
    + DJ(2)
  
  X(O3,2) = (X(O3,1) + Ldt * P) / (1. + Ldt * L)


!-> NO

  P = DJ(3) * X(NO2,2)  &
    + DJ(13) * X(NO3,2)  &
    + DJ(17) * X(HONO,2)
  
  L = RC(3) * X(OP,2)  &
    + RC(5) * X(O3,2)  &
    + RC(9) * X(NO3,2)  &
    + RC(10) * X(HO2,2)  &
    + RC(22) * X(OH,2)  &
    + RC(25) * X(CH3O2,2)  &
    + RC(36) * X(C2H5O2,2)  &
    + RC(40) * X(CH3COO2,2)  &
    + RC(47) * X(secC4H9O2,2)  &
    + RC(47) * X(CH3COCHO2CH3,2)  &
    + RC(47) * X(CH2O2CH2OH,2)  &
    + RC(47) * X(CH3CHO2CH2OH,2)  &
    + RC(47) * X(oXylOHO2,2)  &
    + RC(47) * X(MemalO2,2)  &
    + RC(47) * X(IsopO2,2)  &
    + RC(62) * X(MVKO2,2)  &
    + RC(47) * X(MTO2,2)
  
  X(NO,2) = (X(NO,1) + Ldt * P) / (1. + Ldt * L)


!-> NO2

  P = RC(3) * X(OP,2) * X(NO,2)  &
    + RC(5) * X(O3,2) * X(NO,2)  &
    + RC(9) * X(NO,2) * X(NO3,2)  &
    + RC(9) * X(NO,2) * X(NO3,2)  &
    + RC(10) * X(NO,2) * X(HO2,2)  &
    + 3.5e-12 * X(NO3,2) * X(HO2,2)  &
    + RC(13) * X(N2O5,2)  &
    + RC(21) * X(OH,2) * X(HONO,2)  &
    + RC(25) * X(CH3O2,2) * X(NO,2)  &
    + 1.3e-12 * X(CH3O2,2) * X(NO3,2)  &
    + RC(36) * X(C2H5O2,2) * X(NO,2)  &
    + 2.3e-12 * X(C2H5O2,2) * X(NO3,2)  &
    + RC(39) * X(PAN,2)  &
    + RC(40) * X(CH3COO2,2) * X(NO,2)  &
    + RC(47) * X(NO,2) * X(secC4H9O2,2)  &
    + RC(47) * X(CH3COCHO2CH3,2) * X(NO,2)  &
    + RC(47) * X(CH2O2CH2OH,2) * X(NO,2)  &
    + RC(47) * X(NO,2) * X(CH3CHO2CH2OH,2)  &
    + RC(47) * X(oXylOHO2,2) * X(NO,2)  &
    + RC(47) * X(MemalO2,2) * X(NO,2)  &
    + RC(47) * X(IsopO2,2) * X(NO,2)  &
    + RC(62) * X(MVKO2,2) * X(NO,2)  &
    + RC(47) * X(MTO2,2) * X(NO,2)  &
    + DJ(5) * X(HNO3,2)  &
    + DJ(14) * X(NO3,2)  &
    + DJ(15) * X(N2O5,2)
  
  L = RC(6) * X(O3,2)  &
    + RC(11) * X(NO3,2)  &
    + RC(12) * X(OH,2)  &
    + RC(38) * X(CH3COO2,2)  &
    + DJ(3)
  
  X(NO2,2) = (X(NO2,1) + Ldt * P) / (1. + Ldt * L)


!-> PAN

  P = RC(38) * X(CH3COO2,2) * X(NO2,2)
  
  L = RC(39)
  
  X(PAN,2) = (X(PAN,1) + Ldt * P) / (1. + Ldt * L)


!-> NO3

  P = RC(6) * X(O3,2) * X(NO2,2)  &
    + RC(13) * X(N2O5,2)  &
    + RC(19) * X(OH,2) * X(HNO3,2)  &
    + DJ(15) * X(N2O5,2)
  
  L = RC(9) * X(NO,2)  &
    + 3.5e-12 * X(HO2,2)  &
    + RC(11) * X(NO2,2)  &
    + 1.3e-12 * X(CH3O2,2)  &
    + RC(31) * X(HCHO,2)  &
    + 2.3e-12 * X(C2H5O2,2)  &
    + RC(64) * X(Apinene,2)  &
    + 1.3e-11 * X(Limonene,2)  &
    + DJ(13)  &
    + DJ(14)
  
  X(NO3,2) = (X(NO3,1) + Ldt * P) / (1. + Ldt * L)


!-> N2O5

  P = RC(11) * X(NO2,2) * X(NO3,2)
  
  L = RC(13)  &
    + DJ(15)
  
  X(N2O5,2) = (X(N2O5,1) + Ldt * P) / (1. + Ldt * L)


!-> HNO3

  P = RC(12) * X(NO2,2) * X(OH,2)  &
    + RC(31) * X(NO3,2) * X(HCHO,2)
  
  L = RC(19) * X(OH,2)  &
    + DJ(5)
  
  X(HNO3,2) = (X(HNO3,1) + Ldt * P) / (1. + Ldt * L)


!-> HONO

  P = RC(22) * X(OH,2) * X(NO,2)
  
  L = RC(21) * X(OH,2)  &
    + DJ(17)
  
  X(HONO,2) = (X(HONO,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3COO2

  P = 0.95* RC(37) * X(OH,2) * X(CH3CHO,2)  &
    + RC(39) * X(PAN,2)  &
    + RC(29) * X(CH3COO2H,2) * X(OH,2)  &
    + RC(47) * X(CH3COCHO2CH3,2) * X(NO,2)  &
    + RC(56) * X(OH,2) * X(CH3COCHO,2)  &
    + DJ(9) * X(CH3COC2H5,2)  &
    + DJ(11) * X(CH3COCHO,2)  &
    + DJ(22) * X(MEKO2H,2)
  
  L = RC(38) * X(NO2,2)  &
    + RC(40) * X(NO,2)  &
    + RC(42) * X(HO2,2)  &
    + RC(43) * X(CH3O2,2)  &
    + RC(44) * X(CH3COO2,2)  &
    + RC(44) * X(CH3COO2,2)
  
  X(CH3COO2,2) = (X(CH3COO2,1) + Ldt * P) / (1. + Ldt * L)


!-> MVKetone

  P = 0.776* RC(47) * X(IsopO2,2) * X(NO,2)
  
  L = RC(61) * X(OH,2)
  
  X(MVKetone,2) = (X(MVKetone,1) + Ldt * P) / (1. + Ldt * L)


!-> HCOCHO

  P = RC(47) * X(MemalO2,2) * X(NO,2)  &
    + DJ(25) * X(MemalO2H,2)
  
  L = RC(57) * X(OH,2)  &
    + DJ(12)
  
  X(HCOCHO,2) = (X(HCOCHO,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3COCHO

  P = RC(55) * X(CH3COCH2OH,2) * X(OH,2)  &
    + 0.94* RC(47) * X(oXylOHO2,2) * X(NO,2)  &
    + RC(47) * X(MemalO2,2) * X(NO,2)  &
    + RC(62) * X(MVKO2,2) * X(NO,2)  &
    + DJ(24) * X(OXYO2H,2)  &
    + DJ(25) * X(MemalO2H,2)
  
  L = RC(56) * X(OH,2)  &
    + DJ(11)
  
  X(CH3COCHO,2) = (X(CH3COCHO,1) + Ldt * P) / (1. + Ldt * L)


!-> Memaldial

  P = 0.94* RC(47) * X(oXylOHO2,2) * X(NO,2)  &
    + DJ(24) * X(OXYO2H,2)
  
  L = 5.58e-11 * X(OH,2)  &
    + RC(59) * X(HO2,2)
  
  X(Memaldial,2) = (X(Memaldial,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3COC2H5

  P = 0.65* RC(47) * X(NO,2) * X(secC4H9O2,2)  &
    + 2.15e-11 * X(BURO2H,2) * X(OH,2)  &
    + 0.65* DJ(20) * X(BURO2H,2)
  
  L = RC(48) * X(OH,2)  &
    + DJ(9)
  
  X(CH3COC2H5,2) = (X(CH3COC2H5,1) + Ldt * P) / (1. + Ldt * L)


!-> MTKetone

  P = 0.768* RC(47) * X(MTO2,2) * X(NO,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(MTKetone,2) = (X(MTKetone,1) + Ldt * P) / (1. + Ldt * L)


!-> ProdApinO3

  P = 0.80* RC(67) * X(O3,2) * X(Apinene,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(ProdApinO3,2) = (X(ProdApinO3,1) + Ldt * P) / (1. + Ldt * L)


!-> ProdApinNO3

  P = RC(64) * X(NO3,2) * X(Apinene,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(ProdApinNO3,2) = (X(ProdApinNO3,1) + Ldt * P) / (1. + Ldt * L)


!-> ProdApinOH

  P = RC(63) * X(OH,2) * X(Apinene,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(ProdApinOH,2) = (X(ProdApinOH,1) + Ldt * P) / (1. + Ldt * L)


!-> ProdLimoO3

  P = 2.0e-16 * X(O3,2) * X(Limonene,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(ProdLimoO3,2) = (X(ProdLimoO3,1) + Ldt * P) / (1. + Ldt * L)


!-> ProdLimoNO3

  P = 1.3e-11 * X(NO3,2) * X(Limonene,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(ProdLimoNO3,2) = (X(ProdLimoNO3,1) + Ldt * P) / (1. + Ldt * L)


!-> ProdLimoOH

  P = 1.7e-10 * X(OH,2) * X(Limonene,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(ProdLimoOH,2) = (X(ProdLimoOH,1) + Ldt * P) / (1. + Ldt * L)


!-> HCHO

  P = 4.0e-17 * X(SO2,2) * X(CH3O2,2)  &
    + RC(25) * X(CH3O2,2) * X(NO,2)  &
    + 1.3e-12 * X(CH3O2,2) * X(NO3,2)  &
    + RC(26) * X(OH,2) * X(CH3OH,2)  &
    + 0.1* RC(27) * X(HO2,2) * X(CH3O2,2)  &
    + RC(28) * X(CH3O2H,2) * X(OH,2)  &
    + 2.* RC(33) * X(CH3O2,2) * X(CH3O2,2)  &
    + RC(34) * X(CH3O2,2) * X(CH3O2,2)  &
    + RC(43) * X(CH3O2,2) * X(CH3COO2,2)  &
    + 2.* RC(47) * X(CH2O2CH2OH,2) * X(NO,2)  &
    + 1.14* RC(51) * X(C2H4,2) * X(O3,2)  &
    + RC(47) * X(NO,2) * X(CH3CHO2CH2OH,2)  &
    + 0.545* RC(53) * X(O3,2) * X(C3H6,2)  &
    + 0.776* RC(47) * X(IsopO2,2) * X(NO,2)  &
    + RC(62) * X(MVKO2,2) * X(NO,2)  &
    + 0.19* 2.0e-16 * X(O3,2) * X(Limonene,2)  &
    + 0.1* DJ(12) * X(HCOCHO,2)  &
    + DJ(16) * X(CH3O2H,2)  &
    + 1.56* DJ(19) * X(ETRO2H,2)  &
    + DJ(21) * X(PRRO2H,2)
  
  L = RC(30) * X(OH,2)  &
    + RC(31) * X(NO3,2)  &
    + DJ(6)  &
    + DJ(7)
  
  X(HCHO,2) = (X(HCHO,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3CHO

  P = RC(36) * X(C2H5O2,2) * X(NO,2)  &
    + 2.3e-12 * X(C2H5O2,2) * X(NO3,2)  &
    + 8.01e-12 * X(C2H5OOH,2) * X(OH,2)  &
    + RC(45) * X(OH,2) * X(C2H5OH,2)  &
    + 0.35* RC(47) * X(NO,2) * X(secC4H9O2,2)  &
    + RC(47) * X(CH3COCHO2CH3,2) * X(NO,2)  &
    + 1.38e-11 * X(ETRO2H,2) * X(OH,2)  &
    + RC(47) * X(NO,2) * X(CH3CHO2CH2OH,2)  &
    + 0.545* RC(53) * X(O3,2) * X(C3H6,2)  &
    + DJ(18) * X(C2H5OOH,2)  &
    + 0.22* DJ(19) * X(ETRO2H,2)  &
    + 0.35* DJ(20) * X(BURO2H,2)  &
    + DJ(21) * X(PRRO2H,2)  &
    + DJ(22) * X(MEKO2H,2)
  
  L = RC(37) * X(OH,2)  &
    + DJ(8)
  
  X(CH3CHO,2) = (X(CH3CHO,1) + Ldt * P) / (1. + Ldt * L)


!-> C2H6

  ! P = 0.0
  
  L = RC(35) * X(OH,2)
  
  X(C2H6,2) = X(C2H6,1) / (1. + Ldt * L)


!-> nC4H10

  ! P = 0.0
  
  L = RC(46) * X(OH,2)
  
  X(nC4H10,2) = X(nC4H10,1) / (1. + Ldt * L)


!-> C2H4

  ! P = 0.0
  
  L = RC(50) * X(OH,2)  &
    + RC(51) * X(O3,2)
  
  X(C2H4,2) = X(C2H4,1) / (1. + Ldt * L)


!-> C3H6

  ! P = 0.0
  
  L = RC(52) * X(OH,2)  &
    + RC(53) * X(O3,2)
  
  X(C3H6,2) = X(C3H6,1) / (1. + Ldt * L)


!-> oXylene

  ! P = 0.0
  
  L = 1.36e-11 * X(OH,2)
  
  X(oXylene,2) = X(oXylene,1) / (1. + Ldt * L)


!-> Isoprene

  ! P = 0.0
  
  L = RC(60) * X(OH,2)
  
  X(Isoprene,2) = X(Isoprene,1) / (1. + Ldt * L)


!-> Apinene

  ! P = 0.0
  
  L = RC(63) * X(OH,2)  &
    + RC(64) * X(NO3,2)  &
    + RC(67) * X(O3,2)
  
  X(Apinene,2) = X(Apinene,1) / (1. + Ldt * L)


!-> Limonene

  ! P = 0.0
  
  L = 1.7e-10 * X(OH,2)  &
    + 1.3e-11 * X(NO3,2)  &
    + 2.0e-16 * X(O3,2)
  
  X(Limonene,2) = X(Limonene,1) / (1. + Ldt * L)


!-> CH3O2H

  P = 0.9* RC(27) * X(HO2,2) * X(CH3O2,2)
  
  L = RC(28) * X(OH,2)  &
    + RC(29) * X(OH,2)  &
    + DJ(16)
  
  X(CH3O2H,2) = (X(CH3O2H,1) + Ldt * P) / (1. + Ldt * L)


!-> C2H5OOH

  P = RC(41) * X(C2H5O2,2) * X(HO2,2)
  
  L = 8.01e-12 * X(OH,2)  &
    + RC(29) * X(OH,2)  &
    + DJ(18)
  
  X(C2H5OOH,2) = (X(C2H5OOH,1) + Ldt * P) / (1. + Ldt * L)


!-> BURO2H

  P = 0.95* RC(49) * X(secC4H9O2,2) * X(HO2,2)
  
  L = RC(29) * X(OH,2)  &
    + 2.15e-11 * X(OH,2)  &
    + DJ(20)
  
  X(BURO2H,2) = (X(BURO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> ETRO2H

  P = 1.2e-11 * X(CH2O2CH2OH,2) * X(HO2,2)
  
  L = 1.38e-11 * X(OH,2)  &
    + RC(29) * X(OH,2)  &
    + DJ(19)
  
  X(ETRO2H,2) = (X(ETRO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> PRRO2H

  P = 0.795* RC(54) * X(CH3CHO2CH2OH,2) * X(HO2,2)
  
  L = 2.44e-11 * X(OH,2)  &
    + RC(29) * X(OH,2)  &
    + DJ(21)
  
  X(PRRO2H,2) = (X(PRRO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> OXYO2H

  P = 0.227* RC(58) * X(oXylOHO2,2) * X(HO2,2)
  
  L = 4.2e-11 * X(OH,2)  &
    + DJ(24)
  
  X(OXYO2H,2) = (X(OXYO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> MEKO2H

  P = RC(49) * X(CH3COCHO2CH3,2) * X(HO2,2)
  
  L = RC(29) * X(OH,2)  &
    + DJ(22)
  
  X(MEKO2H,2) = (X(MEKO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> MemalO2H

  P = RC(59) * X(Memaldial,2) * X(HO2,2)
  
  L = RC(29) * X(OH,2)  &
    + DJ(25)
  
  X(MemalO2H,2) = (X(MemalO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> MVKO2H

  P = RC(49) * X(MVKO2,2) * X(HO2,2)
  
  L = 2.2e-11 * X(OH,2)
  
  X(MVKO2H,2) = (X(MVKO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> ISRO2H

  P = 0.857* RC(59) * X(IsopO2,2) * X(HO2,2)
  
  L = 7.5e-11 * X(OH,2)
  
  X(ISRO2H,2) = (X(ISRO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> MTO2H

  P = 0.493* RC(65) * X(MTO2,2) * X(HO2,2)  &
    + RC(66) * X(MTO2,2) * X(CH3O2,2)  &
    + RC(66) * X(MTO2,2) * X(C2H5O2,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(MTO2H,2) = (X(MTO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> H2O2

  P = RC(17) * X(HO2,2) * X(HO2,2)  &
    + RC(18) * X(HO2,2) * X(HO2,2)  &
    + 0.14* RC(51) * X(C2H4,2) * X(O3,2)  &
    + 0.09* RC(53) * X(O3,2) * X(C3H6,2)
  
  L = RC(16) * X(OH,2)  &
    + DJ(4)
  
  X(H2O2,2) = (X(H2O2,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3COO2H

  P = 0.41* RC(42) * X(CH3COO2,2) * X(HO2,2)
  
  L = RC(29) * X(OH,2)  &
    + DJ(23)
  
  X(CH3COO2H,2) = (X(CH3COO2H,1) + Ldt * P) / (1. + Ldt * L)


!-> CH3OH

  P = RC(34) * X(CH3O2,2) * X(CH3O2,2)
  
  L = RC(26) * X(OH,2)
  
  X(CH3OH,2) = (X(CH3OH,1) + Ldt * P) / (1. + Ldt * L)


!-> C2H5OH

  ! P = 0.0
  
  L = RC(45) * X(OH,2)
  
  X(C2H5OH,2) = X(C2H5OH,1) / (1. + Ldt * L)


!-> CH3COCH2OH

  P = 2.44e-11 * X(PRRO2H,2) * X(OH,2)
  
  L = RC(55) * X(OH,2)
  
  X(CH3COCH2OH,2) = (X(CH3COCH2OH,1) + Ldt * P) / (1. + Ldt * L)


!-> H2

  P = DJ(7) * X(HCHO,2)
  
  L = RC(14) * X(OH,2)
  
  X(H2,2) = (X(H2,1) + Ldt * P) / (1. + Ldt * L)


!-> CO

  P = RC(30) * X(OH,2) * X(HCHO,2)  &
    + RC(31) * X(NO3,2) * X(HCHO,2)  &
    + 0.05* RC(37) * X(OH,2) * X(CH3CHO,2)  &
    + 0.63* RC(51) * X(C2H4,2) * X(O3,2)  &
    + 0.56* RC(53) * X(O3,2) * X(C3H6,2)  &
    + RC(56) * X(OH,2) * X(CH3COCHO,2)  &
    + RC(57) * X(OH,2) * X(HCOCHO,2)  &
    + RC(57) * X(OH,2) * X(HCOCHO,2)  &
    + DJ(6) * X(HCHO,2)  &
    + DJ(7) * X(HCHO,2)  &
    + DJ(8) * X(CH3CHO,2)  &
    + DJ(11) * X(CH3COCHO,2)  &
    + 1.9* DJ(12) * X(HCOCHO,2)
  
  L = RC(32) * X(OH,2)
  
  X(CO,2) = (X(CO,1) + Ldt * P) / (1. + Ldt * L)


!-> CH4

  P = 0.1* RC(53) * X(O3,2) * X(C3H6,2)
  
  L = RC(24) * X(OH,2)
  
  X(CH4,2) = (X(CH4,1) + Ldt * P) / (1. + Ldt * L)


!-> SO2

  ! P = 0.0
  
  L = RC(20) * X(OH,2)  &
    + 4.0e-17 * X(CH3O2,2)
  
  X(SO2,2) = X(SO2,1) / (1. + Ldt * L)


!-> Sulphate

  P = RC(20) * X(SO2,2) * X(OH,2)  &
    + 4.0e-17 * X(SO2,2) * X(CH3O2,2)
  
  L = RC(23)
  
  X(Sulphate,2) = (X(Sulphate,1) + Ldt * P) / (1. + Ldt * L)


!-> BLOC

  P = 0.003* RC(47) * X(IsopO2,2) * X(NO,2)  &
    + 0.024* RC(59) * X(IsopO2,2) * X(HO2,2)  &
    + 0.20* RC(67) * X(O3,2) * X(Apinene,2)  &
    + 0.052* RC(47) * X(MTO2,2) * X(NO,2)  &
    + 0.327* RC(66) * X(MTO2,2) * X(HO2,2)  &
    + 4.0e-11 * X(OH,2) * X(BSOC,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(BLOC,2) = (X(BLOC,1) + Ldt * P) / (1. + Ldt * L)


!-> BSOC

  P = 0.101* RC(47) * X(IsopO2,2) * X(NO,2)  &
    + 0.119* RC(59) * X(IsopO2,2) * X(HO2,2)  &
    + 0.184* RC(47) * X(MTO2,2) * X(NO,2)  &
    + 0.180* RC(66) * X(MTO2,2) * X(HO2,2)
  
  L = 4.0e-11 * X(OH,2)
  
  X(BSOC,2) = (X(BSOC,1) + Ldt * P) / (1. + Ldt * L)


!-> ALOC

  P = 0.063* RC(47) * X(oXylOHO2,2) * X(NO,2)  &
    + 0.710* RC(66) * X(oXylOHO2,2) * X(HO2,2)  &
    + 0.205* RC(54) * X(CH3CHO2CH2OH,2) * X(HO2,2)  &
    + 0.050* RC(49) * X(secC4H9O2,2) * X(HO2,2)
  
  L = 1.0e-30 * X(OH,2)
  
  X(ALOC,2) = (X(ALOC,1) + Ldt * P) / (1. + Ldt * L)

