from time import sleep, time
from datetime import datetime
import pandas as pd
import json

match_file = 'EPL_Fixture_1718.csv'
player_file = 'fifa_player.csv'


def find_profile(player_name):
	df_p = pd.read_csv(player_file)
	game_time = ""

	for index, rows in df_p.iterrows():
		if (rows['Name'] == player_name):
			age = df_p.iloc[index]['Age']
			photo = df_p.iloc[index]['photooto']
			nation = df_p.iloc[index]['Nationality']
			pos = df_p.iloc[index]['Preferred Positions']
			break;
	return age, photo, nation, pos

def find_matchInfo(team_name):
	df_m = pd.read_csv(match_file)
	for index, rows in df_m.iterrows():
		game_date = datetime.strptime(df_m.iloc[index]['DATE'], '%Y-%m-%d')
		#print(datetime.strptime((str(datetime.now()))[:10], '%Y-%m-%d'))
		if (game_date > datetime.now()):
			if ((rows['HOME TEAM'] == team_name) or (rows['AWAY TEAM'] == team_name)):
				game_date = df_m.iloc[index]['DATE']
				game_hour = df_m.iloc[index]['TIME']
				fixture = df_m.iloc[index]['FIXTURE']
				game_time = (game_date + " " + game_hour)
				game_time = datetime.strptime(game_time, '%Y-%m-%d %H:%M')
				break;
	return game_time

def main():
	with open('player.json') as data_file:  
		data = json.load(data_file)
	
	club_name = 'Chelsea'
	player = 'Rooney'

	match_info = find_matchInfo(club_name)
	age, photo, nation, pos = find_profile(player)

if __name__ == "__main__":
	main()