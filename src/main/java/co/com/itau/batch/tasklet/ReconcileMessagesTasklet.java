package co.com.itau.batch.tasklet;

import co.com.itau.dto.ReconciliationResultDTO;
import co.com.itau.jpat.dto.BpBatchDTO;
import co.com.itau.service.ReconciliationService;
import co.com.itau.swift.dto.AsMonitoringMessageDTO;
import co.com.itau.utils.ChunkContextUtil;
import co.com.itau.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ReconcileMessagesTasklet implements Tasklet {

    public static final String STATUS_NOT_IN_SWIFT = "NO EN SWIFT";
    public static final String STATUS_NOT_IN_JPAT = "NO EN JPAT";
    public static final String STATUS_DUPLICATE_TRANSACTION_JPAT = "TRANSACCION DUPLICADA JPAT ";
    public static final String STATUS_SUCCESS = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_DUPLICATE_BATCH_JPAT = "LOTE DUPLICADO JPAT";
    public static final String STATUS_VALUE_MISMATCH = "DIFERENCIA EN VALOR";
    public static final String STATUS_TRANSACTIONS_WITH_ERROR = "TRANSACCIONES CON ERROR";

    private static final Logger log = LoggerFactory.getLogger(ReconcileMessagesTasklet.class);


    private final ReconciliationService reconciliationService;

    public ReconcileMessagesTasklet(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("--------------> STARTED MESSAGE RECONCILIATION SWIFT vs JPAT STEP <--------------");

        List<AsMonitoringMessageDTO> messages = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_MESSAGES);
        Map<String, List<BpBatchDTO>> batchesMap = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_BATCH_MAP);

        ReconciliationResultDTO result = reconciliationService.reconcileMessages(messages, batchesMap);

        ChunkContextUtil.setChunkContext(chunkContext , Constants.CONTEXT_KEY_RECONCILIATION_RESULT, result);

        log.info("--------------> FINISHED MESSAGE RECONCILIATION SWIFT vs JPAT STEP <--------------");
        return RepeatStatus.FINISHED;
    }


}