package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;

public record TransactionRequest(String description, Category category, long amountInCents) {
    public PersistTransactionInput toInput() {
        return new PersistTransactionInput(description, amountInCents, category);
    }
}