# Mycellium Codebase Guide

This document explains the project from the inside out. It starts with the most important project code under `src/main/java` and `src/main/resources`, then explains Maven, runtime configuration, and how requests move through the application.

## 1. Big Picture

Summary:

This section explains what the whole project is trying to do. In simple terms, Mycellium is a website where students discover and register for campus events, while organizers create and manage those events. The codebase is arranged so controllers handle web requests, models represent database data, repositories talk to the database, and templates create the pages users see.

Mycellium is a Spring Boot web application for campus events. It has two main user roles:

- `STUDENT`: browses published events, registers for events, views their dashboard, cancels registrations, and sees notifications.
- `ORGANIZER`: creates and manages events, uploads event images, edits event details, creates event timelines and segments, and views registrations.

The app follows a classic Spring MVC structure:

```text
Browser
  -> Controller classes in src/main/java/.../controller
  -> Repository interfaces in src/main/java/.../repository
  -> Entity/model classes in src/main/java/.../model
  -> MySQL database tables
  -> Thymeleaf templates in src/main/resources/templates
  -> HTML response back to browser
```

The app uses Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, MySQL, BCrypt password hashing, Cloudinary image uploads, and SQL migration files.

## 2. Main Java Package

Summary:

This section explains where the main Java code lives and how it is grouped. The package structure keeps similar files together: configuration files set up the app, controllers receive browser requests, models describe database tables, and repositories perform database operations. Knowing these folders makes it much easier to find the part of the project you need to change.

All Java code lives under `src/main/java/com/mycellium/mycellium`.

- `config`: Spring configuration beans and web resource setup.
- `controller`: MVC route handlers.
- `model`: JPA entity classes mapped to database tables.
- `repository`: Spring Data JPA interfaces for database queries.
- `MycelliumApplication.java`: the Spring Boot entry point.

## 3. Entry Point

Summary:

This section explains the file that starts the whole application. When you run the project, Spring Boot begins from `MycelliumApplication.java`, scans the project for controllers, repositories, models, and configuration classes, then starts the web server. Without this file, the app has no main launch point.

### `src/main/java/com/mycellium/mycellium/MycelliumApplication.java`

This is the application bootstrap class. It contains the `main` method, calls `SpringApplication.run(...)`, and uses `@SpringBootApplication`.

Why it matters:

- Spring scans `com.mycellium.mycellium` and all subpackages.
- Controllers, repositories, models, and config classes are detected automatically.
- The embedded web server starts from here.

## 4. Configuration Files

Summary:

This section explains code that prepares external services and special web behavior before normal pages are served. `CloudinaryConfig` connects the project to Cloudinary for event images, while `WebConfig` tells Spring how to serve uploaded files from a local folder. These files do not create pages directly, but they support important features used by the controllers.

### `src/main/java/com/mycellium/mycellium/config/CloudinaryConfig.java`

Creates a Spring-managed `Cloudinary` bean.

What it does:

- Reads `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET` from environment variables.
- Builds a `Cloudinary` client.
- Lets `OrganizerController` inject and use that client for event image uploads.

If Cloudinary variables are missing or upload fails, `OrganizerController` falls back to a default Unsplash image URL.

### `src/main/java/com/mycellium/mycellium/config/WebConfig.java`

Adds a custom static resource mapping.

What it does:

- Maps `/uploads/**` browser paths to files stored in the user home folder under `mycellium-uploads`.
- This appears to support local file uploads, while the current organizer image flow mainly uses Cloudinary.

## 5. Controllers

Summary:

This section explains the request-handling layer of the project. Controllers are the traffic directors: they receive URLs from the browser, check login/role rules, fetch or save data through repositories, and choose which Thymeleaf page to show next. Most user actions, such as logging in, creating events, registering for events, or reading notifications, begin in a controller.

Controllers are where URLs are mapped to Java methods. They decide what page to render, what data to load, and where to redirect.

### `src/main/java/com/mycellium/mycellium/controller/AuthController.java`

Handles login, registration, and logout under `/auth`.

Routes:

- `GET /auth/login`: shows the login page.
- `GET /auth/register`: shows the registration page.
- `POST /auth/register`: creates a new user.
- `POST /auth/login`: logs a user in.
- `GET /auth/logout`: invalidates the session.

Main behavior:

