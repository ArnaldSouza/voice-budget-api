package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SumTransactionsByCategoryUseCaseTest {
    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final SumTransactionsByCategoryUseCase useCase = new SumTransactionsByCategoryUseCase(repository);

    @Test
    void sumsOnlyTransactionsOfRequestedCategory() {
        repository.save(new Transaction("Farmácia", 8000, Category.PHARMA));
        repository.save(new Transaction("Drogaria", 2550, Category.PHARMA));
        repository.save(new Transaction("Mercado", 1000, Category.GROCERIES));

        var total = useCase.execute(Category.PHARMA);

        assertThat(total.category()).isEqualTo("PHARMA");
        assertThat(total.totalInReais()).isEqualTo(new BigDecimal("105.50"));
    }

    @Test
    void returnsZeroForCategoryWithoutTransactions() {
        var total = useCase.execute(Category.AUTO);

        assertThat(total.totalInReais()).isEqualTo(new BigDecimal("0.00"));
    }
}