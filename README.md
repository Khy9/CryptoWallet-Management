
CryptoWallet Management System
A Spring Boot web application that demonstrates authenticated, multi-asset wallet management for Bitcoin (BTC), Ethereum (ETH), and Litecoin (LTC). The application combines server-rendered views, persistent wallet and transaction records, market-price integration, portfolio analytics, and a rule-based wallet-storage recommendation page.

Scope and disclaimer: This is an educational/demo application. Deposits, withdrawals, and transfers update application records only; they do not submit transactions to a blockchain, use private keys, or move real cryptocurrency. Market-price data is indicative and depends on an external API. Recommendations are informational software output, not financial or security advice.

Core Capabilities
Account lifecycle and authentication: registration, login, session-based access control, and logout using Spring Security.
Password hashing: BCrypt password encoder for stored password hashes.
Wallet ledger: account-associated wallet balances, including BTC, ETH, LTC, and the legacy balance field.
Transaction operations: simulated deposits, withdrawals, and transfers with transaction records and timestamps.
Transaction history: view recorded activity and recent transactions.
Portfolio dashboard: holdings overview, estimated INR portfolio value, transaction summaries, recent activity, and charts.
Market-data integration: fetches INR prices and 24-hour price changes for Bitcoin, Ethereum, and Litecoin from CoinGecko when available.
Personalized wallet recommendation: a dedicated /wallet-recommendation page that reads the authenticated user's stored wallet and transaction activity, then applies simple rules to suggest a hot-wallet, cold-storage, or hybrid approach.
Responsive interface: Thymeleaf templates styled with Bootstrap and portfolio charts rendered with Chart.js.
Technology Stack
Layer	Technology	Responsibility
Language / runtime	Java 17+	Application implementation and runtime
Application framework	Spring Boot 3.2	Dependency injection, application startup, configuration
Web layer	Spring MVC	HTTP request mappings and controller-to-view model preparation
View rendering	Thymeleaf	Server-side HTML templates and dynamic page values
Authentication	Spring Security 6	Authentication, route authorization, login and logout flow
Password hashing	BCrypt	Hashing user passwords before persistence
Persistence	Spring Data JPA / Hibernate	ORM mapping and database access
Database	MySQL	Persistent user, wallet, and transaction records
Build	Maven Wrapper	Repeatable Maven build and run commands
UI	Bootstrap 5, CSS	Responsive layout and visual components
Visualization	Chart.js	Portfolio distribution and asset-value charts
External data	CoinGecko simple-price API	Indicative INR crypto prices and 24-hour changes
Architecture
The application follows a conventional layered Spring MVC architecture:

Browser
  │ HTTP request
  ▼
Spring Security Filter Chain
  │ Authentication / authorization
  ▼
Spring MVC Controller
  │ Invokes services and prepares Model attributes
  ▼
Service Layer ───────────────► External CoinGecko API
  │ Business operations / data coordination
  ▼
Spring Data JPA Repository
  │ ORM queries and persistence
  ▼
MySQL Database

Controller Model ────────────► Thymeleaf Template ───────────► HTML response
Layer responsibilities
Controllers (controllers/) map HTTP routes to application actions, coordinate service/repository calls, and populate the model used by Thymeleaf templates.
Services (services/) encapsulate application operations such as user and wallet access, transaction handling, and market-price retrieval.
Repositories (repositories/) provide persistence access through Spring Data interfaces.
Entities (models/) represent the persisted user, wallet, and transaction records using JPA annotations.
Security configuration (config/) defines public routes, authenticated-route requirements, the custom login page, the authentication provider, and logout behavior.
Templates (src/main/resources/templates/) render the dashboard and workflow pages. The recommendation template is wallet-recommendation.html.
Project Structure
cryptowallet/
├── pom.xml
├── mvnw / mvnw.cmd
├── .env                         # Local-only database credentials; do not commit
├── .gitignore
└── src/
    └── main/
        ├── java/com/wallet/cryptowallet/
        │   ├── config/
        │   │   └── SecurityConfig.java
        │   ├── controllers/
        │   │   ├── HomeController.java
        │   │   ├── DashboardController.java
        │   │   ├── RegisterController.java
        │   │   ├── LoginController.java
        │   │   ├── DepositController.java
        │   │   ├── WithdrawController.java
        │   │   ├── TransferController.java
        │   │   ├── HistoryController.java
        │   │   └── WalletRecommendationController.java
        │   ├── models/
        │   │   ├── User.java
        │   │   ├── Wallet.java
        │   │   └── Transaction.java
        │   ├── repositories/
        │   ├── services/
        │   │   ├── CryptoPriceService.java
        │   │   ├── UserService.java / UserServiceImpl.java
        │   │   ├── WalletService.java / WalletServiceImpl.java
        │   │   └── TransactionService.java
        │   └── ...
        └── resources/
            ├── application.properties
            └── templates/
                ├── home.html
                └── wallet-recommendation.html
