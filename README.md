# Car Rental Service API 🚗

Introducing the Car Rental Service API - Your Ultimate Car Rental Hub! This project offers a comprehensive car rental service API, powered by Spring Boot, tailored to streamline your car rental experience. Explore its diverse functionalities encompassing user management, robust cars control, seamless rental processing, secure payment handling with Stripe integration, and dynamic notification service via Telegram.

## Table Of Content
- [Technologies Used](#technologies-used)
- [Key Functionalities](#key-functionalities)
- [SQL Database Diagram And Architecture](#sql-database-diagram-and-architecture)
- [Setup Instructions](#setup-instructions)
- [Challenges Encountered](#challenges-encountered)
- [Contact](#contact)

## Technologies Used

- **Java**: Programming language used as the foundation for the project.
- **Spring Boot**: Framework for building robust Java applications.
- **Stripe API**: Payment processing API used to handle transactions securely and efficiently.
- **Telegram API**: Messaging API utilized for integrating Telegram's notifications functionalities into applications.
- **Spring Security**: Provides authentication and authorization capabilities.
- **JWT**: (JSON Web Token) Standard for securely transmitting information between parties as a JSON object.
- **Spring Data JPA**: Simplifies the data access layer by abstracting away the boilerplate code.
- **Mapstruct**: Used for mapping between DTOs and entities.
- **Lombok**: Library for reducing boilerplate code in Java classes.
- **Maven**: Build automation tool for managing dependencies and building the project.
- **Liquibase**: Manages database schema changes over time.
- **Jackson**: Provides JSON serialization and deserialization capabilities in Java.
- **Swagger**: Generates interactive API documentation.
- **MySQL**: Database for storing application data.
- **Docker**: For containerizing the application.
- **JUnit 5**: Framework for unit testing.
- **Mockito**: Framework for mocking objects in tests.

## Key Functionalities

### Postman

You can explore the Car Rental Service API using my Postman collection. Don't hesitate to reach out if you have any questions or encounter any issues. Happy testing!

[<img src="https://run.pstmn.io/button.svg" alt="Run In Postman" style="width: 128px; height: 32px;">](https://god.gw.postman.com/run-collection/32395887-e754d7fe-b441-4f55-93f3-d16bc44c16f1?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D32395887-e754d7fe-b441-4f55-93f3-d16bc44c16f1%26entityType%3Dcollection%26workspaceId%3Dabe8d2f8-17aa-40cd-afc8-15ba847135a2)

Below, I've included detailed explanations for each endpoint in the Postman collection, helping you navigate and understand the functionalities of the Car Rental Service API more effectively.


### Access to endpoints
🟩 - publicly available  
🟨 - for logged-in users  
🟥 - for administrators  


### Authentication Management

- 🟩 `POST: /api/auth/registration` - **User Registration**: New customers can register with their email, password, and personal details.
- 🟩 `POST: /api/auth/login` - **User Authentication**: Secure login mechanism for registered users.

### User Management

- 🟨 `GET: /api/users/me` - **View Profile**: Users can retrieve their own profile information.
- 🟨 `PATCH: /api/users/me` - **Update Profile**: Users can update their own profile information.
- 🟥 `PUT: /api/users/{userId}/role` - **Update User Roles**: Admin users can update roles of other users.

### Rental Management

- 🟨🟥 `GET: /api/rentals/?user_id=...&is_active=...` - **View Rentals**: Users can view their rental history, Admin Users can view all users' rental history. It can also be filtered by activity status and user ID.
- 🟨   `GET: /api/rentals/{rentalId}` - **View Rental Details**: Users can check details of a specific rental.
- 🟨   `POST: /api/rentals` - **Start Rental**: Users can initiate a new car rental, reducing available inventory by 1.
- 🟨   `POST: /api/rentals/{rentalId}/return` - **Return Car**: Users return a rental, increasing available inventory by 1.

### Payment Management (Stripe API Integration)

- 🟨🟥 `GET: /api/payments/?user_id=...` - **View Payments**: Users can view their payment history, and Admin Users can view all users' payment history.
- 🟨   `GET: /api/payments/success/{rentalId}/?type=...` - **Check Successful Payment**: Check the success of payment based on the rental ID and payment type - Stripe API redirection.
- 🟨   `GET: /api/payments/cancel/{rentalId}` - **Cancel Payment**: Retrieve a message indicating that the payment cancellation process is paused - Stripe API redirection.
- 🟨   `POST: /api/payments` - **Initiate Payment**: Users can start the payment process for their car rental.

### Car Management

- 🟩 `GET: /api/cars` - **Retrieve Cars**: Everyone can browse through the list of available cars.
- 🟩 `GET: /api/cars/{carId}` - **View Car**: Everyone can view detailed information about a specific car.
- 🟥 `POST: /api/cars` - **Add Car**: Admin users can add new cars to the rental inventory.
- 🟥 `PUT: /api/cars/{carId}` - **Update Car**: Admin users can modify car details, including inventory management.
- 🟥 `DELETE: /api/cars/{carId}` - **Delete Car**: Admin users can remove cars from the rental inventory.

### Notification Service (Telegram)
- Handles notifications for new rentals, overdue rentals, and successful payments.
- Integrates with Telegram API, facilitating seamless communication with administrators using Telegram Bot and Chat.

### Notification example:  

<p align="center">
  <img src="https://github.com/MateuszRostek/readme-tests/assets/140905715/3a1bb3ed-d82e-4d95-a757-b3bccc25df8c">
</p>

### Stripe Payment example:  

<p align="center">
  <img src="https://github.com/MateuszRostek/readme-tests/assets/140905715/20981593-a383-4f53-a07e-469238a542d6">
</p>

## SQL Database Diagram And Architecture

### SQL Database Diagram
Below is a simplified representation of the database schema used in the Car Rental Service API:

![rentcar-db-diagram](https://github.com/MateuszRostek/readme-tests/assets/140905715/6d29e8d2-b553-42f1-8240-f22f5cbeaeb1)

This diagram illustrates the relationships between different entities in the database, including tables for users, payments, rentals, cars, and more.

### Architecture
Here is also a **very** simplified version of the architecture of the project:

![architecture-rent-car](https://github.com/MateuszRostek/readme-tests/assets/140905715/562c89d3-194a-4797-ac64-168b404b096b)

## Setup Instructions
Ensure you have Docker and JDK installed  
In this project, I've used `Docker Desktop 4.28.0 (139021)` and `Oracle OpenJDK version 20.0.2`

1. Clone the repository to your local machine.
2. Verify you have Docker Engine running.
3. Navigate to the project directory.
4. Run `./mvnw clean package` to create a .jar file.
5. In the .env file you should provide the necessary DB and Docker variables, here is an example:  
```mysql
MYSQLDB_USER=1234
MYSQLDB_PASSWORD=1234
MYSQLDB_DATABASE=1234

JWT_SECRET=1234

TELEGRAM_BOT_TOKEN=1234

STRIPE_SECRET_KEY=1234

MYSQLDB_ROOT_PASSWORD=1234
MYSQLDB_LOCAL_PORT=1234
MYSQLDB_DOCKER_PORT=1234

SPRING_LOCAL_PORT=1234
SPRING_DOCKER_PORT=1234
DEBUG_PORT=1234
```
(Do you want to fully test my API? Please contact me at: rostek.mateusz@outlook.com for actual .env file)  

6. Run `docker compose up -d --build` to start the application and MySQL database.  
7. Feel free to test my application using Postman/Swagger.  
   **Postman**: Keep in mind that you have to pass the Authorization (Bearer Token) that you receive when logging in.  
  Do you want to test admin features? Here are the credentials of the sample manager:  
   ```json
   {
   "email": "john@manager.com",
   "password": "12345678"
   }
   ```
   And also, the credentials of the sample customer:
   ```json
   {
   "email": "paul@customer.com",
   "password": "12345678"
   }
   ```
8. To stop and remove containers use `docker compose down`.

## Challenges Encountered

Working with the Stripe API was a bit challenging, requiring careful setup to ensure a proper connection and secure handling of financial data. This project was quite robust, requiring careful management of various components such as user authentication, rental processing, and real-time notifications. Coordinating these different elements while maintaining data consistency and security added complexity. Ultimately, I am proud of this project and the valuable lessons I learned while developing it.

## Contact
Thank you for taking the time to explore my Car Rental Service API!

📧 Want to get in touch? Please, feel free to send me an email at rostek.mateusz@outlook.com
