# Implementation Plan: File Storage System

## Overview

Triển khai hệ thống File Storage bằng Java Spring Boot theo layered architecture. Mỗi task xây dựng incremental trên task trước, bắt đầu từ project setup, data models, rồi đến business logic và API endpoints.

## Tasks

- [x] 1. Set up project structure and dependencies
  - Tạo Spring Boot project với Maven (Java 17+)
  - Thêm dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, AWS SDK S3, jjwt (JWT), jqwik (property testing), Lombok, Testcontainers
  - Tạo `application.yml` với cấu hình cho PostgreSQL, S3, JWT secret, internal IP ranges
  - Tạo package structure: `controller`, `service`, `repository`, `model`, `dto`, `config`, `exception`, `security`
  - _Requirements: All_

- [x] 2. Implement data models and enums
  - [x] 2.1 Create enums `AccessLevel` (INTERNAL, PUBLIC) and `PermissionType` (READ, WRITE, DELETE)
    - _Requirements: 3.3, 4.1_
  - [x] 2.2 Create JPA entities: `User`, `FileEntity`, `Folder`, `Permission`, `AccessLog`
    - Implement entity relationships theo ER diagram trong design
    - Set `AccessLevel` default to `INTERNAL` trong `FileEntity`
    - _Requirements: 1.1, 3.1, 4.4, 5.1_
  - [x] 2.3 Create all Request/Response DTOs as Java records
    - `FileUploadRequest`, `GrantPermissionRequest`, `CreateFolderRequest`, `LoginRequest`, `RegisterRequest`, `ChangeAccessLevelRequest`
    - `FileResponse`, `DownloadResponse`, `FolderResponse`, `FolderContentsResponse`, `PermissionResponse`, `AuthResponse`, `SearchResponse`, `ErrorResponse`
    - _Requirements: 1.4, 2.1_

- [x] 3. Implement exception handling
  - [x] 3.1 Create `StorageException` abstract class và các subclass: `FileNotFoundException`, `AccessDeniedException`, `FileTooLargeException`, `DuplicateFolderException`, `InvalidCredentialsException`, `StorageBackendException`
    - _Requirements: 1.3, 2.2, 2.4, 3.4, 4.3, 5.3, 6.2_
  - [x] 3.2 Create `GlobalExceptionHandler` with `@RestControllerAdvice` to map exceptions to `ErrorResponse`
    - _Requirements: 1.3, 2.2, 2.4, 3.4, 4.3, 5.3, 6.2_

- [x] 4. Implement repository layer
  - [x] 4.1 Create Spring Data JPA repositories: `UserRepository`, `FileRepository`, `FolderRepository`, `PermissionRepository`, `AccessLogRepository`
    - `FileRepository`: thêm method `findByNameAndFolderId`, `findByNameContainingIgnoreCaseAndIdIn`
    - `PermissionRepository`: thêm method `findByUserIdAndFileIdAndType`, `findByFileId`, `findByUserIdAndFileId`
    - `FolderRepository`: thêm method `findByNameAndParentFolderId`, `findByParentFolderId`
    - _Requirements: 1.5, 3.5, 5.3, 5.5, 7.1_

- [x] 5. Implement core interfaces and S3 storage
  - [x] 5.1 Create interfaces: `StorageStrategy`, `AccessValidator`, `PermissionChecker`
    - _Requirements: All (SOLID foundation)_
  - [x] 5.2 Implement `S3StorageStrategy` implementing `StorageStrategy`
    - Upload file to S3, download from S3, generate pre-signed URL (15 min expiry), delete from S3
    - _Requirements: 1.1, 2.1, 2.3_
  - [x] 5.3 Create S3 configuration class with `S3Client` and `S3Presigner` beans
    - _Requirements: 1.1, 2.1_

- [x] 6. Implement authentication (JWT + BCrypt)
  - [x] 6.1 Implement `JwtProvider` for token generation (24h expiry) and validation
    - _Requirements: 6.1, 6.3, 6.4_
  - [x] 6.2 Implement `AuthServiceImpl` with register (BCrypt password hashing) and login
    - _Requirements: 6.1, 6.2, 6.5_
  - [x] 6.3 Implement `JwtAuthenticationFilter` extending `OncePerRequestFilter`
    - Extract JWT from Authorization header, validate, set SecurityContext
    - _Requirements: 6.3, 6.4_
  - [x] 6.4 Create `SecurityConfig` with Spring Security configuration
    - Configure stateless session, permit auth endpoints, require authentication for others
    - _Requirements: 6.1, 6.3_
  - [x] 6.5 Implement `AuthController` with POST /register and POST /login
    - _Requirements: 6.1, 6.2_
  - [ ]* 6.6 Write property test for JWT round-trip (Property 12)
    - **Property 12: JWT encode/decode round-trip**
    - **Validates: Requirements 6.3**
  - [ ]* 6.7 Write property test for password hashing (Property 13)
    - **Property 13: Password hashing**
    - **Validates: Requirements 6.5**

