package dev.pizzawunsch.moduledmysql.exceptions;

/**
 * Thrown to indicate that a primary key is missing in a database-related operation.
 * <p>
 * This exception is typically used in the context of object-relational mapping (ORM)
 * or database abstraction layers where the system expects a class or table to have
 * a primary key defined, but none is found.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * if (!tableDefinition.hasPrimaryKey()) {
 *     throw new SQLMissingPrimaryKeyException("The table '" + tableName + "' does not define a primary key.");
 * }
 * }</pre>
 *
 * <p>
 * This exception extends {@link RuntimeException}, meaning it is unchecked
 * and does not need to be declared in a method's {@code throws} clause.
 * </p>
 *
 * @author Lucas | Pizzawunschv
 * @version 1.0
 * @since 1.0
 */
public class SQLMissingPrimaryKeyException extends RuntimeException {

    /**
     * Constructs a new {@code SQLMissingPrimaryKeyException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception.
     */
    public SQLMissingPrimaryKeyException(String message) {
        super(message);
    }
}
