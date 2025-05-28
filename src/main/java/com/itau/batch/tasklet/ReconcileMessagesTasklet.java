package com.itau.batch.tasklet;

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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReconcileMessagesTasklet implements Tasklet {

    private final Logger log = LoggerFactory.getLogger(ReconcileMessagesTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("--------------> STARTED MESSAGE RECONCILIATION SWIFT vs JPAT <--------------");


        ExecutionContext context = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();


        List<AsMonitoringMessageDTO> messages = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_MESSAGES);
        Map<String, BpBatchDTO> batchMap = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_BATCH_MAP);



        for (AsMonitoringMessageDTO message : messages) {
            BpBatchDTO batch = batchMap.get(message.getMessageId());
            if (batch == null) {
                continue;
            }

            boolean conciliado = true;
            List<BpBatchTransactionDTO> transactions = batch.getTransactions();

            for (AsMonitoringPaymentDTO payment : message.getPayments()) {
                Optional<BpBatchTransactionDTO> match = transactions.stream()
                        .filter(t -> Objects.equals(t.getBtrReference(), payment.getReference()))
                        .filter(t -> Objects.equals(t.getBtrAmount(), payment.getAmount()))
                        .filter(t -> t.getBtrSourceAccount().contains(payment.getPayerAccount()))
                        .filter(t -> t.getBtrDestAccount().contains(payment.getBeneficiaryAccount()))
                        .findFirst();

                if (match.isEmpty()) {
                    conciliado = false;
                    break;
                }
            }

            log.info("Mensaje {} conciliado: {}", message.getMessageId(), conciliado);
        }

        log.info("--------------> FINISHED MESSAGE RECONCILIATION SWIFT vs JPAT <--------------");
        return RepeatStatus.FINISHED;
    }
}