- Supports `STUDENT` and `ORGANIZER` roles.
- Validates name, email, password, confirm password, role, and university during registration.
- Normalizes email to lowercase.
- Hashes new passwords with BCrypt.
- Supports old/plain-text passwords by accepting them once, then upgrading them to BCrypt after successful login.
- Stores a simplified `User` object in session as `loggedInUser`.
- Redirects organizers to `/organizer/dashboard`; redirects students to `/`.

Important dependency:

- `UserRepository`

Important design point:

- The app is not using full Spring Security login. Authentication is manual and session-based.

### `src/main/java/com/mycellium/mycellium/controller/HomeController.java`

Handles public pages and public event browsing.

Routes:

- `GET /`: public event feed.
- `GET /events/{id}`: public event details page.
- `GET /about`: about page.
- `GET /for-organizers`: organizer marketing/info page.
- `GET /universities`: university statistics page.
- `GET /insights`: platform insights page.

Main behavior:

- Loads only `PUBLISHED` events for the public feed.
- Supports sorting by date, university, name, type, and popularity.
- Supports category filtering.
- Uses pagination with 9 events per page.
- Allows an organizer owner to view their own unpublished event details.
- Calculates registration count, remaining seats, deadline status, and whether the current student is registered.
- Adds platform stats such as published event count, student count, organizer count, registration count, and university count.

Important dependencies:

- `EventRepository`
- `EventCategoryRepository`
- `EventTimelineRepository`
- `RegistrationRepository`
- `UserRepository`

Templates returned:

- `index.html`
- `event_details.html`
- `about.html`
- `for_organizers.html`
- `universities.html`
- `insights.html`

### `src/main/java/com/mycellium/mycellium/controller/OrganizerController.java`

Handles organizer dashboard and organizer event management under `/organizer`.

Routes:

- `GET /organizer/dashboard`: organizer dashboard.
- `POST /organizer/events/create`: create event.
- `POST /organizer/events/{eventId}/edit`: edit event.
- `POST /organizer/events/{eventId}/delete`: delete event.
- `POST /organizer/events/{eventId}/segments/create`: create event segment.
- `POST /organizer/events/{eventId}/segments/{segmentId}/edit`: edit segment.
- `POST /organizer/events/{eventId}/segments/{segmentId}/delete`: delete segment.
- `POST /organizer/events/{eventId}/timeline/create`: create timeline item.
- `POST /organizer/events/{eventId}/timeline/{timelineId}/edit`: edit timeline item.
- `POST /organizer/events/{eventId}/timeline/{timelineId}/delete`: delete timeline item.

Main behavior:

- Requires a session user with role `ORGANIZER`.
- Only lets organizers manage events where `event.organizerEmail` matches the logged-in organizer email.
- Dashboard loads organizer-owned events, registrations, category options, and registration counts.
- Event creation validates title, date, location, and category.
- Event creation sets organizer email from the session.
- Event images upload to Cloudinary; failure uses a fallback image.
- New categories can be created from organizer forms.
- Creates notifications for all students when a published event is created.
- Deletes related registrations, segments, and timeline items before deleting an event.

Important dependencies:

- `Cloudinary`
- `EventRepository`
- `EventCategoryRepository`
- `EventSegmentRepository`
- `EventTimelineRepository`
- `RegistrationRepository`
- `NotificationRepository`
- `UserRepository`

Template returned:

- `dashboard.html`

### `src/main/java/com/mycellium/mycellium/controller/EventRegistrationController.java`

Handles the newer event-level registration flow.

Routes:

- `GET /events/{id}/register`: shows the event registration form.
- `POST /events/{id}/register`: submits an event-level registration.

Main behavior:

- Requires logged-in `STUDENT` user.
- Blocks registration if the event is not `PUBLISHED`, the deadline has passed, the student already has an active event registration, or capacity is full.
- Validates team size against event `minTeamSize` and `maxTeamSize`.
- Requires `transactionId` for paid events.
- Saves a `Registration` with scope `EVENT`.
- Saves optional team members in `registration_members`.
- Redirects successful registration to `/student/dashboard`.

Important dependencies:

- `EventRepository`
- `RegistrationRepository`
- `RegistrationMemberRepository`

Template returned:

- `event_register.html`

### `src/main/java/com/mycellium/mycellium/controller/StudentController.java`

