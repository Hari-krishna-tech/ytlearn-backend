# ytlearn Backend Implementation Checklist

## 1. Project Setup
- [x] Install Java 17 and your preferred build tool (Maven/Gradle)
- [x] Create a new Spring Boot project using Spring Initializr with these dependencies:
    - [x] Spring Web
    - [x] Spring Data JPA
    - [x] Spring Security
    - [x] OAuth2 Client
    - [x] MySQL Driver

## 2. MySQL Database Setup
- [x] Install MySQL Server on your machine or use a cloud provider.
- [x] Create the database (e.g., `ytlearn_db`).
- [x] Configure the database connection in your `application.properties`:
    - [x] `spring.datasource.url`
    - [x] `spring.datasource.username`
    - [x] `spring.datasource.password`
    - [x] `spring.jpa.hibernate.ddl-auto=update` (for development)

## 3. Define Database Schema & JPA Entities
- [ ] **User Entity**
    - [ ] Fields: `id`, `googleId`, `name`, `email`, `profilePicture`, `createdAt`, `updatedAt`
- [ ] **OAuthToken Entity**
    - [ ] Fields: `id`, `user` (Foreign Key), `accessToken`, `refreshToken`, `tokenType`, `expiresAt`, `scope`, `createdAt`
- [ ] **Playlist Entity**
    - [ ] Fields: `id`, `user` (Foreign Key), `playlistId`, `title`, `description`, `totalVideos`, `lastSyncedAt`, `createdAt`, `updatedAt`
- [ ] **Video Entity**
    - [ ] Fields: `id`, `playlist` (Foreign Key), `youtubeVideoId`, `title`, `description`, `position`, `status` (e.g., COMPLETE, IN_PROGRESS, INCOMPLETE), `createdAt`, `updatedAt`
- [ ] **Note Entity**
    - [ ] Fields: `id`, `video` (Foreign Key), `content`, `createdAt`, `updatedAt`
- [ ] Create the corresponding Spring Data JPA repositories for each entity.

## 4. Configure Google OAuth2/OpenID Connect
- [ ] **Google Cloud Console Setup**
    - [ ] Create a new Google Cloud project.
    - [ ] Enable the YouTube Data API (if needed).
    - [ ] Create OAuth 2.0 credentials.
    - [ ] Set the Authorized Redirect URI (e.g., `http://localhost:8080/login/oauth2/code/google`).
- [ ] **application.properties Configuration**
    - [ ] Add properties for OAuth2 with Google (`client-id`, `client-secret`, `scope`, `redirect-uri`).
- [ ] **Security Configuration**
    - [ ] Implement a custom `SecurityConfig` class to secure endpoints and configure OAuth2 login.
- [ ] **Token Handling**
    - [ ] Create a custom OAuth2 user service (e.g., `OAuth2UserService`) to:
        - [ ] Extract user information from the OAuth2 response.
        - [ ] Extract and persist the access and refresh tokens in the `OAuthToken` table.

## 5. REST API Endpoints (Backend Controllers)
- [ ] **Authentication Endpoints**
    - [ ] Verify that Spring Security handles `/oauth2/authorization/google` and `/login/oauth2/code/google`.
- [ ] **User Endpoints** (Optional)
    - [ ] GET `/api/user` — Return authenticated user profile details.
- [ ] **Playlist Endpoints**
    - [ ] GET `/api/playlists` — Retrieve all playlists for the authenticated user.
    - [ ] POST `/api/playlists` — Create a new playlist tracker.
    - [ ] GET `/api/playlists/{playlistId}` — Get details of a specific playlist.
    - [ ] PUT `/api/playlists/{playlistId}` — Update playlist information.
    - [ ] DELETE `/api/playlists/{playlistId}` — Delete a playlist tracker.
- [ ] **Video Endpoints**
    - [ ] GET `/api/playlists/{playlistId}/videos` — List videos under a playlist.
    - [ ] PUT `/api/videos/{videoId}` — Update a video’s metadata or status.
- [ ] **Note Endpoints**
    - [ ] GET `/api/videos/{videoId}/notes` — Fetch all notes for a video.
    - [ ] POST `/api/videos/{videoId}/notes` — Add a note to a video.
    - [ ] PUT `/api/notes/{noteId}` — Update a note.
    - [ ] DELETE `/api/notes/{noteId}` — Delete a note.

## 6. YouTube Data API Integration
- [ ] Implement services to:
    - [ ] Use the stored access token from `OAuthToken` to request playlist or video data from the YouTube Data API.
    - [ ] Handle token expiration and use the refresh token to get a new access token as needed.

## 7. Frontend-Backend Integration Planning (Token & Session Management)
- [ ] Decide on the authentication architecture:
    - [ ] **Session-Based Approach (recommended):**
        - [ ] Rely on Spring Security’s default session management and HTTP-only cookies.
        - [ ] The frontend API calls automatically send the session cookie.
    - [ ] **Token-Based Approach (if using JWT):**
        - [ ] Decide how to issue and store JWT tokens securely (preferably in HTTP-only cookies).
        - [ ] Securely attach the token to each API request in the HTTP header.
- [ ] Document clearly how the frontend will authenticate and interact with the backend endpoints.
