CREATE PROCEDURE InsertAndShowUser(
    @f_name VARCHAR(50),
    @l_name VARCHAR(50),
    @user_email VARCHAR(100)
)
AS
BEGIN
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
END;
