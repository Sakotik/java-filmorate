package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("{id}")
    public Film getFilm(@PathVariable int id) {
        return filmService.getFilm(id);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam Optional<Integer> count) {
        return filmService.getPopularFilms(count);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public Film likeFilm(@PathVariable int filmId, @PathVariable int userId) {
        return filmService.like(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public Film dislikeFilm(@PathVariable int filmId, @PathVariable int userId) {
        return filmService.dislike(filmId, userId);
    }
}
