package ru.yandex.practicum.filmorate.validator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Component
public class UserValidator implements Validator<User> {

    @Override
    public void validate(User user) {
        if (user.getLogin().contains(" "))
            throw new ValidationException("Логин пользователя не должен содержать пробелов");
        if (user.getBirthday().isAfter(LocalDate.now()))
            throw new ValidationException("День рождения пользователя не должен быть в будущем");
        //Это участок кода отвечает за выдачу имени пользователю при отсутствии имени
        if (user.getName() == null || user.getName().isBlank())
            user.setName(user.getLogin());
    }

}
