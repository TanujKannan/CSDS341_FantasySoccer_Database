CREATE PROCEDURE ManageUserTeam
    @operation NVARCHAR(10),         -- 'INSERT', 'DELETE', or 'EXCHANGE'
    @email VARCHAR(100),             -- User's email
    @f_name NVARCHAR(50),            -- Player's first name
    @l_name NVARCHAR(50),            -- Player's last name
    @team_name NVARCHAR(100),        -- Player's professional team
    @new_f_name NVARCHAR(50) = NULL, -- New player's first name (optional for EXCHANGE)
    @new_l_name NVARCHAR(50) = NULL, -- New player's last name (optional for EXCHANGE)
    @new_team_name NVARCHAR(100) = NULL -- New player's team (optional for EXCHANGE)
AS
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @playerID INT;
        DECLARE @newPlayerID INT;
        -- Retrieve playerID for the given player
        SELECT @playerID = player_ID
        FROM player
        WHERE f_name = @f_name AND l_name = @l_name AND team_name = @team_name;
        -- Validate player existence
        IF @playerID IS NULL
        BEGIN
            RAISERROR ('Error: Player not found.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
        -- Handle INSERT operation
        IF @operation = 'INSERT'
        BEGIN
            -- Check if user already has the player
            IF EXISTS (SELECT 1 FROM user_team WHERE email = @email AND player_ID = @playerID)
            BEGIN
                RAISERROR ('Error: Player is already on the user''s team.', 16, 1);
                ROLLBACK TRANSACTION;
                RETURN;
            END
            INSERT INTO user_team (email, player_ID)
            VALUES (@email, @playerID);
            RAISERROR ('Player successfully added.', 0, 1);
        END
        ELSE IF @operation = 'DELETE'
        BEGIN
            -- Check if the player is on the user's team
            IF NOT EXISTS (SELECT 1 FROM user_team WHERE email = @email AND player_ID = @playerID)
            BEGIN
                RAISERROR ('Error: Cannot remove a player who is not on the user''s team.', 16, 1);
                ROLLBACK TRANSACTION;
                RETURN;
            END
            -- Delete the player from the user's team
            DELETE FROM user_team
            WHERE email = @email AND player_ID = @playerID;
            RAISERROR ('Player successfully removed.', 0, 1);
        END
        ELSE IF @operation = 'EXCHANGE'
        BEGIN
            -- Ensure new player parameters are provided
            IF @new_f_name IS NULL OR @new_l_name IS NULL OR @new_team_name IS NULL
            BEGIN
                RAISERROR ('Error: New player details must be provided for EXCHANGE operation.', 16, 1);
                ROLLBACK TRANSACTION;
                RETURN;
            END
            -- Retrieve newPlayerID for the new player
            SELECT @newPlayerID = player_ID
            FROM player
            WHERE f_name = @new_f_name AND l_name = @new_l_name AND team_name = @new_team_name;
            -- Validate new player existence
            IF @newPlayerID IS NULL
            BEGIN
                RAISERROR ('Error: New player not found.', 16, 1);
                ROLLBACK TRANSACTION;
                RETURN;
            END
            -- Check if the player to be exchanged is on the user's team
            IF NOT EXISTS (SELECT 1 FROM user_team WHERE email = @email AND player_ID = @playerID)
            BEGIN
                RAISERROR ('Error: Cannot exchange a player who is not on the user''s team.', 16, 1);
                ROLLBACK TRANSACTION;
                RETURN;
            END
            -- Check if the new player is already on the user's team
            IF EXISTS (SELECT 1 FROM user_team WHERE email = @email AND player_ID = @newPlayerID)
            BEGIN
                RAISERROR ('Error: The new player is already on the user''s team.', 16, 1);
                ROLLBACK TRANSACTION;
                RETURN;
            END
            -- Exchange the players
            DELETE FROM user_team
            WHERE email = @email AND player_ID = @playerID;
            INSERT INTO user_team (email, player_ID)
            VALUES (@email, @newPlayerID);
            RAISERROR ('Player successfully exchanged.', 0, 1);
        END
        ELSE
        BEGIN
            RAISERROR ('Error: Invalid operation. Use INSERT, DELETE, or EXCHANGE.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
        COMMIT TRANSACTION;
    END TRY
	BEGIN CATCH
		-- Rollback transaction in case of an error
		IF @@TRANCOUNT > 0
		BEGIN
			ROLLBACK TRANSACTION;
		END
		-- Optionally, log the error or notify users
		RAISERROR ('An unexpected error occurred during the operation.', 16, 1);
		RETURN;
	END CATCH
END;
GO
