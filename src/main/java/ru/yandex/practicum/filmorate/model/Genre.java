package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class Genre {
    private final int id;
    @Nullable
    private final String name;
}