Handles student dashboard, older RSVP redirect, segment registration, and cancellation under `/student`.

Routes:

- `GET /student/dashboard`: student dashboard.
- `POST /student/events/rsvp/{id}`: redirects to the newer event registration page.
- `POST /student/events/{eventId}/segments/{segmentId}/register`: registers for a segment.
- `POST /student/registrations/{registrationId}/cancel`: cancels a registration.

Main behavior:

- Requires session user with role `STUDENT`.
- Dashboard loads active registrations for the student.
- Builds maps from registration ID to event and segment so the template can display related event data.
- Segment registration validates event status, segment ownership, deadline, duplicate active registration, capacity, and team size.
- Cancellation does not delete a row; it changes registration status to `CANCELLED`.

Important dependencies:

- `RegistrationRepository`
- `EventRepository`
- `EventSegmentRepository`

Template returned:

- `student_dashboard.html`

### `src/main/java/com/mycellium/mycellium/controller/NotificationController.java`

Handles student notification actions.

Routes:

- `POST /student/notifications/{id}/read`: marks one notification as read.
- `POST /student/notifications/read-all`: marks all notifications as read for the logged-in student.

Main behavior:

- Requires session user with role `STUDENT`.
- Ensures a student can only mark their own notification as read.
- Redirects back to the referring page.

Important dependency:

- `NotificationRepository`

### `src/main/java/com/mycellium/mycellium/controller/NotificationModelAdvice.java`

Adds notification data to every rendered page.

What it does:

- Uses `@ControllerAdvice` so it can affect all controllers.
- Uses `@ModelAttribute` to add shared data.
- If the logged-in user is a student, it adds `recentNotifications` and `unreadNotificationsCount`.

Why it matters:

- `fragments.html` can show the notification dropdown without every controller manually adding notification data.

## 6. Model / Entity Classes

Summary:

This section explains the Java classes that represent the main objects in the project. Each model usually matches a database table, such as users, events, registrations, categories, and notifications. These classes define what information the app stores and how that information is shaped inside Java code.

These classes map Java objects to database tables.

### `src/main/java/com/mycellium/mycellium/model/User.java`

Maps to table `users`.

Fields:

- `id`: primary key.
- `name`: full name.
- `email`: unique email address.
- `password`: BCrypt-hashed password, or legacy plain text before upgrade.
- `role`: `STUDENT` or `ORGANIZER`.
- `university`: selected university.

Used by authentication, session identity, platform stats, organizer ownership, and notification targeting.

### `src/main/java/com/mycellium/mycellium/model/Event.java`

Maps to table `events`.

Fields:

- Basic event info: `title`, `category`, `date`, `location`, `description`.
- Organizer info: `organizerEmail`, `university`.
- Media: `imageUrl`.
- Classification: `eventType`, `seriesName`.
- Registration/payment: `fee`, `minTeamSize`, `maxTeamSize`, `capacity`, `registrationDeadline`.
- Schedule: `startDate`, `endDate`.
- Publishing/content: `status`, `about`.
- Audit timestamps: `createdAt`, `updatedAt`.

Lifecycle hooks:

- `@PrePersist`: sets created/updated timestamps and default status.
- `@PreUpdate`: refreshes updated timestamp.

Used by public browsing, event details, organizer dashboard, and registration rules.

### `src/main/java/com/mycellium/mycellium/model/EventCategory.java`

Maps to table `categories`.

Fields:

- `id`: primary key.
- `name`: unique category name.

Used by public filtering and organizer create/edit forms.

### `src/main/java/com/mycellium/mycellium/model/EventSegment.java`

Maps to table `event_segments`.

Fields:

- `id`: primary key.
- `eventId`: parent event ID.
- `name`, `description`.
- `fee`.
- `teamSize`, `minTeamSize`, `maxTeamSize`.
- `capacity`.
- `registrationLink`.
- `registrationDeadline`.
- `createdAt`, `updatedAt`.

Used by segment management and segment registration.

### `src/main/java/com/mycellium/mycellium/model/EventTimeline.java`

Maps to table `event_timelines`.

Fields:

- `id`: primary key.
- `eventId`: parent event ID.
- `title`.
- `description`.
- `timelineDate`.
- `displayOrder`.

Used by the event details timeline and organizer timeline forms.

### `src/main/java/com/mycellium/mycellium/model/Registration.java`

