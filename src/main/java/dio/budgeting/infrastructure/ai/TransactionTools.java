package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SumTransactionsByCategoryUseCase;
import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.CategoryTotalOutput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionTools {
    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final SumTransactionsByCategoryUseCase sumTransactionsByCategoryUseCase;

    public TransactionTools(PersistTransactionUseCase persistTransactionUseCase,
                            ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                            SumTransactionsByCategoryUseCase sumTransactionsByCategoryUseCase) {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.sumTransactionsByCategoryUseCase = sumTransactionsByCategoryUseCase;
    }

    @Tool(name = "persist-transaction", description = "Persiste uma nova transação financeira")
    public TransactionOutput persistTransaction(
            @ToolParam(description = "Descrição do gasto") String description,
            @ToolParam(description = "Valor do gasto em centavos (ex.: R$ 25,50 = 2550)") long amountInCents,
            @ToolParam(description = "Categoria da transação") Category category) {
        return persistTransactionUseCase.execute(new PersistTransactionInput(description, amountInCents, category));
    }

    @Tool(name = "list-transactions-by-category", description = "Lista transações financeiras por categoria")
    public List<TransactionOutput> listTransactionsByCategory(
            @ToolParam(description = "Categoria da transação") Category category) {
        return listTransactionsByCategoryUseCase.execute(category);
    }

    @Tool(name = "sum-transactions-by-category", description = "Calcula o total gasto em uma categoria")
    public CategoryTotalOutput sumTransactionsByCategory(
            @ToolParam(description = "Categoria da transação") Category category) {
        return sumTransactionsByCategoryUseCase.execute(category);
    }
}