package dio.budgeting.application.output;

import java.math.BigDecimal;

final class Money {
    private static final int CENTS_SCALE = 2;

    private Money() {
    }

    static BigDecimal centsToReais(long amountInCents) {
        return BigDecimal.valueOf(amountInCents, CENTS_SCALE);
    }
}