Maps to table `registrations`.

Fields:

- `id`: primary key.
- `eventId`: event being registered for.
- `segmentId`: optional segment ID; null means full-event registration.
- `studentEmail`: student identity.
- `teamName`, `teamSize`.
- `teamLeaderName`, `teamLeaderEmail`.
- `transactionId`: payment reference for paid events.
- `registrationScope`: `EVENT` or `SEGMENT:{id}`.
- `registrationDate`: date string set by constructor.
- `status`: usually `REGISTERED` or `CANCELLED`.

Used by dashboards, capacity counts, duplicate checks, and organizer registration lists.

### `src/main/java/com/mycellium/mycellium/model/RegistrationMember.java`

Maps to table `registration_members`.

Fields:

- `id`: primary key.
- `registrationId`: parent registration.
- `memberName`.
- `memberEmail`.

Used by event-level team registrations.

### `src/main/java/com/mycellium/mycellium/model/Notification.java`

Maps to table `notifications`.

Fields:

- `id`: primary key.
- `userEmail`: notification recipient.
- `title`.
- `message`.
- `eventId`: optional related event.
- `readStatus`.
- `createdAt`.

Lifecycle hook:

- `@PrePersist`: sets created timestamp and default unread status.

Used by event-published notifications and the student navbar dropdown.

## 7. Repository Interfaces

Summary:

This section explains how the project talks to the database without writing SQL for every simple operation. Repositories are interfaces that Spring Data JPA turns into working database code at runtime. Controllers use them to find users, load events, count registrations, save notifications, and run custom queries.

Repositories are Spring Data JPA interfaces. Spring generates most method implementations automatically based on method names.

### `src/main/java/com/mycellium/mycellium/repository/UserRepository.java`

Entity: `User`

- `findByEmail(String email)`: login and organizer lookup.
- `findByRole(String role)`: find all students for notifications.
- `countByRole(String role)`: platform stats.
- `countStudentsByUniversity()`: grouped student stats.
- `countOrganizersByUniversity()`: grouped organizer stats.
- `countDistinctUserUniversities()`: platform university count.

### `src/main/java/com/mycellium/mycellium/repository/EventRepository.java`

Entity: `Event`

- `findByOrganizerEmail(String organizerEmail)`: organizer dashboard.
- `findByCategory(String category, Pageable pageable)`: category filtering.
- `findByStatus(String status, Pageable pageable)`: public published feed.
- `findByStatusAndCategory(...)`: published feed with category filter.
- `countByStatus(String status)`: platform stats.
- `findAllOrderByPopularity(...)`: custom JPQL query sorting events by registration count.
- `countPublishedEventsByUniversity()`: university ranking.
- `countPublishedEventsByCategory()`: category ranking.
- `countDistinctPublishedUniversities()`: public university count.

### `src/main/java/com/mycellium/mycellium/repository/EventCategoryRepository.java`

Entity: `EventCategory`

- `findByNameIgnoreCase(String name)`: prevents duplicate category creation with different casing.
- `findAllByOrderByNameAsc()`: category dropdowns and filters.

### `src/main/java/com/mycellium/mycellium/repository/EventSegmentRepository.java`

Entity: `EventSegment`

- `findByEventIdOrderByCreatedAtAsc(Long eventId)`: load segments for an event.
- `deleteByEventId(Long eventId)`: cleanup when event is deleted.

### `src/main/java/com/mycellium/mycellium/repository/EventTimelineRepository.java`

Entity: `EventTimeline`

- `findByEventIdOrderByDisplayOrderAscTimelineDateAsc(Long eventId)`: timeline display ordering.
- `deleteByEventId(Long eventId)`: cleanup when event is deleted.

### `src/main/java/com/mycellium/mycellium/repository/RegistrationRepository.java`

Entity: `Registration`

- `findByStudentEmail(...)`: all registrations by student.
- `findByStudentEmailAndStatus(...)`: active student dashboard.
- `findByEventId(...)`: registrations for an event.
- `existsActiveRegistration(...)`: duplicate active registration check per event/scope.
- `countByEventIdAndStatus(...)`: event capacity checks.
- `countBySegmentIdAndStatus(...)`: segment capacity checks.
- `countByStudentEmailAndStatus(...)`: student dashboard count.
- `countByStatus(...)`: platform stats.
- `findActiveSegmentIds(...)`: detect already-registered segments.
- `deleteByEventId(...)`: cleanup when deleting an event.
- `findByEventIds(...)`: batch load registrations for organizer dashboard.

