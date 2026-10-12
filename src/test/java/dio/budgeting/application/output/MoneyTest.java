package dio.budgeting.application.output;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @ParameterizedTest
    @CsvSource({
            "2550, 25.50",
            "8000, 80.00",
            "1, 0.01",
            "0, 0.00"
    })
    void convertsCentsToReaisWithTwoDecimalPlaces(long amountInCents, String expectedReais) {
        assertThat(Money.centsToReais(amountInCents)).isEqualTo(new BigDecimal(expectedReais));
    }
}