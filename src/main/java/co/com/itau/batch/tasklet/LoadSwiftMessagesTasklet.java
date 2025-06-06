package co.com.itau.batch.tasklet;

import co.com.itau.swift.dao.AsMonitoringMessagesDAO;
import co.com.itau.swift.dao.AsMonitoringPaymentsDAO;
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

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoadSwiftMessagesTasklet implements Tasklet {


    private final AsMonitoringMessagesDAO asMonitoringMessagesDAO;
    private final AsMonitoringPaymentsDAO asMonitoringPaymentsDAO;

    private final Logger log = LoggerFactory.getLogger(LoadSwiftMessagesTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("--------------> STARTED LOADING SWIFT MESSAGES STEP <--------------");


        LocalDateTime currentTime = LocalDateTime.of(2025, 1, 23, 0, 0);

        List<AsMonitoringMessageDTO> swiftMessages = asMonitoringMessagesDAO.findAllLoadedMessagesSince(currentTime);

        log.info("Total Swift Messages: {}", swiftMessages.size());

        swiftMessages.forEach(m -> {
            List<AsMonitoringPaymentDTO> payments = asMonitoringPaymentsDAO.findAllPaymentsByMmgSequence(m.getMessageId());

            if (payments == null || payments.isEmpty()) {
                log.warn("No se encontraron pagos para el lote con messageId={}", m.getMessageId());
            } else {
                log.info("Se encontraron {} pagos para el lote con messageId={}", payments.size(), m.getMessageId());
            }

            m.setPayments(payments);
        });

        ChunkContextUtil.setChunkContext(chunkContext , Constants.CONTEXT_KEY_MESSAGES, swiftMessages);


        log.info("--------------> FINISHED LOADING SWIFT MESSAGES STEP <--------------");
        return RepeatStatus.FINISHED;
    }
}

