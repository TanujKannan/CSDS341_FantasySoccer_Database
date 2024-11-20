CREATE PROCEDURE InsertMatchAndUpdateScores
    @team1 NVARCHAR(100),
    @team2 NVARCHAR(100),
    @team1_goals INT,
    @team2_goals INT,
    @goal_details NVARCHAR(MAX) -- A comma-separated string for goal details (e.g., '1:2,3:1')
AS
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Step 1: Insert the match
        INSERT INTO match (team1, team2)
        VALUES (@team1, @team2);

        -- Retrieve the match_ID of the inserted match
        DECLARE @match_ID INT;
        SELECT @match_ID = MAX(match_ID) FROM match;

        -- Step 2: Parse and insert goal details
        -- Split the @goal_details string into individual player-goal pairs
        DECLARE @goal NVARCHAR(50);
        DECLARE @player_ID INT;
        DECLARE @num_goals INT;
        WHILE LEN(@goal_details) > 0
        BEGIN
            -- Extract the next player-goal pair (e.g., '1:2')
            SET @goal = LEFT(@goal_details, CHARINDEX(',', @goal_details + ',') - 1);

            -- Remove the processed pair from the string
            SET @goal_details = STUFF(@goal_details, 1, LEN(@goal) + 1, '');

            -- Split the player_ID and num_goals from the pair
            SET @player_ID = CAST(LEFT(@goal, CHARINDEX(':', @goal) - 1) AS INT);
            SET @num_goals = CAST(RIGHT(@goal, LEN(@goal) - CHARINDEX(':', @goal)) AS INT);

            -- Insert into the goal table
            INSERT INTO goal (match_ID, player_ID, num_goals)
            VALUES (@match_ID, @player_ID, @num_goals);

            -- Update fantasy scores for the player (5 points per goal)
            UPDATE player
            SET fantasy_score = fantasy_score + (@num_goals * 5)
            WHERE player_ID = @player_ID;
        END;

        -- Step 3: Add 2 points for all players on the winning team
        DECLARE @winning_team NVARCHAR(100);
        IF @team1_goals > @team2_goals
            SET @winning_team = @team1;
        ELSE IF @team2_goals > @team1_goals
            SET @winning_team = @team2;
        ELSE
            SET @winning_team = NULL; -- Draw, no winning team

        IF @winning_team IS NOT NULL
        BEGIN
            UPDATE player
            SET fantasy_score = fantasy_score + 2
            WHERE team_name = @winning_team;
        END;

        -- Step 4: Add 4 points for defenders and goalkeepers on teams with a clean sheet
        IF @team1_goals = 0
        BEGIN
            UPDATE player
            SET fantasy_score = fantasy_score + 4
            WHERE team_name = @team1 AND position IN ('goalkeeper', 'defender');
        END;

        IF @team2_goals = 0
        BEGIN
            UPDATE player
            SET fantasy_score = fantasy_score + 4
            WHERE team_name = @team2 AND position IN ('goalkeeper', 'defender');
        END;

        -- Step 5: Add 2 points to all midfielders
        UPDATE player
        SET fantasy_score = fantasy_score + 2
        WHERE position = 'midfielder';

        -- Commit the transaction
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        -- Rollback in case of an error
        ROLLBACK TRANSACTION;

        -- Throw the error
        RAISERROR ('Error occurred while processing the transaction.', 16, 1);
    END CATCH;
END;
GO
