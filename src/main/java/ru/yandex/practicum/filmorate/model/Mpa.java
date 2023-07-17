package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class Mpa {
    private final int id;
    @Nullable
    private final String name;
}
