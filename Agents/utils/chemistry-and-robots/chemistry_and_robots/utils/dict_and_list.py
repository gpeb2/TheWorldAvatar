#######################################################
## Some utility functions handling the list and dict ##
#######################################################
from typing import Any, Dict, List

import logging
logger = logging.getLogger('chemistry_and_robots_dict_and_list_util')
logging.getLogger('py4j').setLevel(logging.INFO)

def get_sublist_in_list_of_dict_matching_key_value(list_of_dict: List[Dict], key: str, value: Any) -> list:
    if len(list_of_dict) > 0:
        try:
            sublist = [d for d in list_of_dict if d[key] == value]
        except KeyError:
            logger.error("Key '%s' is not found in the given list of dict: %s" % (key, str(list_of_dict)))
        else:
            return sublist
    else:
        logger.error("An empty list is passed in while requesting return sublist given key '%s'." % (key))
        return []

def get_unique_values_in_list_of_dict(list_of_dict: List[dict], key: str) -> list:
    return list(set(get_value_from_list_of_dict(list_of_dict, key)))

def get_value_from_list_of_dict(list_of_dict: List[dict], key: str) -> list:
    if len(list_of_dict) > 0:
        try:
            list_of_values = [d[key] for d in list_of_dict]
        except KeyError:
            logger.error("Key '%s' is not found in the given list of dict: %s" % (key, str(list_of_dict)))
            return []
        else:
            return list_of_values
    else:
        logger.error("An empty list is passed in while requesting return value of key '%s'." % (key))
        return []

def keep_wanted_keys_from_list_of_dict(list_of_dict: List[dict], wanted_keys: List[str]) -> list:
    return_list = []
    for one_dict in list_of_dict:
        return_list.append({key:one_dict[key] for key in wanted_keys})
    return return_list

def remove_duplicate_dict_from_list_of_dict(list_of_dict: List[dict]) -> list:
    return [dict(t) for t in {tuple(sorted(d.items())) for d in list_of_dict}]

def check_if_key_in_list_of_dict(list_of_dict: List[dict], key: str):
    for d in list_of_dict:
        if key in d:
            return True
    return False
