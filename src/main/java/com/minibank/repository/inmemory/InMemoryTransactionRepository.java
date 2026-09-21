package com.minibank.repository.inmemory;

import com.minibank.model.Transaction;
import com.minibank.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Реализация журнала транзакций в оперативной памяти.
 * Использует CopyOnWriteArrayList для безопасного чтения и записи в многопоточной среде.
 */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Невозможно сохранить пустую транзакцию (null)");
        }
        transactions.add(transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return transactions.stream()
                .filter(tx -> Objects.equals(tx.getId(), id))
                .findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(transactions));
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId) {
        if (accountId == null) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .filter(tx -> Objects.equals(tx.getSourceAccountId(), accountId) ||
                              Objects.equals(tx.getTargetAccountId(), accountId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public Long nextId() {
        return idSequence.getAndIncrement();
    }
}
