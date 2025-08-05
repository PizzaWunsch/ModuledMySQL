package dev.pizzawunsch.moduledmysql.exceptions;

/**
 * Thrown to indicate that a required table annotation is missing on a class
 * that is expected to be mapped to a database table.
 * <p>
 * This exception is commonly used in custom object-relational mapping (ORM)
 * frameworks or database utility libraries where classes must be explicitly
 * annotated (e.g., with a {@code @Table} annotation) to represent a database table.
 * </p>
 *
 * <p><b>Typical scenario:</b></p>
 * <pre>{@code
 * if (!clazz.isAnnotationPresent(Table.class)) {
 *     throw new SQLMissingTableAnnotationException("Class '" + clazz.getName() + "' is missing @Table annotation.");
 * }
 * }</pre>
 *
 * <p>
 * This exception extends {@link RuntimeException}, making it unchecked.
 * It does not require being declared in a method's {@code throws} clause.
 * </p>
 *
 * @author Lucas | PizzaWunsch
 * @version 1.0
 * @since 1.0
 */
public class SQLMissingTableAnnotationException extends RuntimeException {

    /**
     * Constructs a new {@code SQLMissingTableAnnotationException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception.
     */
    public SQLMissingTableAnnotationException(String message) {
        super(message);
    }
}
