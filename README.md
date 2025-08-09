# UndoSchool Course Search API

A Spring Boot application that provides a comprehensive course search API using Elasticsearch. This application allows searching, filtering, and sorting educational courses with full-text search capabilities, autocomplete suggestions, and fuzzy search functionality.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Data Ingestion](#data-ingestion)
- [API Documentation](#api-documentation)
  - [Course Search API](#course-search-api)
  - [Autocomplete Suggestions API](#autocomplete-suggestions-api)
- [Example Requests and Responses](#example-requests-and-responses)
- [Fuzzy Search Examples](#fuzzy-search-examples)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose
- curl (for testing API endpoints)

## Quick Start

Follow these steps to get the application running:

### 1. Launch Elasticsearch

```bash
# Start Elasticsearch using Docker Compose
docker-compose up -d

# Verify Elasticsearch is running
curl http://localhost:9200
```

Expected response:
```json
{
  "name" : "",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "-hw",
  "version" : {
    "number" : "8.13.4",
    "build_flavor" : "default",
    "build_type" : "docker",
    "build_hash" : "da95df118650b55a500dcc181889ac35c6d8da7c",
    "build_date" : "",
    "build_snapshot" : false,
    "lucene_version" : "9.10.0",
    "minimum_wire_compatibility_version" : "7.17.0",
    "minimum_index_compatibility_version" : "7.0.0"
  },
  "tagline" : "You Know, for Search"
}
```

### 2. Build and Run the Spring Boot Application

```bash
# Build the application
./mvnw clean compile

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Verify Data Ingestion

The application automatically loads sample course data on startup. Check the logs for:
```
Courses loaded into Elasticsearch: [number of courses]
```

You can also verify by checking the Elasticsearch index:
```bash
curl "http://localhost:9200/courses/_count"
```

## Configuration

### Application Properties

The application uses the following Elasticsearch configuration in `src/main/resources/application.properties`:

```properties
# Application name
spring.application.name=undoschool

# Elasticsearch connection settings
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.username=
spring.elasticsearch.password=

# Custom index name for autocomplete features
courses-v2=courses_v2
```

### Customizing Elasticsearch Connection

To connect to a different Elasticsearch instance, modify the `application.properties`:

```properties
# For remote Elasticsearch
spring.elasticsearch.uris=http://your-elasticsearch-host:9200

# For authenticated Elasticsearch
spring.elasticsearch.username=your-username
spring.elasticsearch.password=your-password
```

### Data Ingestion

The application automatically loads course data from `src/main/resources/sample-course.json` on startup using the `@PostConstruct` method in `CourseLoader` class.

**How to trigger data ingestion:**
- Data is automatically loaded when the application starts
- If you need to reload data, restart the application
- The data includes 34 sample courses across various categories

**How to verify data ingestion:**
1. Check application logs for: `"Courses loaded into Elasticsearch: X"`
2. Query Elasticsearch directly:
   ```bash
   curl "http://localhost:9200/courses/_count"
   ```
3. Use the search API to verify data:
   ```bash
   curl "http://localhost:8080/api/search"
   ```

## API Documentation

### Course Search API

**Endpoint:** `GET /api/search`

**Description:** Search and filter courses with pagination and sorting options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | String | No | Search keyword for title and description | `algebra` |
| `minAge` | Integer | No | Minimum age filter | `12` |
| `maxAge` | Integer | No | Maximum age filter | `15` |
| `category` | String | No | Exact category match | `Math` |
| `type` | String | No | Exact type match | `COURSE` |
| `minPrice` | Double | No | Minimum price filter | `100.0` |
| `maxPrice` | Double | No | Maximum price filter | `200.0` |
| `startDate` | String | No | Filter courses on/after date (ISO-8601) | `2025-09-01` |
| `sort` | String | No | Sort order: `upcoming`, `priceAsc`, `priceDesc` | `priceAsc` |
| `page` | Integer | No | Page number (0-based) | `0` |
| `size` | Integer | No | Results per page | `10` |

**Response Format:**
```json
{
  
  "total": 53,
  "page": 0,
  "size": 10,
  "courses": [
    {
      "id": "1",
      "description": "Introduction to Algebra",
      "title": "Algebra Basics",
      "category": "Math",
      "type": "COURSE",
      "gradeRange": null,
      "minAge": 12,
      "maxAge": 15,
      "price": 100,
      "nextSessionDate": "2025-08-08T00:04:09.205302Z"
    },
    {
      "id": "2",
      "description": "Fun painting classes",
      "title": "Painting for Kids",
      "category": "Art",
      "type": "ONE_TIME",
      "gradeRange": null,
      "minAge": 9,
      "maxAge": 12,
      "price": 50,
      "nextSessionDate": "2025-08-08T00:04:09.205Z"
    }
  ]
}
```

### Autocomplete Suggestions API

**Endpoint:** `GET /api/search/suggest`

**Description:** Get autocomplete suggestions for course titles.

**Query Parameters:**

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | String | Yes | Partial title for suggestions | `Ch` |

**Response Format:**
```json
[
  "Chemistry Chaos",
  "Chess Club",
  "Choir Club"
]
```

## Example Requests and Responses

### 1. Basic Search - All Courses

**Request:**
```bash
curl "http://localhost:8080/api/search"
```

**Expected Response:**
```json
{
  
  "total": 53,
  "page": 0,
  "size": 10,
  "courses": [
    {
      "id": "1",
      "description": "Introduction to Algebra",
      "title": "Algebra Basics",
      "category": "Math",
      "type": "COURSE",
      "gradeRange": null,
      "minAge": 12,
      "maxAge": 15,
      "price": 100,
      "nextSessionDate": "2025-08-08T00:04:09.205302Z"
    },
    {
      "id": "2",
      "description": "Fun painting classes",
      "title": "Painting for Kids",
      "category": "Art",
      "type": "ONE_TIME",
      "gradeRange": null,
      "minAge": 9,
      "maxAge": 12,
      "price": 50,
      "nextSessionDate": "2025-08-08T00:04:09.205Z"
    }
  ]
}
```

### 2. Search by Keyword

**Request:**
```bash
curl "http://localhost:8080/api/search?q=algebra"
```

**Expected Behavior:** Returns courses with "algebra" in title or description 

**Expected Total:** 3 courses (Introduction to Algebra, Pre-Algebra Power,Algebra Basics)

### 3. Filter by Category

**Request:**
```bash
curl "http://localhost:8080/api/search?category=Math"
```

**Expected Behavior:** Returns only Math category courses
**Expected Total:** 8 courses

### 4. Filter by Age Range

**Request:**
```bash
curl "http://localhost:8080/api/search?minAge=12&maxAge=15"
```

**Expected Behavior:** Returns courses suitable for ages 12-15
**Expected Total:** Multiple courses within age range

### 5. Filter by Price Range

**Request:**
```bash
curl "http://localhost:8080/api/search?minPrice=100&maxPrice=200"
```

**Expected Behavior:** Returns courses priced between $100-$200
**Expected Total:** Multiple courses within price range

### 6. Filter by Course Type

**Request:**
```bash
curl "http://localhost:8080/api/search?type=ONE_TIME"
```

**Expected Behavior:** Returns only one-time courses
**Expected Total:** 10 courses

### 7. Filter by Date Range (Not Working)

**Request:**
```bash
curl "http://localhost:8080/api/search?startDate=2025-09-01"
```

**Expected Behavior:** Returns courses starting on or after September 1, 2025
**Expected Total:** Courses scheduled for September onwards

### 8. Sort by Price (Ascending)

**Request:**
```bash
curl "http://localhost:8080/api/search?sort=priceAsc"
```

**Expected Behavior:** Returns courses sorted by price (lowest first)
**Expected Order:** DIY Science Fair Projects ($30) should be first

### 9. Sort by Price (Descending)

**Request:**
```bash
curl "http://localhost:8080/api/search?sort=priceDesc"
```

**Expected Behavior:** Returns courses sorted by price (highest first)

**Expected Order:** Mobile App Development ($275) should be first

### 10. Complex Filter Combination

**Request:**
```bash
curl "http://localhost:8080/api/search?q=science&category=Science&minPrice=50&maxPrice=150&sort=priceAsc&page=0&size=5"
```

**Expected Behavior:** 
- Full-text search for "science"
- Only Science category
- Price between $50-$150
- Sorted by price ascending
- First 5 results
**Expected Response**
```json
{
  "total": 1,
  "page": 0,
  "size": 5,
  "courses": [
    {
      "id": "course_048",
      "description": "Join us to work on projects that help our planet, from recycling to local clean-up efforts.",
      "title": "Environmental Science Club",
      "category": "Science",
      "type": "CLUB",
      "gradeRange": "7th–12th",
      "minAge": 12,
      "maxAge": 18,
      "price": 50,
      "nextSessionDate": "2025-10-14T16:00:00.000Z"
    }
  ]
}
```
### 11. Pagination Example

**Request:**
```bash
curl "http://localhost:8080/api/search?page=1&size=5"
```

**Expected Behavior:** Returns results 6-10 (second page)

## Autocomplete Examples

### 1. Chemistry Suggestions

**Request:**
```bash
curl "http://localhost:8080/api/search/suggest?q=Cre"
```

**Expected Response:**
```json
[
  "Creative Canvas Painting",
  "Creative Writing Club"
]
```

### 2. Math Suggestions

**Request:**
```bash
curl "http://localhost:8080/api/search/suggest?q=math"
```

**Expected Response:**
```json
[
  "Math Olympiad Prep"
]
```



## Fuzzy Search Examples

The search API includes fuzzy matching to handle typos and variations in search terms.


### 1. Typo in "Robotics"

**Request:**
```bash
curl "http://localhost:8080/api/search?q=robotic"
```

**Expected Behavior:** Returns "Robotics Club" course (fuzzy matching)
**Expected Response:** Should include the course with id "course_003"

### 2. Typo in "Chemistry"

**Request:**
```bash
curl "http://localhost:8080/api/search?q=chemestry"
```

**Expected Behavior:** Returns "Chemistry Chaos" course despite typo
**Expected Response:** Should include the course with id "course_011"

### 3. Typo in "Guitar"

**Request:**
```bash
curl "http://localhost:8080/api/search?q=gitar"
```

**Expected Behavior:** Returns "Guitar for Beginners" course
**Expected Response:** Should include the course with id "course_014"




## Troubleshooting

### Common Issues

1. **Elasticsearch not responding:**
   ```bash
   # Check if Elasticsearch is running
   docker-compose ps
   
   # Restart if needed
   docker-compose restart elasticsearch
   ```

2. **No search results:**
   ```bash
   # Check if data is indexed
   curl "http://localhost:9200/courses/_search?size=1"
   ```

3. **Connection refused:**
   - Verify Elasticsearch is running on port 9200
   - Check application.properties for correct URL

4. **Application won't start:**
   - Check Java version (requires Java 21+)
   - Verify Maven dependencies: `mvn dependency:resolve`

### Elasticsearch Index Management

```bash
# Check index status
curl "http://localhost:9200/courses"

# Delete index (if needed)
curl -X DELETE "http://localhost:9200/courses"

# Recreate index (restart application after deletion)
```

### Logs and Debugging

- Application logs show data loading progress
- Enable DEBUG logging in `application.properties`:
  ```properties
  logging.level.com.undoschool.undoschool=DEBUG
  ```

## Technical Implementation Details

- **Framework:** Spring Boot 3.5.4
- **Search Engine:** Elasticsearch 8.13.4
- **Java Version:** 21
- **Key Features:**
  - Full-text search with multi-match queries
  - Fuzzy search with AUTO fuzziness
  - Range filtering for age and price
  - Exact term filtering for category and type
  - Date filtering for session dates
  - Multiple sorting options
  - Pagination support
  - Autocomplete suggestions using completion suggester
  - Integration testing with TestContainers

---

**Developed By:** Vedansh Shrivastava 

**Version:** 1.0.0  

**Last Updated:** August 2025
