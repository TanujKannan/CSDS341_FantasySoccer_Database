CREATE TABLE team (
	team_name VARCHAR(100) NOT NULL,
	PRIMARY KEY (team_name)
);

CREATE TABLE users (
	f_name VARCHAR(50) NOT NULL,
	l_name VARCHAR(50) NOT NULL,
	email VARCHAR(100) NOT NULL,
	PRIMARY KEY (email)
);


CREATE TABLE match (
	match_ID INT IDENTITY(1,1) PRIMARY KEY,
	team1 VARCHAR(100) NOT NULL,
	team2 VARCHAR(100) NOT NULL,
	FOREIGN KEY (team1) REFERENCES team(team_name),
	FOREIGN KEY (team2) REFERENCES team(team_name),
	CHECK (team1 <> team2)
);


CREATE TABLE player (
	player_ID INT IDENTITY(1,1) PRIMARY KEY,
	team_name VARCHAR(100) NOT NULL,
	f_name VARCHAR(50) NOT NULL,
	l_name VARCHAR(50) NOT NULL,
	position VARCHAR(20) NOT NULL CHECK (position IN ('forward', 'midfielder', 'defender', 'goalkeeper')),
	fantasy_score INT DEFAULT 0,
	FOREIGN KEY (team_name) REFERENCES team(team_name)
);

CREATE INDEX idx_player_l_name ON player (l_name);

CREATE TABLE user_team (
	email VARCHAR(100) NOT NULL,
	player_ID INT NOT NULL,
	FOREIGN KEY (email) REFERENCES users(email),
	FOREIGN KEY (player_ID) REFERENCES player(player_ID),
	PRIMARY KEY (email,player_ID)
);

CREATE TABLE goal (
	match_ID INT,
	player_ID INT,
	num_goals INT DEFAULT 0 CHECK (num_goals >= 0),
	PRIMARY KEY (match_ID, player_ID)
);

	
