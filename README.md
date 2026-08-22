# 🏢 TechCorp HR Management Portal

An Enterprise Workforce & HR Analytics Dashboard built using **Kotlin HTTP Server**, **Persistent Storage**, and modern **Web Technologies**.

---

## 🌟 Features

* **Dual-Layer Security Architecture:**
  * **Portal Login PIN:** Protected entry requiring secure PIN (`9999`) to access the dashboard.
  * **Admin Action Security:** Critical operations like **Edit** and **Delete** require high-level authorization PIN (`66372`).
* **Live HR Analytics:** Real-time counters for Total Employees, Work From Home (WFH), and On Leave.
* **Employee Management:** Complete CRUD workflows (Add, View, Edit, Delete employee records).
* **Text-to-Speech (TTS) Voice Assistance:** Voice output for employee department and status overview.
* **Live Search & Filter:** Instant workforce search by Name, Employee ID, or Department.
* **Export to Excel/CSV:** One-click data export directly into spreadsheet format.
* **Persistent File Storage:** Seamless server recovery with persistent data backup preventing data loss on restarts.
* **Dark / Light Mode:** Built-in UI toggle for improved visual experience.

---

## 🛠️ Tech Stack

* **Backend:** Kotlin (Native `com.sun.net.httpserver.HttpServer`)
* **Storage:** Persistent File-Based CSV Storage (`employees_data.csv`)
* **Frontend:** HTML5, CSS3, Modern JavaScript (ES6)
* **Deployment:** Render Cloud Platform

---

## 🚀 Local Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/](https://github.com/)<devender01562/college-project-for-internship.git
   cd college-project-for-internship