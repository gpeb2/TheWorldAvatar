import json
import os, sys

sys.path.append("..")
import time

import torch
from torch.nn.functional import one_hot
from transformers import BertTokenizer

from Marie.EntityLinking.IRILookup import IRILookup
from Marie.PubchemEngine import PubChemQAEngine
from Marie.OntoCompChem import OntoCompChemEngine
from Marie.OntoSpecies import OntoSpeciesQAEngine
from Marie.OntoKinReaction import OntoKinReactionInterface
from Marie.OntoMoPs import OntoMoPsQAEngine
from Marie.Agent import AgentInterface
from Marie.OntoSpeciesNew import OntoSpeciesNew
from Marie.Ontokin import OntoKinQAEngine
from Marie.Util.CommonTools.NLPTools import NLPTools
from Marie.Util.Logging import MarieLogger
from Marie.Util.Models.CrossGraphAlignmentModel import CrossGraphAlignmentModel
from Marie.Util.location import DATA_DIR
from Marie.EntityLinking.ChemicalNEL import ChemicalNEL
from Marie.WikidataEngine import WikidataEngine
import threading


def remove_mention(question, mention):
    stop_words = ["what", "is", "are", "the", "more", "less", "than", "species", "find", "all", "over", "under", "of"]
    flag_words = ["mops", "cbu", "assembly model"]
    if "mops" not in question:
        stop_words += ["with"]
    tokens = [t for t in question.split(" ") if t.lower() not in stop_words]
    question = " ".join(tokens).strip()

    for flag_word in flag_words:
        if flag_word in question:
            return question

    if "reaction" not in question.lower():
        question = question.lower().replace(mention, "")
        tokens = [t for t in question.split(" ") if t.lower() not in stop_words]
        question = " ".join(tokens).strip()
        return question
    else:
        return "reaction"


def filter_for_mention(question):
    question = question.lower()
    if "what" not in question:
        question = f"what is the {question}"
    return question.strip()

