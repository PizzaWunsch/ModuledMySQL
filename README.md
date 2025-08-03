# Moduled-MySQL

**Moduled-MySQL** is a lightweight, annotation-driven Java library for seamless integration with MySQL databases.  
It enables easy table creation, data persistence, and asynchronous CRUD operations with minimal configuration and no heavy ORM frameworks.

---

## Features

- **Annotation-based Mapping**: Define your database tables and columns using `@TableName` and `@ColumnName`.
- **Automatic Table Creation**: Tables are generated automatically based on your annotated model classes.
- **Asynchronous Database Operations**: Supports async save, load, and delete operations using Java’s `CompletableFuture`.
- **Flexible SQL Types**: Supports various MySQL column types with customizable length, scale, primary key, and auto-increment options.
- **Simple API**: Easy-to-use repository pattern for CRUD operations without boilerplate code.

---

## Getting Started

### 1. Define your Model

Annotate your Java class with `@TableName` and its fields with `@ColumnName` to map to your MySQL schema.

```java
@TableName("users")
public class User {

    @ColumnName(value = "id", type = SQLType.INT, primaryKey = true, autoIncrement = true)
    private int id;

    @ColumnName(value = "username", type = SQLType.VARCHAR, length = 64)
    private String username;

    @ColumnName(value = "email", type = SQLType.VARCHAR, length = 128)
    private String email;

    // Getters and setters...
}
```

### 2. Connect to Database

Use ``DatabaseManager`` to establish a connection:
```java
DatabaseManager.connect("localhost", "3306", "mydatabase", "user", "password");
```


### 3. Create Table
Invoke ``createTable()`` on an instance of your model or extend the functionality as needed.
```java
User user = new User();
user.createTable(DatabaseManager.getConnection());
```


### 4. Perform CRUD Operations
Use the ``GenericRepository`` to save, load, or delete entities asynchronously or synchronously.
```java
// Save asynchronously
GenericRepository.saveAsync(user, DatabaseManager.getConnection());

// Load asynchronously
GenericRepository.loadAsync(User.class, 1, DatabaseManager.getConnection())
    .thenAccept(loadedUser -> {
        System.out.println("User loaded: " + loadedUser.getUsername());
    });

// Delete asynchronously
GenericRepository.deleteAsync(User.class, 1, DatabaseManager.getConnection());
```

---

## Implementation

You can easily include **ModuledMySQL** in your project using [JitPack](https://jitpack.io/).

### Gradle

Add the JitPack repository to your `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```
Then add the dependency:
```gradle
dependencies {
    implementation 'com.github.PizzaWunsch:ModuledMySQL:1.3-SNAPSHOT'
}
```

### Maven
Add the JitPack repository to your pom.xml:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Then add the dependency:
```xml
<dependency>
    <groupId>com.github.PizzaWunsch</groupId>
    <artifactId>ModuledMySQL</artifactId>
    <version>1.3-SNAPSHOT</version>
</dependency>
```

---


## Requirements

- Java 8 or higher
- MySQL Connector/J (JDBC driver)
- MySQL Server


---
