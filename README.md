# Moduled-MySQL

**Moduled-MySQL** is a lightweight, annotation-driven Java library for seamless integration with MySQL databases.  
It enables easy table creation, data persistence, and asynchronous CRUD operations with minimal configuration and no heavy ORM frameworks.

## Table of Contents

- [Moduled-MySQL](#moduled-mysql)
    - [Features](#features)
    - [Getting Started](#getting-started)
        - [1. Define your Model](#1-define-your-model)
        - [2. Connect to Database](#2-connect-to-database)
        - [3. Create Table](#3-create-table)
        - [4. Perform CRUD Operations](#4-perform-crud-operations)
    - [Query Builder](#query-builder)
        - [Example Table-Class](#example-table-class)
        - [Select All Fields From a Class](#select-all-fields-from-a-class)
        - [Insert From Annotated Object](#insert-from-annotated-object)
        - [Update With Primary Key](#update-with-primary-key)
        - [Delete By Primary Key](#delete-by-primary-key)
        - [Join Example](#join-example)
        - [QueryGroup (Nested Conditions)](#querygroup-nested-conditions)
        - [Parameter Bindings](#parameter-bindings)
    - [Mapping a Single Row to a Java Object](#mapping-a-single-row-to-a-java-object)
    - [Implementation](#implementation)
        - [Gradle](#gradle)
        - [Maven](#maven)
    - [Requirements](#requirements)

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

## Query Builder

The ``QueryBuilder`` is a powerful utility for constructing SQL queries dynamically using Java annotations and reflection. It allows you to build type-safe and readable queries for SELECT, INSERT, UPDATE, DELETE, JOINs, subqueries, and more — without writing raw SQL manually.

**Features**
- Fully annotation-based mapping using @TableName and @ColumnName
- Supports automatic table aliasing
- Safe parameter binding for PreparedStatement
- Supports advanced clauses: JOIN, GROUP BY, HAVING, UNION, IN, nested conditions (QueryGroup)

### Example Table-Class
```java
@TableName("users")
public class User {
    @ColumnName(value = "id", primaryKey = true)
    private int id;

    @ColumnName("email")
    private String email;

    @ColumnName("name")
    private String name;
    
    // constructors, getters, setters...
}
```

### Select All Fields From a Class
```java
QueryBuilder qb = QueryBuilder
    .select(User.class)
    .from(User.class)
    .where("t0.id = ?", 1);

String query = qb.getQuery();
List<Object> params = qb.getParameters();
```

### Insert From Annotated Object
```java
User user = new User("john@example.com", "John");

QueryBuilder qb = QueryBuilder.insertInto(User.class, user);
PreparedStatement ps = qb.prepareStatement(connection);
ps.executeUpdate();
```

### Update With Primary Key
```java
user.setName("Johnny");
QueryBuilder qb = QueryBuilder.update(User.class, user);
PreparedStatement ps = qb.prepareStatement(connection);
ps.executeUpdate();
```

### Delete By Primary Key
```java
QueryBuilder qb = QueryBuilder.deleteFrom(User.class, 1);
PreparedStatement ps = qb.prepareStatement(connection);
ps.executeUpdate();
```

### Join Example
```java
QueryBuilder qb = QueryBuilder
    .select(User.class, "t0.id", "t0.email", "t1.role_name")
    .from(User.class)
    .join(Role.class, "t0.role_id = t1.id")
    .where("t1.role_name = ?", "admin");

String sql = qb.getQuery(); // SELECT t0.id, t0.email, t1.role_name ...
```

### QueryGroup (Nested Conditions)
```java
QueryBuilder.QueryGroup group = new QueryBuilder.QueryGroup()
    .and("age > ?", 25)
    .or("name = ?", "Alice");

QueryBuilder qb = QueryBuilder
    .select("*")
    .from("users")
    .whereGroup(group);
```

Generates:
```sql
SELECT * FROM users WHERE (age > ? OR name = ?)
```

## Parameter Bindings
All values are safely bound using PreparedStatement, preventing SQL injection:
```java
PreparedStatement ps = qb.prepareStatement(connection);
```

---

## Mapping a Single Row to a Java Object

The `ResultMapper.mapRow` method allows you to convert a single row from a `ResultSet` into a typed Java object. This is particularly useful when fetching single records, such as detail views or unique lookups.

To use this functionality, your target class must have:

- A public no-argument constructor
- Fields annotated with `@ColumnName`, specifying the corresponding column names in the database

#### Example

Assuming a table `users` with columns `id`, `email`, and `name`, and a corresponding Java class:

```java
@TableName("users")
public class User {
    @ColumnName(value = "id", primaryKey = true)
    private int id;

    @ColumnName("email")
    private String email;

    @ColumnName("name")
    private String name;

    // Getters and setters omitted for brevity
}
```
You can map a single row from a query result like this:
```java
QueryBuilder qb = QueryBuilder
    .select(User.class)
    .from(User.class)
    .where("t0.id = ?", 1);

PreparedStatement ps = qb.prepareStatement(connection);
ResultSet rs = ps.executeQuery();

User user = null;
if (rs.next()) {
    user = ResultMapper.mapRow(rs, User.class);
}
```
To map multiple rows into a list of objects, use `ResultMapper.mapAll(resultSet, User.class)` instead.

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
    implementation 'com.github.PizzaWunsch:ModuledMySQL:1.4-SNAPSHOT'
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
    <version>1.4-SNAPSHOT</version>
</dependency>
```

---


## Requirements

- Java 8 or higher
- MySQL Connector/J (JDBC driver)
- MySQL Server


---
