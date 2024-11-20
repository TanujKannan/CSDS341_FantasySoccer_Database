CREATE PROCEDURE GetUserFantasyScore(
    @user_email VARCHAR(100)
)
AS
BEGIN
    SELECT 
        u.email AS user_email,
        SUM(p.fantasy_score) AS total_fantasy_score
    FROM 
        user_team ut
    INNER JOIN 
        player p ON ut.player_ID = p.player_ID
    INNER JOIN 
        users u ON ut.email = u.email
    WHERE 
        u.email = @user_email
    GROUP BY 
        u.email;
END;
