from dao.user_dao import find_user_by_id, find_user_by_username


# Find user by username
def test_find_user_by_username_returns_existing_user(temp_database):

    result = find_user_by_username("testname1")

    assert result == (
        1,
        "testname1",
        "newpassword",
        "employee"
    )


# Find user using a username that doesnt exist (Sad Path)
def test_find_user_by_username_returns_none_when_user_does_not_exist(temp_database):

    result = find_user_by_username("unknown")

    assert result is None


# Find by user ID
def test_find_user_by_id_returns_existing_user(temp_database):
    
    result = find_user_by_id(1)

    assert result == (
        1,
        "testname1",
        "newpassword",
        "employee"
    )


# Find by user ID that doesnt exist (Sad Path)
def test_find_user_by_id_returns_none_when_user_does_not_exist(temp_database):
    
    result = find_user_by_id(999)

    assert result is None