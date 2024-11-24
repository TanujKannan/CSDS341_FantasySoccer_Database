ALTER PROCEDURE [dbo].[GetUserTeamPlayers]
    @user_email VARCHAR(100) -- Input parameter for the user's email
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRANSACTION; -- Start transaction

    BEGIN TRY
        -- Query to retrieve players in the user's fantasy team
        IF NOT EXISTS (SELECT 1 FROM users WHERE email = @user_email)
        BEGIN
            -- If the user does not exist, raise an error
            RAISERROR ('No user exists with the specified email.', 16, 1);
            ROLLBACK TRANSACTION; -- Rollback the transaction
            RETURN;
        END

        SELECT 
            p.f_name AS FirstName,
            p.l_name AS LastName,
            p.position AS Position,
            p.fantasy_score AS FantasyScore
        FROM 
            user_team ut
        INNER JOIN 
            player p ON ut.player_ID = p.player_ID
        WHERE 
            ut.email = @user_email;

        -- Return a message if no players are found
        IF @@ROWCOUNT = 0
        BEGIN
            RAISERROR ('No players found for the specified user.', 16, 1);
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
