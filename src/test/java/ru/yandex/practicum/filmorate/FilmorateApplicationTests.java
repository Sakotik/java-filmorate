package ru.yandex.practicum.filmorate;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@AllArgsConstructor(onConstructor_ = @Autowired)
public class FilmorateApplicationTests {

    private final UserService userService;
    private final FilmService filmService;

    @Test
    public void testFindUserById() {
        User testUser = new User(0, "1@yandex.ru", "1", "test",
                LocalDate.of(1999, 12, 1));
        userService.createUser(testUser);

        Optional<User> userOptional = Optional.of(userService.getUser(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    public void testUpdateUser() {
        User oldUser = new User(0, "2@yandex.ru", "2", "test",
                LocalDate.of(1999, 12, 1));
        User newUser = new User(1, "3@yandex.ru", "3", "new",
                LocalDate.of(1999, 12, 1));

        userService.createUser(oldUser);
        userService.updateUser(newUser);

        Optional<User> userOptional = Optional.of(userService.getUser(1));
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("login", "3"));
    }

    @Test
    public void testSendFriendRequest() {
        User user1 = new User(0, "4@yandex.ru", "4", "1",
                LocalDate.of(1999, 12, 1));
        User user2 = new User(0, "5@yandex.ru", "5", "2",
                LocalDate.of(1999, 12, 1));

        userService.createUser(user1);
        userService.createUser(user2);
        userService.addFriend(1, 2);

        Optional<User> user1Optional = Optional.of(userService.getUser(1));
        Optional<User> user2Optional = Optional.of(userService.getUser(2));
        assertThat(user1Optional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getFriends()).first()
                        .hasFieldOrPropertyWithValue("login", "5"));
        assertThat(user2Optional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getFriends()).isEmpty());
    }

    @Test
    public void testFindFilmById() {
        Film testFilm = new Film(0, "1", "test", LocalDate.now(), 100,
                new Mpa(1, null));
        filmService.createFilm(testFilm);

        Optional<Film> filmOptional = Optional.of(filmService.getFilm(1));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 1));
    }

    @Test
    public void testUpdateFilm() {
        Film oldFilm = new Film(0, "1", "test", LocalDate.now(), 100,
                new Mpa(1, null));
        Film newFilm = new Film(1, "new", "new", LocalDate.now(), 100,
                new Mpa(1, null));

        filmService.createFilm(oldFilm);
        filmService.updateFilm(newFilm);

        Optional<Film> filmOptional = Optional.of(filmService.getFilm(1));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("name", "new"));
    }

    @Test
    public void testLikeFilm() {
        User testUser = new User(0, "6@yandex.ru", "test", "test",
                LocalDate.of(1999, 12, 1));
        Film testFilm = new Film(0, "test", "test", LocalDate.now(), 100,
                new Mpa(1, null));

        userService.createUser(testUser);
        filmService.createFilm(testFilm);
        filmService.like(1, 1);

        Optional<Film> filmOptional = Optional.of(filmService.getFilm(1));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film.getFollowers()).first()
                        .hasFieldOrPropertyWithValue("id", 1));
    }

}
