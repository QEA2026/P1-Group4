from dao.user_dao import find_user_by_id, find_user_by_username

def test_find_user_by_username_returns_user(mocker):
    mock_connection=mocker.Mock()
    mock_cursor=mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor

    mock_cursor.fetchone.return_value = (
        1,
        "testname",
        "testpassword",
        "employee"
    )


    mocker.patch(
        "dao.user_dao.get_connection",
        return_value=mock_connection
    )

    result = find_user_by_username("testname")

    assert result == (
        1,
        "testname",
        "testpassword",
        "employee"
    )

    mock_connection.cursor.assert_called_once()

    mock_cursor.execute.assert_called_once_with(
        " Select * from users WHERE username = ?",
        ("testname",)
    )

    mock_cursor.fetchone.assert_called_once()
    mock_connection.close.assert_called_once()


def test_find_user_by_username_returns_none_when_user_doesnt_exist(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = None

    mocker.patch(
        "dao.user_dao.get_connection",
        return_value=mock_connection
    )

    result = find_user_by_username("unknown")

    assert result is None

    mock_cursor.execute.assert_called_once_with(
        " Select * from users WHERE username = ?",
        ("unknown",)
    )

    mock_cursor.fetchone.assert_called_once()
    mock_connection.close.assert_called_once()


def test_find_user_by_username_returns_none_when_database_error_occurs(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.execute.side_effect = Exception("Database error")

    mocker.patch(
        "dao.user_dao.get_connection",
        return_value=mock_connection
    )

    result = find_user_by_username("testname")

    assert result is None

    mock_cursor.execute.assert_called_once_with(
        " Select * from users WHERE username = ?",
        ("testname",)
    )

    mock_cursor.fetchone.assert_not_called()
    mock_connection.close.assert_called_once()



def test_find_user_by_id_returns_user(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor

    mock_cursor.fetchone.return_value = (
        1,
        "testname",
        "testpassword",
        "employee"
    )

    mocker.patch(
        "dao.user_dao.get_connection",
        return_value=mock_connection
    )

    result = find_user_by_id(1)

    assert result == (
        1,
        "testname",
        "testpassword",
        "employee"
    )

    mock_connection.cursor.assert_called_once()

    mock_cursor.execute.assert_called_once_with(
        "SELECT * FROM users WHERE id = ?",
        (1,)
    )

    mock_cursor.fetchone.assert_called_once()
    mock_connection.close.assert_called_once()


def test_find_user_by_id_returns_none_when_user_doesnt_exist(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = None

    mocker.patch(
        "dao.user_dao.get_connection",
        return_value=mock_connection
    )

    result = find_user_by_id(999)

    assert result is None

    mock_cursor.execute.assert_called_once_with(
        "SELECT * FROM users WHERE id = ?",
        (999,)
    )

    mock_cursor.fetchone.assert_called_once()
    mock_connection.close.assert_called_once()


def test_find_user_by_id_returns_none_when_database_error_occurs(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor

    mock_cursor.execute.side_effect = Exception(
        "Database error"
    )

    mocker.patch(
        "dao.user_dao.get_connection",
        return_value=mock_connection
    )

    result = find_user_by_id(1)

    assert result is None

    mock_cursor.execute.assert_called_once_with(
        "SELECT * FROM users WHERE id = ?",
        (1,)
    )

    mock_cursor.fetchone.assert_not_called()
    mock_connection.close.assert_called_once()