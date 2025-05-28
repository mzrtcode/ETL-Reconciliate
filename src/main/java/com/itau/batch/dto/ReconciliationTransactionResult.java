package com.itau.batch.dto;

import lombok.*;

import java.math.BigDecimal;

//todo quitar geter y seter
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReconciliationTransactionResult {


    private String swiftId;
    private String swiftReference;
    private BigDecimal swiftAmount;
    private String swiftSourceAccount;
    private String swiftDestinationAccount;

    private String jpatReference;
    private BigDecimal jpatAmount;
    private String jpatSourceAccount;
    private String jpatDestinationAccount;

    private String status;
}
