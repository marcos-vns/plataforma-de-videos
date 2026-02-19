# Streamly - Video and Post Platform

Streamly is a JavaFX-based desktop application designed for video sharing and social interactions. It allows users to create accounts, manage multiple channels, publish video and text content, and interact through comments and likes.

## Architectural Structure

The project follows a standard N-tier architecture to ensure separation of concerns and maintainability:

- **Model**: Contains the data entities (e.g., `User`, `Channel`, `Post`, `Video`, `TextPost`). These are POJOs (Plain Old Java Objects) representing the core business data.
- **DAO (Data Access Object)**: Responsible for all database interactions. Each major model has a corresponding DAO (e.g., `UserDAO`, `PostDAO`) that executes SQL queries and maps results back to Model objects.
- **Service**: Implements the business logic and orchestrates actions between DAOs and other services. For example, `PostService` handles the logic for publishing posts, including validation and storage of files via `FileService`.
- **Controller**: Manages the UI logic. Controllers (e.g., `DashboardController`, `StudioController`) handle user input from JavaFX FXML files and call Service methods to perform actions.
- **View (FXML)**: Defines the layout and appearance of the user interface using XML-based FXML files located in `src/resources/app/view`.

## Database Schema

The system uses a relational database with the following tables:

- **UserAccount**: Stores user credentials and profile information.
  - `id`, `name`, `username`, `email`, `password`, `profile_picture_url`
- **channels**: Stores information about video channels.
  - `id`, `name`, `subscribers`, `profile_picture_url`
- **user_channel**: Junction table for the Many-to-Many relationship between Users and Channels, including roles.
  - `user_id`, `channel_id`, `role` (OWNER, EDITOR, MODERATOR)
- **posts**: Base table for all content.
  - `id`, `channel_id`, `title`, `thumbnail_url`, `likes`, `dislikes`, `post_type` (VIDEO, TEXT), `created_at`
- **videos**: Specialized table for video posts (Inheritance/Polimorphism).
  - `post_id`, `description`, `duration_seconds`, `video_url`, `video_category` (LONG, SHORT)
- **text_posts**: Specialized table for text posts.
  - `post_id`, `content`
- **post_likes**: Tracks user reactions to posts.
  - `user_id`, `post_id`, `is_like` (Boolean)
- **comments**: Stores user comments on posts.
  - `id`, `post_id`, `user_id`, `text`, `created_at`

## Key Action Flows

### 1. User Registration & Profile
- **Flow**: User enters details in `RegisterController` -> `UserService` hashes password and saves file via `FileService` -> `UserDAO` persists to `UserAccount`.
- **Features**: Supports uploading a profile picture during registration.

### 2. User Creates a Channel
- **Flow**: Handled via `ChannelService.create(name, profilePic)` -> `ChannelDAO` saves the channel -> `UserChannelService` automatically assigns the creator as the `OWNER` in the `user_channel` table.

### 3. Adding a New Role (Member Management)
- **Flow**: A channel `OWNER` uses the Dashboard to add a member -> `UserChannelService` validates permissions and existence of the target user -> `UserChannelDAO` inserts a new record with a specific `Role` (EDITOR, MODERATOR).

### 4. Publishing Content
- **Flow**: User enters post details in the Studio -> `FileService` processes video/thumbnail files -> `PostService` validates business rules -> `PostDAO` performs a multi-table transaction to save both the base `posts` record and the specific child record (`videos` or `text_posts`).

### 5. Interaction (Likes and Comments)
- **Flow**: User clicks Like/Dislike or submits a comment -> `PostService`/`CommentService` communicates with `PostDAO`/`CommentDAO` to update reaction counts or store the new comment.

---
*Note: This documentation is updated automatically as system architectural changes occur.*