- [x] 7. Checkpoint - Ensure authentication works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Implement network access control
  - [x] 8.1 Implement `NetworkAccessValidator` implementing `AccessValidator`
    - Check client IP against configured internal IP ranges
    - PUBLIC files allow all IPs, INTERNAL files allow only internal IPs
    - _Requirements: 4.1, 4.2, 4.3_
  - [ ]* 8.2 Write property test for network access control (Property 8)
    - **Property 8: Network access control**
    - **Validates: Requirements 4.1, 4.2**

- [x] 9. Implement permission service
  - [x] 9.1 Implement `PermissionServiceImpl` implementing `PermissionChecker`
    - Grant permission (owner check), revoke permission (owner check), check permission, list permissions
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  - [ ]* 9.2 Write property test for permission grant/revoke round-trip (Property 5)
    - **Property 5: Permission grant/revoke round-trip**
    - **Validates: Requirements 3.1, 3.2**
  - [ ]* 9.3 Write property test for owner-only permission modification (Property 6)
    - **Property 6: Only owners can modify permissions**
    - **Validates: Requirements 3.4**

- [x] 10. Implement file service
  - [x] 10.1 Implement `FileServiceImpl`
    - Upload: validate size (≤100MB), handle duplicate names (append suffix), store to S3, save entity with default INTERNAL access level
    - Download: check permission + network access, generate pre-signed URL
    - Change access level: owner check, update entity, create AccessLog
    - Delete: owner check, remove from S3 and database
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 4.4, 4.5_
  - [ ] 10.2 Write property test for upload creates matching record (Property 1)
    - **Property 1: Upload creates matching record**
    - **Validates: Requirements 1.1, 1.4**
  - [ ]* 10.3 Write property test for file size validation (Property 2)
    - **Property 2: File size validation boundary**
    - **Validates: Requirements 1.2**
  - [ ]* 10.4 Write property test for duplicate name suffix (Property 3)
    - **Property 3: Duplicate name suffix generation**
    - **Validates: Requirements 1.5**
  - [ ]* 10.5 Write property test for download access (Property 4)
    - **Property 4: Download access equals READ permission**
    - **Validates: Requirements 2.1, 2.2**
  - [ ]* 10.6 Write property test for default access level (Property 9)
    - **Property 9: Default access level is INTERNAL**
    - **Validates: Requirements 4.4**
  - [ ]* 10.7 Write property test for access level change logging (Property 10)
    - **Property 10: Access level change logging**
    - **Validates: Requirements 4.5**

- [x] 11. Implement folder service
  - [x] 11.1 Implement `FolderServiceImpl`
    - Create folder (check duplicate name), list contents, move file to folder, delete folder (move children to parent)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  - [ ]* 11.2 Write property test for folder deletion moves children (Property 11)
    - **Property 11: Folder deletion moves children to parent**
    - **Validates: Requirements 5.4**

- [x] 12. Implement search service
  - [x] 12.1 Implement `SearchServiceImpl`
    - Search by name (case-insensitive), filter by READ permission, paginate (default 20)
    - _Requirements: 7.1, 7.2, 7.3_
  - [ ]* 12.2 Write property test for search returns permitted matching files (Property 14)
    - **Property 14: Search returns permitted matching files**
    - **Validates: Requirements 7.1, 7.2**
  - [ ]* 12.3 Write property test for search pagination (Property 15)
    - **Property 15: Search pagination**
    - **Validates: Requirements 7.3**

- [x] 13. Checkpoint - Ensure all service layer tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 14. Implement REST controllers
  - [x] 14.1 Implement `FileController` (upload, download, get metadata, change access level, delete)
    - Extract authenticated user from SecurityContext
    - Extract client IP from HttpServletRequest for network access check
    - _Requirements: 1.1, 1.4, 2.1, 4.1_
  - [x] 14.2 Implement `FolderController` (create, list contents, update, delete)
    - _Requirements: 5.1, 5.4, 5.5_
  - [x] 14.3 Implement `PermissionController` (grant, revoke, list permissions)
    - _Requirements: 3.1, 3.2, 3.5_
  - [x] 14.4 Implement `SearchController` (search with pagination)
    - _Requirements: 7.1, 7.2, 7.3_
  - [ ]* 14.5 Write property test for permission query completeness (Property 7)
    - **Property 7: Permission query completeness**
    - **Validates: Requirements 3.5**

- [ ] 15. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik
- Unit tests validate specific examples and edge cases using JUnit 5
- Project sử dụng Java 17+, Spring Boot 3.x, Maven
