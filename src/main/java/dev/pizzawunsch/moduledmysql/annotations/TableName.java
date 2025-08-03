package dev.pizzawunsch.moduledmysql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define the table name for a class.
 * <p>
 * This annotation must be placed on entity classes that represent database tables.
 * It specifies the name of the table that should be used for SQL operations like
 * creation, insertion, selection, and deletion.
 * <p>
 * Example:
 * <pre>{@code
 * @TableName("users")
 * public class User {
 *     // fields...
 * }
 * }</pre>
 *
 * @see dev.pizzawunsch.moduledmysql.annotations.ColumnName
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableName {

    /**
     * The name of the database table associated with the annotated class.
     *
     * @return the SQL table name
     */
    String value();
}
