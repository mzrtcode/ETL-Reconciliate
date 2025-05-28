package com.itau.config;

import com.itau.batch.tasklet.LoadJpatBatchesTasklet;
import com.itau.batch.tasklet.LoadSwiftMessagesTasklet;
import com.itau.batch.tasklet.ReconcileMessagesTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final LoadSwiftMessagesTasklet loadSwiftMessagesTasklet;
    private final LoadJpatBatchesTasklet loadJpatBatchesTasklet;
    private final ReconcileMessagesTasklet reconcileMessagesTasklet;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Step loadSwiftMessagesStep(){
        return new StepBuilder("loadSwiftMessagesStep", jobRepository)
                .tasklet(loadSwiftMessagesTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step loadJpatBatchesStep(){
        return new StepBuilder("loadJpatBatchesStep", jobRepository)
                .tasklet(loadJpatBatchesTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step reconciliateMessagesStep(){
        return new StepBuilder("reconciliateMessagesStep", jobRepository)
                .tasklet(reconcileMessagesTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job reconciliateJob(){
        return new JobBuilder("ReconciliateSwiftJpat", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadSwiftMessagesStep())
                .next(loadJpatBatchesStep())
                .next(reconciliateMessagesStep())
                .build();
    }

}
