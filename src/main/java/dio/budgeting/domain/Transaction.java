package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {
    private TransactionId id;
    private String description;
    private long amountInCents;
    private Category category;

    public Transaction(String description, long amountInCents, Category category) {
        this.id = new TransactionId();
        this.description = description;
        this.amountInCents = amountInCents;
        this.category = category;
    }
}