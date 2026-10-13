package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

class InMemoryTransactionRepository implements TransactionRepository {
    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public Transaction save(Transaction transaction) {
        transactions.add(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> findAllByCategory(Category category) {
        return transactions.stream()
                .filter(transaction -> transaction.getCategory() == category)
                .toList();
    }

    @Override
    public long sumAmountInCentsByCategory(Category category) {
        return findAllByCategory(category).stream()
                .mapToLong(Transaction::getAmountInCents)
                .sum();
    }
}