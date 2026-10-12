package dio.budgeting.domain;

import lombok.Getter;

@Getter
public class Transaction {
    private final TransactionId id;
    private final String description;
    private final long amountInCents;
    private final Category category;

    public Transaction(String description, long amountInCents, Category category) {
        this(new TransactionId(), description, amountInCents, category);
    }

    public Transaction(TransactionId id, String description, long amountInCents, Category category) {
        this.id = requirePresent(id, "Transaction id is required");
        this.description = requireNonBlank(description);
        this.amountInCents = requirePositive(amountInCents);
        this.category = requirePresent(category, "Transaction category is required");
    }

    private static <T> T requirePresent(T value, String message) {
        if (value == null) {
            throw new InvalidTransactionException(message);
        }
        return value;
    }

    private static String requireNonBlank(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("Transaction description must not be blank");
        }
        return description.strip();
    }

    private static long requirePositive(long amountInCents) {
        if (amountInCents <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero");
        }
        return amountInCents;
    }
}