# Identity Reconciliation Service

A Spring Boot project for advanced identity reconciliation across customer contacts (emails and phone numbers), featuring **asynchronous processing** (`@Async`) and **transactional integrity** (`@Transactional`).  
This service merges contacts into clusters based on shared attributes, ensuring deduplication and proper grouping.

---

## Features

- Create and reconcile customer contact entries via HTTP API.
- Merges contacts by shared email/phone.
- Handles tree/cluster merges, including primary/secondary rules.
- Fully covers all reconciliation edge cases.
- Utilizes async and transactional logic for scalability and consistency.

---

## Setup

### 1. Clone the Repository

git clone https://github.com/yourusername/identity-reconciliation.git

cd identity-reconciliation

### 2. Database Configuration

Update `src/main/resources/application.properties` as needed:

spring.datasource.url=jdbc:mysql://localhost:3306/customer
spring.datasource.username=root
spring.datasource.password=

text

> **Note:** Spring Boot will use environment variables (`SPRING_DATASOURCE_*`) if present, overriding the above values.[2]

---

### 3. Build & Run

./mvnw spring-boot:run

or
mvn spring-boot:run

text

---

## API Usage

### Reconcile or Create a Customer Contact

**Endpoint**: `POST /identity`

## Usage Examples

### Using cURL
**Basic request**:
```bash
curl -X POST "https://" \
   -H "Content-Type: application/json" \
   -d '{
    "phoneNumber": "8769972003", 
    "email": "vishwas7890@gmail.com"
  }'

```

---

## Example Reconciliation Scenarios

```bash
curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769472003",
    "email": "nainaisumit344@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 1,
    "emails": [],
    "phoneNumbers": [],
    "secondaryContactIds": []
  }
}


curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769489076",
    "email": "vishwas78@gmail.com"     
  }'

Response:
{
  "contact": {
    "primaryContatctId": 2,
    "emails": [],
    "phoneNumbers": [],
    "secondaryContactIds": []
  }
}


 curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769489376",
    "email": "gajjus78@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 3,
    "emails": [],
    "phoneNumbers": [],
    "secondaryContactIds": []
  }
}


 curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "9869489376",
    "email": "skumar78@gmail.com"
  }'


Response:
{
  "contact": {
    "primaryContatctId": 4,
    "emails": [],
    "phoneNumbers": [],
    "secondaryContactIds": []
  }
}


 curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769472003",
    "email": "sumitn@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 1,
    "emails": [
      "nainaisumit344@gmail.com",
      "sumitn@gmail.com"
    ],
    "phoneNumbers": [
      "8769472003"
    ],
    "secondaryContactIds": [
      5
    ]
  }
}


 curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769472003",
    "email": "sumit344@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 1,
    "emails": [
      "nainaisumit344@gmail.com",
      "sumitn@gmail.com",
      "sumit344@gmail.com"
    ],
    "phoneNumbers": [
      "8769472003"
    ],
    "secondaryContactIds": [
      5,
      6
    ]
  }
}


curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "7768923134",
    "email": "sumit344@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 1,
    "emails": [
      "nainaisumit344@gmail.com",
      "sumitn@gmail.com",
      "sumit344@gmail.com"
    ],
    "phoneNumbers": [
      "8769472003",
      "7768923134"
    ],
    "secondaryContactIds": [
      5,
      6,
      7
    ]
  }
}


curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "9869489376",
    "email": "kumars@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 4,
    "emails": [
      "skumar78@gmail.com",
      "kumars@gmail.com"
    ],
    "phoneNumbers": [
      "9869489376"
    ],
    "secondaryContactIds": [
      8
    ]
  }
}


 curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "9078904323",
    "email": "kumars@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 4,
    "emails": [
      "skumar78@gmail.com",
      "kumars@gmail.com"
    ],
    "phoneNumbers": [
      "9869489376",
      "9078904323"
    ],
    "secondaryContactIds": [
      8,
      9
    ]
  }
}


 curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769489376",
    "email": "kumars@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 3,
    "emails": [
      "gajjus78@gmail.com",
      "skumar78@gmail.com",
      "kumars@gmail.com"
    ],
    "phoneNumbers": [
      "8769489376",
      "9869489376",
      "9078904323"
    ],
    "secondaryContactIds": [
      4,
      8,
      9
    ]
  }
}


curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769472003",
    "email": "vishwas78@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 1,
    "emails": [
      "nainaisumit344@gmail.com",
      "vishwas78@gmail.com",
      "sumitn@gmail.com",
      "sumit344@gmail.com"
    ],
    "phoneNumbers": [
      "8769472003",
      "8769489076",
      "7768923134"
    ],
    "secondaryContactIds": [
      2,
      5,
      6,
      7
    ]
  }
}


curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769972003",
    "email": "vishwas7867@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 10,
    "emails": [],
    "phoneNumbers": [],
    "secondaryContactIds": []
  }
}


curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769472003",
    "email": "vishwas78@gmail.com"  
  }'

Response:
{
  "contact": {
    "primaryContatctId": 1,
    "emails": [
      "nainaisumit344@gmail.com",
      "vishwas78@gmail.com",
      "sumitn@gmail.com",
      "sumit344@gmail.com"
    ],
    "phoneNumbers": [
      "8769472003",
      "8769489076",
      "7768923134"
    ],
    "secondaryContactIds": [
      2,
      5,
      6,
      7
    ]
  }
}


 curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "8769972003", 
    "email": "vishwas7890@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 10,
    "emails": [
      "vishwas7867@gmail.com",
      "vishwas7890@gmail.com"
    ],
    "phoneNumbers": [
      "8769972003"
    ],
    "secondaryContactIds": [
      11
    ]
  }
}


curl -X POST http://localhost:8080/api/identify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "9078904323",
    "email": "sumit344@gmail.com"
  }'

Response:
{
  "contact": {
    "primaryContatctId": 1,
    "emails": [
      "nainaisumit344@gmail.com",
      "vishwas78@gmail.com",
      "gajjus78@gmail.com",
      "skumar78@gmail.com",
      "sumitn@gmail.com",
      "sumit344@gmail.com",
      "kumars@gmail.com"
    ],
    "phoneNumbers": [
      "8769472003",
      "8769489076",
      "8769489376",
      "9869489376",
      "7768923134",
      "9078904323"
    ],
    "secondaryContactIds": [
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9
    ]
  }
}
```



### Tech Stack
***Spring Boot***

***MySQL (or compatible relational DB)***