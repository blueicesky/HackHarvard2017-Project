# noinspection PyPep8
from elasticsearch import Elasticsearch
import re
#from elasticsearch_schema import blacklist_schema
#from logger import Logger

import datetime
import csv


class ElasticSearchInteraction:
    def __init__(self, host, port):
        self.es = Elasticsearch([{'host': host, 'port': port}])
        self.team_code_dict = {}

        with open('data/team_codes.csv') as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                self.team_code_dict[row['team_code']] = row['team_name']

    def create_index(self, index_name, schema):

        if not self.es.indices.exists(index_name):
            self.es.indices.create(index_name, schema)

    def get_team_name(self, team_code):
        return self.team_code_dict[team_code]

    def make_icon_link(self, team_name):
        string_name = "icons/"+team_name.replace(" ","")+".png"
        return string_name



