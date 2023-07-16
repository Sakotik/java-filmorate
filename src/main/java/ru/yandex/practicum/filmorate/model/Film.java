package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;
    @NotBlank
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final LocalDate releaseDate;
    private final int duration; //Я указывала класс Duration, но тесты в Постмане требуют целое значение :(
    @EqualsAndHashCode.Exclude
    private final Set<Integer> fans = new HashSet<>();
}