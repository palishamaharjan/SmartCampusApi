# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures  
**Student:** Palisha Maharjan
**StudentId:** w2083955  
**University:** University of Westminster  
**Technology Stack:** Java 11 · JAX-RS (Jersey 2.39.1) · Maven · Apache Tomcat 9  
**API Base URL:** `http://localhost:8080/api/v1`

---

## Table of Contents

1. [API Design Overview](#1-api-design-overview)
2. [Project Structure](#2-project-structure)
3. [How to Build and Run](#3-how-to-build-and-run)
4. [Sample curl Commands](#4-sample-curl-commands)
5. [Conceptual Report — Question Answers](#5-conceptual-report--question-answers)

---

## 1. API Design Overview

This RESTful API is built for the University's **Smart Campus** initiative. It manages two primary resources — **Rooms** and **Sensors** — plus a **SensorReadings** sub-resource that maintains a historical log of sensor measurements.

### Resource Hierarchy

```
GET    /api/v1                                    → Discovery / API metadata
GET    /api/v1/rooms                              → List all rooms
POST   /api/v1/rooms                              → Create a new room
GET    /api/v1/rooms/{roomId}                     → Get a specific room
DELETE /api/v1/rooms/{roomId}                     → Delete a room (blocked if sensors exist)

GET    /api/v1/sensors                            → List all sensors (optional ?type= filter)
POST   /api/v1/sensors                            → Register a new sensor (validates roomId)
GET    /api/v1/sensors/{sensorId}                 → Get a specific sensor
DELETE /api/v1/sensors/{sensorId}                 → Remove a sensor

GET    /api/v1/sensors/{sensorId}/readings        → Get reading history for a sensor
POST   /api/v1/sensors/{sensorId}/readings        → Add a new reading (updates currentValue)
GET    /api/v1/sensors/{sensorId}/readings/{id}   → Get a specific reading
```

### Data Models

**Room**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": ["TEMP-001", "CO2-001"]
}
```

**Sensor**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "LIB-301"
}
```

**SensorReading**
```json
{
  "id": "uuid-here",
  "timestamp": 1714000000000,
  "value": 24.5
}
```

### Error Handling Strategy

| Scenario | HTTP Status | Exception Class |
|---|---|---|
| Delete room with active sensors | 409 Conflict | `RoomNotEmptyException` |
| Sensor POST with non-existent roomId | 422 Unprocessable Entity | `LinkedResourceNotFoundException` |
| POST reading to MAINTENANCE sensor | 403 Forbidden | `SensorUnavailableException` |
| Any unexpected runtime error | 500 Internal Server Error | `GlobalExceptionMapper` |

### In-Memory Storage

All data is stored using `ConcurrentHashMap` (thread-safe) inside a static `DataStore` singleton class. No database is used. Pre-loaded sample data includes two rooms and three sensors on startup.

---

## 2. Project Structure

```
SmartCampusAPI/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── smartcampus/
        │           ├── application/
        │           │   └── SmartCampusApplication.java
        │           ├── model/
        │           │   ├── Room.java
        │           │   ├── Sensor.java
        │           │   ├── SensorReading.java
        │           │   ├── DataStore.java
        │           │   └── ErrorResponse.java
        │           ├── resource/
        │           │   ├── DiscoveryResource.java
        │           │   ├── RoomResource.java
        │           │   ├── SensorResource.java
        │           │   └── SensorReadingResource.java
        │           ├── exception/
        │           │   ├── RoomNotEmptyException.java
        │           │   ├── RoomNotEmptyExceptionMapper.java
        │           │   ├── LinkedResourceNotFoundException.java
        │           │   ├── LinkedResourceNotFoundExceptionMapper.java
        │           │   ├── SensorUnavailableException.java
        │           │   ├── SensorUnavailableExceptionMapper.java
        │           │   └── GlobalExceptionMapper.java
        │           └── filter/
        │               └── LoggingFilter.java
        └── webapp/
            └── WEB-INF/
                └── web.xml
```

---

## 3. How to Build and Run

### Prerequisites

Make sure you have the following installed before starting:

| Tool | Version | Download |
|---|---|---|
| Java JDK | 11 or later | https://adoptium.net |
| Apache Maven | 3.6+ | https://maven.apache.org |
| Apache Tomcat | 9.x | https://tomcat.apache.org/download-90.cgi |
| NetBeans IDE | 20+ | https://netbeans.apache.org |

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/palishamaharjan/SmartCampusApi.git
cd SmartCampusApi
```

---

### Step 2 — Build the Project

```bash
mvn clean package
```

You will see **BUILD SUCCESS** and the file `target/SmartCampusAPI.war` will be generated.

---

### Step 3 — Deploy to Tomcat

Copy the WAR file into Tomcat's webapps folder:

```bash
# Windows example
copy target\SmartCampusAPI.war C:\tomcat9\webapps\

# Mac/Linux example
cp target/SmartCampusAPI.war /opt/tomcat9/webapps/
```

Then start Tomcat:

```bash
# Windows
C:\tomcat9\bin\startup.bat

# Mac/Linux
/opt/tomcat9/bin/startup.sh
```

---

### Step 4 — Run in NetBeans (Easier Method)

1. Open NetBeans → **File → Open Project** → select the `SmartCampusApi` folder
2. Right-click the project → **Clean and Build**
3. Right-click the project → **Run**
4. NetBeans deploys automatically to Tomcat and opens the browser

---

### Step 5 — Verify the Server is Running

Open your browser or Postman and go to:

```
http://localhost:8080/api/v1
```

You should receive a JSON discovery response like:

```json
{
  "apiVersion": "1.0.0",
  "name": "Smart Campus Sensor & Room Management API",
  "status": "operational",
  "_links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors",
    "self": "/api/v1"
  }
}
```

---

## 4. Sample curl Commands

### 1. Discovery Endpoint — GET /api/v1

```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```

**Expected:** `200 OK` with API metadata and resource links.

---

### 2. List All Rooms — GET /api/v1/rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms \
  -H "Accept: application/json"
```

**Expected:** `200 OK` with JSON array of all rooms.

---

### 3. Create a New Room — POST /api/v1/rooms

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"HALL-201\",\"name\":\"Main Lecture Hall\",\"capacity\":120}"
```

**Expected:** `201 Created` with the new room object and Location header.

---

### 4. Get a Specific Room — GET /api/v1/rooms/{roomId}

```bash
curl -X GET http://localhost:8080/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

**Expected:** `200 OK` with full room detail including sensorIds list.

---

### 5. Delete a Room with Sensors — Triggers 409 Conflict

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**Expected:** `409 Conflict` with JSON error explaining the room has active sensors.

---

### 6. Delete an Empty Room — Success

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-ROOM\",\"name\":\"Temporary Room\",\"capacity\":10}"

curl -X DELETE http://localhost:8080/api/v1/rooms/TEMP-ROOM
```

**Expected:** `201 Created` then `204 No Content`.

---

### 7. Register a Sensor with Invalid roomId — Triggers 422

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"BAD-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"DOES-NOT-EXIST\"}"
```

**Expected:** `422 Unprocessable Entity` with JSON error about missing room reference.

---

### 8. Register a Valid Sensor — POST /api/v1/sensors

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-002\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"LAB-101\"}"
```

**Expected:** `201 Created` with the sensor object.

---

### 9. Filter Sensors by Type — GET /api/v1/sensors?type=CO2

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

**Expected:** `200 OK` with only CO2 sensors in the array.

---

### 10. Post a Reading to an Active Sensor — POST /api/v1/sensors/{id}/readings

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":24.5}"
```

**Expected:** `201 Created` with the reading object. Check TEMP-001 currentValue is now 24.5.

---

### 11. Post a Reading to a MAINTENANCE Sensor — Triggers 403

```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":5.0}"
```

**Expected:** `403 Forbidden` — OCC-001 is in MAINTENANCE status.

---

### 12. Get Reading History — GET /api/v1/sensors/{id}/readings

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Accept: application/json"
```

**Expected:** `200 OK` with array of all historical readings for TEMP-001.

---

## 5. Conceptual Report — Question Answers

---

### Part 1.1 — JAX-RS Resource Lifecycle and Concurrency

By default, JAX-RS creates a **new instance** of each resource class for **every incoming HTTP request**. This is known as the request-scoped lifecycle. It means that any data stored as an instance field inside a resource class is destroyed after each request completes and is never shared between requests.

**Impact on in-memory data management:**
Because each request gets a fresh resource instance, shared state such as the HashMap of rooms and sensors cannot be stored as instance fields — they would be empty on every new request. The solution used in this project is to store all collections inside a **static DataStore singleton class** using `ConcurrentHashMap`. Since static fields belong to the class itself rather than any instance, all request instances share the same underlying data automatically.

**Preventing race conditions:**
In a concurrent environment where multiple requests hit the API simultaneously, a standard `HashMap` or `ArrayList` can produce data corruption, lost updates, and `ConcurrentModificationException` errors. `ConcurrentHashMap` is used instead because it provides fully thread-safe read and write operations without requiring manual `synchronized` blocks, ensuring data integrity under concurrent load.

---

### Part 1.2 — HATEOAS (Hypermedia As The Engine Of Application State)

HATEOAS is considered a hallmark of advanced REST design because it makes the API **self-documenting and navigable at runtime**, similar to how HTML web pages use hyperlinks to guide users without requiring them to memorise URLs.

**Benefits over static documentation:**

1. **Discoverability:** A client can start at `GET /api/v1` and discover all available resources by following the `_links` embedded in each response, without reading any external document.
2. **Loose coupling:** If the server changes a URL during a version migration, clients following embedded links adapt automatically rather than breaking due to hard-coded paths.
3. **Always current:** Unlike static documentation which goes stale, hypermedia links are generated by the live server and are always accurate.
4. **Reduced onboarding time:** Client developers do not need to study an API specification first — they can explore the API interactively by following links.

---

### Part 2.1 — Returning IDs vs Full Objects in List Responses

| Approach | Bandwidth | Client Processing |
|---|---|---|
| Return IDs only | Very low per list call | Requires N follow-up GET requests (N+1 problem) |
| Return full objects | Higher per list call | Single request satisfies all display needs |
| Return summary objects | Moderate | Balanced — avoids N+1 while keeping payload lean |

Returning only IDs forces clients to make one additional GET request per room to retrieve usable data. For a list of 100 rooms this means 101 total HTTP requests, significantly increasing latency. Returning full objects eliminates this problem at the cost of a larger single response. For a campus dashboard that displays all rooms simultaneously, full objects are the correct choice. This implementation returns full room objects in the list response.

---

### Part 2.2 — Idempotency of DELETE

Yes, DELETE is idempotent according to the HTTP specification (RFC 7231). In this implementation:

- **First DELETE call:** Room exists → deleted successfully → HTTP **204 No Content** returned.
- **Second DELETE call:** Room no longer exists → HTTP **404 Not Found** returned.

The **server state is identical after both calls** — the room is gone in both cases — which satisfies the definition of idempotency. The response code differs (204 vs 404) but this is acceptable because idempotency refers to the effect on server state, not the response code. This behaviour contrasts with POST, which is not idempotent because sending the same POST request twice creates two separate resources.

---

### Part 3.1 — Consequences of @Consumes Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that the POST endpoint only accepts requests where the `Content-Type` header is `application/json`.

If a client sends data with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS intercepts the mismatch **before the resource method is ever invoked** and automatically returns **HTTP 415 Unsupported Media Type**. No application code executes, which protects the endpoint from receiving malformed or unexpected data formats. The client receives a clear and immediate signal to correct their Content-Type header and resend the request.

---

### Part 3.2 — @QueryParam vs @PathParam for Filtering

Query parameters such as `GET /sensors?type=CO2` are the correct mechanism for filtering collections for the following reasons:

1. **Semantic accuracy:** A URL path segment like `/sensors/type/CO2` implies the existence of a fixed, named resource at that location. A query string correctly implies optional search criteria being applied to a collection, which is the true meaning of filtering.
2. **Optionality:** `@QueryParam` returns null when the parameter is omitted, making it trivial to return all sensors when no filter is specified. A path-based design would require two separate `@GET` methods with different `@Path` values.
3. **Composability:** Multiple filters combine naturally in a query string such as `?type=CO2&status=ACTIVE`. Path-based filtering becomes deeply nested and unreadable such as `/sensors/type/CO2/status/ACTIVE`.
4. **Industry convention:** Query strings are the standard mechanism for filtering, sorting, and pagination in RESTful APIs, consistent with RFC 3986 URI syntax and widely adopted industry practice.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The sub-resource locator pattern delegates control of a nested URL path such as `/sensors/{id}/readings` to a dedicated `SensorReadingResource` class, rather than defining every nested endpoint inside the already large `SensorResource` controller.

**Benefits in large APIs:**

1. **Single Responsibility Principle:** Each class focuses on one resource type. Reading-specific logic such as history retrieval, aggregation, and pagination lives in `SensorReadingResource` without cluttering `SensorResource`.
2. **Testability:** Smaller, focused classes can be unit-tested in complete isolation without starting the entire API server.
3. **Reusability:** The `SensorReadingResource` class could be reused for a different parent resource type without any duplication.
4. **Team scalability:** Multiple developers can work on different resource classes simultaneously without merge conflicts in a single massive file.
5. **Code readability:** The URL hierarchy maps directly to the class hierarchy in code, making navigation between files intuitive for any developer joining the project.

---

### Part 5.2 — Why HTTP 422 is More Semantically Accurate than 404

**HTTP 404 Not Found** conventionally signals that the **request URL itself does not exist** on the server. For example, `GET /api/v1/rooms/UNKNOWN` returns 404 because no resource exists at that URL.

**HTTP 422 Unprocessable Entity** (defined in RFC 4918) is more semantically accurate in the sensor POST scenario because:

- The request URL `/api/v1/sensors` is **valid and exists**.
- The JSON payload is **syntactically correct and well-formed**.
- The problem is a **logical and semantic error**: a field inside the payload (`roomId`) contains a reference to a resource that does not exist in the system.

Using 422 communicates precisely that the request was understood, the JSON was parsed successfully, but a **business rule violation** (referential integrity) was found within the content itself. A 404 response would mislead the client into thinking the URL was wrong rather than the payload content, making debugging unnecessarily difficult.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers creates serious security vulnerabilities:

1. **Internal path disclosure:** A stack trace reveals the full package and class structure such as `com.smartcampus.resource.SensorResource.createSensor:45`, exposing the application's internal architecture to attackers.
2. **Framework version fingerprinting:** Exception messages often include JAX-RS and Jersey version numbers. Attackers cross-reference these against CVE databases to find publicly known exploits for those specific versions.
3. **Business logic leakage:** Method names and call chains visible in a stack trace reveal internal business logic flow, potentially showing authentication checks that could be bypassed.
4. **Injection attack refinement:** Knowing exactly which line throws a `NullPointerException` or database-related exception helps attackers craft precise injection payloads targeting specific code paths.
5. **Free reconnaissance:** Each unique stack trace is essentially a free map of the application's internals. Attackers deliberately probe edge cases specifically to harvest stack traces.

**Solution:** The `GlobalExceptionMapper` logs the full stack trace server-side using `java.util.logging.Logger` at SEVERE level, but returns only a generic HTTP 500 message to the client — never exposing internal details.

---

### Part 5.5 — JAX-RS Filters vs Manual Logging

Using JAX-RS filters (`ContainerRequestFilter` and `ContainerResponseFilter`) for logging is significantly superior to manually inserting `Logger.info()` calls inside every resource method:

1. **DRY Principle (Don't Repeat Yourself):** A single filter class logs every request and response automatically. Manual logging requires inserting statements into every method across every resource class, which is error-prone and easy to forget.
2. **Cross-cutting concerns:** Logging is a cross-cutting concern — it applies to every endpoint regardless of business logic. JAX-RS filters are specifically designed for this purpose, keeping resource classes focused purely on business logic.
3. **Maintainability:** To change the log format, add request IDs, or include timing information, only one filter class needs to be edited rather than dozens of methods across the codebase.
4. **Global toggle:** A filter can be enabled or disabled globally by adding or removing it from the `Application.getClasses()` registration — no resource code needs to change.
5. **Separation of concerns:** Resource classes remain clean, readable, and focused on their single responsibility. Operational infrastructure such as logging and metrics lives in dedicated infrastructure classes where it belongs.
