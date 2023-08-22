import os
import torch

from transformers import BitsAndBytesConfig, AutoModelForSeq2SeqLM, AutoModelForCausalLM, AutoTokenizer
from peft import PeftModel, LoraConfig, TaskType, get_peft_model

from marie.arguments_schema import ModelArguments


TARGET_MODULES_BY_MODEL = dict(
    t5=["q", "v"],
    llama=["q_proj", "v_proj"]
)


def get_model_family_from_model_args(model_args: ModelArguments):
    if model_args.model_family is not None:
        return model_args.model_family
    return get_model_family_from_model_name(model_args.model_path)


def get_model_family_from_model_name(model_name: str):
    model_name = model_name.lower()
    for model_family in ["t5", "llama"]:
        if model_family in model_name:
            return model_family
    raise ValueError("Unable to infer model family from model name: " + model_name)


def get_model(model_args: ModelArguments, is_trainable: bool):
    # if we are in a distributed setting, we need to set the device map per device
    if os.environ.get("LOCAL_RANK") is not None:
        local_rank = int(os.environ.get("LOCAL_RANK", "0"))
        device_map = {"": local_rank}
    else:
        device_map = "auto"

    if model_args.bits is not None:
        bnb_config = BitsAndBytesConfig(
            load_in_8bit=model_args.bits == 8,
            load_in_4bit=model_args.bits == 4,
            bnb_4bit_use_double_quant=True,
            bnb_4bit_quant_type="nf4",
            bnb_4bit_compute_dtype=torch.bfloat16,
        )
    else:
        bnb_config = None

    model_load_kwargs = {
        k: v
        for k, v in dict(
            quantization_config=bnb_config,
            device_map=device_map,
            use_auth_token=os.environ.get("HF_ACCESS_TOKEN"),
        ).items()
        if v is not None
    }

    model_family = get_model_family_from_model_args(model_args)
    auto_model = AutoModelForSeq2SeqLM if model_family == "t5" else AutoModelForCausalLM
    
    model = auto_model.from_pretrained(
        model_args.model_path, **model_load_kwargs
    )

    if model_args.lora_path is not None:
        model = PeftModel.from_pretrained(
            model, model_args.lora_path, is_trainable=is_trainable
        )
    elif all(
        x is not None
        for x in (model_args.lora_r, model_args.lora_alpha, model_args.lora_dropout)
    ):
        lora_config = LoraConfig(
            r=model_args.lora_r,
            lora_alpha=model_args.lora_alpha,
            lora_dropout=model_args.lora_dropout,
            bias="none",
            target_modules=TARGET_MODULES_BY_MODEL[model_family],
            task_type=TaskType.SEQ_2_SEQ_LM if model_family == "t5" else TaskType.CAUSAL_LM,
        )

        model = get_peft_model(model, lora_config)
        model.print_trainable_parameters()

    return model


def get_tokenizer(model_args: ModelArguments):
    tokenizer = AutoTokenizer.from_pretrained(
        model_args.model_path, use_auth_token=os.environ.get("HF_ACCESS_TOKEN")
    )

    model_family = get_model_family_from_model_args(model_args)
    if model_family == "llama":
        tokenizer.pad_token_id = tokenizer.unk_token_id
        tokenizer.padding_side = "right"

    return tokenizer


def get_model_and_tokenizer(model_args: ModelArguments, is_trainable: bool):
    tokenizer = get_tokenizer(model_args)
    model = get_model(model_args, is_trainable)

    return model, tokenizer
