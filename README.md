# Lead Query API

A small Spring Boot API for searching and filtering CRM leads.

The API supports:

- Multi-tenant lead queries
- Role-based visibility
- Free-text search
- AND and OR filters
- System fields and custom fields
- UUID, date, timestamp, number, boolean, and text filtering
- Pagination and sorting
- Custom-field response hydration

## Technologies

- Java 21
- Spring Boot 4.1.1
- Spring JDBC
- PostgreSQL
- Liquibase
- Maven

## Requirements

Install the following before running the project:

- JDK 21 or newer
- PostgreSQL
- Git, if cloning the repository

## Database Setup

Create a PostgreSQL database named `lead_query_db`.

The current local configuration is in `src/main/resources/application.yaml`:

```yaml
url: { DB_URL }
username: { DB_USERNAME } 
password: { DB_PASSWORD }
```

Change the username, password, or database URL if your local PostgreSQL setup is different. Do not use real passwords in a shared repository.

Liquibase automatically creates the tables and inserts the sample data when the application starts. No separate seed command is required.

## Run the Application

From the project folder, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

To build the project:

```powershell
.\mvnw.cmd clean package
```

To run the tests:

```powershell
.\mvnw.cmd test
```

## Main Endpoint

```http
POST /api/v1/leads/query
```

Optional query parameters:

| Parameter | Default | Allowed values |
| --- | --- | --- |
| `page` | `1` | Integer greater than or equal to 1 |
| `limit` | `20` | Integer from 1 to 100 |
| `sortBy` | `createdAt` | `createdAt`, `followUpDate` |
| `sortDirection` | `desc` | `asc`, `desc` |

Every request requires these headers:

```text
x-tenant-id: <tenant UUID>
x-user-id: <user UUID>
x-user-role: owner | admin | manager | agent
```

Missing or invalid authentication headers return `401`.

## Simple Request

```powershell
$headers = @{
  "x-tenant-id" = "11111111-1111-1111-1111-111111111111"
  "x-user-id" = "33333333-3333-3333-3333-333333333333"
  "x-user-role" = "admin"
}

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/leads/query?page=1&limit=20" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{}'
```

The response contains:

- `data`: matching leads
- `meta.page`: current page
- `meta.limit`: page size
- `meta.totalRecords`: total matching records
- `meta.totalPages`: total number of pages

Each lead also contains its `customFields` list.

## Search Example

The `q` value searches `name`, `phone`, `email`, and `e164` using a case-insensitive partial match.

```json
{
  "q": "Ram"
}
```

## AND Filter Example

This example finds Tenant A leads assigned to Agent A1 and with a City containing Chennai.

```json
{
  "logic": "AND",
  "filters": [
    {
      "fieldId": "assignedTo",
      "fieldType": "string",
      "condition": "is",
      "value": "55555555-5555-5555-5555-555555555555"
    },
    {
      "fieldId": "dddddddd-dddd-dddd-dddd-dddddddddddd",
      "fieldType": "string",
      "condition": "contain",
      "value": "Chennai"
    }
  ]
}
```

## OR Filter Example

```json
{
  "logic": "OR",
  "filters": [
    {
      "fieldId": "name",
      "fieldType": "string",
      "condition": "contain",
      "value": "Ram"
    },
    {
      "fieldId": "name",
      "fieldType": "string",
      "condition": "contain",
      "value": "Sita"
    }
  ]
}
```

The default filter logic is `AND` when `logic` is missing.

## Multiselect Example

For `assignedTo` and `createdBy`, set `inputType` to `multiselect` and provide comma-separated UUIDs.

```json
{
  "filters": [
    {
      "fieldId": "assignedTo",
      "fieldType": "string",
      "condition": "is",
      "inputType": "multiselect",
      "value": "55555555-5555-5555-5555-555555555555,66666666-6666-6666-6666-666666666666"
    }
  ]
}
```

- `is` and `contain` match any supplied UUID.
- `is not` and `does not contain` match none of the supplied UUIDs.

## Supported Fields

### System fields

| Field ID | Database type | Supported behavior |
| --- | --- | --- |
| `name` | Text | Text conditions |
| `phone` | Text | Text conditions |
| `email` | Text | Text conditions, including null-safe checks |
| `e164` | Text | Text conditions |
| `status` | Text | Text conditions |
| `assignedTo` | UUID | Equality, multiselect, empty checks |
| `createdBy` | UUID | Equality, multiselect |
| `followUpDate` | Date | Equality, before, after, empty checks |
| `createdAt` | Timestamp with time zone | Equality, before, after, empty checks |
| `updatedAt` | Timestamp with time zone | Equality, before, after, empty checks |

