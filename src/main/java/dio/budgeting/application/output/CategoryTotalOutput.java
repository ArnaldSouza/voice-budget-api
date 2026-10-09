package dio.budgeting.application.output;

import dio.budgeting.domain.Category;

import java.math.BigDecimal;

public record CategoryTotalOutput(String category, BigDecimal totalInReais) {
    public static CategoryTotalOutput from(Category category, long totalInCents) {
        return new CategoryTotalOutput(category.name(), Money.centsToReais(totalInCents));
    }
}