import json
from chemaboxwriters.common.handler import Handler
import chemaboxwriters.common.utilsfunc as utilsfunc
import chemaboxwriters.common.globals as globals
import os
from typing import List, Optional, Dict
from enum import Enum


HANDLER_PREFIXES = {
    "omops_entry_prefix": {"required": True},
}

HANDLER_PARAMETERS = {
    "random_id": {"required": False},
}


class OMINP_JSON_TO_OM_JSON_Handler(Handler):
    """Handler converting ontomops ominp_json files to om_json.
    Inputs: List of ominp_json file paths
    Outputs: List of om_json file paths
    """

    def __init__(self) -> None:
        super().__init__(
            name="OMINP_JSON_TO_OM_JSON",
            in_stage=globals.aboxStages.OMINP_JSON,
            out_stage=globals.aboxStages.OM_JSON,
            prefixes=HANDLER_PREFIXES,
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

        xyz_inputs = self._extract_XYZ_data(inputs)
        if xyz_inputs:
            self.do_uploads(
                inputs=xyz_inputs,
                input_type=globals.aboxStages.OMINP_XYZ,
                dry_run=dry_run,
                triple_store_uploads=triple_store_uploads,
                file_server_uploads=file_server_uploads,
            )

        outputs: List[str] = []
        for json_file_path in inputs:
            out_file_path = utilsfunc.get_out_file_path(
                input_file_path=json_file_path,
                file_extension=self._out_stage.name.lower(),
                out_dir=out_dir,
            )
            self.om_jsonwriter(file_path=json_file_path, output_file_path=out_file_path)
            outputs.append(out_file_path)
        return outputs

    def om_jsonwriter(
        self,
        file_path: str,
        output_file_path: str,
        random_id: str = "",
    ) -> None:

        omops_entry_prefix = self.get_handler_prefix_value(name="omops_entry_prefix")
        random_id = self.get_handler_parameter_value(name="random_id")

        with open(file_path, "r") as file_handle:
            data = json.load(file_handle)

        if random_id is None:
            random_id = utilsfunc.get_random_id()

        data[globals.ENTRY_UUID] = random_id
        data[globals.ENTRY_IRI] = omops_entry_prefix + random_id

        utilsfunc.write_dict_to_file(dict_data=data, dest_path=output_file_path)

    @staticmethod
    def _extract_XYZ_data(inputs: List[str]):
        xyz_file_paths = []
        for file_path in inputs:
            with open(file_path, "r") as file_handle:
                data = json.load(file_handle)
                xyz_file = data.get("Mops_XYZ_coordinates_file")
                if xyz_file is not None:
                    xyz_file = os.path.abspath(xyz_file)
                    if xyz_file not in xyz_file_paths:
                        xyz_file_paths.append(xyz_file)

        return xyz_file_paths
