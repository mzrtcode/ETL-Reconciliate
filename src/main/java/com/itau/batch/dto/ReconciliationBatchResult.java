package com.itau.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

//todo quitar geter y seter
@Getter
@Setter
@AllArgsConstructor
public class ReconciliationBatchResult {

    private String swiftId;
    private String customerNit;
    private String fileName;
    private LocalDate loadingTime;
    private LocalDate applicationDateTime;
    private BigDecimal amountSwift;
    private BigDecimal amountJpat;
    private String status;
}
