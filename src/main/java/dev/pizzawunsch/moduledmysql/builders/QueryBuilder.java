package dev.pizzawunsch.moduledmysql.builders;

import dev.pizzawunsch.moduledmysql.annotations.ColumnName;
import dev.pizzawunsch.moduledmysql.annotations.TableName;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * QueryBuilder is a utility class to dynamically generate SQL queries
 * using Java reflection and annotations. It supports SELECT, INSERT,
 * UPDATE, DELETE, JOINs, subqueries, GROUP BY, HAVING, IN clauses, and unions.
 * <p>
 * Annotations:
 * - @TableName: defines the name of the database table for a class
 * - @ColumnName: defines the name of a column for a field
 * <p>
 * This builder supports aliasing tables and allows safe parameter binding.
 */
public class QueryBuilder {

    private final StringBuilder query = new StringBuilder();
    /**
     * List of parameters corresponding to placeholders in the query.
     */
    @Getter
    private final List<Object> parameters = new ArrayList<>();
    private final Map<Class<?>, String> aliasMap = new HashMap<>();
    private int aliasCounter = 0;

    /**
     * Creates a SELECT query with specified columns.
     */
    public static QueryBuilder select(String... columns) {
        QueryBuilder builder = new QueryBuilder();
        builder.query.append("SELECT ").append(String.join(", ", columns));
        return builder;
    }

    /**
     * Creates a SELECT query using annotated column names from a class.
     */
    public static QueryBuilder select(Class<?> clazz, String... columns) {
        QueryBuilder builder = new QueryBuilder();
        String alias = builder.getAlias(clazz);
        if (columns.length == 0) {
            columns = builder.getAllColumnNames(clazz).stream().map(c -> alias + "." + c).toArray(String[]::new);
        }
        builder.query.append("SELECT ").append(String.join(", ", columns));
        return builder;
    }

    /**
     * Builds an INSERT query from a class and an instance.
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
                    builder.parameters.add(field.get(instance));
                    placeholders.add("?");
                } catch (IllegalAccessException ignored) {}
            }
        }
        builder.query.append("INSERT INTO ").append(table)
                .append(" (").append(String.join(", ", columns)).append(") VALUES (")
                .append(String.join(", ", placeholders)).append(")");
        return builder;
    }

    /**
     * Builds an UPDATE query using annotated fields and values.
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
                } catch (IllegalAccessException ignored) {}
            }
        }
        builder.query.append(String.join(", ", sets));
        if (whereClause != null) {
            builder.query.append(" WHERE ").append(whereClause);
        }
        return builder;
    }

    /**
     * Builds a DELETE query for a given class and primary key value.
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
     * Appends a FROM clause.
     */
    public QueryBuilder from(Class<?> clazz) {
        String table = getTableName(clazz);
        String alias = getAlias(clazz);
        query.append(" FROM ").append(table).append(" ").append(alias);
        return this;
    }

    /**
     * Adds an INNER JOIN clause.
     */
    public QueryBuilder join(Class<?> clazz, String onCondition) {
        String table = getTableName(clazz);
        String alias = getAlias(clazz);
        query.append(" JOIN ").append(table).append(" ").append(alias).append(" ON ").append(onCondition);
        return this;
    }

    /**
     * Adds a LEFT JOIN clause.
     */
    public QueryBuilder leftJoin(Class<?> clazz, String onCondition) {
        String table = getTableName(clazz);
        String alias = getAlias(clazz);
        query.append(" LEFT JOIN ").append(table).append(" ").append(alias).append(" ON ").append(onCondition);
        return this;
    }

    /**
     * Adds a WHERE clause.
     */
    public QueryBuilder where(String condition, Object... values) {
        query.append(" WHERE ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds a grouped WHERE clause.
     */
    public QueryBuilder whereGroup(QueryGroup group) {
        query.append(" WHERE (").append(group.getQuery()).append(")");
        parameters.addAll(group.getParameters());
        return this;
    }

    /**
     * Adds an AND clause.
     */
    public QueryBuilder and(String condition, Object... values) {
        query.append(" AND ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds an OR clause.
     */
    public QueryBuilder or(String condition, Object... values) {
        query.append(" OR ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds an IN clause.
     */
    public QueryBuilder in(String column, List<Object> values) {
        query.append(" ").append(column).append(" IN (")
                .append(values.stream().map(v -> "?").collect(Collectors.joining(", "))).append(")");
        parameters.addAll(values);
        return this;
    }

    /**
     * Adds a GROUP BY clause.
     */
    public QueryBuilder groupBy(String... columns) {
        query.append(" GROUP BY ").append(String.join(", ", columns));
        return this;
    }

    /**
     * Adds a HAVING clause.
     */
    public QueryBuilder having(String condition, Object... values) {
        query.append(" HAVING ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds a UNION clause with another QueryBuilder.
     */
    public QueryBuilder union(QueryBuilder other) {
        query.append(" UNION ").append(other.getQuery());
        parameters.addAll(other.getParameters());
        return this;
    }

    /**
     * Adds a UNION ALL clause with another QueryBuilder.
     */
    public QueryBuilder unionAll(QueryBuilder other) {
        query.append(" UNION ALL ").append(other.getQuery());
        parameters.addAll(other.getParameters());
        return this;
    }

    /**
     * Adds a subquery.
     */
    public QueryBuilder subquery(QueryBuilder subquery, String alias) {
        query.append(" (").append(subquery.getQuery()).append(") AS ").append(alias);
        parameters.addAll(subquery.getParameters());
        return this;
    }

    /**
     * Returns the built SQL query string.
     */
    public String getQuery() {
        return query.toString();
    }

    private String getTableName(Class<?> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        return annotation != null ? annotation.value() : clazz.getSimpleName().toLowerCase();
    }

    private String getAlias(Class<?> clazz) {
        if (!aliasMap.containsKey(clazz)) {
            aliasMap.put(clazz, "t" + aliasCounter++);
        }
        return aliasMap.get(clazz);
    }

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

    private String getPrimaryKeyColumn(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            ColumnName annotation = field.getAnnotation(ColumnName.class);
            if (annotation != null && annotation.primaryKey()) {
                return annotation.value();
            }
        }
        throw new IllegalStateException("No primary key column defined in class " + clazz.getName());
    }

    /**
     * A helper class to group conditions for complex WHERE clauses.
     */
    public static class QueryGroup {
        private final StringBuilder query = new StringBuilder();
        @Getter
        private final List<Object> parameters = new ArrayList<>();

        /**
         * Adds an AND condition to the group.
         */
        public QueryGroup and(String condition, Object... values) {
            if (query.length() > 0) query.append(" AND ");
            query.append(condition);
            parameters.addAll(Arrays.asList(values));
            return this;
        }

        /**
         * Adds an OR condition to the group.
         */
        public QueryGroup or(String condition, Object... values) {
            if (query.length() > 0) query.append(" OR ");
            query.append(condition);
            parameters.addAll(Arrays.asList(values));
            return this;
        }

        /**
         * Returns the grouped condition string.
         */
        public String getQuery() {
            return query.toString();
        }
    }
}
