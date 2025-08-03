package dev.pizzawunsch.moduledmysql.annotations;

import dev.pizzawunsch.moduledmysql.enums.SQLType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define metadata for a database column.
 * <p>
 * This annotation should be placed on fields of an entity class that represents a database table.
 * It defines the column name, type, length, scale, primary key status, and auto-increment behavior.
 * <p>
 * Used in combination with {@link dev.pizzawunsch.moduledmysql.annotations.TableName} to allow
 * automatic table creation and object persistence.
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * @ColumnName(
 *     value = "id",
 *     type = SQLType.INT,
 *     primaryKey = true,
 *     autoIncrement = true,
 *     length = 11
 * )
 * private int id;
 * }</pre>
 *
 * @see dev.pizzawunsch.moduledmysql.enums.SQLType
 * @see dev.pizzawunsch.moduledmysql.annotations.TableName
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnName {

    /**
     * The name of the column in the database table.
     *
     * @return the column name
     */
    String value();

    /**
     * The SQL data type of the column.
     *
     * @return the SQLType enum value
     */
    SQLType type();

    /**
     * Whether the column is a primary key.
     * Default is {@code false}.
     *
     * @return true if this field is a primary key
     */
    boolean primaryKey() default false;

    /**
     * The length of the column (e.g., VARCHAR(255), INT(11)).
     * If unused, set to -1.
     *
     * @return the length value or -1 if not specified
     */
    int length() default -1;

    /**
     * The scale of the column (used with DECIMAL or NUMERIC types).
     * If unused, set to -1.
     *
     * @return the scale or -1 if not specified
     */
    int scale() default -1;

    /**
     * Whether the column should auto-increment (e.g., for primary key IDs).
     * Default is {@code false}.
     *
     * @return true if auto-increment should be applied
     */
    boolean autoIncrement() default false;
}
