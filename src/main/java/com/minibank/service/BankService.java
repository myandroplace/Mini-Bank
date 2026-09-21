package com.minibank.service;

import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Основной сервисный интерфейс для управления банковскими счетами и операциями.
 * Определяет бизнес-контракт банковской системы.
 */
public interface BankService {

    /**
     * Открывает новый банковский счет с генерацией уникального номера.
     */
    Account createAccount(String ownerName, BigDecimal initialDeposit, String currency);

    /**
     * Пополняет указанный счет на заданную сумму.
     */
    Transaction deposit(Long accountId, BigDecimal amount, TransactionCategory category, String description);

    /**
     * Списывает средства со счета с валидацией баланса и статуса.
     */
    Transaction withdraw(Long accountId, BigDecimal amount, TransactionCategory category, String description);

    /**
     * Выполняет перевод между двумя счетами.
     */
    Transaction transfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount, String description);

    /**
     * Получает счет по идентификатору (выбрасывает AccountNotFoundException, если не найден).
     */
    Account getAccount(Long accountId);

    /**
     * Получает счет по его номеру.
     */
    Account getAccountByNumber(String accountNumber);

    /**
     * Возвращает список всех счетов.
     */
    List<Account> getAllAccounts();

    /**
     * Возвращает историю транзакций конкретного счета.
     */
    List<Transaction> getAccountTransactions(Long accountId);

    /**
     * Возвращает полный журнал транзакций системы.
     */
    List<Transaction> getAllTransactions();

    /**
     * Блокирует счет (запрет любых расходных и приходных операций).
     */
    void blockAccount(Long accountId);

    /**
     * Разблокирует счет.
     */
    void activateAccount(Long accountId);

    /**
     * Закрывает счет (возможно только при нулевом остатке).
     */
    void closeAccount(Long accountId);

    /**
     * Рассчитывает суммарный баланс всех активных счетов в заданной валюте.
     */
    BigDecimal getTotalBalance(String currency);

    /**
     * Аналитика: группирует расходы по категориям для указанного счета с использованием Stream API.
     */
    Map<TransactionCategory, BigDecimal> getExpensesByCategory(Long accountId);
}
