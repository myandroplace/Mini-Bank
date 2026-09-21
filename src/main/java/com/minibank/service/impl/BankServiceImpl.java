package com.minibank.service.impl;

import com.minibank.exception.AccountBlockedException;
import com.minibank.exception.AccountNotFoundException;
import com.minibank.exception.InsufficientFundsException;
import com.minibank.exception.InvalidAmountException;
import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionCategory;
import com.minibank.model.TransactionType;
import com.minibank.repository.AccountRepository;
import com.minibank.repository.TransactionRepository;
import com.minibank.service.BankService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Реализация сервиса управления банковскими счетами.
 * 
 * Внедрение зависимостей (Dependency Injection) выполнено через конструктор.
 * Это подготавливает код к бесшовной миграции на Spring Framework (@Service, @Autowired).
 */
public class BankServiceImpl implements BankService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "accountRepository не может быть null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository не может быть null");
    }

    @Override
    public Account createAccount(String ownerName, BigDecimal initialDeposit, String currency) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Имя владельца обязательно");
        }
        String curr = (currency != null && !currency.isBlank()) ? currency.trim().toUpperCase() : "RUB";
        BigDecimal initBalance = initialDeposit != null ? initialDeposit : BigDecimal.ZERO;

        if (initBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Начальный депозит не может быть отрицательным: " + initBalance);
        }

        Long id = accountRepository.nextId();
        String accountNumber = String.format("MB-%s-%06d", curr, id);

        Account account = new Account(id, accountNumber, ownerName.trim(), initBalance, curr);
        accountRepository.save(account);

        // Если был внесен начальный депозит, фиксируем первую транзакцию пополнения
        if (initBalance.compareTo(BigDecimal.ZERO) > 0) {
            Transaction initialTx = new Transaction(
                    transactionRepository.nextId(),
                    initBalance,
                    TransactionType.DEPOSIT,
                    TransactionCategory.OTHER,
                    null,
                    id,
                    "Первоначальный взнос при открытии счета"
            );
            transactionRepository.save(initialTx);
        }

        return account;
    }

    @Override
    public Transaction deposit(Long accountId, BigDecimal amount, TransactionCategory category, String description) {
        validatePositiveAmount(amount);
        Account account = findAccountOrThrow(accountId);
        validateAccountOperational(account);

        account.deposit(amount);
        accountRepository.save(account);

        Transaction tx = new Transaction(
                transactionRepository.nextId(),
                amount,
                TransactionType.DEPOSIT,
                category != null ? category : TransactionCategory.OTHER,
                null,
                accountId,
                description != null ? description : "Пополнение счета"
        );
        return transactionRepository.save(tx);
    }

    @Override
    public Transaction withdraw(Long accountId, BigDecimal amount, TransactionCategory category, String description) {
        validatePositiveAmount(amount);
        Account account = findAccountOrThrow(accountId);
        validateAccountOperational(account);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountId, account.getBalance(), amount);
        }

        account.withdraw(amount);
        accountRepository.save(account);

        Transaction tx = new Transaction(
                transactionRepository.nextId(),
                amount,
                TransactionType.WITHDRAWAL,
                category != null ? category : TransactionCategory.OTHER,
                accountId,
                null,
                description != null ? description : "Снятие средств"
        );
        return transactionRepository.save(tx);
    }

    @Override
    public Transaction transfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount, String description) {
        validatePositiveAmount(amount);

        if (Objects.equals(sourceAccountId, targetAccountId)) {
            throw new IllegalArgumentException("Счет отправителя и получателя не могут совпадать");
        }

        Account source = findAccountOrThrow(sourceAccountId);
        Account target = findAccountOrThrow(targetAccountId);

        validateAccountOperational(source);
        validateAccountOperational(target);

        if (!source.getCurrency().equalsIgnoreCase(target.getCurrency())) {
            throw new IllegalArgumentException(String.format(
                    "Мультивалютные переводы пока не поддерживаются (%s -> %s)",
                    source.getCurrency(), target.getCurrency()));
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(sourceAccountId, source.getBalance(), amount);
        }

        // Атомарное изменение состояния счетов
        source.withdraw(amount);
        target.deposit(amount);

        accountRepository.save(source);
        accountRepository.save(target);

        Transaction tx = new Transaction(
                transactionRepository.nextId(),
                amount,
                TransactionType.TRANSFER,
                TransactionCategory.TRANSFER,
                sourceAccountId,
                targetAccountId,
                description != null ? description : String.format("Перевод со счета %s на счет %s",
                        source.getAccountNumber(), target.getAccountNumber())
        );
        return transactionRepository.save(tx);
    }

    @Override
    public Account getAccount(Long accountId) {
        return findAccountOrThrow(accountId);
    }

    @Override
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Счет с номером " + accountNumber + " не найден"));
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public List<Transaction> getAccountTransactions(Long accountId) {
        findAccountOrThrow(accountId); // Проверяем существование счета
        return transactionRepository.findByAccountId(accountId);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public void blockAccount(Long accountId) {
        Account account = findAccountOrThrow(accountId);
        account.block();
        accountRepository.save(account);
    }

    @Override
    public void activateAccount(Long accountId) {
        Account account = findAccountOrThrow(accountId);
        account.activate();
        accountRepository.save(account);
    }

    @Override
    public void closeAccount(Long accountId) {
        Account account = findAccountOrThrow(accountId);
        account.close();
        accountRepository.save(account);
    }

    @Override
    public BigDecimal getTotalBalance(String currency) {
        String curr = currency != null ? currency.trim().toUpperCase() : "RUB";
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getCurrency().equalsIgnoreCase(curr))
                .filter(acc -> acc.getStatus().isOperational())
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Map<TransactionCategory, BigDecimal> getExpensesByCategory(Long accountId) {
        findAccountOrThrow(accountId);
        return transactionRepository.findByAccountId(accountId).stream()
                .filter(tx -> tx.getType() == TransactionType.WITHDRAWAL ||
                              (tx.getType() == TransactionType.TRANSFER && Objects.equals(tx.getSourceAccountId(), accountId)))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    // Вспомогательные методы валидации
    private Account findAccountOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор счета не может быть null");
        }
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(amount);
        }
    }

    private void validateAccountOperational(Account account) {
        if (!account.getStatus().isOperational()) {
            throw new AccountBlockedException(account.getId(), account.getStatus());
        }
    }
}
