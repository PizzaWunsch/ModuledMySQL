package dev.pizzawunsch.moduledmysql.builders;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The {@code QueryGroup} class provides a fluent API for building SQL WHERE clauses dynamically,
 * along with a list of corresponding parameter values.
 * <p>
 * It supports chaining of conditions using {@code AND} and {@code OR} logical operators,
 * while automatically managing parameter bindings in order.
 * </p>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * QueryGroup group = new QueryGroup()
 *     .and("name = ?", "Alice")
 *     .or("age > ?", 30);
 * 
 * String sql = group.getQuery(); // Produces: "name = ? OR age > ?"
 * List<Object> params = group.getParameters(); // Contains: ["Alice", 30]
 * }</pre>
 * 
 * @author Lucas | Pizzawunsch
 */
public class QueryGroup {
    /**
     * The internal {@link StringBuilder} used to construct the SQL WHERE clause.
     */
    private final StringBuilder query = new StringBuilder();

    /**
     * The list of parameter values corresponding to the placeholders in the SQL query.
     */
    @Getter
    private final List<Object> parameters = new ArrayList<>();

    /**
     * Adds a condition to the query using the {@code AND} operator.
     * <p>
     * If the query already contains conditions, {@code AND} is prepended before the new condition.
     * The provided values are added to the parameters list in the same order.
     * </p>
     *
     * @param condition the SQL condition string, usually containing one or more {@code ?} placeholders
     * @param values the parameter values to bind for this condition
     * @return this {@code QueryGroup} instance for method chaining
     */
    public QueryGroup and(String condition, Object... values) {
        if (query.length() > 0) query.append(" AND ");
        query.append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Adds a condition to the query using the {@code OR} operator.
     * <p>
     * If the query already contains conditions, {@code OR} is prepended before the new condition.
     * The provided values are added to the parameters list in the same order.
     * </p>
     *
     * @param condition the SQL condition string, usually containing one or more {@code ?} placeholders
     * @param values the parameter values to bind for this condition
     * @return this {@code QueryGroup} instance for method chaining
     */
    public QueryGroup or(String condition, Object... values) {
        if (query.length() > 0) query.append(" OR ");
        query.append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Returns the current SQL condition string that has been built.
     * <p>
     * This is typically used to append into a larger query or as the WHERE clause.
     * </p>
     *
     * @return the constructed SQL condition string
     */
    public String getQuery() {
        return query.toString();
    }
}
