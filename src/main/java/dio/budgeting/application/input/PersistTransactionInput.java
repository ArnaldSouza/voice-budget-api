package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionInput(@ToolParam(description = "Descrição do gasto") String description,
                                      @ToolParam(description = "Valor do gasto em centavos (ex.: R$ 25,50 = 2550)") long amountInCents,
                                      @ToolParam(description = "Categoria de uma transação") Category category) {
}
