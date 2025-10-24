# Pharmacy-Management-System
Pharmacy Management System

# 💊 Pharmacy Management System (Java + MySQL)

A **desktop-based Pharmacy Management System** built using **Java Swing** for the GUI and **MySQL** for database management.  
This project allows users to manage medicines — including adding, updating, deleting, and viewing stock — through a clean and interactive interface.

---

## 🚀 Features

- 🧾 **Add / Update / Delete** medicine records  
- 📦 **View medicine stock and pricing** in a dynamic table  
- 💰 **Track home delivery charges**  
- 🧮 Uses **BigDecimal** for precise price calculations  
- 🧱 Automatic database and table creation on first run  
- ⚡ Simple, user-friendly GUI built with **Java Swing**

---

## 🧠 Tech Stack

| Component | Technology |
|------------|-------------|
| Language | Java (JDK 17 or later) |
| GUI Library | Swing |
| Database | MySQL |
| JDBC Driver | MySQL Connector/J |
| IDE (optional) | IntelliJ IDEA / VS Code / Eclipse |

---

## 🗄️ Database Setup

The program automatically creates the database and table on first run.

**Default settings (in the code):**
```java
DB_URL  = "jdbc:mysql://localhost:3306/";
DB_NAME = "pharmacydb";
USER    = "root";
PASS    = "ayushan2831"; 
