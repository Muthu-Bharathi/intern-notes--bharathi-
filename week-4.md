1. What does a Pydantic model give you for free?
 What status code does a bad body produce?
It gives automatic type validation, data parsing/coercion, and JSON schema generation for free, returning a 422 Unprocessable Entity status code when a request body fails validation.

2. Why must SQL values go through ? ,/ %s placeholders and not an f-string?
Placeholders ensure parameters are sent separately to the database engine for pre-compilation, preventing SQL injection and safely handling data escaping.

3. What is a primary key? A foreign key?
A primary key uniquely identifies each record in its table, while a foreign key references a primary key in another table to enforce relational integrity.

4. What does an ORM do? Name one for your language. (Java: what's the difference between JPA and Hibernate?)
An ORM maps database tables directly to object-oriented classes; JPA is the standard specification/interface in Java, whereas Hibernate is the actual underlying implementation framework.

5. What is CORS for, and which clients does it not affect?
CORS is a browser-enforced security mechanism restricting cross-origin HTTP requests, and it does not affect non-browser clients like Postman, cURL, or server-to-server calls.

6. Why read the database URL from an environment variable instead of hardcoding it?
It prevents leaking sensitive credentials into version control and allows the application to switch seamlessly between development, staging, and production environments without rebuilding code.

7. What is Dependency Injection, in general? Give the FastAPI example and the Spring example.
DI is a pattern where an external container injects dependencies into components rather than having them instantiate dependencies themselves; FastAPI does this using Depends(get_db) in route signatures, while Spring uses @Autowired or constructor injection.

8. What is Separation of Concerns, and where do you already practice it in this week's code?
It is the architectural principle of dividing a program into distinct sections where each addresses a specific responsibility, seen in keeping routing/validation out of raw database queries.

9. Java what does each layer do — Controller, Service, Repository, Entity?
Entity maps to the database table, Repository handles database read/write queries, Service executes core business logic and transactions, and Controller handles incoming HTTP requests and API responses.

10. Java how do you run the jar on a different port without editing application.properties?
You pass the port override via command line using java -jar app.jar --server.port=9090 or by setting the environment variable SERVER_PORT=9090.

11. Java name one SOLID letter and point at the line of code that follows it
The "D" (Dependency Inversion Principle) is applied when injecting an interface into a service constructor, such as private final UserRepository userRepository; in UserService(UserRepository repo).

12. Java what problem does Lombok's @Data solve? What's the Python equivalent?
@Data eliminates repetitive boilerplate by generating getters, setters, equals, hashCode, and toString at compile time, which is equivalent to Python's @dataclass decorator.

13. Java what design pattern is ResponseEntity.notFound().build(), and why chain calls instead of one constructor?
It uses the Builder pattern, which allows flexible, readable step-by-step construction of complex immutable response objects without unwieldy multi-parameter constructors.