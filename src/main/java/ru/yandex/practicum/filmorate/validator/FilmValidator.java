package ru.yandex.practicum.filmorate.validator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Component
public class FilmValidator implements Validator<Film> {

    //Введем дату-ограничение, раньше которой нельзя ввести фильм
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public void validate(Film film) {
        if (film.getDescription().length() > 200)
            throw new ValidationException("Описание фильма не должно быть больше 200 символов");
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY))
            throw new ValidationException("Дата релиза фильма не должно быть раньше 28 декабря 1895 года");
        if (film.getDuration() <= 0)
            throw new ValidationException("Длительность фильма должна быть положительной");
    }
}
