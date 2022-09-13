from pprint import pprint

import numpy as np
import torch
import transformers
from torch import nn, optim, FloatTensor, LongTensor
import os
import pandas as pd
from torch.nn.functional import normalize
from torch.utils.data.dataset import Dataset as TorchDataset
from tqdm import tqdm
from transformers import BertModel, BertTokenizer, AdamW

from Marie.Util.location import DATA_DIR

# TODO: a Dataset class that provides question examples and their relation
# TODO: also provides a rel embedding


max_len = 12


def to_categorical(y, num_classes):
    """ 1-hot encodes a tensor """
    return np.eye(num_classes, dtype='float32')[y]


class Dataset(TorchDataset):
    def __init__(self, df):
        super(Dataset, self).__init__()

        self.tokenizer = BertTokenizer.from_pretrained('bert-base-cased')
        self.df = df
        self.max_len = 12
        self.tokenized_questions = [self.tokenizer(text,
                                                   padding='max_length', max_length=self.max_len, truncation=True,
                                                   return_tensors="pt") for text in self.df.iloc[:, 0]]
        # self.y = to_categorical(self.df.iloc[:, 1], 21)
        self.rel_embedding = pd.read_csv(os.path.join(DATA_DIR, 'rel_embedding.tsv'), sep='\t', header=None)
        self.y = self.rel_embedding.iloc[self.df.iloc[:, 1].tolist()].reset_index(drop=True)

    def classes(self):
        return self.y

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):
        return self.tokenized_questions[idx], torch.FloatTensor(self.y.iloc[idx])


class StandAloneBERT(nn.Module):

    def __init__(self, device=torch.device("cuda")):
        super(StandAloneBERT, self).__init__()
        self.device = device
        self.bert = BertModel.from_pretrained('bert-base-cased')
        # self.dropout = nn.Dropout(0.5)
        self.dropout = nn.Dropout(0)
        # self.linear = nn.Linear(768, 512)  # keep this model ...
        self.linear = nn.Linear(768, 20)  # keep this model ...
        # self.mid_1 = nn.Linear(512, 512)
        self.mid_2 = nn.Linear(512, 20)
        self.relu = nn.ReLU()  # a good bert -> relation idx model is trained, how do we project it?
        self.criterion = torch.nn.CosineEmbeddingLoss()

    def load_model(self, model_name):
        self.load_state_dict(torch.load(os.path.join(DATA_DIR, model_name)))

    def loss(self, emb_1, emb_2):
        # set target to be -1, which measures the dissimilarity
        assert emb_1.shape[1] == emb_2.shape[1]
        target = torch.LongTensor([-1]).to(self.device)
        return self.criterion(emb_1, emb_2, target).to(self.device)

    def distance(self, emb_1, emb_2):
        assert emb_1.shape[1] == emb_2.shape[1]
        # calculate the L1 distance between emb_1 and emb_2
        # distance = (normalize(emb_1) - normalize(emb_2)).norm(p=1, dim=1).to(self.device)
        # distance = normalize(emb_1) - normalize(emb_2)
        # print(distance)
        # cos = torch.nn.CosineSimilarity(dim=1, eps=1e-6)
        # target = torch.tensor([-1], dtype=torch.long).to(self.device)
        # distance = self.criterion(normalize(emb_1), emb_2, target)
        # distance = 1 - cos(normalize(emb_1), normalize(emb_2))
        # distance = pairwise_manhattan_distance(normalize(emb_1), emb_2)
        distance = (emb_1 - emb_2).norm(p=1, dim=1)
        return distance


    def predict(self, question):
        input_ids = torch.reshape(question['input_ids'], (-1, max_len))
        attention_mask = torch.reshape(question['attention_mask'], (-1, max_len))
        pooled_output = self.bert(input_ids=input_ids.to(self.device),
                                  attention_mask=attention_mask.to(self.device),
                                  return_dict=False)[1]

        dropout_output = self.dropout(pooled_output)
        linear_output = self.linear(dropout_output)
        return linear_output


    def forward(self, question, y):
        input_ids = torch.reshape(question['input_ids'], (-1, max_len))
        attention_mask = torch.reshape(question['attention_mask'], (-1, max_len))
        pooled_output = self.bert(input_ids=input_ids.to(self.device),
                                  attention_mask=attention_mask.to(self.device),
                                  return_dict=False)[1]
        dropout_output = self.dropout(pooled_output)
        linear_output = self.linear(dropout_output)
        distance = self.distance(linear_output, y)
        '''
        Update the loss function to do a calculation eh + r = et, where r is the output of linear_output ... 
        If the fine-tuning of the current thing fails 
        '''

        return distance, normalize(linear_output)


