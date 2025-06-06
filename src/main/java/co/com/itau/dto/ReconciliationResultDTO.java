package co.com.itau.dto;

import java.io.Serializable;
import java.util.List;

public class ReconciliationResultDTO implements Serializable {
    private List<ReconciliationBatchResult> batchResults;
    private List<ReconciliationTransactionResult> transactionResults;

    public ReconciliationResultDTO(List<ReconciliationBatchResult> batchResults, List<ReconciliationTransactionResult> transactionResults) {
        this.batchResults = batchResults;
        this.transactionResults = transactionResults;
    }

    public List<ReconciliationBatchResult> getBatchResults() {
        return batchResults;
    }

    public List<ReconciliationTransactionResult> getTransactionResults() {
        return transactionResults;
    }
}
