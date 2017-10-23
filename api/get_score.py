import falcon
import json
import sys
from get_stats import get_stats
sys.path.append("indexing/")
from predict import predict

class get_score(object):

    def __init__(self):
        # self.predict_instance = predict()
        self.stats = get_stats()
        self.stats.get_data()
        self.stats.make_dict()
        self.stats.augment_profile()
        self.predicter = predict()

        print('hello')

    def on_get(self, req, resp):
        msg = {
            'score': '100!'
        }
        resp.body = json.dumps(msg)
        resp.status = falcon.HTTP_200
        print(resp.status)

    def on_post(self, req, resp):
        # print('post')
        # resp.body = json.dumps("yeah we can post")

        data = req.stream.read(req.content_length or 0)
        name = json.loads(data)
        player_data = self.stats.name_search(name['detected_text'])

        # print(player_data)

        # result = self.predict_instance.process_new_feature(player_json)
        # home_score = result[0]
        # away_score = result[1]
        # print result
        # result = player_json['team1_name'] + " " + str(home_score) + "-" + str(away_score) + " "+ player_json['team2_name']
        player_data = player_data[0]
        home_team = player_data['team']
        away_team = self.stats.find_matchInfo(player_data['team'])
        lol = self.predicter.make_struct(home_team, away_team)
        score = self.predicter.process_new_feature(lol)
        player_data['home_score'] = score[0]
        player_data['away_score'] = score[1]
        player_data['away_team'] = away_team
        player_data['home_team'] = home_team


        resp.status = falcon.HTTP_201
        msg = {
            'player_data': player_data
        }

        resp.body = json.dumps(msg)