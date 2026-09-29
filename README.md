# 🏢 Enterprise Workspace Management System

A professional desktop application for managing office/coworking workspaces, built as an **Object-Oriented Programming** academic project using Java, Java Swing, MySQL, JDBC, and the DAO architecture pattern.

---

## ✨ Features

### Employee (Role: EMPLOYEE)
- 📅 **Book Desks** — Browse available desks by zone, filter by amenities, book with conflict protection
- 🏠 **Book Meeting Rooms** — View room capacity/equipment, make time-slot bookings
- 📋 **My Bookings** — View, cancel, and manage all desk and room bookings
- ✅ **Check-In** — Check into booked desks (with 15-minute grace period)
- 🗺️ **Floor Map** — Interactive floor plan showing real-time desk availability

### Facility Manager (Role: FACILITY_MANAGER)
- All Employee features
- 📊 **Analytics Dashboard** — Occupancy rates, popular zones, booking trends, charts
- 🪑 **Desk Management** — Add, edit, change desk status, manage floor map positions
- 🚪 **Room Management** — Add, edit, update meeting room details
- 🔧 **Equipment Management** — Manage equipment inventory and room assignments

### System Administrator (Role: SYSTEM_ADMIN)
- All Facility Manager features
- 👤 **User Management** — Add, edit, disable/enable users, reset passwords, assign roles

---

## ⚙️ Setup Instructions

### Prerequisites
- Java JDK 17+
- MySQL 8.0+
- Apache Maven 3.6+

### Step 1 — Configure the Database
1. Open your MySQL Command Line or MySQL Workbench.
2. Run the following commands (assuming you are in the root folder of this project):
```sql
SOURCE database/schema.sql;
SOURCE database/seed.sql;
```

### Step 2 — Configure DB Connection
Edit `src/main/resources/db.properties` and add your MySQL root password:
```properties
db.host=localhost
db.port=3306
db.name=enterprise_workspace
db.user=root
db.password=YOUR_MYSQL_PASSWORD
db.pool.size=10
```

### Step 3 — Build and Run
Using Maven:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.workspace.management.Main"
```

Or build the runnable JAR file:
```bash
mvn clean package
java -jar target/enterprise-workspace-management-1.0.0.jar
```

---

## 🔑 Demo Credentials

| Role             | Email                        | Password      |
|------------------|------------------------------|---------------|
| System Admin     | admin@example.com            | Admin@123     |
| Facility Manager | manager@example.com          | Manager@123   |
| Employee         | john.smith@example.com       | Employee@123  |

---

## 🧪 Running Tests

```
mvn test -Dtest=PasswordUtilTest,ValidationUtilTest,BookingTest,ConcurrencyLogicTest
```

---

## 🛠️ Technology Stack

| Component        | Technology                        |
|------------------|-----------------------------------|
| Language         | Java 17+                          |
| GUI Framework    | Java Swing (FlatLaf 3.4 L&F)     |
| Database         | MySQL 8.0+                        |
| DB Access        | JDBC with DAO architecture        |
| Password Hashing | BCrypt (at.favre.lib v0.10.2)    |
| Build Tool       | Apache Maven 3.9+                 |
| Unit Testing     | JUnit 5                           |
| Background Tasks | SwingWorker                       |

---

Built as an Object-Oriented Programming academic project.
