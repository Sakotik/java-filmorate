package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user_storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user_storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final Validator<User> userValidator;

    @Autowired
    public UserService(InMemoryUserStorage userStorage, UserValidator userValidator) {
        this.userStorage = userStorage;
        this.userValidator = userValidator;
    }

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
            throw new ObjectNotFoundException("User with id=" + id + " not found");
        log.info("Sent all user's (id=" + id + ") friends");
        return users.get(id).getFriends()
                .stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        Map<String, User> users = getTwoUsers(id, otherId);
        Set<Integer> commonFriends = new HashSet<>(users.get("user1").getFriends());
        commonFriends.retainAll(users.get("user2").getFriends());
        log.info("Sent users' (id=" + id + "/" + otherId + ") common friends");
        return commonFriends
                .stream()
                .map(userStorage.getUsers()::get)
                .collect(Collectors.toList());
    }

    public User addFriend(int id, int friendId) {
        Map<String, User> users = getTwoUsers(id, friendId);
        users.get("user1").getFriends().add(friendId);
        users.get("user2").getFriends().add(id);
        log.info("Users (id=" + id + "/" + friendId + ") made friend relationships");
        return users.get("user1");
    }

    public User deleteFriend(int id, int friendId) {
        Map<String, User> users = getTwoUsers(id, friendId);
        users.get("user1").getFriends().remove(friendId);
        users.get("user2").getFriends().remove(id);
        log.info("Users (id=" + id + "/" + friendId + ") broke friend relationships");
        return users.get("user1");
    }

    private Map<String, User> getTwoUsers(int id1, int id2) {
        Map<Integer, User> users = userStorage.getUsers();
        if (!users.containsKey(id1))
            throw new ObjectNotFoundException("User with id=" + id1 + " not found");
        if (!users.containsKey(id2))
            throw new ObjectNotFoundException("User with id=" + id2 + " not found");
        User user1 = users.get(id1);
        User user2 = users.get(id2);

        Map<String, User> result = new HashMap<>();
        result.put("user1", user1);
        result.put("user2", user2);

        return result;
    }
}