The tree highlights the principal files; additional controllers, repositories, services, templates, and static assets may be present in the repository.

Domain Model
The primary persistence model uses three JPA entities:

User
Stores the user's identifier, username, email, and password hash. A transient password field is used for registration input and is not intended to be persisted. User has a one-to-one relationship with Wallet.

Wallet
Stores the user's wallet identifier and balances (balance, btcBalance, ethBalance, and ltcBalance) as BigDecimal values. The wallet belongs to a user through user_id and has a one-to-many relationship with Transaction records. Crypto balances use decimal precision suitable for fractional asset quantities; displayed market values are estimates.

Transaction
Records a transaction identifier, associated wallet, amount, type, description, currency, and timestamp. The transaction's wallet relationship associates activity with the corresponding account. The history and recommendation features read these records to summarize activity.

The database schema is managed through JPA/Hibernate configuration. The development configuration uses spring.jpa.hibernate.ddl-auto=update; review and replace this approach with controlled database migrations before production use.

Main Application Routes
The following table describes the principal routes exposed by the application. Routes that modify data are handled by form submissions; use the application UI to provide valid inputs.

Route	Purpose	Access
/	Landing / entry route	Public entry point
/register	Display and submit user registration	Public
/login	Login page	Public
/logout	End the authenticated session	Authenticated session
/home	Main wallet dashboard	Authenticated
/deposit	Display and submit a simulated deposit	Authenticated
/withdraw	Display and submit a simulated withdrawal	Authenticated
/transfer	Transfer workflow	Authenticated
/history	Transaction history	Authenticated
/transactions	Transaction workflow endpoint	Authenticated
/wallet-recommendation	Personalized wallet-storage recommendation	Authenticated
/admin/create-user	Administrative user-creation workflow	Authenticated; apply role-level restrictions before production use
SecurityConfig permits the login/registration/entry routes and designated static resources, then requires authentication for other requests. The recommendation page uses the current authenticated username to retrieve that user's profile and wallet data instead of showing a generic static recommendation.

Personalized Wallet Recommendation
The recommendation page is a lightweight, deterministic rule-based feature. It does not run a machine-learning model or evaluate a user's financial circumstances. It reads the authenticated user's wallet, calculates how many of the supported cryptocurrency balances are greater than zero, retrieves transactions in descending timestamp order, and selects a message using the current rule set:

Condition	Recommendation	Interpretation
5 or more recorded transactions	Hybrid Storage	A hot wallet may support frequent access while cold storage may suit assets held for longer periods.
Fewer than 5 transactions and at least one positive BTC, ETH, or LTC balance	Consider Cold Storage	For a long-term holding pattern, consider cold storage and retain only a limited amount in a hot wallet for routine use.
No recorded positive BTC, ETH, or LTC holdings	Start With Your Usage Needs	Choose based on expected access frequency and revisit the suggestion as use changes.
The page displays the signed-in username, wallet balances, total recorded transaction count, number of supported asset types with positive balances, recommendation, and rationale. A lack of recorded holdings or transactions is treated as zero-valued activity where applicable.

These thresholds are demonstration rules rather than a security assessment. The feature does not inspect device security, attack exposure, custody arrangements, regulatory requirements, or actual blockchain activity.

Market-Price Data Flow
CryptoPriceService calls the CoinGecko simple-price endpoint for the identifiers bitcoin, ethereum, and litecoin, requesting values in INR and 24-hour percentage changes. The service parses numeric values into BigDecimal values used by the dashboard. If the request fails or the response omits expected data, price fields can be unavailable and the dashboard indicates that market data could not be loaded.