Text conditions are:

- `is`
- `is not`
- `contain`
- `does not contain`
- `starts with`
- `ends with`
- `is empty`
- `is not empty`

### Custom fields

Any non-system `fieldId` must be a custom-field UUID.

Supported custom field types:

- `string`: text conditions and empty checks
- `number`: `is`, `greater than`, `less than`, empty checks
- `date`: `is`, `before`, `after`, empty checks
- `boolean`: `is` with `true` or `false`

Custom fields are stored in the EAV table `lead_custom_field_values`. Filtering uses SQL `EXISTS` and `NOT EXISTS` clauses instead of loading all leads into Java memory.

## Empty Values

- Text fields: `NULL` or an empty string
- UUID fields: `NULL`
- Date and timestamp fields: `NULL`
- Custom fields: no matching value, or a null/empty value

## Tenant and Role Visibility

Every query is restricted to the tenant from `x-tenant-id`.

- `owner`, `admin`, and `manager` see all leads in their tenant.
- `agent` sees only leads assigned to the `x-user-id` value.

A lead from another tenant is never returned.

## Seed Data IDs

The sample data includes two tenants.

| Item | UUID |
| --- | --- |
| Tenant A | `11111111-1111-1111-1111-111111111111` |
| Tenant B | `22222222-2222-2222-2222-222222222222` |
| Tenant A creator/admin | `33333333-3333-3333-3333-333333333333` |
| Tenant A second creator | `44444444-4444-4444-4444-444444444444` |
| Tenant A Agent A1 | `55555555-5555-5555-5555-555555555555` |
| Tenant A Agent A2 | `66666666-6666-6666-6666-666666666666` |
| City custom field | `dddddddd-dddd-dddd-dddd-dddddddddddd` |
| Budget custom field | `eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee` |
| Interested custom field | `ffffffff-ffff-ffff-ffff-ffffffffffff` |
| Follow Up Date custom field | `99999999-9999-9999-9999-999999999999` |

## Project Structure

```text
lead-query-api/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
└── src/
    ├── main/
    │   ├── java/com/surendhar/leadquery/
    │   │   ├── LeadQueryApiApplication.java
    │   │   ├── controller/
    │   │   │   ├── LeadQueryController.java
    │   │   │   └── TestAuthController.java
    │   │   ├── dto/
    │   │   │   ├── LeadFilter.java
    │   │   │   ├── FilterCondition.java
    │   │   │   ├── FilterFieldType.java
    │   │   │   ├── QueryLeadsRequest.java
    │   │   │   ├── LeadResponse.java
    │   │   │   ├── LeadQueryResponse.java
    │   │   │   └── CustomFieldValue.java
    │   │   ├── repository/
    │   │   │   ├── LeadQueryRepository.java
    │   │   │   └── LeadFilterSqlBuilder.java
    │   │   ├── service/
    │   │   │   ├── LeadQueryService.java
    │   │   │   └── LeadQueryValidator.java
    │   │   ├── security/
    │   │   │   ├── AuthSimulationFilter.java
    │   │   │   ├── CurrentUser.java
    │   │   │   └── CurrentUserContext.java
    │   │   ├── util/
    │   │   │   └── SystemFieldMapper.java
    │   │   └── exception/
    │   │       ├── BadRequestException.java
    │   │       └── GlobalExceptionHandler.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/changelog/
    │           ├── db.changelog-master.yaml
    │           └── 001-011 migration and seed files
    └── test/
        └── java/com/surendhar/leadquery/
            └── LeadQueryApiApplicationTests.java
```

## Design Notes

- SQL values are passed as parameters through `JdbcTemplate`.
- Database column names and sort directions come only from fixed allow-lists.
- System UUID/date/timestamp fields use Java types matching PostgreSQL types.
- Custom-field filters remain in SQL and use correlated EAV subqueries.
- Returned custom fields are loaded for the page in one additional query, avoiding N+1 queries.
- The project uses simulated request headers instead of JWT authentication.

## Known Notes

- The current database password is a local development value in `application.yaml`; replace it for another environment.
