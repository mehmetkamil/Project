# Event Planner - User Guide

## Starting the Main Application

1. Open Terminal
2. Navigate to the project folder (COMP301Sec2F25-master 4 folder)
3. Run the following commands:

cd firstspingproject
mvn spring-boot:run

##  Web Addresses

- **User Page:** http://localhost:7777
- **Admin Panel:** http://localhost:7777/admin-panel.html

---

##  Microservices (Optional)

1. Navigate to the project folder
2. Run the following commands in separate terminals:

cd microservices-parent
mvn spring-boot:run -pl discovery-server

cd microservices-parent
mvn spring-boot:run -pl user-service

cd microservices-parent
mvn spring-boot:run -pl event-service

cd microservices-parent
mvn spring-boot:run -pl booking-service

cd microservices-parent
mvn spring-boot:run -pl payment-service

- **Eureka Dashboard:** http://localhost:8761
- **User Service:**     http://localhost:8081
- **Event Service:**    http://localhost:8082
- **Booking Service:**  http://localhost:8083
- **Payment Service:**  http://localhost:8084 