The displayed portfolio value is an estimate based on the available price response and balances stored by the application. It is not a guaranteed sell price and does not include exchange spreads, fees, slippage, or settlement effects.

Prerequisites
JDK 17 or later
MySQL Server running locally or an accessible MySQL instance
Git (for cloning the repository)
Internet access for retrieving external market-price data (optional; wallet pages can still have unavailable price fields when the API is unreachable)
Local Setup
1. Clone the repository
git clone https://github.com/Khy9/CryptoWallet-Management.git
cd CryptoWallet-Management
Run commands from the directory containing pom.xml and mvnw.cmd.

2. Create the database
Create the database in MySQL:

CREATE DATABASE cryptowalletdb;
The application uses Hibernate/JPA to update entity-backed schema objects under the current development configuration. If you change the database name, update the datasource URL as well.

3. Configure credentials
Create a local file named .env in the same directory as pom.xml and mvnw.cmd:

DB_USERNAME=root
DB_PASSWORD=replace_with_your_mysql_password
The application.properties configuration should import this file and reference the variables, for example:

spring.config.import=optional:file:./.env[.properties]
spring.datasource.url=jdbc:mysql://localhost:3306/cryptowalletdb?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
server.port=8081
Keep the real .env file out of Git. Commit a sanitized example file such as .env.example only if useful; never put live passwords or API credentials in it. If application startup reports unresolved DB_USERNAME or DB_PASSWORD placeholders, verify that .env is in the working directory from which the application is started and that the import line is present.

4. Start the application
Windows PowerShell

.\mvnw.cmd spring-boot:run
macOS / Linux

./mvnw spring-boot:run
The configured HTTP port is 8081. The first run may take longer while Maven resolves dependencies.

5. Use the application
Open http://localhost:8081, register an account or log in, then navigate through the dashboard. After authentication, the recommendation page is available at:

http://localhost:8081/wallet-recommendation

The dashboard navigation includes a Wallet Recommendation link to that route.

Configuration Notes
Common application settings are defined in src/main/resources/application.properties:

Property	Purpose
server.port	HTTP port; the local default is 8081
spring.datasource.url	MySQL JDBC connection URL
spring.datasource.username / spring.datasource.password	Database credentials supplied through environment configuration
spring.jpa.hibernate.ddl-auto	Schema behavior; currently configured for development updates
spring.jpa.show-sql	SQL logging; useful during development, potentially noisy in production
spring.thymeleaf.cache	Template caching behavior during development
Do not expose SQL bind values, credentials, session cookies, or user data in production logs. Use environment-specific configuration and migration tooling for deployment.

Security Considerations and Known Limitations
Simulated asset operations: wallet actions update application data only; there is no blockchain node, transaction signing, private-key storage, or custody integration.
Session-based access control: protected routes require authentication, but administrative routes should be protected with explicit role/authority checks before production deployment.
Password protection: BCrypt is configured for password hashing. This does not by itself make the application production-ready; validation, rate limiting, password-reset procedures, and operational controls should also be reviewed.
Database configuration: local credentials must be stored outside tracked source files. Rotate any credential that has previously been committed or exposed.
Development schema and logging: automatic schema updates and verbose SQL logging are convenient locally but should be reassessed for production.
External price dependency: CoinGecko responses may fail, be delayed, or be rate-limited. The application can therefore show unavailable prices.
Recommendation limits: the rules are simple thresholds over application records and do not constitute investment advice or a comprehensive wallet-security assessment.
Production hardening: deployment would require review of HTTPS, secret management, authorization boundaries, input validation, error handling, database backups, monitoring, and dependency updates.
Development Workflow
For a typical change:

Implement or update the controller, service, repository, entity, or template responsible for the feature.
Run the application locally and exercise the affected route with an authenticated test account.
Review the server output for template-resolution, persistence, or API errors.
Check the resulting changes with git status, stage the intended files, commit, and push to the configured remote.
Useful commands:

git status
git diff
git add <files>
git commit -m "Describe the change"
git push origin main
License
No license has been specified. Add a LICENSE file if you intend to define permissions for copying, modification, or redistribution.

