package com.minibank.repository.inmemory;

import com.minibank.model.Account;
import com.minibank.repository.AccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Потокобезопасная реализация хранилища счетов в оперативной памяти на базе Map.
 * Использует ConcurrentHashMap для обеспечения целостности данных при параллельном доступе
 * и AtomicLong для генерации монотонно возрастающих первичных ключей.
 */
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<Long, Account> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Account save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Невозможно сохранить пустой объект счета (null)");
        }
        storage.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<Account> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(acc -> accountNumber.equalsIgnoreCase(acc.getAccountNumber()))
                .findFirst();
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public Long nextId() {
        return idSequence.getAndIncrement();
    }
}
