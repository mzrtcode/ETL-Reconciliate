package com.itau.batch.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

//todo quitar geter y seter
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReconciliationBatchResult {

    private String swiftId;
    private String customerNit;
    private String fileName;
    private LocalDate loadingTime;
    private LocalDate applicationDate;
    private BigDecimal amountSwift;
    private BigDecimal amountJpat;
    private String status;
}
