# Local Restaurant Menu and Order Management System

Semester-end Spring Boot MVC project for a local restaurant back-office. The app uses Java 21, Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, MySQL, Maven, Bootstrap, BCrypt password hashing, server-side sessions, form validation, full CRUD, search/filtering, and database BLOB image storage.

## Features

- Login with username/password and clear failed-login message
- Session-based Spring Security authentication
- Roles: `MANAGER` and `COURIER`
- Manager access to dashboard, categories, products, and all order operations
- Courier access to order list/detail and assigned-order status updates
- Category CRUD
- Product CRUD with image upload to MySQL `LONGBLOB`
- Product image serving from database bytes through `/products/{id}/image`
- Order CRUD, order detail, courier assignment, and status update
- Search products by name and category
- Search/filter orders by customer name, date, and status
- Bootstrap Thymeleaf UI with validation and flash messages

## Default Users

Data is created at startup by `DataInitializer`.

| Username | Password | Role |
| --- | --- | --- |
| `manager` | `1234` | `MANAGER` |
| `courier` | `1234` | `COURIER` |

Passwords are stored encoded with BCrypt, never as plain text.

## Run Steps

1. Install **Java 21** and MySQL.
2. Create or allow the app to create the database:

```sql
CREATE DATABASE IF NOT EXISTS restaurant_management;
```

3. Update MySQL credentials in `src/main/resources/application.properties` if your local MySQL user is not `root` with an empty password.
4. From the project root, run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

5. Open:
   - Public menu: `http://localhost:9091/menu`
   - Back-office login: `http://localhost:9091/login`
6. Log in with `manager / 1234` or `courier / 1234`.

## MySQL Schema

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

That means tables are created/updated automatically from JPA mappings. A compatible manual schema is also provided in `schema-mysql.sql`.

## Image BLOB Storage

Product images are handled in the `Product` entity:

- `imageName` stores the uploaded file name.
- `imageType` stores the MIME type, such as `image/png`.
- `imageData` is a `byte[]` annotated with `@Lob` and mapped to MySQL `LONGBLOB`.

When a manager uploads a product image, `ProductService` reads the `MultipartFile` bytes and saves them directly into `imageData`. No local file path is stored. The UI displays images with:

```html
<img src="/products/{id}/image">
```

`ProductController` loads the product, reads the stored bytes, sets the correct content type, and returns the image as the HTTP response body.

## Login And Session Flow

Spring Security uses form login at `/login`. The submitted username/password is processed by the security filter chain, then `CustomUserDetailsService` loads the user from MySQL. BCrypt verifies the password hash.

After successful login:

- `MANAGER` is redirected to `/dashboard`.
- `COURIER` is redirected to `/orders`.

The authenticated user is stored in the HTTP session. Logout invalidates the session and removes the `JSESSIONID` cookie.

## Project Tree

```text
.
|-- pom.xml
|-- schema-mysql.sql
|-- README.md
|-- mvnw
|-- mvnw.cmd
`-- src
    |-- main
    |   |-- java/com/example/restaurantmanagement
    |   |   |-- RestaurantManagementApplication.java
    |   |   |-- config
    |   |   |   |-- DataInitializer.java
    |   |   |   `-- SecurityConfig.java
    |   |   |-- controller
    |   |   |   |-- AuthController.java
    |   |   |   |-- CategoryController.java
    |   |   |   |-- DashboardController.java
    |   |   |   |-- GlobalModelControllerAdvice.java
    |   |   |   |-- OrderController.java
    |   |   |   `-- ProductController.java
    |   |   |-- dto
    |   |   |   |-- OrderFormDTO.java
    |   |   |   `-- ProductFormDTO.java
    |   |   |-- entity
    |   |   |   |-- Category.java
    |   |   |   |-- Order.java
    |   |   |   |-- OrderStatus.java
    |   |   |   |-- Product.java
    |   |   |   |-- RoleEnum.java
    |   |   |   `-- User.java
    |   |   |-- repository
    |   |   |   |-- CategoryRepository.java
    |   |   |   |-- OrderRepository.java
    |   |   |   |-- ProductRepository.java
    |   |   |   `-- UserRepository.java
    |   |   |-- security
    |   |   |   `-- CustomUserDetailsService.java
    |   |   `-- service
    |   |       |-- CategoryService.java
    |   |       |-- OrderService.java
    |   |       |-- ProductService.java
    |   |       `-- UserService.java
    |   `-- resources
    |       |-- application.properties
    |       |-- static/css/app.css
    |       `-- templates
    |           |-- access-denied.html
    |           |-- dashboard.html
    |           |-- login.html
    |           |-- categories/list.html
    |           |-- categories/form.html
    |           |-- fragments/layout.html
    |           |-- orders/list.html
    |           |-- orders/form.html
    |           |-- orders/detail.html
    |           |-- products/list.html
    |           `-- products/form.html
    `-- test/java/com/example/restaurantmanagement
        `-- RestaurantManagementApplicationTests.java
```

## Notes

- This is a server-rendered MVC app. There is no REST API except the internal image-byte endpoint needed by the UI.
- A public, Yemeksepeti-like menu page is available at `/menu` (basket + checkout creates an order).
- The package structure follows the requested layered architecture: `controller`, `service`, `repository`, `entity`, plus `dto`, `config`, and `security`.
- The Maven wrapper script was adjusted so it works when the local Maven cache directory is not a symbolic link on Windows.
