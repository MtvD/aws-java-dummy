# Requirements Document

## Introduction

Hệ thống backend lưu trữ file (giống Google Drive) được xây dựng bằng Java Spring Boot, triển khai trên AWS. Hệ thống hỗ trợ upload/download file, quản lý quyền truy cập, phân biệt file nội bộ (LAN) và file public (Internet). Project được thiết kế làm demo cho người học DevOps với Terragrunt, đồng thời tuân thủ chuẩn OOP và SOLID.

## Glossary

- **Storage_Service**: Dịch vụ backend chính xử lý logic lưu trữ và quản lý file
- **File_Entity**: Đối tượng đại diện cho một file trong hệ thống, bao gồm metadata và thông tin quyền truy cập
- **Access_Level**: Mức độ truy cập của file, gồm INTERNAL (nội bộ LAN) và PUBLIC (Internet)
- **Permission**: Quyền thao tác trên file, gồm READ, WRITE, DELETE
- **User**: Người dùng hệ thống, được xác thực qua JWT token
- **Folder**: Thư mục ảo để tổ chức file theo cấu trúc cây
- **S3_Bucket**: AWS S3 bucket dùng để lưu trữ file thực tế
- **Internal_Network**: Mạng LAN nội bộ công ty, được xác định qua IP range

## Requirements

### Requirement 1: Upload File

**User Story:** As a User, I want to upload files to the system, so that I can store and manage my files centrally.

#### Acceptance Criteria

1. WHEN a User uploads a file with valid metadata, THE Storage_Service SHALL store the file in S3_Bucket and create a corresponding File_Entity record in the database
2. WHEN a User uploads a file, THE Storage_Service SHALL validate the file size does not exceed 100MB
3. IF a User uploads a file exceeding the size limit, THEN THE Storage_Service SHALL reject the upload and return an error message with the maximum allowed size
4. WHEN a file is uploaded successfully, THE Storage_Service SHALL return the File_Entity metadata including file ID, name, size, and upload timestamp
5. IF a User uploads a file with a duplicate name in the same Folder, THEN THE Storage_Service SHALL append a numeric suffix to the file name

### Requirement 2: Download File

**User Story:** As a User, I want to download files from the system, so that I can retrieve my stored files when needed.

#### Acceptance Criteria

1. WHEN a User requests to download a file with valid Permission, THE Storage_Service SHALL generate a pre-signed URL from S3_Bucket and return it to the User
2. IF a User requests to download a file without READ Permission, THEN THE Storage_Service SHALL reject the request and return a 403 Forbidden response
3. WHEN a pre-signed URL is generated, THE Storage_Service SHALL set the URL expiration to 15 minutes
4. IF a User requests to download a non-existent file, THEN THE Storage_Service SHALL return a 404 Not Found response

### Requirement 3: Permission Management

**User Story:** As a User, I want to manage access permissions on my files, so that I can control who can view, edit, or delete my files.

#### Acceptance Criteria

1. WHEN a file owner grants Permission to another User, THE Storage_Service SHALL create a Permission record linking the User to the File_Entity with the specified access type
2. WHEN a file owner revokes Permission from a User, THE Storage_Service SHALL remove the corresponding Permission record
3. THE Storage_Service SHALL support three Permission types: READ, WRITE, and DELETE
4. IF a non-owner User attempts to modify Permission on a File_Entity, THEN THE Storage_Service SHALL reject the request and return a 403 Forbidden response
5. WHEN a User queries permissions for a File_Entity, THE Storage_Service SHALL return the list of all Users and their Permission types for that file

### Requirement 4: Access Level Management (Internal/Public)

**User Story:** As a User, I want to set files as internal (LAN only) or public (Internet accessible), so that I can control the network scope of file access.

#### Acceptance Criteria

1. WHEN a file owner sets Access_Level to INTERNAL, THE Storage_Service SHALL restrict access to requests originating from Internal_Network IP ranges only
2. WHEN a file owner sets Access_Level to PUBLIC, THE Storage_Service SHALL allow access from any network
3. IF a request for an INTERNAL file originates from outside Internal_Network, THEN THE Storage_Service SHALL reject the request and return a 403 Forbidden response with a message indicating internal-only access
4. WHEN a File_Entity is created, THE Storage_Service SHALL default the Access_Level to INTERNAL
5. WHEN a file owner changes Access_Level, THE Storage_Service SHALL log the change with timestamp, previous level, and new level

### Requirement 5: Folder Management

**User Story:** As a User, I want to organize files into folders, so that I can maintain a structured file hierarchy.

#### Acceptance Criteria

1. WHEN a User creates a Folder, THE Storage_Service SHALL create a Folder record with the specified name and parent Folder reference
2. WHEN a User moves a File_Entity to a different Folder, THE Storage_Service SHALL update the File_Entity parent Folder reference
3. IF a User attempts to create a Folder with a duplicate name under the same parent Folder, THEN THE Storage_Service SHALL reject the request and return a 409 Conflict response
4. WHEN a User deletes a Folder, THE Storage_Service SHALL move all contained File_Entity and sub-Folder items to the parent Folder
5. WHEN a User lists contents of a Folder, THE Storage_Service SHALL return all File_Entity and sub-Folder items with their metadata

### Requirement 6: User Authentication

**User Story:** As a User, I want to authenticate with the system, so that my identity is verified and my files are secured.

#### Acceptance Criteria

1. WHEN a User provides valid credentials, THE Storage_Service SHALL issue a JWT token with a 24-hour expiration
2. IF a User provides invalid credentials, THEN THE Storage_Service SHALL return a 401 Unauthorized response
3. WHEN a User sends a request with a valid JWT token, THE Storage_Service SHALL extract the User identity and authorize the request
4. IF a User sends a request with an expired or invalid JWT token, THEN THE Storage_Service SHALL return a 401 Unauthorized response
5. THE Storage_Service SHALL hash all User passwords using BCrypt before storing them in the database

### Requirement 7: File Search

**User Story:** As a User, I want to search for files by name or metadata, so that I can quickly find specific files.

#### Acceptance Criteria

1. WHEN a User searches with a query string, THE Storage_Service SHALL return all File_Entity items where the file name contains the query string (case-insensitive)
2. WHEN search results are returned, THE Storage_Service SHALL only include File_Entity items the User has READ Permission for
3. WHEN search results are returned, THE Storage_Service SHALL paginate results with a default page size of 20 items
