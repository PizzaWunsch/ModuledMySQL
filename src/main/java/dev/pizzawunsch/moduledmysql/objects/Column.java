package dev.pizzawunsch.moduledmysql.objects;

import dev.pizzawunsch.moduledmysql.enums.SQLType;

/**
 * Represents a single column in a MySQL table schema definition.
 * <p>
 * A {@code Column} object is used to model metadata for a database column,
 * including its name, SQL data type, optional length/scale, and constraints such
 * as primary key or auto-increment.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * Column idColumn = new Column("id", SQLType.INT, 11, 0, true, true);
 * System.out.println(idColumn.toSQLDefinition()); // => `id` INT(11) PRIMARY KEY AUTO_INCREMENT
 * }</pre>
 */
public class Column {

    /** The name of the column (as used in SQL). */
    private final String name;

    /** The SQL data type for this column. */
    private final SQLType type;

    /** Optional length for types that support it (e.g., VARCHAR, INT). */
    private final int length;

    /** Optional scale for decimal/numeric types (e.g., DECIMAL(10,2)). */
    private final int scale;

    /** Whether this column is the primary key of the table. */
    private final boolean primaryKey;

    /** Whether this column should auto-increment its value (e.g., for ID fields). */
    private final boolean autoIncrement;

    /**
     * Constructs a new {@code Column} object with the given properties.
     *
     * @param name          the column name (must not be null)
     * @param type          the SQL data type (must not be null)
     * @param length        optional length (set 0 if not applicable)
     * @param scale         optional scale (for decimal types; 0 if not applicable)
     * @param primaryKey    true if this column is a primary key
     * @param autoIncrement true if this column should auto-increment (only valid for numeric primary keys)
     */
    public Column(String name, SQLType type, int length, int scale, boolean primaryKey, boolean autoIncrement) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.scale = scale;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
    }

    /**
     * Generates a valid SQL column definition string for use in CREATE TABLE statements.
     *
     * @return a SQL snippet defining this column, e.g. {@code `id` INT(11) PRIMARY KEY AUTO_INCREMENT}
     */
    public String toSQLDefinition() {
        StringBuilder builder = new StringBuilder("`" + name + "` " + type.getSql());

        if (length > 0) {
            builder.append("(").append(length);
            if (scale > 0) {
                builder.append(", ").append(scale);
            }
            builder.append(")");
        }

        if (primaryKey) {
            builder.append(" PRIMARY KEY");
        }

        if (autoIncrement) {
            builder.append(" AUTO_INCREMENT");
        }

        return builder.toString();
    }

    /**
     * Returns the SQL definition string of this column.
     *
     * @return SQL string representing this column
     */
    @Override
    public String toString() {
        return toSQLDefinition();
    }
}
