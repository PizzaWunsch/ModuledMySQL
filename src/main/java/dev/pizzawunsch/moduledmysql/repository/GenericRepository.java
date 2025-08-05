package dev.pizzawunsch.moduledmysql.repository;

import dev.pizzawunsch.moduledmysql.annotations.ColumnName;
import dev.pizzawunsch.moduledmysql.annotations.TableName;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Generic repository for basic CRUD operations on annotated entity classes.
 * <p>
 * Supports synchronous and asynchronous persistence logic by using Java Reflection and custom annotations.
 * Classes must be annotated with {@link TableName} and fields with {@link ColumnName}.
 */
public class GenericRepository {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Asynchronously saves the given entity to the database using REPLACE INTO.
     *
     * @param entity     the entity to save
     * @param connection the active JDBC connection
     * @param <T>        the type of the entity
     * @return a CompletableFuture that completes when the save is done
     */
    public static <T> CompletableFuture<Void> saveAsync(T entity, Connection connection) {
        return CompletableFuture.runAsync(() -> {
            try {
                save(entity, connection);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Asynchronously loads an entity by its primary key.
     *
     * @param clazz            the class type of the entity
     * @param primaryKeyValue  the primary key value
     * @param connection       the active JDBC connection
     * @param <T>              the type of the entity
     * @return a CompletableFuture containing the loaded entity or null
     */
    public static <T> CompletableFuture<T> loadAsync(Class<T> clazz, Object primaryKeyValue, Connection connection) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return load(clazz, primaryKeyValue, connection);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Asynchronously deletes an entity by its primary key.
     *
     * @param clazz            the class type of the entity
     * @param primaryKeyValue  the primary key value
     * @param connection       the active JDBC connection
     * @param <T>              the type of the entity
     * @return a CompletableFuture that completes when deletion is done
     */
    public static <T> CompletableFuture<Void> deleteAsync(Class<T> clazz, Object primaryKeyValue, Connection connection) {
        return CompletableFuture.runAsync(() -> {
            try {
                delete(clazz, primaryKeyValue, connection);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Saves the given entity using REPLACE INTO. Fields are mapped from @ColumnName annotations.
     *
     * @param entity     the entity to persist
     * @param connection the JDBC connection
     * @param <T>        the entity type
     * @throws SQLException if any database error occurs
     */
    @Deprecated
    public static <T> void save(T entity, Connection connection) throws SQLException {
        Class<?> clazz = entity.getClass();
        String table = getTableName(clazz);

        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ColumnName.class)) {
                ColumnName column = field.getAnnotation(ColumnName.class);
                columnNames.add(column.value());

                field.setAccessible(true);
                try {
                    values.add(field.get(entity));
                } catch (IllegalAccessException e) {
                    System.err.println("[Moduled - MySQL] Failed to access field value: " + field.getName());
                    throw new RuntimeException(e);
                }
            }
        }

        if (columnNames.isEmpty()) {
            throw new IllegalArgumentException("[Moduled - MySQL] No @ColumnName-annotated fields in class: " + clazz.getSimpleName());
        }

        String columns = String.join(", ", columnNames);
        String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
        String sql = "REPLACE INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            int rowsAffected = stmt.executeUpdate();
            System.out.println("[Moduled - MySQL] " + rowsAffected + " row(s) inserted/updated in table: " + table);
        }
    }

    /**
     * Loads an entity by primary key value.
     *
     * @param clazz            the entity class
     * @param primaryKeyValue  the primary key to look up
     * @param connection       the JDBC connection
     * @param <T>              the entity type
     * @return the loaded entity instance or null if not found
     * @throws SQLException if SQL execution fails
     */
    @Deprecated
    public static <T> T load(Class<T> clazz, Object primaryKeyValue, Connection connection) throws SQLException {
        String table = getTableName(clazz);
        Field pkField = getPrimaryKeyField(clazz);
        ColumnName column = pkField.getAnnotation(ColumnName.class);
        String pkColumn = column.value();

        String sql = "SELECT * FROM " + table + " WHERE " + pkColumn + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKeyValue);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T instance = clazz.getDeclaredConstructor().newInstance();

                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(ColumnName.class)) {
                            ColumnName col = field.getAnnotation(ColumnName.class);
                            field.setAccessible(true);
                            Object value = rs.getObject(col.value());
                            field.set(instance, value);
                        }
                    }

                    return instance;
                }
            } catch (ReflectiveOperationException e) {
                System.err.println("[Moduled - MySQL] Failed to instantiate entity: " + clazz.getSimpleName());
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * Deletes an entity row by its primary key.
     *
     * @param clazz            the entity class
     * @param primaryKeyValue  the key to delete
     * @param connection       the JDBC connection
     * @param <T>              the entity type
     * @throws SQLException if deletion fails
     */
    @Deprecated
    public static <T> void delete(Class<T> clazz, Object primaryKeyValue, Connection connection) throws SQLException {
        String table = getTableName(clazz);
        Field pkField = getPrimaryKeyField(clazz);
        ColumnName column = pkField.getAnnotation(ColumnName.class);
        String pkColumn = column.value();

        String sql = "DELETE FROM " + table + " WHERE " + pkColumn + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKeyValue);
            stmt.executeUpdate();
            System.out.println("[Moduled - MySQL] Entity deleted from table: " + table + ", PK: " + primaryKeyValue);
        }
    }

    /**
     * Retrieves the table name from the class's {@link TableName} annotation.
     *
     * @param clazz the entity class
     * @return the table name
     * @throws IllegalArgumentException if the class lacks the @TableName annotation
     */
    private static String getTableName(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(TableName.class)) {
            throw new IllegalArgumentException("[Moduled - MySQL] Missing @TableName annotation on class: " + clazz.getSimpleName());
        }
        return clazz.getAnnotation(TableName.class).value();
    }

    /**
     * Returns the field marked as the primary key via {@link ColumnName#primaryKey()}.
     *
     * @param clazz the entity class
     * @return the field marked as primary key
     * @throws IllegalArgumentException if no primary key is defined
     */
    private static Field getPrimaryKeyField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ColumnName.class)) {
                ColumnName column = field.getAnnotation(ColumnName.class);
                if (column.primaryKey()) {
                    return field;
                }
            }
        }
        throw new IllegalArgumentException("[Moduled - MySQL] No primary key defined in class: " + clazz.getSimpleName());
    }

    /**
     * Shuts down the async executor. Should be called on application shutdown.
     */
    public static void shutdown() {
        executor.shutdown();
        System.out.println("[Moduled - MySQL] Executor service shut down.");
    }
}
