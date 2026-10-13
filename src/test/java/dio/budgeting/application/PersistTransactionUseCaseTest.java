package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistTransactionUseCaseTest {
    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final PersistTransactionUseCase useCase = new PersistTransactionUseCase(repository);

    @Test
    void persistsTransactionAndReturnsAmountInReais() {
        var output = useCase.execute(new PersistTransactionInput("Farmácia", 8000, Category.PHARMA));

        assertThat(output.amountInReais()).isEqualTo(new BigDecimal("80.00"));
        assertThat(repository.findAllByCategory(Category.PHARMA)).hasSize(1);
    }

    @Test
    void doesNotPersistInvalidTransaction() {
        var invalidInput = new PersistTransactionInput("Farmácia", 0, Category.PHARMA);

        assertThatThrownBy(() -> useCase.execute(invalidInput))
                .isInstanceOf(InvalidTransactionException.class);
        assertThat(repository.findAllByCategory(Category.PHARMA)).isEmpty();
    }
}