### `src/main/java/com/mycellium/mycellium/repository/RegistrationMemberRepository.java`

Entity: `RegistrationMember`

- `findByRegistrationId(Long registrationId)`: loads team members for a registration.

### `src/main/java/com/mycellium/mycellium/repository/NotificationRepository.java`

Entity: `Notification`

- `findTop5ByUserEmailOrderByCreatedAtDesc(...)`: navbar recent notifications.
- `countByUserEmailAndReadStatus(...)`: unread count badge.
- `markAllRead(...)`: bulk update query for a student.

## 8. Resource Files

Summary:

This section explains project files that are not Java classes but still control important runtime behavior. `application.properties` tells Spring how to connect to the database, what port to use, how Hibernate behaves, and how large uploads can be. These settings are loaded automatically when the app starts.

Everything under `src/main/resources` is packaged into the app classpath.

### `src/main/resources/application.properties`

Main Spring Boot configuration.

Important settings:

- `spring.application.name=mycellium`: application name.
- `spring.datasource.url`: MySQL connection URL.
- `spring.datasource.username=avnadmin`: database username.
- `spring.datasource.password=${DB_PASSWORD}`: password comes from environment variable `DB_PASSWORD`.
- `spring.jpa.hibernate.ddl-auto=update`: Hibernate updates schema automatically.
- `spring.jpa.show-sql=true`: SQL statements are printed in logs.
- `spring.jpa.open-in-view=false`: disables lazy database access during view rendering.
- `server.port=${PORT:8080}`: uses `PORT` environment variable or defaults to `8080`.
- Multipart limits allow 20MB uploads.

Important note:

- The app has SQL migration files and also uses `ddl-auto=update`. In production, it is usually cleaner to make migrations the source of truth and avoid automatic schema updates.

## 9. Thymeleaf Templates

Summary:

This section explains the HTML pages users actually see in the browser. Thymeleaf templates combine normal HTML with dynamic values sent by controllers, such as event lists, user names, registration counts, and error messages. Controllers return template names, and Spring/Thymeleaf turns those templates into final HTML responses.

Templates live in `src/main/resources/templates`. Spring Boot automatically resolves a controller return value like `index` to `templates/index.html`.

### `src/main/resources/templates/fragments.html`

Reusable UI fragments:

- Top navigation bar.
- Role-aware links.
- Login/register/sign-out links.
- Student notification dropdown.
- Shared alert blocks for `message` and `error`.
- Footer.

Most pages include this using `th:replace`.

### `src/main/resources/templates/index.html`

Public homepage and event feed.

Backed by `HomeController.showPublicFeed()`.

Uses:

- `publicEvents`
- `eventPage`
- `sort`
- `category`
- `categories`
- `studentCount`

Main UI:

- Hero section.
- Sort/filter controls.
- Category pills.
- Event cards.
- Pagination.

### `src/main/resources/templates/event_details.html`

Event detail page.

Backed by `HomeController.showEventDetails()` plus organizer timeline/segment routes.

Uses:

- `event`
- `timelineItems`
- `organizerOwner`
- `activeRegistrations`
- `remainingSeats`
- `eventFull`
- `eventRegistrationClosed`
- `registeredForEvent`
- `organizerName`
- `message`
- `error`

Main UI:

- Event details and image.
- Registration state/action.
- Timeline display.
- Organizer-only timeline and management forms.

### `src/main/resources/templates/event_register.html`

Event-level registration form.

Backed by `EventRegistrationController`.

Uses:

- `event`
- `student`
- `defaultTeamSize`
- `paidEvent`

Main UI:

- Team name.
- Team size.
- Team leader info.
- Transaction ID for paid events.
- Dynamic member fields.

### `src/main/resources/templates/dashboard.html`

Organizer dashboard.

Backed by `OrganizerController`.

Uses:

- `organizer`
- `events`
- `categories`
- `registrations`
- `eventRegistrationCounts`
- `segmentRegistrationCounts`
- `message`
- `error`

Main UI:

- Organizer identity.
- Event list.
- Event edit/delete forms.
- Registration table.
- Create-event modal/form.

