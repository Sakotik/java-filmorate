package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user_storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    @Autowired
    @Qualifier("dbStorage")
    private UserStorage userStorage;
    @Autowired
    private Validator<User> userValidator;

    public User createUser(User user) {
        userValidator.validate(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (!userStorage.getUsers().containsKey(user.getId()))
            throw new ObjectNotFoundException("User with id=" + user.getId() + " not found");
        userValidator.validate(user);
        return userStorage.updateUser(user);
    }

    public User getUser(int id) {
        Map<Integer, User> users = userStorage.getUsers();
        if (!users.containsKey(id))
            throw new ObjectNotFoundException("User with id=" + id + " not found");
        log.info("Sent user (id=" + id + ")");
        return users.get(id);
    }

    public Map<Integer, User> getUsers() {
        return userStorage.getUsers();
    }

    public List<User> getFriends(int id) {
        Map<Integer, User> users = userStorage.getUsers();
        if (!users.containsKey(id))
            throw new ObjectNotFoundException("Пользователь с id=" + id + " не найден");
        User user = users.get(id);
        log.info("Sent all user's (id=" + id + ") friends");
        return user.getFriends()
                .stream()
                .sorted(Comparator.comparingInt(User::getId))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        Map<String, User> users = getTwoUsers(id, otherId);
        Set<User> commonFriends = new HashSet<>(users.get("user1").getFriends());
        commonFriends.retainAll(users.get("user2").getFriends());
        log.info("Sent users' (id=" + id + "/" + otherId + ") common friends");
        return new ArrayList<>(commonFriends);
    }

    public User addFriend(int id, int friendId) {
        Map<String, User> users = getTwoUsers(id, friendId);
        User user1 = users.get("user1");
        User user2 = users.get("user2");
        user1.getFriends().add(user2);
        userStorage.updateUser(user1);
        log.info("User (id=" + id + ") made friend-request to user (id=" + friendId + ")");
        return user1;
    }

    public User deleteFriend(int id, int friendId) {
        Map<String, User> users = getTwoUsers(id, friendId);
        User user1 = users.get("user1");
        User user2 = users.get("user2");
        user1.getFriends().remove(user2);
        userStorage.updateUser(user1);
        log.info("User (id=" + id + ") canceled friend-request to user (id=" + friendId + ")");
        return user1;
    }

    private Map<String, User> getTwoUsers(int id_1, int id_2) {
        Map<Integer, User> users = userStorage.getUsers();
        if (!users.containsKey(id_1))
            throw new ObjectNotFoundException("Пользователь с id=" + id_1 + " не найден");
        if (!users.containsKey(id_2))
            throw new ObjectNotFoundException("Пользователь с id=" + id_2 + " не найден");
        User user1 = users.get(id_1);
        User user2 = users.get(id_2);

        Map<String, User> result = new HashMap<>();
        result.put("user1", user1);
        result.put("user2", user2);

        return result;

    }
}