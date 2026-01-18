# ☁️ Cloud Box Storage

A **simple Spring Boot-based cloud file storage backend** with **JWT authentication**, **per-user storage**, and **file streaming**.  
Users can **upload multiple files**, **list their files**, **stream/download**, and **delete files** securely.  

---

![CloudBox-vr](https://github.com/user-attachments/assets/013bb080-0b9f-4a48-9d36-490c8fe2fc53)


## 📋 Table of Contents

- [Features](#features)  
- [Tech Stack](#tech-stack)  
- [Setup](#setup)  
- [Configuration](#configuration)  
- [API Endpoints](#api-endpoints)  
- [File Storage Structure](#file-storage-structure)  
- [Security](#security)  
- [Future Improvements](#future-improvements)  
- [Notes](#notes)  
- [Contact / Support](#contact--support)  

---

## ✨ Features

-  Upload **multiple files** (up to 10 per request)  
-  Files stored in `/storage/{userId}/` directories  
-  Unique file names to avoid overwriting  
-  File metadata stored in the database  
-  **Stream/download files** via `/meta/{fileKey}`  
-  List user files with DTOs (hides sensitive info)  
-  Delete files **per user**  
-  JWT-based authentication  

---

## 🛠 Tech Stack

- Java 21  
- Spring Boot 3.x  
- Spring Security (JWT)  
- Spring Data JPA  
- H2 / MySQL / PostgreSQL (configurable)  
- Lombok  
- Maven / Gradle  
- REST API  

---

## ⚡ Setup

1. **Clone the repository**  
   ```bash
   git clone https://github.com/tharshan2001/CloudBox.git
   cd cloud-box-storage

2. **Build the project:**
   ./mvnw clean install

3. **Run the app:**
   ./mvnw spring-boot:run

   

## **Configuration**

API Endpoints

Authentication

/auth/register – Register a new user

/auth/login – Login and receive JWT token

Files

Upload multiple files
POST /upload
FormData: files (array, max 10 files)
Response: List of streaming URLs

Stream / download file
GET /meta/{fileKey}
Public streaming (or secure if JWT enforced)

List user files
GET /my-files
Returns FileDTO list: filename, relative path, fileKey, uploadedAt

Delete file
DELETE /delete/{fileKey}
Deletes file if owned by the user


## **File Storage Structure**       

/storage
   ├── 1                  # userId = 1
   │   ├── 1674132123123_filename1.txt
   │   └── 1674132123456_filename2.jpg
   └── 2                  # userId = 2
       └── 1674132136789_image.png


## **Security**       

JWT-based authentication

Each file is associated with a user

Only the owner can delete their files
Streaming can be made private by enforcing JWT (currently public for testing)


## **Future Improvements**   

File size and type validation

Pagination for file listing

Shareable public links

Search / filter files

Logging and analytics

Integration tests


## **Notes**   


Maximum 10 files per upload

Files are stored uniquely to prevent overwriting
Metadata is required for streaming files correctly

## **Contact / Support**

For any issues or feature requests, please contact the maintainer.
Email: aptharshan@gmail.com




   
   
