from chemutils.mathutils.linalg import (
    getXYZPointsDistance,
    getPlaneAngle,
    getDihedralAngle,
)
import chemaboxwriters.common.utilsfunc as utilsfunc
import json
import numpy as np
import chemaboxwriters.common.globals as globals
from compchemparser.parsers.ccgaussian_parser import GEOM
from chemaboxwriters.common.handler import Handler
from typing import List, Optional, Dict
from enum import Enum

SCAN_COORDINATE_ATOMS_IRIS = "ScanCoordinateAtomsIRIs"
SCAN_COORDINATE_TYPE = "ScanCoordinateType"
SCAN_COORDINATE_UNIT = "ScanCoordinateUnit"
SCAN_COORDINATE_VALUE = "ScanCoordinateValue"
SCAN_POINTS_JOBS = "ScanPointsJobs"
SCAN_ATOM_IDS = "ScanAtomIDs"


HANDLER_PARAMETERS = {
    "os_iris": {"required": True},
    "os_atoms_iris": {"required": True},
    "oc_atoms_pos": {"required": True},
    "random_id": {"required": False},
}


class OC_JSON_TO_OPS_JSON_Handler(Handler):
    """Handler converting oc_json files to ops_json.
    Inputs: List of oc_json file paths
    Outputs: List of ops_json file paths
    """

    def __init__(self) -> None:
        super().__init__(
            name="OC_JSON_TO_OPS_JSON",
            in_stage=globals.aboxStages.OC_JSON,
            out_stage=globals.aboxStages.OPS_JSON,
            handler_params=HANDLER_PARAMETERS,
        )

    def _handle_input(
        self,
        inputs: List[str],
        out_dir: str,
        input_type: Enum,
        dry_run: bool,
        triple_store_uploads: Optional[Dict] = None,
        file_server_uploads: Optional[Dict] = None,
    ) -> List[str]:

        outputs: List[str] = []
        out_file_path = utilsfunc.get_out_file_path(
            input_file_path=inputs[0],
            file_extension=self._out_stage.name.lower(),
            out_dir=out_dir,
        )
        self._ops_jsonwriter(file_paths=inputs, output_file_path=out_file_path)
        outputs.append(out_file_path)
        return outputs

    def _ops_jsonwriter(self, file_paths: List[str], output_file_path: str):

        random_id = self.get_parameter_value(name="random_id")
        os_iris = self.get_parameter_value(name="os_iris")
        os_atoms_iris = self.get_parameter_value(name="os_atoms_iris")
        oc_atoms_pos = self.get_parameter_value(name="oc_atoms_pos")

        if os_iris is None:
            os_iris = ''

        if os_atoms_iris is None:
            os_atoms_iris = ''

        if oc_atoms_pos is None:
            oc_atoms_pos = ''

        data_out = {}
        data_out[globals.SPECIES_IRI] = os_iris.split(",")
        data_out[SCAN_COORDINATE_ATOMS_IRIS] = [
            iri.strip() for iri in os_atoms_iris.split(",")
        ]
        data_out[SCAN_ATOM_IDS] = " ".join(oc_atoms_pos.split(",")[:])
        oc_atoms_pos_ids = [int(at_pos) - 1 for at_pos in oc_atoms_pos.split(",")]

        ndegrees = len(os_atoms_iris.split(","))
        if ndegrees == 2:
            data_out[SCAN_COORDINATE_TYPE] = "DistanceCoordinate"
            data_out[SCAN_COORDINATE_UNIT] = "Angstrom"
        else:
            data_out[SCAN_COORDINATE_UNIT] = "Degree"
            if ndegrees == 3:
                data_out[SCAN_COORDINATE_TYPE] = "AngleCoordinate"
            else:
                data_out[SCAN_COORDINATE_TYPE] = "DihedralAngleCoordinate"

        if not random_id:
            random_id = utilsfunc.get_random_id()
        data_out[globals.ENTRY_UUID] = random_id
        data_out[globals.ENTRY_IRI] = f"PotentialEnergySurfaceScan_{random_id}"

        scanCoordinateValue = []
        ontoCompChemJobs = []

        for file_path in file_paths:

            with open(file_path, "r") as file_handle:
                data_item = json.load(file_handle)

            ontoCompChemJobs.append(data_item[globals.ENTRY_IRI])
            xyz = np.array(data_item[GEOM])

            if ndegrees == 2:
                scanAtomsPos = xyz[oc_atoms_pos_ids]
                scanCoordinateValue.append(
                    getXYZPointsDistance(scanAtomsPos[0], scanAtomsPos[1])
                )
            elif ndegrees == 3:
                scanAtomsPos = xyz[oc_atoms_pos_ids]
                scanCoordinateValue.append(
                    getPlaneAngle(scanAtomsPos[0], scanAtomsPos[1], scanAtomsPos[2])
                )

            elif ndegrees == 4:
                scanAtomsPos = xyz[oc_atoms_pos_ids]
                scanCoordinateValue.append(
                    getDihedralAngle(
                        scanAtomsPos[0],
                        scanAtomsPos[1],
                        scanAtomsPos[2],
                        scanAtomsPos[3],
                    )
                )

        scanCoordinateValue, ontoCompChemJobs = zip(
            *sorted(zip(scanCoordinateValue, ontoCompChemJobs))
        )
        scanCoordinateValue = list(scanCoordinateValue)
        ontoCompChemJobs = list(ontoCompChemJobs)

        data_out[SCAN_COORDINATE_VALUE] = scanCoordinateValue
        data_out[SCAN_POINTS_JOBS] = ontoCompChemJobs

        utilsfunc.write_dict_to_file(dict_data=data_out, dest_path=output_file_path)