class CrossGraphQAEngine:
    """
    The cross graph QA engine will answer question across domains
    """

    def __init__(self):
        self.marie_logger = MarieLogger()
        self.nel = ChemicalNEL()
        self.ner_lite = IRILookup(nel=self.nel, enable_class_ner=False)

        self.domain_encoding = {"pubchem": 0, "ontocompchem": 1, "ontospecies": 2,
                                "ontokin": 3, "wikidata": 4, "ontospecies_new": 5,
                                "ontoagent": 6, "ontomops": 7, "ontokin_reaction": 8}

        # self.domain_encoding = {"ontoagent": 0}

        self.got_numerical_values = False
        self.numerical_domain_list = ["wikidata", "ontospecies_new", "ontomops"]
        self.numerical_label_list = ["numerical", "multi-target"]

        self.encoding_domain = {v: k for k, v in self.domain_encoding.items()}
        self.domain_list = list(self.domain_encoding.keys())
        print(self.domain_list)
        self.engine_list = [None] * len(self.domain_list)
        self.lock = threading.Lock()
        self.device = torch.device("cpu")
        self.max_length = 12
        # self.tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
        self.nlp = NLPTools(tokenizer_name="bert-base-uncased")
        self.dataset_path = os.path.join(DATA_DIR, 'CrossGraph')
        self.score_adjust_model = CrossGraphAlignmentModel(device=self.device).to(self.device)
        self.score_adjust_model.load_state_dict(torch.load(os.path.join(self.dataset_path,
                                                                        'cross_graph_model_with_all_9_updated'),
                                                           map_location=self.device))
        self.stop_words = ["find", "all", "species", "what", "is", "the", "show", "me", "and", "as", "product"
            , "products", "reactant", "reactants"]
        self.init_engines()

    def init_engines(self):
        '''
        Init all engines for each domain
        '''
        for domain, index in self.domain_encoding.items():
            print("============== init engines ==============")
            print("domain:", domain)
            print("index: ", index)
            if domain == "pubchem":
                self.engine_list[index] = PubChemQAEngine(nel=self.nel)
            elif domain == "ontocompchem":
                self.engine_list[index] = OntoCompChemEngine(nel=self.nel)
            elif domain == "ontospecies":
                self.engine_list[index] = OntoSpeciesQAEngine(nel=self.nel)
            elif domain == "ontokin":
                self.engine_list[index] = OntoKinQAEngine(nel=self.nel)
            elif domain == "wikidata":
                self.engine_list[index] = WikidataEngine(nel=self.nel)
            elif domain == "ontospecies_new":
                self.engine_list[index] = OntoSpeciesNew(nel=self.nel)
            elif domain == "ontoagent":
                self.engine_list[index] = AgentInterface(nel=self.nel)
            elif domain == "ontomops":
                self.engine_list[index] = OntoMoPsQAEngine(nel=self.nel)
            elif domain == "ontokin_reaction":
                self.engine_list[index] = OntoKinReactionInterface()
            print(f"Engine for {domain} created")

    def normalize_scores(self, scores_list):
        normalized_scores = {}
        print("========================")
        print("score_list", scores_list)
        print("=======================")
        for domain in self.domain_list:
            if domain in scores_list:
                scores = scores_list[domain]
                max_score = max(scores)
                scores = [score / max_score for score in scores]
                if len(scores) < 5:
                    diff = 5 - len(scores)
                    for i in range(diff):
                        scores.append(-999)
                normalized_scores[domain] = scores
        return normalized_scores

    def prepare_for_visualization(self, answer_list, target_list):
        result = []
        for ans, tar in zip(answer_list, target_list):
            row = {"node": tar, "value": ans}
            result.append(row)

        return result

    def re_rank_answers(self, domain_list, score_list, answer_list, target_list):
        values, indices = torch.topk(torch.FloatTensor(score_list), k=min(10, len(score_list)), largest=True)
        re_ranked_labels = [answer_list[i] for i in indices]
        re_ranked_domain_list = [domain_list[i] for i in indices]
        re_ranked_engine_list = [self.engine_list[domain] for domain in re_ranked_domain_list]
        re_ranked_score_list = [float(v.item()) for v in values]
        re_ranked_domain_list = [self.encoding_domain[d] for d in re_ranked_domain_list]

        result = []



        for label, domain, score, target in zip(re_ranked_labels, re_ranked_domain_list, re_ranked_score_list,
                                                target_list):

            if "EMPTY" not in label:
                row = {"node": label, "domain": domain, "score": score, "target": target}
                result.append(row)

        return result

    # def filter_for_cross_graph(self, question):
    #     tokens = question.strip().lower().split(" ")
    #     tokens = [t for t in tokens if t not in self.stop_words]
    #     return " ".join(tokens)

    def get_domain_list(self, question):

        question = filter_for_mention(question)
        mention = self.ner_lite.get_mention(question)
        question = remove_mention(question, mention=mention)

        # question = self.filter_for_cross_graph(question)
        predicted_domain_labels = []
        _, tokenized_q = self.nlp.tokenize_question(question, 1)
        pred_domain_list = self.score_adjust_model.predict_domain([tokenized_q])[0]
        for idx, domain in enumerate(pred_domain_list):
            if domain == 1:
                predicted_domain_labels.append(self.domain_list[idx])

        return predicted_domain_labels, pred_domain_list

    def run(self, question, disable_alignment: bool = False, heads={}):
        """
        The main interface for the integrated QA engine
        :param disable_alignment: whether run for test purpose, if true, score alignment will be disabled
        :param question: question in string, with head entity removed
        :param heads: IRI of the head entity before cross-ontology translation, always in the form of CID (pubchem ID)
        :return: the re-ranked list of answer labels according to the adjusted scores
        """
        # lets get the domain list in advance first, then selectively call the engines to shorten the response time

        score_list = {}
        label_list = {}
        domain_list = []
        target_list = {}
        got_numerical_values = False
        numerical_domain = None

        #
        # stop_words = []
        # tokens = [t for t in question.split(" ") if t.lower().strip() not in stop_words]
        # question = " ".join(tokens)

        # mention = self.ner_lite.get_mention(question)

        # if mention is not None:
        #     question = question.replace(mention, "")
        domain_list_for_question, score_factors = self.get_domain_list(question)

        # print("predicted domain for this question:", domain_list_for_question)

        def call_domain(domain, index, head, numerical_domain_list, numerical_label_list):
            nonlocal score_list, label_list, target_list, got_numerical_values, numerical_domain
            if domain in domain_list_for_question:
                if not got_numerical_values:
                    engine = self.engine_list[index]

                    self.marie_logger.debug(
                        f"======================== USING ENGINE {domain}============================")

                    if domain == "ontoagent":
                        results = engine.run(question=question)
                        labels, scores, targets = results
                        print("=============== RESULT FROM AGENT ============")
                        print(results)

                    elif domain in numerical_domain_list:
                        results = engine.run(question=question)
                        labels, scores, targets, numerical_list, question_type = results
                        # if question_type == "numerical":
                        if question_type in numerical_label_list and (len(labels) > 0):
                            got_numerical_values = True
                            numerical_domain = domain
                    else:
                        results = engine.run(question=question)
                        labels, scores, targets = results
                    length_diff = 5 - len(labels)
                    scores = scores + [-999] * length_diff
                    labels = labels + ["EMPTY SLOT"] * length_diff
                    targets = targets + ["EMPTY SLOT"] * length_diff
                    with self.lock:
                        score_list[domain] = scores
                        label_list[domain] = labels
                        target_list[domain] = targets

            else:
                score_list[domain] = [-999] * 5
                label_list[domain] = ["EMPTY SLOT"] * 5
                target_list[domain] = ["EMPTY SLOT"] * 5

        threads = []
        for i, domain in enumerate(self.domain_list):
            if domain in heads:
                head = heads[domain]
            else:
                head = None
            t = threading.Thread(target=call_domain, args=(domain, i, head, self.numerical_domain_list,
                                                           self.numerical_label_list))
            threads.append(t)
            t.start()

        for t in threads:
            t.join()

        if got_numerical_values:
            return self.prepare_for_visualization(target_list=target_list[numerical_domain],
                                                  answer_list=label_list[numerical_domain])
        else:
            # question_for_cross_graph = self.filter_for_cross_graph(question)
            # tokenized_question = self.nlp.tokenize_question(question_for_cross_graph, 1)
            # score_factors = self.score_adjust_model.predict_domain(tokenized_question).squeeze()
            # score_factors = score_factor
            adjusted_score_list = []
            normalized_score_list = self.normalize_scores(score_list)
            for score_factor, domain in zip(score_factors, self.domain_list):
                if domain in normalized_score_list:
                    score = normalized_score_list[domain]
                    if disable_alignment:
                        score = torch.FloatTensor([1, 1, 1, 1, 1])
                    else:
                        score = torch.FloatTensor(score)
                    adjusted_score = score + score_factor.repeat(5)
                    adjusted_score = adjusted_score.tolist()
                    adjusted_score_list = adjusted_score_list + adjusted_score
                    domain_list = domain_list + [self.domain_encoding[domain]] * 5

            new_label_list = []
            new_target_list = []

            for domain in self.domain_list:
                if domain in label_list and domain in target_list:
                    new_label_list += label_list[domain]
                    new_target_list += target_list[domain]

            return self.re_rank_answers(domain_list=domain_list, score_list=adjusted_score_list,
                                        answer_list=new_label_list,
                                        target_list=new_target_list)


