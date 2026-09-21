# Mini-Bank (FinFlow Core) 🏦

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%26%20Layered-blue?style=for-the-badge)](https://en.wikipedia.org/wiki/Multitier_architecture)

Консольная банковская система управления счетами и транзакциями с чистой многослойной архитектурой (**Clean / Layered Architecture**), созданная с соблюдением принципов ООП, SOLID и лучших практик разработки на Java.

Проект спроектирован поэтапно: от чистого Java Core с In-Memory хранилищем до последующего расширения в полноценный микросервис на **Spring Boot, Spring Data JPA, PostgreSQL и REST API**.

---

## 🚀 Архитектурные слои (Layered Architecture)

```
com.minibank
├── model/          # Доменные сущности (Account, Transaction) и перечисления (Enum)
├── exception/      # Иерархия бизнес-исключений (Business Exceptions)
├── repository/     # Абстракции хранилища (Interfaces) и их In-Memory реализации
├── service/        # Бизнес-логика банковских операций (Интерфейс + Реализация)
└── Main.java       # Точка входа, демонстрационный сценарий и интерактивный запуск
```

---

## 🎯 Реализованные концепции (Этап 1: Java Core)

1. **Объектно-Ориентированное Программирование (ООП)**:
   - **Инкапсуляция**: все поля сущностей приватны; изменение баланса и статусов доступно только через контролируемые методы с проверками.
   - **Полиморфизм и DIP (Dependency Inversion Principle)**: слой бизнес-логики (`BankService`) зависит исключительно от интерфейсов репозиториев (`AccountRepository`, `TransactionRepository`), а не от конкретных деталей хранения.
2. **Точные финансовые расчеты**:
   - Использование `BigDecimal` вместо `double`/`float` для предотвращения ошибок округления с плавающей точкой.
3. **Строгая типизация через Enum**:
   - `AccountStatus` (`ACTIVE`, `BLOCKED`, `CLOSED`).
   - `TransactionType` (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`).
   - `TransactionCategory` (`SALARY`, `GROCERIES`, `UTILITIES`, `INVESTMENTS`, `TRANSFER`, `OTHER`).
4. **Иерархия исключений**:
   - Кастомные unchecked исключения, наследуемые от `BankException` (`RuntimeException`), что соответствует принятым практикам в современном Java/Spring.
5. **Коллекции Java**:
   - `Map<Long, Account>` для O(1) поиска по идентификатору счета.
   - `List<Transaction>` для хронологического лога операций с неизменяемыми выборками (`Collections.unmodifiableList`).

---

## 🛠️ Сборка и запуск

### Требования:
- JDK 17 или выше
- Apache Maven 3.8+

### Команды:

```bash
# Компиляция проекта
mvn clean compile

# Запуск тестов
mvn test

# Запуск приложения
mvn exec:java
```

---

## 🗺️ Дорожная карта развития проекта (Roadmap)

- [x] **Шаг 1**: Структура проекта, Maven, базовые модели (`Account`, `Transaction`) и Enum
- [x] **Шаг 2**: Бизнес-исключения (`InsufficientFundsException`, `AccountBlockedException` и др.)
- [x] **Шаг 3**: Интерфейсы репозиториев и In-Memory хранилище на коллекциях (`Map`, `List`)
- [x] **Шаг 4**: Сервисный слой банковских операций и валидация (`BankService`)
- [x] **Шаг 5**: Точка входа `Main` с интерактивным CLI и демонстрационным сценарием
- [ ] **Шаг 6 (Next)**: Модульные тесты JUnit 5 + AssertJ
- [ ] **Шаг 7 (Future)**: Миграция на Spring Boot (IoC / Dependency Injection)
- [ ] **Шаг 8 (Future)**: Spring Web REST API (`/api/v1/accounts`, `/api/v1/transactions`)
- [ ] **Шаг 9 (Future)**: PostgreSQL + Spring Data JPA + миграции Liquibase/Flyway
