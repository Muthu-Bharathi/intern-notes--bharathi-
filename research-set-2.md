1. Front end vs back end
Front end: The client-facing layer (HTML, CSS, JS, frameworks) that renders UI and captures user input in the browser.
Back end: The server-side environment (APIs, business logic, databases) that processes requests, enforces security, and persists data.

2. Three-tier architecture in a web application
A classic software design that decouples an entire system into Presentation, Application (Business Logic), and Data tiers.
It ensures high scalability, security, and maintainability by preventing clients from accessing databases directly.

3. Three-tier architecture from a web-development point of view
Implemented as the UI layer (React/browser), the API/server layer (Spring Boot/Node.js handling business logic), and the database (PostgreSQL/MySQL).
Each tier runs independently, communicates via standard protocols (HTTP/SQL), and can be scaled or updated in isolation.

4. SSL / TLS encryption
Cryptographic protocols that secure client-server communication by encrypting data in transit and authenticating server identity over HTTPS.
They use asymmetric cryptography for the initial handshake and symmetric keys for fast, tamper-proof session data exchange.

5. HTTP methods
Standard verbs that define the desired action to be performed on a target resource.
Common methods include GET (read), POST (create), PUT/PATCH (replace/modify), and DELETE (remove).

6. HTTP status codes
Three-digit standardized server responses indicating the result of an HTTP request.
Grouped into classes: 2xx (Success), 3xx (Redirection), 4xx (Client Errors like 404), and 5xx (Server Errors like 500).

7. CRUD operations
The four foundational persistent-storage actions: Create, Read, Update, and Delete.
In REST APIs, they map directly to HTTP verbs: POST, GET, PUT/PATCH, and DELETE.

8. Stateful vs stateless communication in web applications
Stateful: The server stores client session data in memory or storage across successive requests.
Stateless: Every request contains all context needed for execution; the server retains zero client memory between calls.

9. What is authentication? What are the different types?
The process of verifying the actual identity of a user, service, or device attempting access.
Common types include password-based, multi-factor (MFA), biometric, token-based (OAuth/JWT), and certificate-based auth.

10. What is authorization? What are the different types?
The process of verifying what specific resources or actions an authenticated identity has permission to access.
Common types include Role-Based Access Control (RBAC), Attribute-Based Access Control (ABAC), and Access Control Lists (ACL).

11. How does authentication differ from authorization?
Authentication verifies who you are (identity confirmation), occurring first in the request lifecycle.
Authorization determines what you can do (permission checks), evaluated after identity has been proven.

12. What are cookies for a website — how does a server generate one, and how does a client (a browser) use it?
Small key-value data snippets sent by a server via the Set-Cookie header to persist state on the client.
The browser automatically stores the cookie and attaches it to the Cookie header on subsequent requests to that domain.

13. How are tokens used in authentication? How do they work between the server, the client, and an Identity Provider?
Tokens serve as portable, cryptographically verifiable credentials issued to clients upon successful login.
The Identity Provider authenticates credentials and issues the token; the client sends it in the Authorization header for resource server validation.

14. What are opaque tokens, versus JWT-like tokens?
Opaque tokens: Random reference strings containing no data, requiring a database/cache lookup by the server to validate.
JWTs: Self-contained tokens carrying encoded user claims and signatures that servers validate locally without database lookups.

15. How do you know a JWT's header and payload were not tampered with?
The server recalculates the cryptographic signature using the received header, payload, and a private/secret key.
If any character in the header or payload changed, the calculated signature will not match the token's attached signature.

16. How does stateful vs stateless HTTP relate to all of the above?
Stateful auth relies on server-stored session IDs (often in cookies) checked against server memory on each hit.
Stateless auth relies on self-contained tokens (like JWTs) where the client carries the state, allowing servers to scale horizontally without shared session stores.

17. What is serialization? What is deserialization? Why do APIs need them?
Serialization converts in-memory data objects into a transmittable byte or text format; deserialization reverses the process.
APIs require them to pass structured data seamlessly across different programming languages, networks, and operating systems.

18. Name some serialization formats.
Common text formats include JSON, XML, YAML, and CSV.
High-performance binary formats include Protocol Buffers (Protobuf), MessagePack, and Apache Avro.

19. JSON and XML: what they are, how each is used in HTTP responses, and how they differ.
Both are text formats defining payload structure via Content-Type: application/json or application/xml.
JSON is lightweight, key-value based, and native to JavaScript, whereas XML uses verbose tag-based markup with strict schema/attribute support.

20. Postman: what it is, how you test an API with it.
A GUI-based API platform for designing, mocking, debugging, and documenting HTTP requests.
You test APIs by entering URLs, setting headers/bodies, sending requests, and inspecting status codes, response times, and payloads.

21. cURL: what it is, how you send an HTTP request with it.
A command-line tool and library used to transfer data across networks using protocols like HTTP and HTTPS.
You send requests via shell commands like curl -X POST https://api.com/data -H "Content-Type: application/json" -d '{"key":"val"}'.

22. Postman vs cURL — when you'd use each.
Use Postman for exploratory visual testing, complex multi-step workflows, automated test suites, and team documentation sharing.
Use cURL for quick command-line checks, lightweight debugging on remote servers/terminals, and CI/CD shell scripts.

23. Query parameter, path variable, request payload — and how query parameters differ from path variables.
Path variables identify a specific resource (/users/123), while query parameters filter or paginate resources (/users?role=admin).
Payloads transport the actual body data (POST/PUT), unlike URL-bound path variables (identity) and query parameters (modifiers).

24. Request headers and response headers — what they carry.
Request headers: Carry client metadata, such as authentication tokens, accepted content formats (Accept), and client device details (User-Agent).
Response headers: Carry server metadata, such as payload MIME type (Content-Type), caching rules (Cache-Control), and cookie directives (Set-Cookie).