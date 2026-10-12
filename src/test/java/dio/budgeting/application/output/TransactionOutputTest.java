package dio.budgeting.application.output;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionOutputTest {

    @Test
    void exposesAmountInReaisInsteadOfCents() {
        var transaction = new Transaction("Mercado", 2550, Category.GROCERIES);

        var output = TransactionOutput.from(transaction);

        assertThat(output.amountInReais()).isEqualTo(new BigDecimal("25.50"));
        assertThat(output.description()).isEqualTo("Mercado");
        assertThat(output.category()).isEqualTo("GROCERIES");
        assertThat(output.id()).isEqualTo(transaction.getId().uuid().toString());
    }
}