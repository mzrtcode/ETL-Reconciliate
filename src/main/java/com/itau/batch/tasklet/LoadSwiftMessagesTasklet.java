package com.itau.batch.tasklet;

import com.itau.swift.dao.AsMonitoringMessagesDAO;
import com.itau.swift.dao.AsMonitoringPaymentsDAO;
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

        LocalDateTime currentTime = LocalDateTime.of(2025, 1, 23, 0, 0);

        List<AsMonitoringMessageDTO> swiftMessages = asMonitoringMessagesDAO.findAllLoadedMessagesSince(currentTime);

        swiftMessages.forEach(m -> {
            List<AsMonitoringPaymentDTO> payments = asMonitoringPaymentsDAO.findAllPaymentsByMmgSequence(m.getMessageId());
            m.setPayments(payments);
        });

        ChunkContextUtil.setChunkContext(chunkContext , Constants.CONTEXT_KEY_MESSAGES, swiftMessages);

        log.info("Swift Messages Loaded");
        log.info("Swift Payments Loaded");
        log.info("TOTAL Messages: " + swiftMessages.size());

        swiftMessages.forEach(System.out::println);
        return RepeatStatus.FINISHED;
    }
}

