CREATE OR ALTER PROCEDURE [dbo].[GetPlayersByTeam]
    @team_name VARCHAR(100) -- Input parameter for the team name
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRANSACTION; -- Start transaction

    BEGIN TRY
        -- Check if the team exists
        IF NOT EXISTS (SELECT 1 FROM team WHERE team_name = @team_name)
        BEGIN
            RAISERROR('No team exists with the specified name.', 16, 1);
            ROLLBACK TRANSACTION; -- Rollback the transaction
            RETURN;
        END

        -- Query to retrieve players in the specified professional team
        SELECT 
            f_name AS FirstName,
            l_name AS LastName,
            position AS Position,
            fantasy_score AS FantasyScore
        FROM 
            player
        WHERE 
            team_name = @team_name;

        -- Return a message if no players are found
        IF @@ROWCOUNT = 0
        BEGIN
            PRINT 'No players found for the specified team.';
            ROLLBACK TRANSACTION; -- Rollback the transaction
            RETURN;
        END

        COMMIT TRANSACTION; -- Commit the transaction
    END TRY
    BEGIN CATCH
        -- Handle errors
        DECLARE @ErrorMessage NVARCHAR(4000);
        DECLARE @ErrorSeverity INT;
        DECLARE @ErrorState INT;

        SELECT 
            @ErrorMessage = ERROR_MESSAGE(),
            @ErrorSeverity = ERROR_SEVERITY(),
            @ErrorState = ERROR_STATE();

        -- Rollback the transaction and re-throw the error
        ROLLBACK TRANSACTION;
        PRINT 'An error occurred: ' + @ErrorMessage;
        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH
END;
