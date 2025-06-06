package co.com.itau.service;

import co.com.itau.dto.ReconciliationBatchResult;
import co.com.itau.dto.ReconciliationResultDTO;
import co.com.itau.dto.ReconciliationTransactionResult;
import co.com.itau.jpat.dto.BpBatchDTO;
import co.com.itau.jpat.dto.BpBatchTransactionDTO;
import co.com.itau.swift.dto.AsMonitoringMessageDTO;
import co.com.itau.swift.dto.AsMonitoringPaymentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    public static final String STATUS_NOT_IN_SWIFT = "NO EN SWIFT";
    public static final String STATUS_NOT_IN_JPAT = "NO EN JPAT";
    public static final String STATUS_DUPLICATE_TRANSACTION_JPAT = "TRANSACCION DUPLICADA JPAT ";
    public static final String STATUS_SUCCESS = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_DUPLICATE_BATCH_JPAT = "LOTE DUPLICADO JPAT";
    public static final String STATUS_VALUE_MISMATCH = "DIFERENCIA EN VALOR";
    public static final String STATUS_TRANSACTIONS_WITH_ERROR = "TRANSACCIONES CON ERROR";

    public ReconciliationResultDTO reconcileMessages(List<AsMonitoringMessageDTO> messages, Map<String, List<BpBatchDTO>> batchesMap) {

        if (isEmpty(messages)) {
            log.warn("No messages found to reconcile.");
            return new ReconciliationResultDTO(Collections.emptyList(), Collections.emptyList());
        }

        List<ReconciliationBatchResult> batchResults = new ArrayList<>();
        List<ReconciliationTransactionResult> transactionResults = new ArrayList<>();

        messages.forEach(message -> processMessage(message, batchesMap, batchResults, transactionResults));

        return new ReconciliationResultDTO(batchResults, transactionResults);
    }

    private void processMessage(AsMonitoringMessageDTO message, Map<String, List<BpBatchDTO>> batchesMap,
                                List<ReconciliationBatchResult> batchResults, List<ReconciliationTransactionResult> transactionResults) {
        List<BpBatchDTO> batches = batchesMap.getOrDefault(message.getMessageId(), Collections.emptyList());
        if (batches.isEmpty()) {
            log.warn("No batches found for message ID={}", message.getMessageId());
        }

        List<ReconciliationTransactionResult> trxResults = reconcileTransactions(message, batches);
        transactionResults.addAll(trxResults);

        ReconciliationBatchResult batchResult = reconcileBatch(message, batches, trxResults);
        batchResults.add(batchResult);
    }

    private ReconciliationBatchResult reconcileBatch(AsMonitoringMessageDTO message, List<BpBatchDTO> batches,
                                                     List<ReconciliationTransactionResult> trxResults) {
        BpBatchDTO selectedBatch = batches.isEmpty() ? new BpBatchDTO() : batches.getFirst();
        String status = determineBatchStatus(batches, trxResults, message.getAmount(), selectedBatch.getTotalAmount());

        ReconciliationBatchResult result = getReconciliationBatchResult(message, selectedBatch, status);

        log.info("==> Batch reconciliation for messageId={} | customer={} | file={} | fechaCargue={} | fechaAplicacion={} | " +
                        "amountSwift={} | amountJpat={} | trxCountSwift={} | trxCountJpat={} | status={}",
                result.getSwiftId(), result.getCustomerNit(), result.getFileName(),
                result.getLoadingTime(), result.getApplicationDate(),
                result.getAmountSwift(), result.getAmountJpat(),
                Optional.ofNullable(message.getPayments()).orElse(Collections.emptyList()).size(),
                batches.isEmpty() ? 0 : batches.getFirst().getTransactions().size(),
                result.getStatus());

        return result;
    }

    private ReconciliationTransactionResult createMatchedTransaction(String swiftId, AsMonitoringPaymentDTO payment,
                                                                     BpBatchTransactionDTO match, String status) {
        ReconciliationTransactionResult result = new ReconciliationTransactionResult();
        result.setSwiftId(swiftId);
        result.setSwiftReference(payment.getReference());
        result.setSwiftAmount(payment.getAmount());
        result.setSwiftSourceAccount(payment.getPayerAccount());
        result.setSwiftDestinationAccount(payment.getBeneficiaryAccount());
        result.setJpatReference(match.getBtrReference());
        result.setJpatAmount(match.getBtrAmount());
        result.setJpatSourceAccount(match.getBtrSourceAccount());
        result.setJpatDestinationAccount(match.getBtrDestAccount());
        result.setStatus(status);
        return result;
    }

    private ReconciliationTransactionResult createUnmatchedSwiftTransaction(String swiftId, AsMonitoringPaymentDTO payment) {
        ReconciliationTransactionResult result = new ReconciliationTransactionResult();
        result.setSwiftId(swiftId);
        result.setSwiftReference(payment.getReference());
        result.setSwiftAmount(payment.getAmount());
        result.setSwiftSourceAccount(payment.getPayerAccount());
        result.setSwiftDestinationAccount(payment.getBeneficiaryAccount());
        result.setStatus(STATUS_NOT_IN_JPAT);
        return result;
    }

    private List<ReconciliationTransactionResult> reconcileTransactions(AsMonitoringMessageDTO message, List<BpBatchDTO> batches) {
        List<AsMonitoringPaymentDTO> swiftPayments = Optional.ofNullable(message.getPayments()).orElse(Collections.emptyList());
        List<BpBatchTransactionDTO> jpatTransactions = batches.stream()
                .flatMap(b -> Optional.ofNullable(b.getTransactions()).orElse(Collections.emptyList()).stream())
                .collect(Collectors.toList());

        Map<BpBatchTransactionDTO, Boolean> usedJpatTransactions = new HashMap<>();
        jpatTransactions.forEach(trx -> usedJpatTransactions.put(trx, false));

        List<ReconciliationTransactionResult> results = new ArrayList<>();
        swiftPayments.forEach(payment -> reconcilePayment(payment, jpatTransactions, usedJpatTransactions, message.getMessageId(), results));
        addUnmatchedJpatTransactions(jpatTransactions, usedJpatTransactions, message.getMessageId(), results);

        return results;
    }

    private void reconcilePayment(AsMonitoringPaymentDTO payment, List<BpBatchTransactionDTO> jpatTransactions,
                                  Map<BpBatchTransactionDTO, Boolean> usedJpatTransactions, String swiftId,
                                  List<ReconciliationTransactionResult> results) {
        List<BpBatchTransactionDTO> matches = jpatTransactions.stream()
                .filter(trxJpat -> isMatchingTransaction(trxJpat, payment))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            ReconciliationTransactionResult result = createUnmatchedSwiftTransaction(swiftId, payment);
            logTransactionResult(result);
            results.add(result);
            return;
        }

        int matchIndex = 1;
        for (BpBatchTransactionDTO match : matches) {
            String status = matches.size() > 1 ? STATUS_DUPLICATE_TRANSACTION_JPAT + matchIndex++ : STATUS_SUCCESS;
            ReconciliationTransactionResult result = createMatchedTransaction(swiftId, payment, match, status);
            logTransactionResult(result);
            results.add(result);
            usedJpatTransactions.put(match, true);
        }
    }

    private boolean isMatchingTransaction(BpBatchTransactionDTO jpat, AsMonitoringPaymentDTO swift) {
        return Objects.equals(jpat.getBtrReference(), swift.getReference())
                && Objects.equals(jpat.getBtrSourceAccount(), swift.getPayerAccount())
                && Objects.equals(jpat.getBtrDestAccount(), swift.getBeneficiaryAccount())
                && jpat.getBtrAmount().compareTo(swift.getAmount()) == 0;
    }

    private void addUnmatchedJpatTransactions(List<BpBatchTransactionDTO> jpatTransactions,
                                              Map<BpBatchTransactionDTO, Boolean> usedJpatTransactions,
                                              String swiftId, List<ReconciliationTransactionResult> results) {
        usedJpatTransactions.entrySet().stream()
                .filter(jpatEntry -> !jpatEntry.getValue())
                .map(Map.Entry::getKey)
                .forEach(trx -> {
                    ReconciliationTransactionResult result = new ReconciliationTransactionResult();
                    result.setSwiftId(swiftId);
                    result.setJpatReference(trx.getBtrReference());
                    result.setJpatAmount(trx.getBtrAmount());
                    result.setJpatSourceAccount(trx.getBtrSourceAccount());
                    result.setJpatDestinationAccount(trx.getBtrDestAccount());
                    result.setStatus(STATUS_NOT_IN_SWIFT);
                    logTransactionResult(result);
                    results.add(result);
                });
    }

    private void logTransactionResult(ReconciliationTransactionResult tr) {
        log.info(" -> Transaction reconciliation | refSwift={} | valSwift={} | srcSwift={} | dstSwift={} | " +
                        "refJpat={} | valJpat={} | srcJpat={} | dstJpat={} | status={}",
                tr.getSwiftReference(), tr.getSwiftAmount(), tr.getSwiftSourceAccount(), tr.getSwiftDestinationAccount(),
                tr.getJpatReference(), tr.getJpatAmount(), tr.getJpatSourceAccount(), tr.getJpatDestinationAccount(),
                tr.getStatus());
    }

    private static ReconciliationBatchResult getReconciliationBatchResult(AsMonitoringMessageDTO message, BpBatchDTO selectedBatch, String status) {
        ReconciliationBatchResult result = new ReconciliationBatchResult();
        result.setSwiftId(message.getMessageId());
        result.setCustomerNit(message.getCustomerId());
        result.setFileName(selectedBatch.getBatName());
        result.setLoadingTime(message.getFechaCargue());
        result.setApplicationDate(message.getFechaAplicacion());
        result.setAmountSwift(message.getAmount());
        result.setAmountJpat(selectedBatch.getTotalAmount());
        result.setStatus(status);
        return result;
    }

    private String determineBatchStatus(List<BpBatchDTO> batches, List<ReconciliationTransactionResult> trxResults,
                                        BigDecimal swiftAmount, BigDecimal jpatAmount) {
        if (batches.isEmpty()) return STATUS_ERROR;
        if (batches.size() > 1) return STATUS_DUPLICATE_BATCH_JPAT;
        if (trxResults.stream().anyMatch(t -> !STATUS_SUCCESS.equals(t.getStatus()))) return STATUS_TRANSACTIONS_WITH_ERROR;
        if (!Objects.equals(swiftAmount, jpatAmount)) return STATUS_VALUE_MISMATCH;
        return STATUS_SUCCESS;
    }

    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}
