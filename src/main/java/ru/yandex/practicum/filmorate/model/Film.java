package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

@Data
@AllArgsConstructor
public class Film {
    private int id;
    @NotBlank
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final LocalDate releaseDate;
    @NotNull
    private final int duration; //Я указывал класс Duration, но тесты в Постмане требуют целое значение :(
    @EqualsAndHashCode.Exclude
    private final Set<User> followers = new TreeSet<>(new Comparator<User>() {
        @Override
        public int compare(User u1, User u2) {
            return u1.getId() - u2.getId();
        }
    });
    @NotNull
    @EqualsAndHashCode.Exclude
    private final Mpa mpa;
    @EqualsAndHashCode.Exclude
    private final Set<Genre> genres = new TreeSet<>(new Comparator<Genre>() {
        @Override
        public int compare(Genre g1, Genre g2) {
            return g1.getId() - g2.getId();
        }
    });
}
