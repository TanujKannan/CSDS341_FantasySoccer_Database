USE [FantasySoccerDB]
GO
/****** Object:  StoredProcedure [dbo].[GetUserTeamPlayers]    Script Date: 11/22/2024 2:27:49 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER   PROCEDURE [dbo].[GetUserTeamPlayers]
    @user_email VARCHAR(100) -- Input parameter for the user's email
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        -- Query to retrieve players in the user's fantasy team
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
            PRINT 'No players found for the specified user.';
        END
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

        -- Print the error message and re-throw the error
        PRINT 'An error occurred: ' + @ErrorMessage;
        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH
END;
