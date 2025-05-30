package co.com.itau.controller;

import co.com.itau.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
public class ReconciliationController {

    private final Logger log = LoggerFactory.getLogger(ReconciliationController.class);

    private final JobLauncher jobLauncher;

    private final Job job;

    public ReconciliationController(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @GetMapping(path = Constants.EXECUTE_ENDPOINT)
    public ResponseEntity<String> executeReconciliation() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        reconcile();
        return ResponseEntity.ok(Constants.JOB_STARTED_MSG);
    }

    //@Scheduled(cron = Constants.CRON_EXPRESSION)
    public void reconcile() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        log.info("--------------> JOB STARTED <--------------");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(Constants.UUID, UUID.randomUUID().toString())
                .addString(Constants.DATE, LocalDateTime.now().toString())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
        log.info("--------------> JOB FINISHED <--------------");

    }

}
