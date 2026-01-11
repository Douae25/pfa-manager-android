### 1. PFADossier

| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `pfa_id` | Long | Primary Key |
| `student_id` | Long | Foreign Key (User) |
| `supervisor_id` | Long | Foreign Key (User) |
| `title` | String |  |
| `description` | String |  |
| **`current_status`** | **Enum** | `[CONVENTION_PENDING, IN_PROGRESS, CLOSED, CANCELED]` |
| `updated_at` | Long |  |

### 2. Convention
 
State Enum to handle the "Two-Gate" process (Demand Approval vs. File Validation).

| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `convention_id` | Long | Primary Key |
| `pfa_id` | Long | Foreign Key (PFADossier) |
| `company_name` | String |  |
| `company_address` | String |  |
| `company_supervisor_name` | String |  |
| `company_supervisor_email` | String |  |
| `start_date` | Long |  |
| `end_date` | Long |  |
| `scanned_file_uri` | String |  |
| `is_validated` | Boolean | Helper flag (optional given the state) |
| **`state`** | **Enum** | `[DEMAND_PENDING, DEMAND_APPROVED, DEMAND_REJECTED, SIGNED_UPLOADED, UPLOAD_REJECTED, VALIDATED]` |
| `admin_comment` | String |  |

### 3. Deliverable


| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `deliverable_id` | Long | Primary Key |
| `pfa_id` | Long | Foreign Key (PFADossier) |
| `file_title` | String |  |
| `file_uri` | String |  |
| **`deliverable_type`** | **Enum** | `[BEFORE_DEFENSE, AFTER_DEFENSE]` |
| `uploaded_at` | Long |  |

### 4. Soutenance


| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `soutenance_id` | Long | Primary Key |
| `pfa_id` | Long | Foreign Key (PFADossier) |
| `location` | String |  |
| `date_soutenance` | Long |  |
| `status` | Enum | `[PLANNED, DONE]` |
| `created_at` | Long |  |

### 5. Evaluation


| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `evaluation_id` | Long | Primary Key |
| `pfa_id` | Long | Foreign Key (PFADossier) |
| `evaluator_id` | Long | Foreign Key (User) |
| `date_evaluation` | Long |  |
| `total_score` | Double |  |

### 6. EvaluationDetail


| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `detail_id` | Long | Primary Key |
| `evaluation_id` | Long | Foreign Key (Evaluation) |
| `criteria_id` | Long | Foreign Key (EvaluationCriteria) |
| `score_given` | Double |  |

### 7. EvaluationCriteria


| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `criteria_id` | Long | Primary Key |
| `label` | String |  |
| `max_points` | Int |  |
| `description` | String |  |
| `is_active` | Boolean |  |

### 8. User


| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `user_id` | Long | Primary Key |
| `email` | String | Unique |
| `password` | String |  |
| `first_name` | String |  |
| `last_name` | String |  |
| `role` | Enum | `[STUDENT, PROFESSOR, ADMIN, COORDINATOR]` |
| `phone_number` | String |  |
| `created_at` | Long |  |

### 9. Department


| Attribute Name | Data Type | Notes |
| --- | --- | --- |
| `department_id` | Long | Primary Key |
| `name` | String |  |
| `code` | String |  |
