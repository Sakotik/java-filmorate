package ru.yandex.practicum.filmorate.storage.film_storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Map;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    Map<Integer, Film> getFilms();

    Map<Integer, Genre> getGenres();

    Map<Integer, Mpa> getMpa();
}
