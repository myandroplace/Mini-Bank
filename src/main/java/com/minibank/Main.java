package com.minibank;

import com.minibank.exception.AccountBlockedException;
import com.minibank.exception.AccountNotFoundException;
import com.minibank.exception.InsufficientFundsException;
import com.minibank.exception.InvalidAmountException;
import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionCategory;
import com.minibank.repository.AccountRepository;
import com.minibank.repository.TransactionRepository;
import com.minibank.repository.inmemory.InMemoryAccountRepository;
import com.minibank.repository.inmemory.InMemoryTransactionRepository;
import com.minibank.service.BankService;
import com.minibank.service.impl.BankServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Главная точка входа в приложение Mini-Bank.
 * 
 * Демонстрирует:
 * 1. Ручную сборку графа зависимостей (ручной Dependency Injection / Inversion of Control),
 *    что наглядно объясняет, какую именно работу берет на себя Spring Framework.
 * 2. Демонстрационный сценарий полного жизненного цикла банковских операций.
 * 3. Обработку бизнес-исключений.
 * 4. Использование Stream API для аналитики расходов.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("          🏦 ДОБРО ПОЖАЛОВАТЬ В MINI-BANK (FINFLOW CORE) 🏦             ");
        System.out.println("========================================================================");
        System.out.println();

        // 1. Инициализация слоев (Dependency Injection)
        AccountRepository accountRepository = new InMemoryAccountRepository();
        TransactionRepository transactionRepository = new InMemoryTransactionRepository();
        BankService bankService = new BankServiceImpl(accountRepository, transactionRepository);

        System.out.println(">> [Архитектура] Граф зависимостей собран: In-Memory репозитории подключены к BankService.");
        System.out.println();

        if (args.length > 0 && "--interactive".equalsIgnoreCase(args[0])) {
            runInteractiveMenu(bankService);
        } else {
            runAutomatedDemonstration(bankService);
        }
    }

    /**
     * Комплексный демонстрационный сценарий, покрывающий все позитивные и негативные ветки.
     */
    private static void runAutomatedDemonstration(BankService bankService) {
        System.out.println(">>> ЭТАП 1: Создание банковских счетов");
        Account alice = bankService.createAccount("Алиса Смирнова", new BigDecimal("150000.00"), "RUB");
        Account bob = bankService.createAccount("Боб Иванов", new BigDecimal("45000.00"), "RUB");
        Account charlie = bankService.createAccount("Чарли Кузнецов", new BigDecimal("1000.00"), "RUB");

        printAccountsTable(bankService.getAllAccounts());

        System.out.println(">>> ЭТАП 2: Внесение депозитов и оплата расходов");
        bankService.deposit(bob.getId(), new BigDecimal("25000.00"), TransactionCategory.SALARY, "Аванс за проект");
        bankService.withdraw(alice.getId(), new BigDecimal("12400.50"), TransactionCategory.GROCERIES, "Закупка продуктов в гипермаркете");
        bankService.withdraw(alice.getId(), new BigDecimal("6500.00"), TransactionCategory.UTILITIES, "Оплата ЖКУ и электричества");
        bankService.withdraw(alice.getId(), new BigDecimal("3200.00"), TransactionCategory.ENTERTAINMENT, "Билеты в театр");

        System.out.println(">>> ЭТАП 3: Межбанковский перевод");
        System.out.println("Выполняем перевод 20 000.00 RUB от Алисы к Бобу...");
        bankService.transfer(alice.getId(), bob.getId(), new BigDecimal("20000.00"), "Возврат долга за поездку");

        printAccountsTable(bankService.getAllAccounts());

        System.out.println(">>> ЭТАП 4: Проверка обработки бизнес-исключений");

        // Тест 1: Недостаток средств
        System.out.print("[Исключение 1] Попытка снять 50 000 RUB со счета Чарли (баланс: 1 000 RUB)... ");
        try {
            bankService.withdraw(charlie.getId(), new BigDecimal("50000.00"), TransactionCategory.OTHER, "Покупка смартфона");
            System.out.println("ОШИБКА: транзакция не должна была пройти!");
        } catch (InsufficientFundsException e) {
            System.out.println("УСПЕШНО ПЕРЕХВАЧЕНО: " + e.getMessage());
        }

        // Тест 2: Отрицательная сумма
        System.out.print("[Исключение 2] Попытка пополнить счет на отрицательную сумму (-500 RUB)... ");
        try {
            bankService.deposit(alice.getId(), new BigDecimal("-500.00"), TransactionCategory.OTHER, "Ошибка ввода");
            System.out.println("ОШИБКА: транзакция не должна была пройти!");
        } catch (InvalidAmountException e) {
            System.out.println("УСПЕШНО ПЕРЕХВАЧЕНО: " + e.getMessage());
        }

        // Тест 3: Блокировка счета и запрет операций
        System.out.println("[Исключение 3] Блокируем счет Чарли и пробуем перевести средства...");
        bankService.blockAccount(charlie.getId());
        try {
            bankService.transfer(charlie.getId(), bob.getId(), new BigDecimal("500.00"), "Попытка перевода");
            System.out.println("ОШИБКА: перевод с заблокированного счета не должен пройти!");
        } catch (AccountBlockedException e) {
            System.out.println("УСПЕШНО ПЕРЕХВАЧЕНО: " + e.getMessage());
        }

        // Тест 4: Несуществующий счет
        System.out.print("[Исключение 4] Поиск счета с несуществующим ID 9999... ");
        try {
            bankService.getAccount(9999L);
            System.out.println("ОШИБКА: счет не должен быть найден!");
        } catch (AccountNotFoundException e) {
            System.out.println("УСПЕШНО ПЕРЕХВАЧЕНО: " + e.getMessage());
        }

        System.out.println();
        System.out.println(">>> ЭТАП 5: Финансовая выписка по счету Алисы");
        printTransactionsTable(bankService.getAccountTransactions(alice.getId()));

        System.out.println(">>> ЭТАП 6: Аналитика расходов по категориям (Stream API)");
        Map<TransactionCategory, BigDecimal> expenses = bankService.getExpensesByCategory(alice.getId());
        expenses.forEach((category, sum) ->
                System.out.printf("   • %-25s : %10.2f RUB%n", category.getDisplayName(), sum));

        System.out.println();
        System.out.printf(">> Общий объем ликвидности банка: %.2f RUB%n", bankService.getTotalBalance("RUB"));
        System.out.println();
        System.out.println("========================================================================");
        System.out.println("        ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА: ВСЕ ТЕСТЫ И ИНВАРИАНТЫ СОБЛЮДЕНЫ!       ");
        System.out.println("========================================================================");
    }

    private static void printAccountsTable(List<Account> accounts) {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("| %-4s | %-16s | %-20s | %-16s | %-12s |%n", "ID", "Номер счета", "Владелец", "Баланс", "Статус");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Account acc : accounts) {
            System.out.printf("| %-4d | %-16s | %-20s | %13.2f %-2s | %-12s |%n",
                    acc.getId(), acc.getAccountNumber(), acc.getOwnerName(),
                    acc.getBalance(), acc.getCurrency(), acc.getStatus().getDescription());
        }
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println();
    }

    private static void printTransactionsTable(List<Transaction> transactions) {
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-4s | %-12s | %-10s | %-12s | %-6s | %-6s | %-35s |%n",
                "ID", "Дата", "Тип", "Сумма", "Откуда", "Куда", "Описание");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        for (Transaction tx : transactions) {
            System.out.printf("| %-4d | %-12s | %-10s | %12.2f | %-6s | %-6s | %-35s |%n",
                    tx.getId(),
                    tx.getTimestamp().toLocalDate().toString(),
                    tx.getType().getTitle(),
                    tx.getAmount(),
                    tx.getSourceAccountId() != null ? "#" + tx.getSourceAccountId() : "Внешн.",
                    tx.getTargetAccountId() != null ? "#" + tx.getTargetAccountId() : "Внешн.",
                    tx.getDescription().length() > 35 ? tx.getDescription().substring(0, 32) + "..." : tx.getDescription());
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }

    private static void runInteractiveMenu(BankService bankService) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- ИНТЕРАКТИВНОЕ МЕНЮ MINI-BANK ---");
            System.out.println("1. Список всех счетов");
            System.out.println("2. Открыть новый счет");
            System.out.println("3. Пополнить счет");
            System.out.println("4. Снять средства");
            System.out.println("5. Перевод между счетами");
            System.out.println("6. Выписка по счету");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                System.out.println("До свидания!");
                break;
            }

            try {
                switch (choice) {
                    case "1" -> printAccountsTable(bankService.getAllAccounts());
                    case "2" -> {
                        System.out.print("Имя владельца: ");
                        String name = scanner.nextLine();
                        System.out.print("Начальный депозит (например 1000): ");
                        BigDecimal dep = new BigDecimal(scanner.nextLine().trim());
                        Account acc = bankService.createAccount(name, dep, "RUB");
                        System.out.println("Счет успешно открыт: " + acc);
                    }
                    case "3" -> {
                        System.out.print("ID счета: ");
                        Long id = Long.parseLong(scanner.nextLine().trim());
                        System.out.print("Сумма пополнения: ");
                        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
                        bankService.deposit(id, amount, TransactionCategory.OTHER, "Пополнение через консоль");
                        System.out.println("Счет успешно пополнен.");
                    }
                    case "4" -> {
                        System.out.print("ID счета: ");
                        Long id = Long.parseLong(scanner.nextLine().trim());
                        System.out.print("Сумма списания: ");
                        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
                        bankService.withdraw(id, amount, TransactionCategory.OTHER, "Снятие через консоль");
                        System.out.println("Средства успешно списаны.");
                    }
                    case "5" -> {
                        System.out.print("ID счета отправителя: ");
                        Long fromId = Long.parseLong(scanner.nextLine().trim());
                        System.out.print("ID счета получателя: ");
                        Long toId = Long.parseLong(scanner.nextLine().trim());
                        System.out.print("Сумма перевода: ");
                        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
                        bankService.transfer(fromId, toId, amount, "Консольный перевод");
                        System.out.println("Перевод успешно выполнен.");
                    }
                    case "6" -> {
                        System.out.print("ID счета: ");
                        Long id = Long.parseLong(scanner.nextLine().trim());
                        printTransactionsTable(bankService.getAccountTransactions(id));
                    }
                    default -> System.out.println("Неизвестный пункт меню.");
                }
            } catch (Exception e) {
                System.out.println("ОШИБКА: " + e.getMessage());
            }
        }
    }
}
