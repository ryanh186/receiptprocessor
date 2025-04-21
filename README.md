# Receipt Processor

**Author:** Ryan Hassoun (<ryanbhas@gmail.com>)

This Spring Boot application implements the Fetch Rewards Receipt Processor challenge. It exposes two REST endpoints that allow you to submit purchase receipts and retrieve the points earned based on a set of scoring rules.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Building Locally](#building-locally)
3. [Running Locally](#running-locally)
4. [Running Tests](#running-tests)
5. [Docker](#docker)
6. [API Usage](#api-usage)
   - [POST /receipts/process](#post-receiptsprocess)
   - [GET /receipts/{id}/points](#get-receiptsidpoints)
7. [Scoring Rules](#scoring-rules)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

- **Java 17** or newer
- **Maven 3.6+** (or use the included Maven Wrapper)
- **Docker** (for containerized setup, optional)
- **cURL** or **Postman** for testing HTTP endpoints

---

## Building Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/receiptprocessor.git
   cd receiptprocessor
   ```
2. Build the project and resolve dependencies:
   ```bash
   ./mvnw clean install
   ```
   or, if Maven is installed:
   ```bash
   mvn clean install
   ```

This will compile the code and run unit tests.

---

## Running Locally

Start the Spring Boot application on port 8080:

```bash
./mvnw spring-boot:run
```  
or:
```bash
mvn spring-boot:run
```

When successful, you should see a log entry:

```
Started ReceiptProcessorApplication in X.Y seconds
```

You can then access the API at: `http://localhost:8080`

---

## Running Tests

### Unit Tests

```bash
./mvnw test
```

This runs all JUnit 5 unit tests for both the service layer and controller.

### Manual Testing with cURL

1. **Submit a Receipt**

   ```bash
   curl -X POST http://localhost:8080/receipts/process \
     -H "Content-Type: application/json" \
     -d '{
       "retailer":"M&M Corner Market",
       "purchaseDate":"2022-01-01",
       "purchaseTime":"13:01",
       "items":[
         {"shortDescription":"Mountain Dew 12PK","price":"6.49"},
         {"shortDescription":"Emils Cheese Pizza","price":"12.25"}
       ],
       "total":"18.74"
     }'
   ```

   Response:

   ```json
   { "id": "<uuid>" }
   ```

2. **Retrieve Points**

   ```bash
   curl http://localhost:8080/receipts/<uuid>/points
   ```

   Response:

   ```json
   { "points": 17 }
   ```

---

## Docker

This project includes a `Dockerfile` for a multi-stage build.

### Build the Docker Image

```bash
docker build -t receipt-processor .
```

### Run the Container

```bash
docker run --rm -p 8080:8080 receipt-processor
```

- The API will be available at `http://localhost:8080`.
- No additional configuration is required.

---

## API Usage

### POST `/receipts/process`

- **Description:** Submit a receipt JSON to be processed.
- **Request Body:** See [api.yml](./api.yml) for schema details.
- **Responses:**
  - `200 OK` with `{ "id": "<uuid>" }`
  - `400 Bad Request` with body:
    > The receipt is invalid. Please verify input.

### GET `/receipts/{id}/points`

- **Description:** Retrieve points for a previously processed receipt.
- **Path Parameter:** `id` (UUID returned by the POST)
- **Responses:**
  - `200 OK` with `{ "points": <integer> }`
  - `404 Not Found` with body:
    > No receipt found for that ID.

---

## Scoring Rules

1. **1 point** per alphanumeric character in the retailer name.  
2. **+50 points** if the total is a whole dollar amount (e.g., ends in `.00`).  
3. **+25 points** if the total is a multiple of `0.25`.  
4. **+5 points** for every two items on the receipt.  
5. **Item description rule:** If trimmed description length is a multiple of 3, add `ceil(price * 0.2)` points.  
6. **+6 points** if the purchase date’s day is odd.  
7. **+10 points** if the purchase time is after 2:00 PM and before 4:00 PM.  

Refer to the examples in this README for detailed breakdowns.

---

## Troubleshooting

- **400 Bad Request**: Verify your JSON matches the schema (see `api.yml`) and includes all required fields.  
- **404 Not Found**: Ensure you’re using the exact UUID returned by the POST call.  
- **Port conflicts**: If port 8080 is in use, you can change the port in `src/main/resources/application.properties`:
  ```properties
  server.port=8081
  ```

---

