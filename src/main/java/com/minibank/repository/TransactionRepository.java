package com.minibank.repository;

import com.minibank.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для неизменяемого журнала транзакций (Ledger).
 */
public interface TransactionRepository {

    /**
     * Записывает новую транзакцию в журнал.
     */
    Transaction save(Transaction transaction);

    /**
     * Поиск транзакции по id.
     */
    Optional<Transaction> findById(Long id);

    /**
     * Возвращает полный список всех совершенных транзакций.
     */
    List<Transaction> findAll();

    /**
     * Возвращает историю транзакций, относящихся к указанному счету
     * (где счет выступает отправителем или получателем).
     */
    List<Transaction> findByAccountId(Long accountId);

    /**
     * Генерирует следующий уникальный идентификатор транзакции.
     */
    Long nextId();
}
