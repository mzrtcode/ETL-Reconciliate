package com.itau.batch.tasklet;

import com.itau.jpat.dao.BpBatchDAO;
import com.itau.jpat.dao.BpBatchTransactionDAO;
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

import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Component
public class LoadJpatBatchesTasklet implements Tasklet {

    private final BpBatchDAO batchDao;
    private final BpBatchTransactionDAO batchTransactionDAO;


    private static final Logger log = LoggerFactory.getLogger(LoadJpatBatchesTasklet.class);


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("--------------> STARTED LOADING JPAT BATCHES <--------------");


        ExecutionContext context = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();


        List<AsMonitoringMessageDTO> messages = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_MESSAGES);


        if (messages == null || messages.isEmpty()) {
            log.warn("No se encontraron mensajes en el contexto.");
            return RepeatStatus.FINISHED;
        }

        Map<String, BpBatchDTO> batchMap = new HashMap<>();

        for (AsMonitoringMessageDTO message : messages) {
            processMessage(message, batchMap);
        }

        log.info("Batches encontrados: {}", batchMap.keySet());
        context.put(Constants.CONTEXT_KEY_BATCH_MAP, batchMap);

        log.info("--------------> FINISHED LOADING JPAT BATCHES <--------------");

        return RepeatStatus.FINISHED;
    }

    private void processMessage(AsMonitoringMessageDTO message, Map<String, BpBatchDTO> batchMap) {
        if (message == null || message.getMessageId() == null) {
            log.warn("Mensaje nulo o sin ID, omitiendo");
            return;
        }

        log.debug("Procesando message con messageId: {}", message.getMessageId());

        List<AsMonitoringPaymentDTO> payments = message.getPayments();
        if (payments == null || payments.isEmpty()) {
            log.warn("No payments para messageId: {}", message.getMessageId());
            return;
        }

        AsMonitoringPaymentDTO firstPayment = payments.get(0);
        if (firstPayment == null || firstPayment.getReference() == null) {
            log.warn("Payment inválido o sin referencia para messageId: {}", message.getMessageId());
            return;
        }

        String reference = firstPayment.getReference();
        String customerId = message.getCustomerId();
        LocalDate searchDate = LocalDate.now().minusMonths(2);

        log.info("Buscando batch con customerId: {}, reference: {}", customerId, reference);

        Optional<List<BpBatchDTO>> batchOpt = batchDao.findAllBatchesByCustomerAndCreationDateAfterAndReference(
                customerId,
                searchDate.atStartOfDay(),
                reference
        );

        batchOpt.ifPresent(batchList -> batchList.forEach(batch -> {
            List<BpBatchTransactionDTO> transactions = batchTransactionDAO.findTransactionByBatchUUID(batch.getUuid());
            batch.setTransactions(transactions);
            batchMap.put(message.getMessageId(), batch);
            log.debug("Batch agregado para messageId: {}", message.getMessageId());
        }));

    }
}
