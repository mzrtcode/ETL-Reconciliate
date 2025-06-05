package co.com.itau.batch.tasklet;

import co.com.itau.dto.ReconciliationBatchResult;
import co.com.itau.dto.ReconciliationTransactionResult;
import co.com.itau.jpat.dto.BpBatchDTO;
import co.com.itau.jpat.dto.BpBatchTransactionDTO;
import co.com.itau.service.ExcelReportService;
import co.com.itau.swift.dto.AsMonitoringMessageDTO;
import co.com.itau.swift.dto.AsMonitoringPaymentDTO;
import co.com.itau.utils.ChunkContextUtil;
import co.com.itau.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReconcileMessagesTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(ReconcileMessagesTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("--------------> STARTED MESSAGE RECONCILIATION SWIFT vs JPAT <--------------");

        List<AsMonitoringMessageDTO> messages = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_MESSAGES);
        Map<String, List<BpBatchDTO>> batchesMap = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_BATCH_MAP);

        if (isEmpty(messages)) {
            log.warn("No messages found to reconcile.");
            return RepeatStatus.FINISHED;
        }

        List<ReconciliationBatchResult> batchResults = new ArrayList<>();
        List<ReconciliationTransactionResult> transactionResults = new ArrayList<>();

        messages.forEach(message -> processMessage(message, batchesMap, batchResults, transactionResults));

        ExcelReportService.generarExcel(batchResults, transactionResults);
        log.info("--------------> FINISHED MESSAGE RECONCILIATION SWIFT vs JPAT <--------------");
        return RepeatStatus.FINISHED;
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

        logBatchResult(batchResult, message, batches);
        trxResults.forEach(this::logTransactionResult);
    }

    private ReconciliationBatchResult reconcileBatch(AsMonitoringMessageDTO message, List<BpBatchDTO> batches,
                                                     List<ReconciliationTransactionResult> trxResults) {
        BpBatchDTO selectedBatch = batches.isEmpty() ? new BpBatchDTO() : batches.get(0);
        String status = determineBatchStatus(batches, trxResults, message.getAmount(), selectedBatch.getTotalAmount());

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
        if (batches.isEmpty()) return "ERROR";
        if (batches.size() > 1) return "LOTE DUPLICADO JPAT";
        if (trxResults.stream().anyMatch(t -> !"OK".equals(t.getStatus()))) return "TRANSACCIONES CON ERROR";
        if (!Objects.equals(swiftAmount, jpatAmount)) return "DIFERENCIA EN VALOR";
        return "OK";
    }

    private List<ReconciliationTransactionResult> reconcileTransactions(AsMonitoringMessageDTO message, List<BpBatchDTO> batches) {
        List<AsMonitoringPaymentDTO> swiftPayments = Optional.ofNullable(message.getPayments()).orElse(Collections.emptyList());
        List<BpBatchTransactionDTO> jpatTransactions = batches.stream()
                .flatMap(b -> Optional.ofNullable(b.getTransactions()).orElse(Collections.emptyList()).stream())
                .collect(Collectors.toList());

        Map<BpBatchTransactionDTO, Boolean> usedJpatTransactions = jpatTransactions.stream()
                .collect(Collectors.toMap(t -> t, t -> false, (a, b) -> a, HashMap::new));

        List<ReconciliationTransactionResult> results = new ArrayList<>();
        swiftPayments.forEach(payment -> reconcilePayment(payment, jpatTransactions, usedJpatTransactions, message.getMessageId(), results));
        addUnmatchedJpatTransactions(jpatTransactions, usedJpatTransactions, message.getMessageId(), results);

        return results;
    }

    private void reconcilePayment(AsMonitoringPaymentDTO payment, List<BpBatchTransactionDTO> jpatTransactions,
                                  Map<BpBatchTransactionDTO, Boolean> usedJpatTransactions, String swiftId,
                                  List<ReconciliationTransactionResult> results) {
        List<BpBatchTransactionDTO> matches = jpatTransactions.stream()
                .filter(t -> isMatchingTransaction(t, payment))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            results.add(createUnmatchedSwiftTransaction(swiftId, payment));
            return;
        }

        int matchIndex = 1;
        for (BpBatchTransactionDTO match : matches) {
            results.add(createMatchedTransaction(swiftId, payment, match, matches.size() > 1 ? "TRANSACCIÓN DUPLICADA JPAT " + matchIndex++ : "OK"));
            usedJpatTransactions.put(match, true);
        }
    }

    private boolean isMatchingTransaction(BpBatchTransactionDTO jpat, AsMonitoringPaymentDTO swift) {
        return Objects.equals(jpat.getBtrReference(), swift.getReference())
                && Objects.equals(jpat.getBtrSourceAccount(), swift.getPayerAccount())
                && Objects.equals(jpat.getBtrDestAccount(), swift.getBeneficiaryAccount())
                && jpat.getBtrAmount().compareTo(swift.getAmount()) == 0;
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
        result.setStatus("NO EN JPAT");
        return result;
    }

    private void addUnmatchedJpatTransactions(List<BpBatchTransactionDTO> jpatTransactions,
                                              Map<BpBatchTransactionDTO, Boolean> usedJpatTransactions,
                                              String swiftId, List<ReconciliationTransactionResult> results) {
        usedJpatTransactions.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .forEach(trx -> {
                    ReconciliationTransactionResult result = new ReconciliationTransactionResult();
                    result.setSwiftId(swiftId);
                    result.setJpatReference(trx.getBtrReference());
                    result.setJpatAmount(trx.getBtrAmount());
                    result.setJpatSourceAccount(trx.getBtrSourceAccount());
                    result.setJpatDestinationAccount(trx.getBtrDestAccount());
                    result.setStatus("NO EN SWIFT");
                    results.add(result);
                });
    }

    private void logBatchResult(ReconciliationBatchResult result, AsMonitoringMessageDTO message, List<BpBatchDTO> batches) {
        log.info("==> Batch reconciliation for messageId={} | customer={} | file={} | fechaCargue={} | fechaAplicacion={} | " +
                        "amountSwift={} | amountJpat={} | trxCountSwift={} | trxCountJpat={} | status={}",
                result.getSwiftId(), result.getCustomerNit(), result.getFileName(),
                result.getLoadingTime(), result.getApplicationDate(),
                result.getAmountSwift(), result.getAmountJpat(),
                Optional.ofNullable(message.getPayments()).orElse(Collections.emptyList()).size(),
                batches.isEmpty() ? 0 : batches.get(0).getTransactions().size(),
                result.getStatus());
    }

    private void logTransactionResult(ReconciliationTransactionResult tr) {
        log.info(" -> Transaction reconciliation | refSwift={} | valSwift={} | srcSwift={} | dstSwift={} | " +
                        "refJpat={} | valJpat={} | srcJpat={} | dstJpat={} | status={}",
                tr.getSwiftReference(), tr.getSwiftAmount(), tr.getSwiftSourceAccount(), tr.getSwiftDestinationAccount(),
                tr.getJpatReference(), tr.getJpatAmount(), tr.getJpatSourceAccount(), tr.getJpatDestinationAccount(),
                tr.getStatus());
    }

    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}

