# 🕶️ VisionCompanion: Assistive IoT Dashboard

![Build Status](https://img.shields.io/badge/Status-Live-success)
![Platform](https://img.shields.io/badge/Platform-Web-blue)
![Tech Stack](https://img.shields.io/badge/Backend-Kotlin-purple)

## 📌 Project Overview
**VisionCompanion** is a specialized, highly accessible web dashboard designed to monitor and manage assistive hardware devices (like smart goggles, smart canes, or tracking devices) used by visually impaired or elderly individuals. 

Built with a focus on **Digital Inclusion**, this system allows guardians, family members, or admins to track real-time device status, battery health, and emergency contacts in a centralized, cloud-hosted environment.

## ✨ Key Features
* **🎙️ Assistive Text-to-Speech (TTS):** Integrated audio feedback that reads out the technical status, battery percentage, and owner details aloud for visually impaired admins.
* **🌓 High-Contrast Dark Mode:** A toggleable UI mode to reduce eye strain and assist users with low-vision or light sensitivity.
* **⚡ Real-time Device Management (CRUD):** Add, update, monitor, and delete connected IoT devices seamlessly.
* **📥 Data Export:** One-click functionality to download all device logs in an Excel-compatible CSV format for analysis.
* **☁️ Cloud-Ready Architecture:** Containerized using Docker and deployed on a live cloud environment.

## 🛠️ Technology Stack
* **Backend:** Kotlin (Custom HTTP Server without external heavy frameworks)
* **Database:** SQLite (Lightweight, file-based relational database)
* **Frontend:** HTML5, CSS3 (Modern Tech-Themed UI), Vanilla JavaScript
* **DevOps & Deployment:** Git, Docker, Render.com (Cloud Hosting)

## 🚀 Live Demo
The project is currently hosted and live. 
**(Note for Devender: Apni live Render website ka link yahan paste kar dena, jaise: https://visioncompanion-app.onrender.com)**

## 💻 Local Setup Instructions
If you want to run this project locally on your machine:
1. Clone the repository.
2. Ensure Java 17 and Kotlin Compiler (`kotlinc`) are installed.
3. Compile the backend: `kotlinc server.kt -include-runtime -d server.jar`
4. Run the server: `java -cp sqlite-jdbc-3.42.0.0.jar:server.jar ServerKt`
5. Open your browser and navigate to `http://localhost:8080/`