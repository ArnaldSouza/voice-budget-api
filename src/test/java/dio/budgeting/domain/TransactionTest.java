package dio.budgeting.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {
    private static final String VALID_DESCRIPTION = "Farmácia";
    private static final long VALID_AMOUNT_IN_CENTS = 8000;

    @Test
    void createsTransactionWithGeneratedIdAndTrimmedDescription() {
        var transaction = new Transaction("  Farmácia  ", VALID_AMOUNT_IN_CENTS, Category.PHARMA);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("Farmácia");
        assertThat(transaction.getAmountInCents()).isEqualTo(VALID_AMOUNT_IN_CENTS);
        assertThat(transaction.getCategory()).isEqualTo(Category.PHARMA);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -8000})
    void rejectsNonPositiveAmount(long amountInCents) {
        assertThatThrownBy(() -> new Transaction(VALID_DESCRIPTION, amountInCents, Category.PHARMA))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transaction amount must be greater than zero");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankDescription(String description) {
        assertThatThrownBy(() -> new Transaction(description, VALID_AMOUNT_IN_CENTS, Category.PHARMA))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transaction description must not be blank");
    }

    @Test
    void rejectsMissingCategory() {
        assertThatThrownBy(() -> new Transaction(VALID_DESCRIPTION, VALID_AMOUNT_IN_CENTS, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transaction category is required");
    }
}