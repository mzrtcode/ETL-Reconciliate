package com.itau.batch.tasklet;

import com.itau.batch.TestBorrar;
import com.itau.batch.dto.ReconciliationBatchResult;
import com.itau.batch.dto.ReconciliationTransactionResult;
import com.itau.jpat.dto.BpBatchDTO;
import com.itau.jpat.dto.BpBatchTransactionDTO;
import com.itau.swift.dto.AsMonitoringMessageDTO;
import com.itau.swift.dto.AsMonitoringPaymentDTO;
import com.itau.utils.ChunkContextUtil;
import com.itau.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ReconcileMessagesTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(ReconcileMessagesTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("--------------> STARTED MESSAGE RECONCILIATION SWIFT vs JPAT <--------------");

        List<ReconciliationBatchResult> batchResultsExcel = new ArrayList<>();
        List<ReconciliationTransactionResult> transactionResultsExcel = new ArrayList<>();

        ExecutionContext context = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

        List<AsMonitoringMessageDTO> messages =
                ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_MESSAGES);

        Map<String, List<BpBatchDTO>> batchesMap =
                ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_BATCH_MAP);

        if (messages == null || messages.isEmpty()) {
            log.warn("No messages found to reconcile.");
            return RepeatStatus.FINISHED;
        }

        for (AsMonitoringMessageDTO message : messages) {
            List<BpBatchDTO> batches = batchesMap.getOrDefault(message.getMessageId(), Collections.emptyList());

            if (batches.isEmpty()) {
                log.warn("No batches found for message ID={}", message.getMessageId());
            }

            List<ReconciliationTransactionResult> trxResults = conciliarTransacciones(message, batches);
            transactionResultsExcel.addAll(trxResults);

            ReconciliationBatchResult batchResult = conciliarLote(message, batches, trxResults);
            batchResultsExcel.add(batchResult);

            log.info("==> Batch reconciliation for messageId={} | customer={} | file={} | fechaCargue={} | fechaAplicacion={} | " +
                            "amountSwift={} | amountJpat={} | trxCountSwift={} | trxCountJpat={} | status={}",
                    batchResult.getSwiftId(), batchResult.getCustomerNit(), batchResult.getFileName(),
                    batchResult.getLoadingTime(), batchResult.getApplicationDate(),
                    batchResult.getAmountSwift(), batchResult.getAmountJpat(),
                    message.getPayments().size(),
                    batches.isEmpty() ? 0 : batches.get(0).getTransactions().size(),
                    batchResult.getStatus());

            for (ReconciliationTransactionResult tr : trxResults) {
                log.info(" -> Transaction reconciliation | refSwift={} | valSwift={} | srcSwift={} | dstSwift={} | " +
                                "refJpat={} | valJpat={} | srcJpat={} | dstJpat={} | status={}",
                        tr.getSwiftReference(), tr.getSwiftAmount(), tr.getSwiftSourceAccount(), tr.getSwiftDestinationAccount(),
                        tr.getJpatReference(), tr.getJpatAmount(), tr.getJpatSourceAccount(), tr.getJpatDestinationAccount(),
                        tr.getStatus());
            }
        }

        TestBorrar.generarExcel(batchResultsExcel, transactionResultsExcel);

        log.info("--------------> FINISHED MESSAGE RECONCILIATION SWIFT vs JPAT <--------------");
        return RepeatStatus.FINISHED;
    }

    private ReconciliationBatchResult conciliarLote(AsMonitoringMessageDTO message, List<BpBatchDTO> batches,
                                                    List<ReconciliationTransactionResult> trxResults) {
        boolean duplicado = batches.size() > 1;
        boolean sinLote = batches.isEmpty();
        boolean allOk = trxResults.stream().allMatch(tr -> "OK".equals(tr.getStatus()));

        String status;
        if (sinLote) {
            status = "ERROR";
        } else if (duplicado) {
            status = "LOTE DUPLICADO JPAT";
        } else if (allOk) {
            status = "OK";
        } else {
            status = "ERROR";
        }

        BpBatchDTO selectedBatch = sinLote ? new BpBatchDTO() : batches.get(0);

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

    private List<ReconciliationTransactionResult> conciliarTransacciones(AsMonitoringMessageDTO message, List<BpBatchDTO> batches) {
        List<ReconciliationTransactionResult> results = new ArrayList<>();

        if (message.getPayments() == null || message.getPayments().isEmpty()) {
            log.warn("No payments found in message ID={}", message.getMessageId());
            return results;
        }

        List<BpBatchTransactionDTO> transactions = batches.isEmpty() ? Collections.emptyList() : batches.get(0).getTransactions();
        if (transactions == null) transactions = Collections.emptyList();

        for (AsMonitoringPaymentDTO payment : message.getPayments()) {
            ReconciliationTransactionResult result = new ReconciliationTransactionResult();

            List<BpBatchTransactionDTO> matches = transactions.stream()
                    .filter(t -> Objects.equals(t.getBtrReference(), payment.getReference()))
                    .filter(t -> t.getBtrAmount().compareTo(payment.getAmount()) == 0)
                    .filter(t -> Objects.equals(t.getBtrSourceAccount(), payment.getPayerAccount()))
                    .filter(t -> Objects.equals(t.getBtrDestAccount(), payment.getBeneficiaryAccount()))
                    .toList();

            String status;
            if (matches.isEmpty()) {
                status = "NO EN JPAT";
            } else if (matches.size() > 1) {
                status = "DUPLICADO JPAT";
            } else {
                status = "OK";
                BpBatchTransactionDTO match = matches.get(0);
                result.setJpatReference(match.getBtrReference());
                result.setJpatAmount(match.getBtrAmount());
                result.setJpatSourceAccount(match.getBtrSourceAccount());
                result.setJpatDestinationAccount(match.getBtrDestAccount());
            }

            result.setSwiftId(message.getMessageId());
            result.setSwiftReference(payment.getReference());
            result.setSwiftAmount(payment.getAmount());
            result.setSwiftSourceAccount(payment.getPayerAccount());
            result.setSwiftDestinationAccount(payment.getBeneficiaryAccount());
            result.setStatus(status);

            results.add(result);
        }

        return results;
    }
}

