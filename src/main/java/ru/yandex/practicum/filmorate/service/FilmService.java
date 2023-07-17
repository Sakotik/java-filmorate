package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film_storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user_storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    @Autowired
    @Qualifier("dbStorage")
    private FilmStorage filmStorage;
    @Autowired
    @Qualifier("dbStorage")
    private UserStorage userStorage;
    @Autowired
    private Validator<Film> filmValidator;

    public Film createFilm(Film film) {
        filmValidator.validate(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.getFilms().containsKey(film.getId()))
            throw new ObjectNotFoundException("Film with id=" + film.getId() + " not found");
        filmValidator.validate(film);
        return filmStorage.updateFilm(film);
    }

    public Film getFilm(int id) {
        Map<Integer, Film> films = filmStorage.getFilms();
        if (!films.containsKey(id))
            throw new ObjectNotFoundException("Film with id=" + id + " not found");
        log.info("Sent film with id=" + id);
        return films.get(id);
    }

    public List<Film> getFilms() {
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    public List<Film> getPopularFilms(Optional<Integer> count) {
        Collection<Film> films = filmStorage.getFilms().values();
        log.info("Sent " + (count.isPresent() ? count.get() : 10) + " popular films");
        return films.stream()
                .sorted((film1, film2) -> film2.getFollowers().size() - film1.getFollowers().size())
                .limit(count.isPresent() ? count.get() : 10)
                .collect(Collectors.toList());
    }

    public Film like(int filmId, int userId) {
        Map<String, Object> filmAndUser = getFilmUser(filmId, userId);
        Film film = (Film) filmAndUser.get("film");
        User user = (User) filmAndUser.get("user");
        if (!film.getFollowers().contains(user))   //Проверка на наличие лайка
            film.getFollowers().add(user);
        filmStorage.updateFilm(film);
        log.info("User (id=" + userId + ") liked film (id=" + filmId + ")");
        return film;
    }

    public Film dislike(int filmId, int userId) {
        Map<String, Object> filmAndUser = getFilmUser(filmId, userId);
        Film film = (Film) filmAndUser.get("film");
        User user = (User) filmAndUser.get("user");

        if (film.getFollowers().contains(user)) //Проверка на наличие лайка
            film.getFollowers().remove(user);
        log.info("User (id=" + userId + ") disliked film (id=" + filmId + ")");
        return film;
    }

    public Genre getGenre(int id) {
        Map<Integer, Genre> genres = filmStorage.getGenres();
        if (!genres.containsKey(id))
            throw new ObjectNotFoundException("Жанр с id=" + id + " не найден");
        return genres.get(id);
    }

    public Map<Integer, Genre> getGenres() {
        return filmStorage.getGenres();
    }

    public Mpa getMpa(int id) {
        Map<Integer, Mpa> mpa = filmStorage.getMpa();
        if (!mpa.containsKey(id))
            throw new ObjectNotFoundException("Рейтинг с id=" + id + " не найден");
        return mpa.get(id);
    }

    public Map<Integer, Mpa> getAllMpa() {
        return filmStorage.getMpa();
    }

    private void checkContains(int filmId, int userId) {
        if (!filmStorage.getFilms().containsKey(filmId))
            throw new ObjectNotFoundException("Film with id=" + filmId + " not found");
        if (!userStorage.getUsers().containsKey(userId))
            throw new ObjectNotFoundException("User with id=" + userId + " not found");
    }

    private Map<String, Object> getFilmUser(int filmId, int userId) {
        checkContains(filmId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("film", filmStorage.getFilms().get(filmId));
        result.put("user", userStorage.getUsers().get(userId));
        return result;
    }
}
