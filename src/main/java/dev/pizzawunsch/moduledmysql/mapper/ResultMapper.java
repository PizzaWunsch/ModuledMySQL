package dev.pizzawunsch.moduledmysql.mapper;

import dev.pizzawunsch.moduledmysql.annotations.ColumnName;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code ResultMapper} is a utility class that maps rows from a {@link ResultSet}
 * into instances of Java classes using the {@link ColumnName} annotation.
 * <p>
 * It supports mapping both single rows and full result sets into typed objects,
 * making it easy to convert JDBC results into domain models without writing repetitive code.
 * </p>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * ResultSet rs = preparedStatement.executeQuery();
 * if (rs.next()) {
 *     User user = ResultMapper.mapRow(rs, User.class);
 * }
 *
 * // or to map all rows
 * List<User> users = ResultMapper.mapAll(rs, User.class);
 * }</pre>
 *
 * <p>
 * The target class must be annotated with {@code @ColumnName} on its fields,
 * and must have a public no-argument constructor.
 * </p>
 *
 * @author Lucas | PizzaWunsch
 */
public class ResultMapper {

    /**
     * Maps a single row of the given {@link ResultSet} into an instance of the specified class.
     * <p>
     * This method expects the {@code resultSet} to already point to a valid row
     * (i.e., {@code resultSet.next()} has already been called).
     * It uses reflection to instantiate the class and populate fields annotated with {@link ColumnName}.
     * </p>
     *
     * @param resultSet the JDBC {@link ResultSet}, positioned at a valid row
     * @param clazz     the class to map the row to (must have a public no-arg constructor)
     * @param <T>       the generic type corresponding to {@code clazz}
     * @return an instance of {@code T} with fields populated from the result set
     * @throws SQLException if instantiation or field access fails
     *
     * @see ColumnName
     */
    public static <T> T mapRow(ResultSet resultSet, Class<T> clazz) throws SQLException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                ColumnName column = field.getAnnotation(ColumnName.class);
                if (column != null) {
                    field.setAccessible(true);
                    Object value = resultSet.getObject(column.value());
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new SQLException("Failed to map ResultSet to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Maps all rows of a {@link ResultSet} into a {@link List} of instances of the specified class.
     * <p>
     * This method iterates through the entire result set and maps each row using {@link #mapRow(ResultSet, Class)}.
     * The result is a list of typed objects, each representing a row from the result set.
     * </p>
     *
     * @param rs    the JDBC {@link ResultSet} to map (positioned before the first row)
     * @param clazz the class to map each row to (must have a public no-arg constructor)
     * @param <T>   the generic type corresponding to {@code clazz}
     * @return a {@link List} of mapped objects of type {@code T}
     * @throws SQLException if reading or mapping rows fails
     *
     * @see #mapRow(ResultSet, Class)
     */
    public static <T> List<T> mapAll(ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs, clazz));
        }
        return result;
    }
}
