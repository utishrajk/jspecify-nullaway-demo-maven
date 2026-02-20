# JSpecify + NullAway + MapStruct Demo

Test change

This project demonstrates the integration of JSpecify annotations with NullAway static analysis tool in a Spring Boot application using MapStruct for object mapping.

## Project Overview

The demo showcases:
- **JSpecify annotations** for null safety
- **NullAway** static analysis integration
- **MapStruct** object mapping with null safety
- **Spring Boot** REST endpoints
- **Compile-time null safety** enforcement

## Architecture

```
src/main/java/org/example/
├── controller/     # REST controllers
├── service/        # Business logic
├── mapper/         # MapStruct mappers with JSpecify
├── entity/         # Domain models
├── dto/            # Data transfer objects
└── util/           # Utility classes with JSpecify demonstrations
```

## JSpecify/NullAway Setup

### 1. Dependencies

```xml
<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Annotation Processors

```xml
<annotationProcessorPaths>
    <path>
        <groupId>com.google.errorprone</groupId>
        <artifactId>error_prone_core</artifactId>
        <version>2.38.0</version>
    </path>
    <path>
        <groupId>com.uber.nullaway</groupId>
        <artifactId>nullaway</artifactId>
        <version>0.12.7</version>
    </path>
</annotationProcessorPaths>
```

### 3. Compiler Configuration

```xml
<compilerArgs>
    <arg>-Xplugin:ErrorProne -Xep:NullAway:ERROR -XepOpt:NullAway:OnlyNullMarked</arg>
    <!-- Additional JVM exports for ErrorProne -->
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
</compilerArgs>
```

## JSpecify Annotations Explained

### `@NullMarked`
- **Purpose**: Makes all parameters and return types non-null by default within the annotated scope
- **Usage**: Applied to classes, interfaces, or packages
- **Example**:
```java
@NullMarked
public class StringUtils {
    // All parameters and return types are non-null by default
}
```

### `@Nullable`
- **Purpose**: Explicitly allows null values where business logic requires it
- **Usage**: Applied to parameters, return types, or fields
- **Example**:
```java
public static String combineNames(@Nullable String firstName, @Nullable String lastName) {
    if (firstName == null && lastName == null) {
        return "Unknown";
    }
    // ... safe null handling
}
```

## How JSpecify/NullAway Works

### Step 1: Annotation Processing
- NullAway runs as an ErrorProne plugin during compilation
- Analyzes all `@NullMarked` classes and interfaces
- The `-XepOpt:NullAway:OnlyNullMarked` option restricts analysis to marked classes only

### Step 2: Default Assumption
- Inside `@NullMarked` scope: all types are non-null by default
- Any deviation must be explicitly marked with `@Nullable`

### Step 3: Static Analysis
- **Dereference Checks**: Ensures you don't call methods on potentially null references
- **Assignment Checks**: Prevents assigning null to non-null variables  
- **Return Value Checks**: Ensures non-null methods don't return null

### Step 4: Error Reporting
- Compile-time errors for null safety violations
- Example: `error: [NullAway] dereferenced expression input is @Nullable`

## Demonstration Examples

### 1. Utility Class with Null Safety
```java
@NullMarked
public class StringUtils {
    public static String combineNames(@Nullable String firstName, @Nullable String lastName) {
        if (firstName == null && lastName == null) {
            return "Unknown";
        }
        if (firstName == null) {
            return lastName != null ? lastName : "Unknown";
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
```

### 2. MapStruct Integration
```java
@NullMarked
@Mapper(imports = {StringUtils.class})
public interface UserMapper {
    @Mapping(target = "fullName", expression = "java(StringUtils.combineNames(user.getFirstName(), user.getLastName()))")
    UserResponse userToUserResponse(User user);

    default @Nullable UserResponse safeUserMapping(@Nullable User user) {
        if (user == null) {
            return null;
        }
        return userToUserResponse(user);
    }
}
```

### 3. Compile-time Error Example
```java
// This will cause a compilation error:
public static String unsafeMethod(@Nullable String input) {
    return input.toUpperCase(); // ERROR: dereferenced expression input is @Nullable
}

// Correct version:
public static String safeUpperCase(@Nullable String input) {
    if (input == null) {
        return "NULL_INPUT";
    }
    return input.toUpperCase(Locale.ROOT);
}
```

## API Endpoints

- `GET /hello` - Returns list of all users with MapStruct mapping
- `GET /hello/{id}` - Returns single user using safe mapping
- `GET /hello/safe/{id}` - Returns user with extra null safety (returns "User Not Found" for id > 100)

## Running the Application

### Compile and Check Null Safety
```bash
mvn clean compile
```

### Start the Application
```bash
mvn spring-boot:run
```

### Test the Endpoints
```bash
curl http://localhost:8080/hello
curl http://localhost:8080/hello/1
curl http://localhost:8080/hello/safe/999
```

## Key Benefits

1. **Compile-time Safety**: Catches `NullPointerException` before runtime
2. **Explicit Contracts**: Makes nullability part of the API contract
3. **Better Documentation**: Code self-documents what can and cannot be null
4. **IDE Integration**: IDEs provide better null safety warnings and suggestions
5. **MapStruct Integration**: Object mapping works seamlessly with null safety

## Technologies Used

- **Java 21**
- **Spring Boot 3.2.0**
- **MapStruct 1.5.5**
- **JSpecify 1.0.0**
- **NullAway 0.12.7**
- **ErrorProne 2.38.0**
- **Maven**

## Learning Outcomes

This demo illustrates how JSpecify and NullAway integration:
- Prevents null pointer exceptions at compile time
- Makes null handling explicit and documented
- Works seamlessly with popular frameworks like Spring Boot and MapStruct
- Improves overall code quality and maintainability