def one_train_iteration(learning_rate=1e-8, model_name='bert_model_embedding_20_cosine_self_made', resume_training = False, batch_size = 64):
    learning_rate = learning_rate
    step = 0
    # ===========================
    use_cuda = torch.cuda.is_available()
    device = torch.device("cuda" if use_cuda else "cpu")
    print(f'=========== USING {device} ===============')
    # ===========================

    df = pd.read_csv(os.path.join(DATA_DIR, 'question_set_rel'), sep=',', header=None)
    df = df.sample(frac=1)
    df_train, df_test = np.split(df.sample(frac=1, random_state=42), [int(.8 * len(df))])
    dataset_test = Dataset(df_test)
    dataset_train = Dataset(df_train)
    test_dataloader = torch.utils.data.DataLoader(dataset_test, batch_size=batch_size, shuffle=True)
    train_dataloader = torch.utils.data.DataLoader(dataset_train, batch_size=batch_size, shuffle=True)
    model = StandAloneBERT(device=device)
    optimizer = AdamW(model.parameters(), lr=learning_rate)
    model = model.cuda()
    if resume_training:
        model.load_model(model_name)


    for epoch in range(50):
        total_loss_train = 0
        model.train()
        for train_batch in tqdm(train_dataloader):
            true_y = train_batch[1].to(device)  # the y is replaced by the embedding now, the loss is the distance
            loss, output = model(train_batch[0], true_y)
            loss.mean().backward()
            # loss.backward()
            optimizer.step()
            step += 1
            total_loss_train += loss.mean().item()

        print(f'\ntotal_loss_train: {total_loss_train}')
        if epoch % 10 == 0:
            model.eval()
            total_loss_val = 0
            avg_cos_similarity = 0
            dist_similarity = 0
            for test_batch in tqdm(test_dataloader):
                true_y = test_batch[1].to(device)  # the y is replaced by the embedding now, the loss is the distance
                loss, output_val = model(test_batch[0], true_y)
                total_loss_val += loss.detach().mean().item()
                test_cosine = torch.nn.CosineSimilarity(dim=1, eps=1e-08)
                cos_similarity = test_cosine(output_val, true_y).detach()
                avg_cos_similarity += torch.mean(cos_similarity).detach().item()
                dist_similarity += (output_val - true_y).norm(p=1, dim=1).detach().mean()

            print(f'\ntotal_loss_val: {total_loss_val}')
            print('average cosine similarity', avg_cos_similarity / len(test_dataloader))
            print('average dist similarity', dist_similarity / len(test_dataloader))
            torch.save(model.state_dict(), os.path.join(DATA_DIR, model_name))
            print('model saved')


if __name__ == '__main__':
    starting_lr = 1e-6 # this is probably the best lr
    # starting_lr = 1e-4
    current_lr = starting_lr
    for i in range(10):
        print(f'current learning rate {current_lr}')
        one_train_iteration(current_lr,
                            model_name='bert_model_embedding_20_cosine_single_layer_pubchem500_no_eh',
                            resume_training=True, batch_size= 128)
        # current_lr = current_lr / 100
