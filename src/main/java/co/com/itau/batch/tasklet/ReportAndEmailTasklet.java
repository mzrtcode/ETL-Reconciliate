package co.com.itau.batch.tasklet;


import co.com.itau.dto.ReconciliationResultDTO;
import co.com.itau.service.EmailService;
import co.com.itau.service.ExcelReportService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ReportAndEmailTasklet implements Tasklet {


    private final EmailService emailService;
    private final ExcelReportService excelReportService;

    Logger log = LoggerFactory.getLogger(ReportAndEmailTasklet.class);

    private static final String EMAIL_BODY = "✅ Se ha generado correctamente el archivo de conciliación Swift-JPAT. Puedes consultarlo en el archivo adjunto.";
    private static final String ATTATCHMENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String FILE_NAME_BASE =  "Conciliacion Swift-JPAT";
    private static final String EMAIL_SUBJECT = "Archivo de conciliación Swift-JPAT generado";
    private static final String EMAIL_FROM = "stivenmedina164@gmail.com";
    private static final String EMAIL_TO = "stvnm33@gmail.com";


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

       log.info("--------------> STARTED REPORT AND EMAIL STEP <--------------");

       ReconciliationResultDTO reconciliationResult = ChunkContextUtil.getChunkContext(chunkContext, Constants.CONTEXT_KEY_RECONCILIATION_RESULT);

        byte[] bytes = excelReportService.generarExcel(reconciliationResult.getBatchResults(), reconciliationResult.getTransactionResults());
        emailService.sendEmail(EMAIL_FROM, EMAIL_TO,EMAIL_SUBJECT, EMAIL_BODY, bytes, generateFileName(), ATTATCHMENT_TYPE);


        log.info("--------------> FINISHED REPORT AND EMAIL STEP <--------------");

        return RepeatStatus.FINISHED;
    }

    private String generateFileName() {
        LocalDate nowDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s %s.xlsx", FILE_NAME_BASE, nowDate.format(formatter));
    }
}
