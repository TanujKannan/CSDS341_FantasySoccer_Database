CREATE TRIGGER trg_CheckMaxPlayers
ON user_team
INSTEAD OF INSERT
AS
BEGIN
    -- Check if any user exceeds 11 players
    IF EXISTS (
        SELECT i.email
        FROM inserted AS i
        JOIN user_team AS ut ON ut.email = i.email
        GROUP BY i.email
        HAVING COUNT(ut.player_ID) + COUNT(i.player_ID) > 11
    )
    BEGIN
        RAISERROR ('A user cannot have more than 11 players in their team.', 16, 1);
        RETURN;
    END

    -- If the condition is not violated, perform the insert
    INSERT INTO user_team (email, player_ID)
    SELECT email, player_ID FROM inserted;
END;
GO
