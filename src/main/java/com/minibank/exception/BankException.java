package com.minibank.exception;

/**
 * Базовое непроверяемое (unchecked) исключение банковского домена.
 * Наследование от RuntimeException — стандарт в современной Java и Spring Boot:
 * позволяет избежать загромождения сигнатур методов throws-декларациями
 * и централизованно перехватывать ошибки на верхнем слое (CLI или RestControllerAdvice).
 */
public class BankException extends RuntimeException {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
