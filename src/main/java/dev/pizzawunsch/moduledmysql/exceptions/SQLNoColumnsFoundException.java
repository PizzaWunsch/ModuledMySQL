package dev.pizzawunsch.moduledmysql.exceptions;

/**
 * Thrown to indicate that no column definitions were found for a given class
 * or table during a database mapping or schema generation process.
 * <p>
 * This exception is typically used in the context of object-relational mapping (ORM),
 * schema inspection, or automatic query generation, where a class or entity is
 * expected to have one or more fields mapped to database columns, but none are detected.
 * </p>
 *
 * <p><b>Possible causes:</b></p>
 * <ul>
 *   <li>The class lacks annotated fields (e.g., {@code @Column} annotations).</li>
 *   <li>All fields were filtered out by access level, modifiers, or naming conventions.</li>
 *   <li>A misconfiguration in the reflection or entity scanning process.</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * if (columns.isEmpty()) {
 *     throw new SQLNoColumnsFoundException("No columns found for class " + clazz.getName());
 * }
 * }</pre>
 *
 * <p>
 * This is an unchecked exception, as it extends {@link RuntimeException}, and does not need
 * to be declared in a method's {@code throws} clause.
 * </p>
 *
 * @author Lucas | PizzaWunsch
 * @version 1.0
 * @since 1.0
 */
public class SQLNoColumnsFoundException extends RuntimeException {

    /**
     * Constructs a new {@code SQLNoColumnsFoundException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception.
     */
    public SQLNoColumnsFoundException(String message) {
        super(message);
    }
}
