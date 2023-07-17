package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class User {
    private int id;
    @NotNull
    @Email
    private final String email;
    @NotBlank
    private final String login;
    private String name;
    @NotNull
    private final LocalDate birthday;
    @EqualsAndHashCode.Exclude
    private final Set<User> friends = new HashSet<>();
}