### `src/main/resources/templates/student_dashboard.html`

Student dashboard.

Backed by `StudentController`.

Uses:

- `student`
- `registrations`
- `eventMap`
- `segmentMap`
- `rsvpCount`
- `message`
- `error`

Main UI:

- Student summary.
- Registered event cards.
- View event link.
- Cancel registration form.

### `src/main/resources/templates/login.html`

Login page backed by `AuthController`.

Contains:

- Email field.
- Password field.
- Error display.
- Link to registration.

### `src/main/resources/templates/register.html`

Registration page backed by `AuthController`.

Contains:

- Name field.
- Email field.
- University selector.
- Role radio buttons.
- Password and confirm password fields.

### `src/main/resources/templates/about.html`

Public about page backed by `HomeController.showAbout()`. It uses platform stats and university/category data to explain the platform.

### `src/main/resources/templates/for_organizers.html`

Public marketing/info page for organizers backed by `HomeController.showForOrganizers()`.

### `src/main/resources/templates/universities.html`

University stats page backed by `HomeController.showUniversities()`.

Shows:

- Published events grouped by university.
- Student counts by university.
- Organizer counts by university.

### `src/main/resources/templates/insights.html`

Platform analytics page backed by `HomeController.showInsights()`.

Shows:

- Event count.
- Registration count.
- Student count.
- Organizer count.
- University count.
- Top categories.
- Top universities.

## 10. Static Resources

Summary:

This section explains files that are served directly to the browser without going through controller logic. Static resources include CSS, JavaScript, images, and uploaded/sample media. They support the visual design and client-side behavior of the Thymeleaf pages.

Static files are served directly by Spring Boot from `src/main/resources/static`.

### `src/main/resources/static/styles.css`

Custom site CSS. It can be referenced directly as `/styles.css`.

### `src/main/resources/static/auth.js`

Client-side JavaScript for authentication-related pages or UI behavior.

### `src/main/resources/static/css/output.css`

Generated CSS output, likely from Tailwind or another CSS build step.

Important note:

- Many templates also load Tailwind from CDN.
- If using a production CSS build, this file may be where compiled styles belong.

### `src/main/resources/static/images/*.jpg`

Default category/event images:

- `arts.jpg`
- `business.jpg`
- `seminar.jpg`
- `sports.jpg`
- `tech.jpg`
- `workshop.jpg`

These are available under `/images/...`.

### `src/main/resources/static/uploads/*`

Contains uploaded/sample image files packaged into the app.

Important image-source note:

- `static/uploads` files are bundled with the application.
- `WebConfig` maps `/uploads/**` to a user-home folder.
- Cloudinary uploads return external URLs.
- So the project currently has three possible image sources: bundled static uploads, local user-home uploads, and Cloudinary URLs.

## 11. Database Migration Files

Summary:

This section explains the SQL files that describe how the database structure evolved over time. These migrations add columns, create tables, add indexes, and enforce registration rules. They are especially important for understanding what tables and columns the JPA model classes expect to exist.

Migration files live in `src/main/resources/db/migration`. They are named like Flyway migrations. If Flyway is added as a dependency, it runs them in version order.

### `src/main/resources/db/migration/V2__upgrade_events_model.sql`

Adds richer event fields: university, event type, start/end dates, registration deadline, status, about, and timestamps.

### `src/main/resources/db/migration/V3__create_event_segments.sql`

Creates `event_segments` for sub-events/segments under a main event.

### `src/main/resources/db/migration/V4__upgrade_registrations.sql`

Adds segment/team/status fields to `registrations` and creates indexes for common registration lookups.

### `src/main/resources/db/migration/V5__create_event_timeline.sql`

Creates `event_timelines` for event schedule/timeline entries.

### `src/main/resources/db/migration/V6__create_notifications.sql`

Creates `notifications` for student notifications.

### `src/main/resources/db/migration/V7__improve_registration_rules.sql`

Adds segment min/max team size, registration scope, a generated active scope column, and a unique index to prevent duplicate active registrations per event/student/scope.

### `src/main/resources/db/migration/V8__event_level_registration_and_categories.sql`

Adds categories, event-level registration fields, team leader/payment fields, registration members, and useful indexes.

### `src/main/resources/db/migration/V9__add_university_to_users.sql`

Adds `university` to `users` if it does not already exist.

