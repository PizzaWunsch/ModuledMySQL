package dev.pizzawunsch.moduledmysql.enums;

import lombok.Getter;

/**
 * Represents supported SQL data types for MySQL column definitions.
 * <p>
 * This enum is intended to standardize the SQL types used when defining or working
 * with MySQL tables programmatically. Each enum constant maps to its corresponding
 * SQL data type string used in DDL or prepared statements.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *     SQLType type = SQLType.VARCHAR;
 *     String sqlType = type.toString(); // "VARCHAR"
 * </pre>
 *
 * @author Lucas | PizzaWunsch
 */
@Getter
public enum SQLType {

    // Numeric Types
    /**
     * 1-byte integer, range: -128 to 127 or 0 to 255 (UNSIGNED).
     */
    TINYINT("TINYINT"),

    /**
     * 2-byte integer, range: -32,768 to 32,767 or 0 to 65,535 (UNSIGNED).
     */
    SMALLINT("SMALLINT"),

    /**
     * 3-byte integer, range: -8,388,608 to 8,388,607 or 0 to 16,777,215 (UNSIGNED).
     */
    MEDIUMINT("MEDIUMINT"),

    /**
     * 4-byte integer, standard integer type. Range: -2^31 to 2^31-1.
     */
    INT("INT"),

    /**
     * Synonym for INT. Use either as preferred style.
     */
    INTEGER("INTEGER"),

    /**
     * 8-byte integer, used for very large numeric values.
     */
    BIGINT("BIGINT"),

    /**
     * Fixed-point number with exact precision and scale. Preferred for financial data.
     */
    DECIMAL("DECIMAL"),

    /**
     * Synonym for DECIMAL. ANSI SQL compliant.
     */
    NUMERIC("NUMERIC"),

    /**
     * Single-precision floating point number. Subject to rounding errors.
     */
    FLOAT("FLOAT"),

    /**
     * Double-precision floating point number.
     */
    DOUBLE("DOUBLE"),

    // Boolean / Bit Type
    /**
     * Stores bits (1 or more), used for bitwise operations.
     */
    BIT("BIT"),

    /**
     * Synonym for TINYINT(1), used for true/false values.
     */
    BOOLEAN("BOOLEAN"),

    // String Types
    /**
     * Fixed-length character string (padded with spaces).
     */
    CHAR("CHAR"),

    /**
     * Variable-length character string. Most commonly used string type.
     */
    VARCHAR("VARCHAR"),

    /**
     * Very small text field (up to 255 bytes).
     */
    TINYTEXT("TINYTEXT"),

    /**
     * Standard text field (up to 65,535 bytes).
     */
    TEXT("TEXT"),

    /**
     * Medium-length text field (up to 16,777,215 bytes).
     */
    MEDIUMTEXT("MEDIUMTEXT"),

    /**
     * Large text field (up to 4,294,967,295 bytes).
     */
    LONGTEXT("LONGTEXT"),

    // Binary Types
    /**
     * Fixed-length binary data.
     */
    BINARY("BINARY"),

    /**
     * Variable-length binary data.
     */
    VARBINARY("VARBINARY"),

    /**
     * Very small binary object (up to 255 bytes).
     */
    TINYBLOB("TINYBLOB"),

    /**
     * Standard binary object (up to 65,535 bytes).
     */
    BLOB("BLOB"),

    /**
     * Medium-sized binary object (up to 16,777,215 bytes).
     */
    MEDIUMBLOB("MEDIUMBLOB"),

    /**
     * Large binary object (up to 4,294,967,295 bytes).
     */
    LONGBLOB("LONGBLOB"),

    // Date and Time Types
    /**
     * Stores only date values (YYYY-MM-DD).
     */
    DATE("DATE"),

    /**
     * Stores only time values (HH:MM:SS).
     */
    TIME("TIME"),

    /**
     * Stores a 4-digit year.
     */
    YEAR("YEAR"),

    /**
     * Stores date and time (YYYY-MM-DD HH:MM:SS).
     */
    DATETIME("DATETIME"),

    /**
     * Like DATETIME, but with automatic initialization and update on row change.
     */
    TIMESTAMP("TIMESTAMP"),

    // JSON and Enumeration Types
    /**
     * JSON formatted data. Requires MySQL 5.7+.
     */
    JSON("JSON"),

    /**
     * A predefined set of string values (e.g., 'small', 'medium', 'large').
     */
    ENUM("ENUM"),

    /**
     * A set of zero or more values chosen from a predefined list.
     */
    SET("SET"),

    // Spatial Types (used in GIS)
    /**
     * Base spatial type.
     */
    GEOMETRY("GEOMETRY"),

    /**
     * Stores a single geographic coordinate.
     */
    POINT("POINT"),

    /**
     * Stores a line of connected points.
     */
    LINESTRING("LINESTRING"),

    /**
     * Stores a polygon (closed shape).
     */
    POLYGON("POLYGON");

    /**
     * The raw SQL string representation of the type.
     */
    private final String sql;

    /**
     * Constructs a new SQLType enum with the given SQL keyword.
     *
     * @param sql the SQL keyword as used in MySQL column definitions
     */
    SQLType(String sql) {
        this.sql = sql;
    }

    /**
     * Returns the raw SQL string representation of the enum.
     *
     * @return the SQL keyword (e.g., "VARCHAR", "INT")
     */
    @Override
    public String toString() {
        return sql;
    }
}
