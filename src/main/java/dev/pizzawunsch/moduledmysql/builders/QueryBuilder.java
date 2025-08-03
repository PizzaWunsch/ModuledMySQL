package dev.pizzawunsch.moduledmysql.builders;

import dev.pizzawunsch.moduledmysql.annotations.ColumnName;
import dev.pizzawunsch.moduledmysql.annotations.TableName;
import lombok.Getter;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * QueryBuilder is a utility class that facilitates dynamic SQL query construction.
 * It leverages Java reflection and custom annotations to map class fields to database
 * tables and columns. The builder supports complex queries including SELECT, INSERT,
 * UPDATE, DELETE, JOINs, subqueries, GROUP BY, HAVING, IN clauses, and UNION operations.
 * <p>
 * Annotations used:
 * <ul>
 *   <li>{@link TableName} to define the database table name for a class.</li>
 *   <li>{@link ColumnName} to define column names and metadata on fields.</li>
 * </ul>
 * <p>
 * The builder supports aliasing of tables to allow disambiguation in complex queries
 * and ensures safe parameter binding to prevent SQL injection.
 * </p>
 */
public class QueryBuilder {

    private final StringBuilder query = new StringBuilder();

    /**
     * Parameters bound to the prepared statement placeholders in the query.
     */
    @Getter
    private final List<Object> parameters = new ArrayList<>();

    private final Map<Class<?>, String> aliasMap = new HashMap<>();
    private int aliasCounter = 0;

    private QueryBuilder() {
        // Private constructor to enforce usage of static factory methods.
    }

    /**
     * Creates a SELECT query with the specified columns.
     *
     * @param columns Columns to select.
     * @return A new QueryBuilder instance for building the SELECT query.
     */
    public static QueryBuilder select(String... columns) {
        QueryBuilder builder = new QueryBuilder();
        builder.query.append("SELECT ").append(String.join(", ", columns));
        return builder;
    }

    /**
     * Creates a SELECT query with columns from the annotated class fields.
     * If no columns are specified, all annotated columns are selected with an alias.
     *
     * @param clazz   Class annotated with {@link TableName} and {@link ColumnName}.
     * @param columns Optional columns to select; if none, selects all.
     * @return A new QueryBuilder instance.
     */
    public static QueryBuilder select(Class<?> clazz, String... columns) {
        QueryBuilder builder = new QueryBuilder();
        String alias = builder.getAlias(clazz);
        if (columns.length == 0) {
            columns = builder.getAllColumnNames(clazz).stream()
                    .map(col -> alias + "." + col)
                    .toArray(String[]::new);
        }
        builder.query.append("SELECT ").append(String.join(", ", columns));
        return builder;
    }

    /**
     * Builds an INSERT query for the given class instance using reflection.
     * Columns annotated with {@link ColumnName#autoIncrement()} = true are skipped.
     *
     * @param clazz    Class representing the database table.
     * @param instance Instance of the class with values to insert.
     * @return A new QueryBuilder with the INSERT statement prepared.
     * @throws IllegalStateException If field values are not accessible.
     */
    public static QueryBuilder insertInto(Class<?> clazz, Object instance) {
        QueryBuilder builder = new QueryBuilder();
        String table = builder.getTableName(clazz);

        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            ColumnName col = field.getAnnotation(ColumnName.class);
            if (col != null && !col.autoIncrement()) {
                columns.add(col.value());
                field.setAccessible(true);
                try {
                    Object value = field.get(instance);
                    builder.parameters.add(value);
                    placeholders.add("?");
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot access field value for insert: " + field.getName(), e);
                }
            }
        }

        builder.query.append("INSERT INTO ").append(table)
                .append(" (").append(String.join(", ", columns)).append(") VALUES (")
                .append(String.join(", ", placeholders)).append(")");

