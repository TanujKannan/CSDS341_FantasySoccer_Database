ALTER PROCEDURE [dbo].[InsertMatchAndUpdateScores]
    @team1 NVARCHAR(100),
    @team2 NVARCHAR(100),
    @team1_goals INT,
    @team2_goals INT,
    @goal_details NVARCHAR(MAX) -- Format: 'FirstName LastName:numGoals,FirstName LastName:numGoals'
AS
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Validate input parameters
        IF @team1 IS NULL OR @team2 IS NULL OR LEN(@goal_details) = 0
        BEGIN
            RAISERROR ('Team names and goal details cannot be null or empty.', 16, 1);
            RETURN;
        END

        -- Validate teams exist
        IF NOT EXISTS (SELECT 1 FROM team WHERE team_name = @team1)
        BEGIN
            RAISERROR ('Team %s not found.', 16, 1, @team1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        IF NOT EXISTS (SELECT 1 FROM team WHERE team_name = @team2)
        BEGIN
            RAISERROR ('Team %s not found.', 16, 1, @team2);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Step 1: Insert the match
        INSERT INTO match (team1, team2)
        VALUES (@team1, @team2);

        -- Retrieve the match_ID of the inserted match
        DECLARE @match_ID INT;
        SELECT @match_ID = MAX(match_ID) FROM match WHERE team1 = @team1 AND team2 = @team2;

        -- Step 2: Parse and process goal details
        DECLARE @goal NVARCHAR(100);
        DECLARE @f_name NVARCHAR(50);
        DECLARE @l_name NVARCHAR(50);
        DECLARE @num_goals INT;
        DECLARE @player_ID INT;

        -- Variables to track team goal sums
        DECLARE @team1_goal_sum INT = 0;
        DECLARE @team2_goal_sum INT = 0;

        WHILE LEN(@goal_details) > 0
        BEGIN
            -- Extract the next player-goal pair (e.g., 'Lionel Messi:2')
            SET @goal = LTRIM(RTRIM(LEFT(@goal_details, CHARINDEX(',', @goal_details + ',') - 1)));

            -- Remove the processed pair from the string
            SET @goal_details = SUBSTRING(@goal_details, LEN(@goal) + 2, LEN(@goal_details));

            -- Find the position of the colon separating the name and numGoals
            DECLARE @colonPos INT = CHARINDEX(':', @goal);

            IF @colonPos = 0
            BEGIN
                RAISERROR ('Invalid format in goal details: %s. Expected format: "FirstName LastName:numGoals".', 16, 1, @goal);
                ROLLBACK TRANSACTION;
                RETURN;
            END

            -- Extract the name part and num_goals
            DECLARE @namePart NVARCHAR(100) = LTRIM(RTRIM(LEFT(@goal, @colonPos - 1)));
            SET @num_goals = CAST(LTRIM(RTRIM(SUBSTRING(@goal, @colonPos + 1, LEN(@goal)))) AS INT);

            -- Now, split the name part into first name and last name
            DECLARE @spacePos INT = CHARINDEX(' ', @namePart);

            IF @spacePos = 0
            BEGIN
                RAISERROR ('Invalid name format in goal details: %s. Expected format: "FirstName LastName".', 16, 1, @goal);
                ROLLBACK TRANSACTION;
                RETURN;
            END

            SET @f_name = LTRIM(RTRIM(LEFT(@namePart, @spacePos - 1)));
            SET @l_name = LTRIM(RTRIM(SUBSTRING(@namePart, @spacePos + 1, LEN(@namePart))));

            -- Retrieve player_ID and team_name
            SELECT @player_ID = player_ID
            FROM player
            WHERE f_name = @f_name AND l_name = @l_name AND team_name IN (@team1, @team2);

            -- Validate if player exists and belongs to one of the teams
            IF @player_ID IS NULL
            BEGIN
                RAISERROR ('Player %s %s does not belong to either team %s or %s.', 16, 1, @f_name, @l_name, @team1, @team2);
                ROLLBACK TRANSACTION;
                RETURN;
            END

            -- Track team goal sums
            DECLARE @player_team NVARCHAR(100);
            SELECT @player_team = team_name
            FROM player
            WHERE player_ID = @player_ID;

            IF @player_team = @team1
                SET @team1_goal_sum = @team1_goal_sum + @num_goals;
            ELSE IF @player_team = @team2
                SET @team2_goal_sum = @team2_goal_sum + @num_goals;

            -- Insert into the goal table
            INSERT INTO goal (match_ID, player_ID, num_goals)
            VALUES (@match_ID, @player_ID, @num_goals);

            -- Update fantasy scores for the player (5 points per goal)
            UPDATE player
            SET fantasy_score = fantasy_score + (@num_goals * 5)
            WHERE player_ID = @player_ID;
        END

        -- Validate team goal sums match the total goals
        IF @team1_goal_sum != @team1_goals
        BEGIN
            RAISERROR ('Total goals listed for team %s (%d) do not match the team''s total goals (%d).', 16, 1, @team1, @team1_goal_sum, @team1_goals);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        IF @team2_goal_sum != @team2_goals
        BEGIN
            RAISERROR ('Total goals listed for team %s (%d) do not match the team''s total goals (%d).', 16, 1, @team2, @team2_goal_sum, @team2_goals);
            ROLLBACK TRANSACTION;
            RETURN;
        END

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
        END

        -- Step 4: Add 4 points for defenders and goalkeepers on teams with a clean sheet
        IF @team1_goals = 0
        BEGIN
            UPDATE player
            SET fantasy_score = fantasy_score + 4
            WHERE team_name = @team2 AND position IN ('goalkeeper', 'defender');
        END

        IF @team2_goals = 0
        BEGIN
            UPDATE player
            SET fantasy_score = fantasy_score + 4
            WHERE team_name = @team1 AND position IN ('goalkeeper', 'defender');
        END

        -- Step 5: Add 2 points to all midfielders in the match for both teams
        UPDATE player
        SET fantasy_score = fantasy_score + 2
        WHERE position = 'midfielder' AND team_name IN (@team1,@team2);

        -- Commit the transaction
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        -- Rollback in case of an error
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
