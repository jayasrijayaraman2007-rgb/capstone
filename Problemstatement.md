
# Problem Statement

## 1. Title

**Visitor Entry & Gate Pass Management System**

## 2. Domain

**Visitor Management / Security Management / Access Control**

## 3. Who is the User?

### 1. Admin

* Manages the overall system.
* Manages users, visitors, and visitor records.
* Views and monitors visitor history.
* Has permission to manage system data.

### 2. Security Officer

* Registers visitors at the entrance.
* Verifies visitor details.
* Creates and issues gate passes.
* Records visitor entry and exit times.
* Updates gate pass status.

### 3. Host / Employee

* Receives information about visitors.
* Confirms or rejects visitor visits.
* Views visitor details related to their visit.

## 4. What Problem Are We Solving?

Many organizations such as colleges, offices, and institutions still use manual registers to record visitor information. This process can be time-consuming and may result in incomplete, inaccurate, or difficult-to-track records. Security staff may also find it difficult to quickly verify visitors and track whether they have entered or exited the premises.

For example, when a visitor arrives at an organization, the security officer may have to manually write the visitor's name, contact details, purpose of visit, and host information in a register. Later, finding a particular visitor's entry or checking their exit time can be difficult.

The proposed system solves this problem by providing a digital platform for registering visitors, generating gate passes, recording entry and exit details, and maintaining visitor history.

## 5. Proposed Solution

The **Visitor Entry & Gate Pass Management System** will provide the following features:

* Visitor registration and profile management.
* Recording visitor name, contact details, ID proof, and address.
* Recording the purpose of the visit.
* Selecting the host/employee to be visited.
* Host confirmation of visitor requests.
* Generating a unique gate pass for approved visitors.
* Recording visitor entry time.
* Recording visitor exit time.
* Updating gate pass status such as Pending, Approved, Rejected, Active, and Completed.
* Searching and viewing visitor records.
* Maintaining visitor entry and exit history.
* Allowing authorized users to manage visitor information.

## 6. Core Entities / Database Tables

The system will contain the following main database tables:

### 1. Users

Stores login and user information.

**Attributes:**

* user_id
* name
* username
* password
* role

### 2. Visitors

Stores visitor personal information.

**Attributes:**

* visitor_id
* name
* phone
* email
* address
* id_proof

### 3. Hosts

Stores information about employees or persons being visited.

**Attributes:**

* host_id
* name
* department
* phone
* email

### 4. Gate_Passes

Stores gate pass information.

**Attributes:**

* pass_id
* visitor_id
* host_id
* purpose
* issue_date
* status

### 5. Entry_Exit

Stores visitor entry and exit information.

**Attributes:**

* entry_exit_id
* pass_id
* entry_time
* exit_time

### 6. Visit_Requests

Stores visitor visit requests and their approval status.

**Attributes:**

* request_id
* visitor_id
* host_id
* purpose
* request_date
* status

## 7. User Roles & Permissions

| Role                 | Permissions                                                                                                 |
| -------------------- | ----------------------------------------------------------------------------------------------------------- |
| **Admin**            | Manage users, visitors, hosts, gate passes, and view all visitor records and reports.                       |
| **Security Officer** | Register visitors, verify visitor details, generate gate passes, record entry/exit, and update pass status. |
| **Host / Employee**  | View visitor requests, approve or reject visits, and view visitors assigned to them.                        |

## 8. Success Criteria

The system will be considered successful when:

* A security officer can register a visitor within **1–2 minutes**.
* A gate pass can be generated for an approved visitor without manual paperwork.
* Visitor entry and exit times are recorded accurately.
* Authorized users can search and retrieve visitor records quickly.
* The system maintains complete visitor history.
* Only authorized users can access restricted visitor information.
* The system reduces dependency on manual visitor registers.
* Visitor records can be managed in a structured and organized manner.

## 9. Out of Scope

The following features will **not** be included in the initial version:

* Facial recognition.
* Biometric authentication.
* Automatic security-camera integration.
* SMS or WhatsApp notification services.
* Online payment processing.
* Mobile application.
* Hardware-based access control such as smart gates or RFID scanners.
* Advanced AI-based visitor verification.
* Multi-branch or large-scale enterprise deployment.

These features may be considered for future versions.

## 10. Chosen Track

**Java – Spring Boot**

### Technologies

* **Programming Language:** Java
* **Backend Framework:** Spring Boot
* **Database:** MySQL
* **API:** REST API
* **Development Environment:** Visual Studio Code
* **Version Control:** Git & GitHub
* **Build Tool:** Maven
