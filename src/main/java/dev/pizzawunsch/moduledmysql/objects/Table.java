package dev.pizzawunsch.moduledmysql.objects;

import dev.pizzawunsch.moduledmysql.annotations.ColumnName;
import dev.pizzawunsch.moduledmysql.annotations.TableName;
import dev.pizzawunsch.moduledmysql.database.DatabaseManager;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for database table definitions.
 * <p>
 * Subclasses of {@code Table} are expected to be annotated with {@link TableName}
 * and contain fields annotated with {@link ColumnName} to define table schema.
 * This class provides logic to extract metadata from the annotations and generate
 * a `CREATE TABLE IF NOT EXISTS` SQL statement accordingly.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * @TableName("users")
 * public class UserTable extends Table {
 *     @ColumnName(value = "id", type = SQLType.INT, length = 11, primaryKey = true, autoIncrement = true)
 *     private int id;
 *
 *     @ColumnName(value = "username", type = SQLType.VARCHAR, length = 255)
 *     private String username;
 * }
 *
 * new UserTable().createTable();
 * }</pre>
 */
public abstract class Table {

    /**
     * Creates the table in the connected MySQL database if it doesn't already exist.
     * <p>
     * Uses class and field annotations to extract schema metadata and build the SQL statement.
     *
     * @throws SQLException if the SQL execution fails
     * @throws IllegalStateException if the class is not annotated with {@link TableName}
     */
    public void createTable() throws SQLException {
        Class<?> clazz = this.getClass();

        if (!clazz.isAnnotationPresent(TableName.class))
            throw new IllegalStateException("[Moduled - MySQL] Missing @TableName annotation on " + clazz.getSimpleName());

        String tableName = clazz.getAnnotation(TableName.class).value();
        List<Column> columnList = extractColumns(clazz);

        if (columnList.isEmpty())
            throw new IllegalStateException("[Moduled - MySQL] No fields annotated with @ColumnName in class: " + clazz.getSimpleName());

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n");

        List<String> columnDefinitions = new ArrayList<>();
        for (Column column : columnList) {
            columnDefinitions.add("  " + column.toSQLDefinition());
        }

        sql.append(String.join(",\n", columnDefinitions));
        sql.append("\n);");

        DatabaseManager.update(sql.toString());

        System.out.println("[Moduled - MySQL] Table created or already exists: " + tableName);
    }

    /**
     * Extracts column metadata from class fields annotated with {@link ColumnName}.
     *
     * @param clazz the class to inspect for annotated fields
     * @return a list of {@link Column} definitions based on the annotations
     */
    private List<Column> extractColumns(Class<?> clazz) {
        List<Column> columns = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ColumnName.class)) {
                continue;
            }
            ColumnName annotation = field.getAnnotation(ColumnName.class);
            Column column = new Column(
                    annotation.value(),
                    annotation.type(),
                    annotation.length(),
                    annotation.scale(),
                    annotation.primaryKey(),
                    annotation.autoIncrement()
            );
            columns.add(column);
        }
        return columns;
    }
}
