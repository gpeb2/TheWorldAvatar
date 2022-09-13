import json


def load_entity_dict(path, name_only=False):
    assert path is not None, "Error! entity_dict_path is empty."

    entity_list = []
    with open(path, 'rt') as f:
        for line in f:
            sample = json.loads(line.rstrip())
            title = sample['title']
            text = sample.get("text", "").strip()
            idx = sample.get("idx")
            if name_only:
                entity_list.append(title.lower())
            else:
                entity_list.append((title, text, idx))

    return entity_list