        return builder;
    }

    /**
     * Builds an UPDATE query for the given class instance.
     * Fields annotated with {@link ColumnName#primaryKey()} = true are used in WHERE clause.
     *
     * @param clazz    Class representing the table.
     * @param instance Instance containing updated values.
     * @return A new QueryBuilder with the UPDATE statement prepared.
     * @throws IllegalStateException If no primary key is defined or field values inaccessible.
     */
    public static QueryBuilder update(Class<?> clazz, Object instance) {
        QueryBuilder builder = new QueryBuilder();
        String table = builder.getTableName(clazz);
        builder.query.append("UPDATE ").append(table).append(" SET ");

        List<String> sets = new ArrayList<>();
        String whereClause = null;

        for (Field field : clazz.getDeclaredFields()) {
            ColumnName col = field.getAnnotation(ColumnName.class);
            if (col != null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(instance);
                    if (col.primaryKey()) {
                        whereClause = col.value() + " = ?";
                        builder.parameters.add(value);
                    } else {
                        sets.add(col.value() + " = ?");
                        builder.parameters.add(value);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot access field value for update: " + field.getName(), e);
                }
            }
        }

        if (whereClause == null) {
            throw new IllegalStateException("No primary key column defined for update in class " + clazz.getName());
        }

        builder.query.append(String.join(", ", sets));
        builder.query.append(" WHERE ").append(whereClause);

        return builder;
    }

    /**
     * Builds a DELETE query for the given class and primary key value.
     *
     * @param clazz           Class annotated with {@link TableName} and {@link ColumnName}.
     * @param primaryKeyValue Value of the primary key for the WHERE clause.
     * @return A new QueryBuilder with the DELETE statement prepared.
     * @throws IllegalStateException If no primary key column is defined.
     */
    public static QueryBuilder deleteFrom(Class<?> clazz, Object primaryKeyValue) {
        QueryBuilder builder = new QueryBuilder();
        String table = builder.getTableName(clazz);
        String primaryKeyColumn = builder.getPrimaryKeyColumn(clazz);

        builder.query.append("DELETE FROM ").append(table).append(" WHERE ")
                .append(primaryKeyColumn).append(" = ?");
        builder.parameters.add(primaryKeyValue);

        return builder;
    }

    /**
     * Adds a FROM clause specifying the table corresponding to the given class,
     * appending the generated alias.
     *
     * @param clazz Class annotated with {@link TableName}.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder from(Class<?> clazz) {
        String table = getTableName(clazz);
        String alias = getAlias(clazz);
        query.append(" FROM ").append(table).append(" ").append(alias);
        return this;
    }

    /**
     * Adds a FROM clause with a raw table name.
     *
     * @param tableName The table name to add.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder from(String tableName) {
        query.append(" FROM ").append(tableName);
        return this;
    }

    /**
     * Builds a DELETE query for a given table name and primary key column.
     *
     * @param tableName        Name of the database table.
     * @param primaryKeyColumn Name of the primary key column.
     * @param primaryKeyValue  Value of the primary key.
     * @return A new QueryBuilder with the DELETE statement prepared.
     */
    public static QueryBuilder deleteFrom(String tableName, String primaryKeyColumn, Object primaryKeyValue) {
        QueryBuilder builder = new QueryBuilder();
        builder.query.append("DELETE FROM ").append(tableName).append(" WHERE ")
                .append(primaryKeyColumn).append(" = ?");
        builder.parameters.add(primaryKeyValue);
        return builder;
    }

    /**
     * Builds an UPDATE query from table name, primary key column and value,
     * and a map of columns to update.
     *
     * @param tableName        Name of the table to update.
     * @param primaryKeyColumn Name of the primary key column.
     * @param primaryKeyValue  Value of the primary key.
     * @param columnsToUpdate  Map of column names to new values.
     * @return A new QueryBuilder with the UPDATE statement prepared.
     */
    public static QueryBuilder update(String tableName, String primaryKeyColumn, Object primaryKeyValue, Map<String, Object> columnsToUpdate) {
        QueryBuilder builder = new QueryBuilder();
        builder.query.append("UPDATE ").append(tableName).append(" SET ");

        List<String> sets = new ArrayList<>();
        for (Map.Entry<String, Object> entry : columnsToUpdate.entrySet()) {
            sets.add(entry.getKey() + " = ?");
            builder.parameters.add(entry.getValue());
        }

        builder.query.append(String.join(", ", sets))
                .append(" WHERE ").append(primaryKeyColumn).append(" = ?");
        builder.parameters.add(primaryKeyValue);
        return builder;
    }

    /**
     * Builds an INSERT query for the given table name and columns with values.
     *
     * @param tableName         Name of the table.
     * @param columnsAndValues  Map of column names and corresponding values.
     * @return A new QueryBuilder with the INSERT statement prepared.
     */
    public static QueryBuilder insertInto(String tableName, Map<String, Object> columnsAndValues) {
        QueryBuilder builder = new QueryBuilder();
        List<String> columns = new ArrayList<>(columnsAndValues.keySet());
        List<String> placeholders = Collections.nCopies(columns.size(), "?");

        builder.query.append("INSERT INTO ").append(tableName)
                .append(" (").append(String.join(", ", columns)).append(") VALUES (")
                .append(String.join(", ", placeholders)).append(")");

        builder.parameters.addAll(columnsAndValues.values());
        return builder;
    }

    /**
     * Adds an INNER JOIN clause using the table and alias for the specified class.
     *
     * @param clazz       Class annotated with {@link TableName}.
     * @param onCondition SQL condition for the ON clause.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder join(Class<?> clazz, String onCondition) {
        return appendJoin("JOIN", clazz, onCondition);
    }

    /**
     * Adds a LEFT JOIN clause using the table and alias for the specified class.
     *
     * @param clazz       Class annotated with {@link TableName}.
     * @param onCondition SQL condition for the ON clause.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder leftJoin(Class<?> clazz, String onCondition) {
        return appendJoin("LEFT JOIN", clazz, onCondition);
    }

    private QueryBuilder appendJoin(String joinType, Class<?> clazz, String onCondition) {
        String table = getTableName(clazz);
        String alias = getAlias(clazz);
        query.append(" ").append(joinType).append(" ").append(table).append(" ").append(alias)
                .append(" ON ").append(onCondition);
        return this;
    }

    /**
     * Adds a WHERE clause with the specified condition and parameter values.
     *
     * @param condition SQL condition string with placeholders.
     * @param values    Parameter values to bind.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder where(String condition, Object... values) {
        query.append(" WHERE ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds a grouped WHERE clause from a {@link QueryGroup} instance.
     *
     * @param group Group of conditions to include in parentheses.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder whereGroup(QueryGroup group) {
        query.append(" WHERE (").append(group.getQuery()).append(")");
        parameters.addAll(group.getParameters());
        return this;
    }

    /**
     * Adds an AND condition to the query.
     *
     * @param condition SQL condition string with placeholders.
     * @param values    Parameter values to bind.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder and(String condition, Object... values) {
        query.append(" AND ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds an OR condition to the query.
     *
     * @param condition SQL condition string with placeholders.
     * @param values    Parameter values to bind.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder or(String condition, Object... values) {
        query.append(" OR ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds an IN clause for the specified column and values.
     *
     * @param column Column name for the IN clause.
     * @param values List of values to include.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder in(String column, List<Object> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values list for IN clause cannot be null or empty");
        }
        String placeholders = values.stream().map(v -> "?").collect(Collectors.joining(", "));
        query.append(" ").append(column).append(" IN (").append(placeholders).append(")");
        parameters.addAll(values);
        return this;
    }

    /**
     * Adds a GROUP BY clause with the specified columns.
     *
     * @param columns Columns to group by.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder groupBy(String... columns) {
        query.append(" GROUP BY ").append(String.join(", ", columns));
        return this;
    }

    /**
     * Adds a HAVING clause with the specified condition and parameters.
     *
     * @param condition HAVING condition string.
     * @param values    Parameter values to bind.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder having(String condition, Object... values) {
        query.append(" HAVING ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Combines this query with another using the UNION operator.
     *
     * @param other Another QueryBuilder instance representing the query to union.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder union(QueryBuilder other) {
        query.append(" UNION ").append(other.getQuery());
        parameters.addAll(other.getParameters());
        return this;
    }

    /**
     * Combines this query with another using the UNION ALL operator.
     *
     * @param other Another QueryBuilder instance representing the query to union all.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder unionAll(QueryBuilder other) {
        query.append(" UNION ALL ").append(other.getQuery());
        parameters.addAll(other.getParameters());
        return this;
    }

    /**
     * Adds a subquery with an alias.
     *
     * @param subquery The subquery builder instance.
     * @param alias    Alias for the subquery.
     * @return This QueryBuilder instance for chaining.
     */
    public QueryBuilder subquery(QueryBuilder subquery, String alias) {
        query.append(" (").append(subquery.getQuery()).append(") AS ").append(alias);
        parameters.addAll(subquery.getParameters());
        return this;
    }

    /**
     * Returns the built SQL query string.
     *
     * @return The SQL query string.
     */
    public String getQuery() {
        return query.toString();
    }

    /**
     * Extracts the table name from the class annotation or defaults to lowercase class name.
     *
     * @param clazz Class annotated with {@link TableName}.
     * @return Table name string.
     */
    private String getTableName(Class<?> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        return annotation != null ? annotation.value() : clazz.getSimpleName().toLowerCase(Locale.ROOT);
    }

    /**
     * Retrieves or assigns a unique alias for the given class.
     *
     * @param clazz The class for which to get the alias.
     * @return Alias string.
     */
    private String getAlias(Class<?> clazz) {
        return aliasMap.computeIfAbsent(clazz, c -> "t" + aliasCounter++);
    }

    /**
     * Collects all annotated column names for the given class.
     *
     * @param clazz Class to inspect.
     * @return List of column names.
     */
    private List<String> getAllColumnNames(Class<?> clazz) {
        List<String> columns = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            ColumnName annotation = field.getAnnotation(ColumnName.class);
            if (annotation != null) {
                columns.add(annotation.value());
            }
        }
        return columns;
    }

    /**
     * Finds the primary key column annotated in the class.
     *
     * @param clazz Class to inspect.
     * @return Primary key column name.
     * @throws IllegalStateException If no primary key is defined.
     */
    private String getPrimaryKeyColumn(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            ColumnName annotation = field.getAnnotation(ColumnName.class);
            if (annotation != null && annotation.primaryKey()) {
                return annotation.value();
            }
        }
        throw new IllegalStateException("No primary key column found in class " + clazz.getName());
    }

    /**
     * Prepares a {@link PreparedStatement} from the built query and parameters.
     *
     * @param connection JDBC connection.
     * @return PreparedStatement with bound parameters.
     * @throws SQLException on JDBC errors.
     */
    public PreparedStatement prepareStatement(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(getQuery());
        for (int i = 0; i < parameters.size(); i++) {
            ps.setObject(i + 1, parameters.get(i));
        }
        return ps;
    }
}
