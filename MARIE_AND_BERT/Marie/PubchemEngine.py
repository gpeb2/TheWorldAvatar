from Marie.QAEngine import QAEngine
from Marie.Util.Models.TransEScoreModel import TransEScoreModel


class PubChemQAEngine(QAEngine):

    def __init__(self, dataset_dir="CrossGraph/pubchem", dataset_name="pubchem", embedding="others", dict_type="pkl"):
        super().__init__(dataset_dir, dataset_name, embedding=embedding, dim=20, dict_type=dict_type)
        self.score_model_name = 'bert_embedding_10000'
        self.score_model = TransEScoreModel(device=self.device, model_name=self.score_model_name,
                                            dataset_dir=self.dataset_dir, dim=20)
        self.score_model = self.score_model.to(self.device)


if __name__ == "__main__":
    my_engine = PubChemQAEngine(dataset_dir="CrossGraph/pubchem", dataset_name="pubchem", embedding="others",
                                dict_type="pkl")
    rst = my_engine.run("what is ch2o's smiles")
    print(rst)
