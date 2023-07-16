package ru.yandex.practicum.filmorate.storage.film_storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 0;

    @Override
    public Film createFilm(Film film) {
        idCounter++;
        film.setId(idCounter);
        films.put(idCounter, film);
        log.info("Created " + film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        log.info("Updated " + film);
        return film;
    }

    @Override
    public Map<Integer, Film> getFilms() {
        log.info("Sent all films");
        return films;
    }
}
