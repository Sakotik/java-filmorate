package ru.yandex.practicum.filmorate.storage.user_storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user_storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Qualifier("dbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt("USER_ID");
            String email = rs.getString("EMAIL");
            String login = rs.getString("LOGIN");
            String name = rs.getString("NAME");
            LocalDate birthday = rs.getDate("BIRTHDAY").toLocalDate();

            return new User(id, email, login, name, birthday);
        }
    };

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) " +
                "VALUES (?, ?, ?, ?);";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(getId(user));
        log.info("Created " + user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!check(user.getId()))
            throw new ObjectNotFoundException("Пользователь с id=" + user.getId() + " не найден");

        String updateUserSql = "UPDATE USERS " +
                "SET EMAIL=?, LOGIN=?, NAME=?, BIRTHDAY=? " +
                "WHERE USER_ID=?;";
        jdbcTemplate.update(updateUserSql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(),
                user.getId());
        String deleteUserFriendRequests = "DELETE FROM FRIEND_REQUESTS " +
                "WHERE SENDER_ID=?"; //Очищаем старые запросы в друзья
        jdbcTemplate.update(deleteUserFriendRequests, user.getId());
        for (User friend : user.getFriends()) { //Обновляем запросы в друзья
            String updateUserFriendRequests = "INSERT INTO FRIEND_REQUESTS (SENDER_ID, RECEIVER_ID) " +
                    "VALUES (?, ?);";
            jdbcTemplate.update(updateUserFriendRequests, user.getId(), friend.getId());
        }
        log.info("Updated " + user);
        return user;
    }

    @Override
    public Map<Integer, User> getUsers() {
        String getUsersSql = "SELECT * FROM USERS";
        List<User> users = jdbcTemplate.query(getUsersSql, userMapper);
        for (User user : users) {
            int id = user.getId();
            String getFriendsSql = "SELECT U.USER_ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY " +
                    "FROM USERS AS U " +
                    "INNER JOIN FRIEND_REQUESTS AS FR ON U.USER_ID=FR.RECEIVER_ID " +
                    "WHERE FR.SENDER_ID=?";
            List<User> friendsId = jdbcTemplate.query(getFriendsSql, userMapper, id);
            user.getFriends().addAll(friendsId);
        }
        log.info("Sent all users");
        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private int getId(User user) {
        String sql = "SELECT USER_ID " +
                "FROM USERS " +
                "WHERE LOGIN=?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, user.getLogin());
        rs.next();
        return rs.getInt("USER_ID");
    }

    //Метод по проверке наличия записи в БД
    private boolean check(int id) {
        String sql = "SELECT * " +
                "FROM USERS " +
                "WHERE USER_ID=?";
        List<User> user = jdbcTemplate.query(sql, userMapper, id);
        return user.size() == 1;
    }
}
