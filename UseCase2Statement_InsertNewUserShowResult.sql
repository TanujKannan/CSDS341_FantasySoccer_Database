USE [FantasySoccerDB]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[InsertAndShowUser](
    @f_name VARCHAR(50),
    @l_name VARCHAR(50),
    @user_email VARCHAR(100)
)
AS
BEGIN
    -- Start a transaction
    BEGIN TRANSACTION;

    BEGIN TRY
        -- Insert new user
        INSERT INTO users (f_name, l_name, email)
        VALUES (@f_name, @l_name, @user_email);

        -- Select and return the inserted user record
        SELECT 
            f_name,
            l_name,
            email
        FROM 
            users
        WHERE 
            email = @user_email;

        -- Commit the transaction if everything succeeds
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        -- Rollback the transaction if an error occurs
        ROLLBACK TRANSACTION;

        -- Return the error message
        THROW;
    END CATCH
END;