## 12. Maven and Project-Level Files

Summary:

This section explains the files that make the project buildable and runnable. Maven uses `pom.xml` to know which Java version, Spring Boot version, libraries, and plugins the app needs. Other root-level files support running with Maven Wrapper, Docker deployment, Git behavior, documentation, screenshots, and sample data.

### `pom.xml`

This is the Maven build file. It defines project metadata, Java version, dependencies, and build plugins.

Key metadata:

- Group ID: `com.mycellium`
- Artifact ID: `mycellium`
- Version: `0.0.1-SNAPSHOT`
- Parent: `spring-boot-starter-parent` version `3.5.0`
- Java version: `17`

Dependencies:

- `spring-boot-starter-web`: Spring MVC, embedded server, JSON/web support.
- `spring-boot-starter-thymeleaf`: server-rendered HTML templates.
- `spring-boot-starter-data-jpa`: JPA/Hibernate database access.
- `spring-security-crypto`: BCrypt password hashing.
- `mysql-connector-j`: MySQL JDBC driver at runtime.
- `spring-boot-starter-test`: test support.
- `cloudinary-http44`: Cloudinary upload client.

Build plugin:

- `spring-boot-maven-plugin`: packages and runs the Spring Boot app.

Common Maven commands:

- `mvnw.cmd spring-boot:run`: run the app on Windows.
- `mvnw.cmd test`: run tests on Windows.
- `mvnw.cmd package`: build a runnable JAR on Windows.
- `./mvnw spring-boot:run`: run the app on macOS/Linux.
- `./mvnw test`: run tests on macOS/Linux.
- `./mvnw package`: build a runnable JAR on macOS/Linux.

Important Maven note:

- The project has migration files but no explicit `flyway-core` dependency in `pom.xml`. Spring Boot only runs Flyway migrations automatically if Flyway is on the classpath.

### `mvnw` and `mvnw.cmd`

Maven Wrapper scripts. They let you run Maven without installing Maven globally.

### `.mvn/`

Maven Wrapper support directory.

### `Dockerfile`

Container build instructions for packaging/running the app in Docker.

### `.dockerignore`

Controls which files are excluded from Docker build context.

### `.gitignore`

Controls files Git should ignore, such as build outputs and IDE files.

### `.gitattributes`

Git behavior configuration, often line endings or file handling.

### `README.md`

Human-facing project overview/instructions.

### `event_inserts.sql`

SQL seed/sample data for events. Useful for demos or local testing.

### `input.css`

Likely Tailwind input/source CSS that may be compiled into `src/main/resources/static/css/output.css`.

### `Screenshot.png` and `Screenshot2.png`

Project screenshots, probably for README/demo documentation.

### `.idea/`

IntelliJ IDEA project metadata, not runtime behavior.

### `target/`

Maven build output directory. It is generated and should not be edited manually.

## 13. How the App Holds Together

Summary:

This section connects the individual files into complete user flows. It shows how startup, login, registration, event browsing, organizer event creation, student registration, dashboards, and notifications move through controllers, repositories, models, templates, and the database. This is the best section to read when you want to understand the whole app as one working system.

### Startup Flow

1. `MycelliumApplication.main()` starts Spring Boot.
2. Spring scans the package tree.
3. Controllers, repositories, config classes, and JPA entities are discovered.
4. Spring creates beans like `Cloudinary`, repositories, and controllers.
5. Spring connects to MySQL using `application.properties`.
6. Hibernate maps entity classes to database tables.
7. The embedded web server starts on `PORT` or `8080`.

### Login Flow

1. Browser opens `/auth/login`.
2. `AuthController` returns `login.html`.
3. User submits email/password to `POST /auth/login`.
4. Controller finds the user through `UserRepository`.
5. Password is checked with BCrypt or legacy plain-text comparison.
6. Session is recreated and `loggedInUser` is stored.
7. Student redirects to `/`; organizer redirects to `/organizer/dashboard`.

### Registration Flow

1. Browser opens `/auth/register`.
2. `AuthController` returns `register.html`.
3. User submits account data.
4. Controller validates role, email, password, and university.
5. Password is BCrypt-hashed.
6. `UserRepository.save(user)` inserts the user.
7. User is redirected to login.

### Public Event Browsing Flow

