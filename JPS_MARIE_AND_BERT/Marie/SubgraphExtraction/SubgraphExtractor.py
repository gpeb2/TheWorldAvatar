import os
from .location import DATA_PATH


# we need a proper place to hold all the triples, currently in file form

class SubgraphExtractor:
    def __init__(self):
        self.PUBCHEM_PATH = os.path.join(DATA_PATH, 'pubchemmedium.txt')
        self.entity_dictionary = {}
        self.pubchem_triples = []
        self.load_pubchem()
        self.make_dictionary()

    def load_pubchem(self):
        self.pubchem_triples = open(self.PUBCHEM_PATH).readlines()
        return self.PUBCHEM_PATH

    # make a mapping between head entities and their related tail entities
    def make_dictionary(self):
        tmp = {}
        for triple in self.pubchem_triples:
            entities = triple.split('\t')
            head_entity = entities[0].strip()
            tail_entity = entities[2].strip()
            if head_entity in tmp:
                tmp[head_entity].append(tail_entity)
            else:
                tmp[head_entity] = [tail_entity]
        self.entity_dictionary['pubchem'] = tmp

    def retrieve_subgraph(self, _head_entity):
        return self.entity_dictionary['pubchem'][_head_entity]
