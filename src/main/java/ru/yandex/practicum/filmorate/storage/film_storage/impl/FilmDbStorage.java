package ru.yandex.practicum.filmorate.storage.film_storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film_storage.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@Qualifier("dbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmMapper = new RowMapper<Film>() {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            int fId = rs.getInt("F_ID");
            String fName = rs.getString("F_NAME");
            String description = rs.getString("DESCRIPTION");
            LocalDate releaseDate = rs.getDate("RELEASE_DATE").toLocalDate();
            int duration = rs.getInt("DURATION");
            int mId = rs.getInt("M_ID");
            String mName = rs.getString("M_NAME");

            Mpa mpa = new Mpa(mId, mName);
            return new Film(fId, fName, description, releaseDate, duration, mpa);
        }
    };

    private final RowMapper<Genre> genreMapper = new RowMapper<Genre>() {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt("GENRE_ID");
            String name = rs.getString("NAME");
            return new Genre(id, name);
        }
    };

    private final RowMapper<Mpa> mpaMapper = new RowMapper<Mpa>() {
        @Override
        public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt("MPA_ID");
            String name = rs.getString("NAME");
            return new Mpa(id, name);
        }
    };

    private final RowMapper<User> userMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt("USER_ID");
            String email = rs.getString("EMAIL");
            String login = rs.getString("LOGIN");
            String name = rs.getString("NAME");
            LocalDate birthday = rs.getDate("BIRTHDAY").toLocalDate();

            return new User(id, email, login, name, birthday);
        }
    };

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) {
        String createFilmSql = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) " +
                "VALUES (?, ?, ?, ?, ?);";
        jdbcTemplate.update(createFilmSql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId());

        film.setId(getId(film));

        for (Genre genre : film.getGenres()) {
            String updateFilmGenreSql = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) " +
                    "VALUES (?, ?)";
            jdbcTemplate.update(updateFilmGenreSql, film.getId(), genre.getId());
        }
        log.info("Created " + film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!check(film.getId()))
            throw new ObjectNotFoundException("Фильм с id=" + film.getId() + " не найден");

        String updateFilmSql = "UPDATE FILMS " +
                "SET NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_ID=? " +
                "WHERE FILM_ID=?;";
        jdbcTemplate.update(updateFilmSql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());

        String deleteFilmLikes = "DELETE FROM FILM_FOLLOWER " +
                "WHERE FILM_ID=?"; //Очищаем старые лайки
        jdbcTemplate.update(deleteFilmLikes, film.getId());
        for (User follower : film.getFollowers()) { //Обновляем запросы в друзья
            String updateFilmLikes = "INSERT INTO FILM_FOLLOWER (FILM_ID, FOLLOWER_ID) " +
                    "VALUES (?, ?);";
            jdbcTemplate.update(updateFilmLikes, film.getId(), follower.getId());
        }

        String deleteFilmGenresSql = "DELETE FROM FILM_GENRE " +
                "WHERE FILM_ID=?;";
        jdbcTemplate.update(deleteFilmGenresSql, film.getId()); //Очищаем старые жанры
        for (Genre genre : film.getGenres()) {
            String updateFilmGenres = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) " +
                    "VALUES (?, ?);";
            jdbcTemplate.update(updateFilmGenres, film.getId(), genre.getId());
        }
        log.info("Updated " + film);
        return getFilms().get(film.getId());
    }

    @Override
    public Map<Integer, Genre> getGenres() {
        String sql = "SELECT GENRE_ID, " +
                "NAME " +
                "FROM GENRE;";
        List<Genre> genres = jdbcTemplate.query(sql, genreMapper);
        log.info("Sent all genres");
        return genres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));
    }

    @Override
    public Map<Integer, Mpa> getMpa() {
        String sql = "SELECT * FROM MPA";
        List<Mpa> mpa = jdbcTemplate.query(sql, mpaMapper);
        log.info("Sent all mpa");
        return mpa.stream()
                .collect(Collectors.toMap(Mpa::getId, Function.identity()));
    }

    @Override
    public Map<Integer, Film> getFilms() {
        String getFilmsSql = "SELECT F.FILM_ID AS F_ID, " +
                "F.NAME AS F_NAME, " +
                "F.DESCRIPTION AS DESCRIPTION, " +
                "F.RELEASE_DATE AS RELEASE_DATE, " +
                "F.DURATION AS DURATION, " +
                "M.MPA_ID AS M_ID, " +
                "M.NAME AS M_NAME " +
                "FROM FILMS AS F " +
                "INNER JOIN MPA AS M ON F.MPA_ID = M.MPA_ID";
        List<Film> films = jdbcTemplate.query(getFilmsSql, filmMapper);
        for (Film film : films) {
            int filmId = film.getId();

            String getGenresSql = "SELECT G.GENRE_ID, " +
                    "G.NAME " +
                    "FROM GENRE AS G " +
                    "INNER JOIN FILM_GENRE AS FG ON G.GENRE_ID=FG.GENRE_ID " +
                    "WHERE FG.FILM_ID=?";
            List<Genre> genres = jdbcTemplate.query(getGenresSql, genreMapper, filmId);
            film.getGenres().addAll(genres);

            String getFollowersSql = "SELECT U.USER_ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY " +
                    "FROM USERS AS U " +
                    "INNER JOIN FILM_FOLLOWER AS FF ON U.USER_ID=FF.FOLLOWER_ID " +
                    "WHERE FF.FILM_ID=?";
            List<User> followers = jdbcTemplate.query(getFollowersSql, userMapper, filmId);
            film.getFollowers().addAll(followers);
        }
        log.info("Sent all films");
        return films.stream().collect(Collectors.toMap(Film::getId, Function.identity()));
    }

    private int getId(Film film) {
        String sql = "SELECT FILM_ID " +
                "FROM FILMS " +
                "WHERE NAME=?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, film.getName());
        rs.next();
        return rs.getInt("FILM_ID");
    }

    //Метод по проверке наличия записи в БД
    private boolean check(int id) {
        String getFilmsSql = "SELECT F.FILM_ID AS F_ID, " +
                "F.NAME AS F_NAME, " +
                "F.DESCRIPTION AS DESCRIPTION, " +
                "F.RELEASE_DATE AS RELEASE_DATE, " +
                "F.DURATION AS DURATION, " +
                "M.MPA_ID AS M_ID, " +
                "M.NAME AS M_NAME " +
                "FROM FILMS AS F " +
                "INNER JOIN MPA AS M ON F.MPA_ID = M.MPA_ID " +
                "WHERE FILM_ID=?";
        List<Film> film = jdbcTemplate.query(getFilmsSql, filmMapper, id);
        return film.size() == 1;
    }
}