1. Browser opens `/`.
2. `HomeController.showPublicFeed()` queries published events.
3. It applies sorting, category filtering, and pagination.
4. It loads categories and platform stats.
5. It returns `index.html`.
6. Thymeleaf renders event cards and pagination.

### Event Details Flow

1. Browser opens `/events/{id}`.
2. `HomeController.showEventDetails()` loads the event.
3. If event is unpublished, only the owning organizer can view it.
4. Timeline, registration count, remaining seats, and registration state are calculated.
5. It returns `event_details.html`.
6. Students see registration state/actions; owners see management forms.

### Organizer Create Event Flow

1. Organizer opens `/organizer/dashboard`.
2. `OrganizerController` loads their events and registrations.
3. Organizer submits create form to `/organizer/events/create`.
4. Controller validates basic fields.
5. Category is resolved or created.
6. Image uploads to Cloudinary.
7. Event is saved with organizer email.
8. If published, notifications are created for all students.
9. Organizer is redirected to dashboard.

### Student Event Registration Flow

1. Student opens `/events/{id}/register`.
2. `EventRegistrationController` validates that registration is allowed.
3. It returns `event_register.html`.
4. Student submits team/payment/member info.
5. Controller validates deadline, capacity, duplicate registration, team size, and transaction ID.
6. Registration is saved.
7. Optional team members are saved.
8. Student is redirected to `/student/dashboard`.

### Student Dashboard Flow

1. Student opens `/student/dashboard`.
2. `StudentController` loads active registrations.
3. It fetches related events/segments into maps.
4. It returns `student_dashboard.html`.
5. Student can cancel a registration, changing status to `CANCELLED`.

### Notification Flow

1. Organizer creates a published event.
2. `OrganizerController.createEventNotifications()` creates one notification per student.
3. On later page renders, `NotificationModelAdvice` adds recent notifications to the model.
4. `fragments.html` shows the notification dropdown.
5. Student marks one or all notifications as read through `NotificationController`.

## 14. Key Concepts to Remember

Summary:

This section highlights the rules and naming patterns that appear again and again in the code. Things like `loggedInUser`, `STUDENT`, `ORGANIZER`, `PUBLISHED`, `REGISTERED`, and `CANCELLED` are small strings, but they control major behavior across the project. Remembering these concepts makes the rest of the code much easier to follow.

### Session-based auth

The app manually stores a `User` object in `HttpSession` as `loggedInUser`. Controllers check this object directly.

### Roles

Role strings are plain strings:

- `STUDENT`
- `ORGANIZER`

There is no enum yet, so spelling must remain consistent.

### Event status

Public browsing depends heavily on `PUBLISHED`. Unpublished events are hidden from the public unless the logged-in organizer owns them.

### Registration status

Active registrations use `REGISTERED`. Cancelled registrations use `CANCELLED`. Most counts and dashboard views only include `REGISTERED`.

### Event-level vs segment-level registrations

Event-level registration:

- `segmentId = null`
- `registrationScope = EVENT`

Segment-level registration:

- `segmentId = segment id`
- `registrationScope = SEGMENT:{id}`

### Templates depend on model names

If you rename model attributes in controllers, update the matching Thymeleaf templates.

Examples:

- `publicEvents` is used by `index.html`.
- `event` is used by `event_details.html` and `event_register.html`.
- `registrations`, `eventMap`, and `segmentMap` are used by `student_dashboard.html`.
- `recentNotifications` and `unreadNotificationsCount` are used by `fragments.html`.

## 15. Things Worth Improving Later

Summary:

This section lists future cleanup ideas that could make the project more maintainable, safer, and easier to scale. The current code works as a student/organizer event platform, but improvements like service classes, stronger security, Flyway setup, enums, tests, and cleaner image handling would make the architecture stronger over time.

These are useful future cleanup ideas:

- Add Flyway dependency if the migration files are meant to run automatically.
- Consider changing `spring.jpa.hibernate.ddl-auto=update` to `validate` when migrations become the source of truth.
- Replace manual session checks with Spring Security if the app grows.
- Move business logic out of controllers into service classes.
- Replace repeated role/status strings with constants or enums.
- Add foreign-key relationships or JPA associations if you want stronger database consistency.
- Add tests for registration rules, authorization checks, and repository queries.
- Clean up mixed image upload strategies: Cloudinary vs local `/uploads/**` vs bundled static uploads.
