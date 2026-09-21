package com.minibank.repository;

import com.minibank.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с банковскими счетами.
 * 
 * Соблюдает принцип инверсии зависимостей (DIP из SOLID):
 * бизнес-слой зависит от этой абстракции, а не от деталей хранения.
 * В будущем реализация легко заменяется на Spring Data JPA без изменений в сервисах.
 */
public interface AccountRepository {

    /**
     * Сохраняет новый счет или обновляет существующий.
     */
    Account save(Account account);

    /**
     * Поиск счета по первичному числовому ключу.
     * Возвращает Optional для явной защиты от NullPointerException.
     */
    Optional<Account> findById(Long id);

    /**
     * Поиск счета по уникальному номеру счета.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Возвращает список всех счетов в системе.
     */
    List<Account> findAll();

    /**
     * Проверяет существование счета по id.
     */
    boolean existsById(Long id);

    /**
     * Удаляет счет по id.
     */
    void deleteById(Long id);

    /**
     * Генерирует следующий уникальный идентификатор.
     */
    Long nextId();
}
