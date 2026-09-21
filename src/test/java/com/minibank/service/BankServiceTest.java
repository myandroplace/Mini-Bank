package com.minibank.service;

import com.minibank.exception.AccountBlockedException;
import com.minibank.exception.AccountNotFoundException;
import com.minibank.exception.InsufficientFundsException;
import com.minibank.exception.InvalidAmountException;
import com.minibank.model.Account;
import com.minibank.model.AccountStatus;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionCategory;
import com.minibank.model.TransactionType;
import com.minibank.repository.AccountRepository;
import com.minibank.repository.TransactionRepository;
import com.minibank.repository.inmemory.InMemoryAccountRepository;
import com.minibank.repository.inmemory.InMemoryTransactionRepository;
import com.minibank.service.impl.BankServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для сервиса BankService с использованием JUnit 5.
 * 
 * Покрывают как позитивные сценарии (happy path),
 * так и проверку генерации бизнес-исключений (edge cases).
 */
class BankServiceTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private BankService bankService;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        transactionRepository = new InMemoryTransactionRepository();
        bankService = new BankServiceImpl(accountRepository, transactionRepository);
    }

    @Nested
    @DisplayName("Операции открытия счета")
    class AccountCreationTests {

        @Test
        @DisplayName("Успешное открытие счета с положительным начальным балансом")
        void createAccount_withValidData_shouldCreateActiveAccount() {
            Account account = bankService.createAccount("Иван Петров", new BigDecimal("10000.00"), "RUB");

            assertNotNull(account.getId());
            assertEquals("Иван Петров", account.getOwnerName());
            assertEquals(new BigDecimal("10000.00"), account.getBalance());
            assertEquals("RUB", account.getCurrency());
            assertEquals(AccountStatus.ACTIVE, account.getStatus());
            assertTrue(account.getAccountNumber().startsWith("MB-RUB-"));

            // Проверяем, что была записана первоначальная транзакция
            List<Transaction> transactions = bankService.getAccountTransactions(account.getId());
            assertEquals(1, transactions.size());
            assertEquals(TransactionType.DEPOSIT, transactions.get(0).getType());
        }

        @Test
        @DisplayName("Выброс исключения при попытке открыть счет с отрицательным балансом")
        void createAccount_withNegativeInitialBalance_shouldThrowException() {
            assertThrows(InvalidAmountException.class, () ->
                    bankService.createAccount("Иван Петров", new BigDecimal("-100.00"), "RUB"));
        }
    }

    @Nested
    @DisplayName("Операции пополнения и списания")
    class DepositAndWithdrawTests {

        private Account testAccount;

        @BeforeEach
        void initAccount() {
            testAccount = bankService.createAccount("Анна Сидорова", new BigDecimal("5000.00"), "RUB");
        }

        @Test
        @DisplayName("Успешное пополнение счета")
        void deposit_validAmount_shouldIncreaseBalance() {
            bankService.deposit(testAccount.getId(), new BigDecimal("2500.00"), TransactionCategory.SALARY, "Зарплата");

            Account updated = bankService.getAccount(testAccount.getId());
            assertEquals(new BigDecimal("7500.00"), updated.getBalance());
        }

        @Test
        @DisplayName("Пополнение счета с неположительной суммой выбрасывает InvalidAmountException")
        void deposit_zeroOrNegativeAmount_shouldThrowException() {
            assertThrows(InvalidAmountException.class, () ->
                    bankService.deposit(testAccount.getId(), BigDecimal.ZERO, TransactionCategory.OTHER, "Тест 0"));

            assertThrows(InvalidAmountException.class, () ->
                    bankService.deposit(testAccount.getId(), new BigDecimal("-50.00"), TransactionCategory.OTHER, "Тест -50"));
        }

        @Test
        @DisplayName("Успешное списание при достаточном балансе")
        void withdraw_sufficientFunds_shouldDecreaseBalance() {
            bankService.withdraw(testAccount.getId(), new BigDecimal("2000.00"), TransactionCategory.GROCERIES, "Продукты");

            Account updated = bankService.getAccount(testAccount.getId());
            assertEquals(new BigDecimal("3000.00"), updated.getBalance());
        }

        @Test
        @DisplayName("Списание суммы больше баланса выбрасывает InsufficientFundsException")
        void withdraw_insufficientFunds_shouldThrowException() {
            InsufficientFundsException ex = assertThrows(InsufficientFundsException.class, () ->
                    bankService.withdraw(testAccount.getId(), new BigDecimal("6000.00"), TransactionCategory.OTHER, "Овердрафт"));

            assertEquals(testAccount.getId(), ex.getAccountId());
            assertEquals(new BigDecimal("5000.00"), ex.getCurrentBalance());
            assertEquals(new BigDecimal("6000.00"), ex.getRequestedAmount());
        }
    }

    @Nested
    @DisplayName("Операции перевода средств")
    class TransferTests {

        private Account sender;
        private Account recipient;

        @BeforeEach
        void initAccounts() {
            sender = bankService.createAccount("Отправитель", new BigDecimal("10000.00"), "RUB");
            recipient = bankService.createAccount("Получатель", new BigDecimal("2000.00"), "RUB");
        }

        @Test
        @DisplayName("Успешный перевод между счетами сохраняет финансовый инвариант")
        void transfer_sufficientFunds_shouldTransferMoneyAtomically() {
            BigDecimal transferAmount = new BigDecimal("3500.00");
            Transaction tx = bankService.transfer(sender.getId(), recipient.getId(), transferAmount, "Оплата услуг");

            Account updatedSender = bankService.getAccount(sender.getId());
            Account updatedRecipient = bankService.getAccount(recipient.getId());

            assertEquals(new BigDecimal("6500.00"), updatedSender.getBalance());
            assertEquals(new BigDecimal("5500.00"), updatedRecipient.getBalance());
            assertEquals(TransactionType.TRANSFER, tx.getType());
            assertEquals(sender.getId(), tx.getSourceAccountId());
            assertEquals(recipient.getId(), tx.getTargetAccountId());
        }

        @Test
        @DisplayName("Перевод при недостатке средств выбрасывает InsufficientFundsException")
        void transfer_insufficientFunds_shouldThrowException() {
            assertThrows(InsufficientFundsException.class, () ->
                    bankService.transfer(sender.getId(), recipient.getId(), new BigDecimal("15000.00"), "Слишком много"));
        }

        @Test
        @DisplayName("Перевод самому себе выбрасывает IllegalArgumentException")
        void transfer_toSelf_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    bankService.transfer(sender.getId(), sender.getId(), new BigDecimal("100.00"), "Самому себе"));
        }

        @Test
        @DisplayName("Перевод с заблокированного счета выбрасывает AccountBlockedException")
        void transfer_fromBlockedAccount_shouldThrowException() {
            bankService.blockAccount(sender.getId());

            assertThrows(AccountBlockedException.class, () ->
                    bankService.transfer(sender.getId(), recipient.getId(), new BigDecimal("100.00"), "С заблокированного"));
        }
    }
}
