import json

import transformers
from tqdm.auto import tqdm

from core.arguments_schema import DatasetArguments, InferenceArguments, ModelArguments
from core.translation import TranslationModel


def rename_dict_keys(d: dict, mappings: dict):
    return {mappings[k] if k in mappings else k: v for k, v in d.items()}


def infer():
    hfparser = transformers.HfArgumentParser(
        (ModelArguments, DatasetArguments, InferenceArguments)
    )
    model_args, data_args, infer_args = hfparser.parse_args_into_dataclasses()

    trans_model = TranslationModel(model_args, max_new_tokens=infer_args.max_new_tokens)

    with open(data_args.eval_data_path, "r") as f:
        data = json.load(f)

    preds = []
    for datum in tqdm(data):
        pred = trans_model.nl2sparql(datum["question"])
        preds.append(pred)

    data_out = [
        {
            **rename_dict_keys(
                datum, {"sparql_query": "gt", "sparql_query_compact": "gt_compact"}
            ),
            **pred,
        }
        for datum, pred in zip(data, preds)
    ]
    with open(infer_args.out_file, "w") as f:
        json.dump(data_out, f, indent=4)


if __name__ == "__main__":
    infer()
