# cURL & HTTP API Exploration Notes

## 1. Quick Reference: Common cURL Flags

| Flag | Long Option | Purpose |
| :--- | :--- | :--- |
| `-I` | `--head` | Fetches only HTTP headers (sends a `HEAD` request; omits body). |
| `-i` | `--include` | Includes response headers before the body in the output. |
| `-v` | `--verbose` | Full handshake, TLS, request headers (`>`), and response headers (`<`). |
| `-s` | `--silent` | Mutes progress meter and error messages. |
| `-S` | `--show-error` | Shows errors even when `-s` is active (commonly used as `-sS`). |
| `-L` | `--location` | Follows HTTP redirects (301, 302, 307). |
| `-o` | `--output <file>` | Writes response payload to a specific file instead of standard output. |
| `-O` | `--remote-name` | Saves output using the remote file name from the URL. |
| `-X` | `--request <METHOD>` | Sets custom HTTP method (e.g., `-X POST`, `-X PUT`, `-X DELETE`). |
| `-H` | `--header <header>` | Adds a custom request header (e.g., `-H "Content-Type: application/json"`). |
| `-d` | `--data <data>` | Sends HTTP `POST` body data (implicitly turns method to `POST`). |
| `-u` | `--user <user:pwd>` | Supplies basic authentication credentials. |
| `-k` | `--insecure` | Skips SSL/TLS certificate verification. |
| `-f` | `--fail` | Fails silently on HTTP server errors (returns non-zero exit code without body). |

---

## 2. API Call Analysis

### Call 1: Public User Profile
* **Command:** `curl -i https://api.github.com/users/torvalds`
* **Status Code:** `200 OK` — Request succeeded; target resource was found and returned.
* **Three Key Response Headers:**
  * `X-RateLimit-Remaining: 58` — Tracks remaining API quota for the hourly window (out of 60 for unauthenticated requests).
  * `X-RateLimit-Reset: 1789556153` — Unix epoch timestamp indicating when the rate limit resets to 60.
  * `Strict-Transport-Security: max-age=31536000; includeSubdomains; preload` — Enforces HTTPS-only communication for one year (31,536,000 seconds) to mitigate downgrade attacks.
* **Where Data Appeared in Response Body:**
  * Returned as a flat root-level JSON object. Target fields mapped directly to root keys: `login` (`"torvalds"`), `name` (`"Linus Torvalds"`), `public_repos` (`12`), and `followers` (`323734`).
* **cURL Key Takeaways:**
  * `curl -i` outputs both HTTP response headers and body.
  * `curl -s` runs silently and outputs only the payload body.

---

### Call 2: Verbose Connection & Echo Inspection
* **Command:** `curl -v https://httpbin.org/get`
* **Status Code:** `200 OK` — Server received and successfully processed the GET request.
* **Three Key Response Headers:**
  * `Content-Type: application/json` — Tells the client the response body is formatted as JSON.
  * `Access-Control-Allow-Origin: *` — Permissive CORS header indicating any origin domain can access this response in a browser.
  * `Server: gunicorn/19.9.0` — Discloses the backend WSGI application server handling the request.
* **Where Data Appeared in Response Body:**
  * The response body echoed client connection data:
    * Sent headers (`User-Agent`, `Host`, `Accept`) are nested inside `"headers"`.
    * Client IP address appears under `"origin"` (`"103.113.190.250"`).
    * Resolved URL appears under `"url"` (`"https://httpbin.org/get"`).
    * Query parameters appear empty under `"args": {}` because none were supplied.
* **cURL Key Takeaways (`-v` flag):**
  * Lines starting with `*` show connection establishment, DNS resolution, and TLS handshake.
  * Lines starting with `>` show raw outgoing request headers sent to the server.
  * Lines starting with `<` show raw incoming response headers returned by the server.

---

### Call 3: POST Request with JSON Body
* **Command:** `curl -i -X POST https://httpbin.org/post -H "Content-Type: application/json" -d '{"name":"Bharathi","week":1}'`
* **Status Code:** `200 OK` — Confirms the POST request succeeded and payload was processed.
* **Three Key Response Headers:**
  * `Content-Type: application/json` — Identifies the format of the response payload.
  * `Access-Control-Allow-Credentials: true` — Indicates credentials/tokens can be exposed to cross-origin front-end scripts.
  * `Connection: keep-alive` — Instructs client and server to keep the underlying TCP connection open for subsequent requests.
* **Where Data Appeared in Response Body:**
  * Transmitted data appears in two locations:
    1. **`"json"` object:** Parsed key-value pairs (`"name": "Bharathi"`, `"week": 1`) because `Content-Type: application/json` was declared.
    2. **`"data"` field:** Raw string representation containing unparsed text and escape characters.

---

### Call 4: GET Request with Query Parameters
* **Command:** `curl -i "https://httpbin.org/get?role=intern&track=python"`
* **Status Code:** `200 OK` — GET request with query string parameters was validated and processed.
* **Three Key Response Headers:**
  * `Content-Length: 330` — Size of the response body in octets (bytes).
  * `Date: Thu, 17 Sep 2026 04:37:03 GMT` — Server timestamp recording when the response was generated.
  * `Access-Control-Allow-Origin: *` — Broad CORS allowance for cross-origin callers.
* **Where Data Appeared in Response Body:**
  * Query string parameters (`?role=intern&track=python`) were extracted and placed directly inside the **`"args"`** object:
    * `"role": "intern"`
    * `"track": "python"`
  * The full query string is also preserved intact at the end of the **`"url"`** string.

---

### Call 5: Negative Scenario / Non-Existent Resource
* **Command:** `curl -i https://api.github.com/users/this-user-does-not-exist-99999`
* **Status Code:** `404 Not Found`
* **Three Key Response Headers:**
  * `x-github-api-version-selected: 2022-11-28` — Tracks active GitHub REST API schema version evaluated.
  * `X-RateLimit-Used: 1` (and `X-RateLimit-Remaining: 59`) — Confirms invalid client lookups still consume rate-limit quota.
  * `Content-Type: application/json; charset=utf-8` — Specifies JSON payload formatting and character encoding.
* **Where Data Appeared in Response Body:**
  * Root-level JSON error payload:
    ```json
    {
      "message": "Not Found",
      "documentation_url": "[https://docs.github.com/rest](https://docs.github.com/rest)"
    }
    ```

---

## 3. Call 5 Analysis: Status Code & Root Cause

* **Status Code Returned:** `404 Not Found`
* **Why this code came back:**
  1. **Syntactically Valid Request:** The HTTP request itself was well-formed, so the server did not return `400 Bad Request`.
  2. **Active Quota:** The client had remaining rate limit capacity, so it did not return `429 Too Many Requests`.
  3. **Missing Resource:** The GitHub identity routing service evaluated the path `/users/this-user-does-not-exist-99999` against its database and found no corresponding record. Per REST specifications, servers return `404 Not Found` when the endpoint route exists but the specified entity cannot be found.