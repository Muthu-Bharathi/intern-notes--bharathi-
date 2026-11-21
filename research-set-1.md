1. HTTP
A stateless, application-layer request-response protocol running over TCP/QUIC to transmit hypermedia like HTML and JSON across the web.

2. Web Application
An interactive client-server software application accessed via a web browser that executes dynamic business logic, manages application state, and persists database records.

3. Web Server
A host system and software (e.g., Nginx, Apache) that listens on network ports to serve static assets or reverse-proxy incoming HTTP/HTTPS traffic to application services.

4. HTTPS & Security
HTTP encrypted over TLS on port 443; it guarantees confidentiality via asymmetric/symmetric encryption, message integrity using MACs, and server authenticity via CA-signed certificates.

5. Authentication vs. Authorization
Authentication validates identity ("who you are" via passwords/MFA), while authorization determines access privileges ("what you can do" via roles, scopes, or ACLs).

6. Social Login Flow
Delegates identity verification to a provider (Google/Facebook) using OpenID Connect and OAuth 2.0, exchanging a front-channel authorization code for a cryptographically verified ID/access token.

7. Synchronous vs. Asynchronous Communication
Synchronous calls block execution while waiting for an immediate HTTP/gRPC response, whereas asynchronous systems decouple operations by emitting non-blocking messages/events through brokers like Kafka or SQS.

8. REST Architecture
An architectural style—not an enforced standard—defined by Roy Fielding that relies on statelessness, uniform resource endpoints (URIs), standard HTTP verbs, and decoupled client-server interaction.