if __name__ == '__main__':
    result_list = []

    my_qa_engine = CrossGraphQAEngine()

    # text = "what is the charge of CH4"
    # rst = my_qa_engine.run(question=text)
    # print(rst)
    # print("==============================================================================")
    # text = "find all species with molecular weight more than 100"
    # rst = my_qa_engine.run(question=text)
    # print(rst)
    question_list = ["Topological Polar Surface Area of methane?", "heat capacity of C3H4O"]
    for question in question_list:
        START_TIME = time.time()
        print("==============================================================================")
#         text = "heat capacity of C3H4O"
        rst = my_qa_engine.run(question=question)
        print(rst)
        print(f"TIME USED: {time.time() - START_TIME}")





    # while text != "quit":
    #     START_TIME = time.time()
    #     rst = my_qa_engine.run(question=text)
    #     print(rst)
    #     print(f"TIME USED: {time.time() - START_TIME}")
    #     text = input("Question: ")

    #
    # START_TIME = time.time()
    # rst = my_qa_engine.run(question="find melting point of all species")
    # print(rst)
    # print(f"TIME USED: {time.time() - START_TIME}")
    #
    # START_TIME = time.time()
    # rst = my_qa_engine.run(question="what is the boiling point of 1-methoxy-2-propanol")
    # print(rst)
    # print(f"TIME USED: {time.time() - START_TIME}")

    # question_answer_dict_path = os.path.join(DATA_DIR, "CrossGraph/selected_question_answer_list.json")
    # question_answer_list = json.loads(open(question_answer_dict_path).read())
    # for obj in question_answer_list:
    #     question = obj["question"]
    #     answer = obj["answer"]
    #     pred_answers = my_qa_engine.run(question=question)
    #     print("predicted answers", pred_answers)
    #     row = {"question": question, "true_answer": answer, "pred_answer": pred_answers}
    #     result_list.append(row)
    #
    # result_path = os.path.join(DATA_DIR, "CrossGraph/selected_result_list.json")
    # with open(result_path, "w") as f:
    #     f.write(json.dumps(result_list))
    #     f.close()
    #
