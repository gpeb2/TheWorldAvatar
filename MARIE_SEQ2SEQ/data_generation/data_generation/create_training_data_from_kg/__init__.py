from collections import defaultdict
import itertools
import random
from typing import Dict, Iterable, List, Optional

from data_generation.constants import (
    CHEMICALCLASSES,
    IDENTIFIER_NAMES,
    PROPERTY_NAMES,
    USES,
)
from .retrieve_subgraph import SubgraphRetriever
from .make_example_head2tail import ExampleMakerHead2Tail
from .make_example_tail2head import ExampleMakerTail2Head
from .make_example_tail2tail import ExampleMakerTail2Tail


class DatasetFromKgMaker:
    # SubstructureKeysFingerprint is not a numerical value for comparison.
    # We thus exclude it from tail-to-head queries.
    PROPERTY_NAMES_FOR_T2H = [
        x for x in PROPERTY_NAMES if x != "SubStructureKeysFingerprint"
    ]

    def __init__(self):
        self.subgraph_retriever = SubgraphRetriever()
        self.example_maker_head2tail = ExampleMakerHead2Tail()
        self.example_maker_tail2head = ExampleMakerTail2Head()
        self.example_maker_tail2tail = ExampleMakerTail2Tail()
        self.missing_entries = defaultdict(set)

    def make_examples(self, repeats: int = 1):
        self.missing_entries = defaultdict(set)
        examples = []

        for _ in range(repeats):
            print("Generating examples with one head and one tail...")
            for query_path in ["h2t", "t2h"]:
                examples.extend(
                    self.make_examples_1h_1t(
                        query_path=query_path,
                        tail_class="property",
                        sampling_size=len(
                            PROPERTY_NAMES
                            if query_path != "t2h"
                            else self.PROPERTY_NAMES_FOR_T2H
                        ),
                    )
                )
                examples.extend(
                    self.make_examples_1h_1t(
                        query_path=query_path,
                        tail_class="identifier",
                        sampling_size=len(IDENTIFIER_NAMES),
                    )
                )
                sampling_size = 3
                examples.extend(
                    self.make_examples_1h_1t(
                        query_path=query_path,
                        tail_class="use",
                        sampling_size=sampling_size,
                    )
                )
                examples.extend(
                    self.make_examples_1h_1t(
                        query_path=query_path,
                        tail_class="chemicalclass",
                        sampling_size=sampling_size,
                    )
                )

            print("Generating examples with one head and two tails...")
            for query_path in ["h2t", "t2h"]:
                examples.extend(
                    self.make_examples_1h_2t(
                        query_path=query_path,
                        tail_class="property",
                        sampling_size=len(
                            PROPERTY_NAMES
                            if query_path != "t2h"
                            else self.PROPERTY_NAMES_FOR_T2H
                        ),
                    )
                )
            examples.extend(
                self.make_examples_1h_2t(
                    query_path="h2t",
                    tail_class="identifier",
                    sampling_size=len(IDENTIFIER_NAMES),
                )
            )
            for query_path in ["h2t", "t2h", "t2t"]:
                examples.extend(
                    self.make_examples_1h_2t(query_path=query_path, sampling_size=5)
                )

            print("Generating examples with one head and three tails...")
            for query_path in ["h2t", "t2h", "t2t"]:
                examples.extend(
                    self.make_examples_1h_3t(query_path=query_path, sampling_size=5)
                )

        if len(self.missing_entries) > 0:
            print("Missing entries: ")
            for k, v in self.missing_entries.items():
                print(k, ": ", v)

        return examples

    def make_examples_1h_1t(
        self, query_path: str, tail_class: str, sampling_size: int = 1
    ):
        examples: List[dict] = []

        if tail_class == "property":
            sampling_frame = (
                self.PROPERTY_NAMES_FOR_T2H if query_path == "t2h" else PROPERTY_NAMES
            )
            for p in random.sample(sampling_frame, k=sampling_size):
                example = self.make_example(
                    property_names=[p],
                    tail_nums=dict(property_num=1),
                    query_path=query_path,
                )

                if example is None:
                    self.missing_entries["property"].add(p)
                else:
                    examples.append(example)

        elif tail_class == "identifier":
            for i in random.sample(IDENTIFIER_NAMES, k=sampling_size):
                example = self.make_example(
                    identifier_names=[i],
                    tail_nums=dict(identifier_num=1),
                    query_path="h2t",
                )

                if example is None:
                    self.missing_entries["identifier"].add(i)
                else:
                    examples.append(example)

        elif tail_class == "use":
            for u in random.sample(USES, k=sampling_size or 1):
                example = self.make_example(
                    uses=[u], tail_nums=dict(use_num=1), query_path=query_path
                )

                if example is None:
                    self.missing_entries["use"].append(u)
                else:
                    examples.append(example)

        elif tail_class == "chemicalclass":
            for c in random.sample(CHEMICALCLASSES, k=sampling_size):
                example = self.make_example(
                    chemicalclasses=[c],
                    tail_nums=dict(chemicalclass_num=1),
                    query_path=query_path,
                )

                if example is None:
                    self.missing_entries["chemicalclass"].add(c)
                else:
                    examples.append(example)

        else:
            raise ValueError(
                f"Unexpected value for argument `tail_class`: {tail_class}."
            )

        for example in examples:
            example["tail_num"] = 1

        return examples

    def make_examples_1h_2t(
        self, query_path: str, tail_class: Optional[str] = None, sampling_size: int = 1
    ):
        examples: List[dict] = []

        if tail_class == "property":
            sampling_frame = (
                self.PROPERTY_NAMES_FOR_T2H if query_path == "t2h" else PROPERTY_NAMES
            )
            for p_pair in random.sample(
                list(itertools.combinations(sampling_frame, r=2)), k=sampling_size
            ):
                example = self.make_example(
                    property_names=p_pair,
                    tail_nums=dict(property_num=2),
                    query_path=query_path,
                )
                if example is not None:
                    examples.append(example)

        elif tail_class == "identifier":
            if query_path == "t2h":
                raise ValueError(
                    "Since a species' identifier is used to detect the head entity, tail-to-head query is not applicable when the tail is an identifier."
                )

            for i_pair in random.sample(
                list(itertools.combinations(IDENTIFIER_NAMES, r=2)), k=sampling_size
            ):
                example = self.make_example(
                    identifier_names=i_pair,
                    tail_nums=dict(identifier_num=2),
                    query_path="h2t",
                )
                if example is not None:
                    examples.append(example)

        elif tail_class is None:
            for _ in range(sampling_size):
                try_num = 0
                example = None

                while example is None and try_num < 3:
                    tail_nums = self.get_tail_nums_total2(
                        identifier_num=0 if query_path == "t2h" else None,
                        chemicalclass_num=1 if query_path == "t2t" else None,
                    )
                    example = self.make_example(
                        tail_nums=tail_nums,
                        query_path=query_path,
                        uses=[] if query_path == "t2t" else None,
                    )

                if example is not None:
                    examples.append(example)

        else:
            raise ValueError(
                f"Unexpected value for argument `tail_class`: {tail_class}."
            )

        for example in examples:
            example["tail_num"] = 2

        return examples

    def make_examples_1h_3t(self, query_path: str, sampling_size: int = 1):
        examples: List[dict] = []

        for _ in range(sampling_size):
            try_num = 0
            example = None

            while example is None and try_num < 3:
                tail_nums = self.get_tail_nums_total3(
                    identifier_num=0 if query_path == "t2h" else None,
                    chemicalclass_num=random.randint(1, 2)
                    if query_path == "t2t"
                    else None,
                )
                example = self.make_example(
                    tail_nums=tail_nums,
                    query_path=query_path,
                    uses=[] if query_path == "t2t" else None,
                )
                try_num += 1

            if example is not None:
                examples.append(example)

        for example in examples:
            example["tail_num"] = 3

        return examples

    def make_example(
        self,
        query_path: str,
        property_names: Optional[Iterable[str]] = None,
        identifier_names: Optional[Iterable[str]] = None,
        uses: Optional[Iterable[str]] = None,
        chemicalclasses: Optional[Iterable[str]] = None,
        tail_nums: Dict[str, int] = dict(),
    ):
        property_sampling_frame = (
            self.PROPERTY_NAMES_FOR_T2H if query_path == "t2h" else PROPERTY_NAMES
        )
        if property_names is None:
            property_names = random.sample(
                property_sampling_frame, tail_nums.get("property_num", 0)
            )

        if identifier_names is None:
            identifier_names = random.sample(
                IDENTIFIER_NAMES, tail_nums.get("identifier_num", 0)
            )
        if uses is None:
            uses = random.sample(USES, tail_nums.get("use_num", 0))
        if chemicalclasses is None:
            chemicalclasses = random.sample(
                CHEMICALCLASSES, tail_nums.get("chemicalclass_num", 0)
            )

        property_bindings = {
            f"hasProperty{i + 1}": "os:has" + property_name
            for i, property_name in enumerate(property_names)
        }
        identifier_bindings = {
            f"hasIdentifier{i + 1}": "os:has" + identifier_name
            for i, identifier_name in enumerate(identifier_names)
        }
        usevalue_bindings = {f"UseValue{i + 1}": use for i, use in enumerate(uses)}
        chemicalclassvalue_bindings = {
            f"ChemicalClassValue{i + 1}": chemicalclass
            for i, chemicalclass in enumerate(chemicalclasses)
        }

        subgraph = self.subgraph_retriever.get_subgraph(
            tail_nums=tail_nums,
            bindings={
                **property_bindings,
                **identifier_bindings,
                **usevalue_bindings,
                **chemicalclassvalue_bindings,
            },
        )

        if query_path == "h2t":
            example = self.example_maker_head2tail.make_example(subgraph)
        elif query_path == "t2h":
            example = self.example_maker_tail2head.make_example(subgraph)
        elif query_path == "t2t":
            example = self.example_maker_tail2tail.make_example(subgraph)
        else:
            raise ValueError(f"Unexpected value for `query_path`: {query_path}.")

        if example is not None:
            example["type"] = query_path

        return example

    def get_tail_nums_total2(
        self,
        identifier_num: Optional[int] = None,
        chemicalclass_num: Optional[int] = None,
    ):
        if chemicalclass_num == 1:
            identifier_num = 0
            property_num, use_num = random.choice([(0, 1), (1, 0)])
        elif chemicalclass_num == None:
            property_num = random.randint(0, 2)
            if identifier_num is None:
                identifier_num = random.randint(property_num, 2) - property_num

            p_i_num = property_num + identifier_num
            if p_i_num == 0:
                use_num, chemicalclass_num = 1, 1
            elif p_i_num == 1:
                use_num, chemicalclass_num = random.choice([(0, 1), (1, 0)])
            else:
                use_num, chemicalclass_num = 0, 0
        else:
            raise ValueError(
                f"Unexpected value for argument `chemicalclass_num`: {chemicalclass_num}."
            )

        assert sum([property_num, identifier_num, use_num, chemicalclass_num]) == 2

        return dict(
            property_num=property_num,
            identifier_num=identifier_num,
            use_num=use_num,
            chemicalclass_num=chemicalclass_num,
        )

    def get_tail_nums_total3(
        self,
        identifier_num: Optional[int] = None,
        chemicalclass_num: Optional[int] = None,
    ):
        if chemicalclass_num == 1:
            identifier_num = 0
            property_num, use_num = random.choice([(1, 1), (2, 0)])
        elif chemicalclass_num == 2:
            identifier_num = 0
            property_num, use_num = random.choice([(0, 1), (1, 0)])
        elif chemicalclass_num == None:
            sampling_space_p_i = [
                (0, 1),
                (0, 2),
                (0, 3),
                (1, 0),
                (1, 1),
                (1, 2),
                (2, 0),
                (2, 1),
                (3, 0),
            ]
            if identifier_num is not None:
                sampling_space_p_i = [
                    (p, i) for p, i in sampling_space_p_i if i == identifier_num
                ]

            property_num, identifier_num = random.choice(sampling_space_p_i)
            p_i_num = property_num + identifier_num
            if p_i_num == 1:
                use_num, chemicalclass_num = 1, 1
            elif p_i_num == 2:
                use_num, chemicalclass_num = random.choice([(0, 1), (1, 0)])
            elif p_i_num == 3:
                use_num, chemicalclass_num = 0, 0
            else:
                raise Exception(
                    f"Unexpected `property_num` value of {property_num} and `identifier_num` value of {identifier_num}."
                )
        else:
            raise ValueError(
                f"Unexpected value for argument `chemicalclass_num`: {chemicalclass_num}."
            )

        assert sum([property_num, identifier_num, use_num, chemicalclass_num]) == 3

        return dict(
            property_num=property_num,
            identifier_num=identifier_num,
            use_num=use_num,
            chemicalclass_num=chemicalclass_num,
        )


if __name__ == "__main__":
    pass
