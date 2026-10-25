# CryptoWallet Management System

A web-based cryptocurrency wallet management application built with Java, Spring Boot, Thymeleaf, and MySQL. It enables users to manage wallet balances, track cryptocurrency holdings, and view transaction history through a web interface.

## ✨ Features

- **User Authentication** — Registration and login functionality.
- **Wallet Dashboard** — View wallet balances and cryptocurrency holdings.
- **Cryptocurrency Management** — Simulate deposits and withdrawals for Bitcoin (BTC), Ethereum (ETH), and Litecoin (LTC).
- **Fund Transfers** — Transfer funds between application wallets.
- **Transaction History** — Review recorded wallet transactions.
- **Cryptocurrency Prices** — Access cryptocurrency price information through the application's price service.
- **Analytics Dashboard** — Visualize wallet and transaction data.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend development |
| Spring Boot | Application framework |
| Spring MVC | Web request handling |
| Spring Security | Authentication and security |
| Thymeleaf, HTML, CSS, JavaScript | Frontend |
| MySQL | Database |
| Hibernate / JPA | Database persistence |
| Maven | Dependency management and builds |

## 📋 Prerequisites

Before running the project, ensure you have:

- Java Development Kit (JDK) 21
- MySQL Server
- Git
- A web browser

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/YOUR-USERNAME/CryptoWallet-Management.git
cd CryptoWallet-Management
```

Replace `YOUR-USERNAME` with your GitHub username.

### 2. Create the database

Log in to MySQL and run:

```sql
CREATE DATABASE cryptowalletdb;
```

### 3. Configure environment variables

Create a `.env` file in the project root, alongside `pom.xml`:

```properties
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
```

Replace the placeholder with your local MySQL password.

**Security:** Never commit your `.env` file or publish database credentials, API keys, or other secrets.

### 4. Verify application configuration

In `src/main/resources/application.properties`, configure the database connection:

```properties
spring.config.import=optional:file:./.env[.properties]

server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/cryptowalletdb?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
```

Retain the other application settings already present in the project.

### 5. Run the application

On Windows, open a terminal in the project root and execute:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux, use:

```bash
./mvnw spring-boot:run
```

### 6. Open the application

Visit:

**http://localhost:8081/**

Ensure your MySQL service is running before starting the application.

## 🔐 Security Notes

- Keep `.env` and other files containing secrets out of version control.
- Use strong, unique credentials and review authentication and authorization settings before deployment.
- Do not use real cryptocurrency or financial information in this demonstration application.

## ⚠️ Disclaimer

This project is intended for educational and demonstration purposes. Cryptocurrency deposits, withdrawals, and wallet transfers are application-level simulations; they do not represent actual blockchain transactions or move real cryptocurrency.

## 📌 Future Enhancements

- Personalized wallet recommendations based on holdings and transaction activity.
- Improved security and wallet access controls.
- Integration with blockchain networks and verified cryptocurrency market data.
- Deployment to a cloud hosting platform.

## 👩‍💻 Contributions

Contributions, suggestions, and bug reports are welcome. Feel free to fork the repository and submit a pull request.

---

