list of **all current endpoints**.
---

### ✅ **1. TeamFormationController**

| Method  | Endpoint                                 | Purpose                                     |
| ------- | ---------------------------------------- | ------------------------------------------- |
| GET     | `/api/team-formations/{id}`              | Get team formation by ID                    |
| GET     | `/api/team-formations/case/{caseId}`     | Get team formation by case ID               |
|  POST | `/api/team-formations`                   | (Used internally) Create a team manually    |
|  PUT  | `/api/team-formations/{id}/response`     | Team member accepts/rejects notification    |
|  GET  | `/api/team-formations/pending-responses` | 🔍 View pending responses from team members |
| POST  | 	`/api/team-formations/manual`           |  Manually create a team formation           |
|  GET  | 	`/api/team-formations/teams`	              |  Get all added team details                 |

---

### ✅ **2. ReportController**

| Method | Endpoint                                | Purpose                                                    |
| ------ | --------------------------------------- | ---------------------------------------------------------- |
| DELETE | `/api/reports/{id}`                     | Delete a report                                            |
| GET    | `/api/reports/{id}`                     | Get report by ID                                           |
| GET    | `/api/reports/team-member/{personId}`   | Get all reports submitted by a team member                 |
| GET    | `/api/reports/case/{caseId}`            | Get all reports for a given case                           |
| GET    | `/api/reports/case/{caseId}/department` | Get reports grouped by department for a case               |
| GET    | `/api/reports/case/{caseId}/final`      | ✅ Get final compiled (merged) report for the case          |
| POST   | `/api/reports`                          | Submit a report (one per dept per case)                    |
| POST   | `/api/reports/merge`                    | 🔐 Manually merge reports (only by higher-rank supervisor) |
| PUT    | `/api/reports/{id}`                     | Update a submitted report                                  |

---

### ✅ **3. PersonController**

| Method | Endpoint             | Purpose                    |
| ------ | -------------------- | -------------------------- |
| DELETE | `/api/persons/{id}`  | Delete person              |
| GET    | `/api/persons/{id}`  | Get person by ID           |
| GET    | `/api/persons`       | List all persons           |
| POST   | `/api/persons`       | Create/register new person |
| POST   | `/api/persons/login` | Authenticate (login)       |
| POST   | `/api/persons/bulk`  | Bulk import persons        |
| PUT    | `/api/persons/{id}`  | Update person information  |

---

### ✅ **4. CaseController**

| Method | Endpoint                       | Purpose                        |
|------| ------------------------------ | ------------------------------ |
| GET  | `/api/cases/{id}`              | Get case by ID                 |
| GET  | `/api/cases`                   | Get all cases                  |
| POST | `/api/cases`                   | Submit/register a new case     |
| PUT  | `/api/cases/{id}`              | Update case                    |
|  GET | `/api/cases/{id}/status`       | 🔍 View current case status    |
---

### ✅ **5. DepartmentController**

| Method | Endpoint                               | Purpose                         |
| ------ | -------------------------------------- | ------------------------------- |
| DELETE | `/api/departments/{id}`                | Delete department               |
| GET    | `/api/departments/{id}`                | Get department by ID            |
| GET    | `/api/departments`                     | List all departments            |
| GET    | `/api/departments/name/{name}`         | Get department by name          |
| POST   | `/api/departments`                     | Create department               |
| PUT    | `/api/departments/{id}`                | Update department               |

---

### ✅ **6. PostController**

| Method | Endpoint          | Purpose        |
| ------ | ----------------- | -------------- |
| DELETE | `/api/posts/{id}` | Delete post    |
| GET    | `/api/posts/{id}` | Get post by ID |
| GET    | `/api/posts`      | Get all posts  |
| POST   | `/api/posts`      | Create post    |
| PUT    | `/api/posts/{id}` | Update post    |

---

### ✅ **7. AuthController**

| Method | Endpoint       | Purpose                |
| ------ | -------------- | ---------------------- |
| POST   | `/auth/login`  | Authenticate and login |
| POST   | `/auth/logout` | Logout                 |

---

### ✅ **8. HiController** (for testing)

| Method | Endpoint | Purpose     |
| ------ | -------- | ----------- |
| GET    | `/hi`    | Ping server |

---
 
