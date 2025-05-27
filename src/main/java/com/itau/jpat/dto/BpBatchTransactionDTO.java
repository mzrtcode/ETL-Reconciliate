package com.itau.jpat.dto;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

//TODO eliminar to string
@ToString
public class BpBatchTransactionDTO implements Serializable {

    private String uuid;
    private BigDecimal btrAmount;
    private String bank;
    private String btrReference;
    private String btrDestAccount;
    private String btrSourceAccount;
    private String btrBankOrigen;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getBtrAmount() {
        return btrAmount;
    }

    public void setBtrAmount(BigDecimal btrAmount) {
        this.btrAmount = btrAmount;
    }

    public String getBtrSourceAccount() {
        return btrSourceAccount;
    }

    public void setBtrSourceAccount(String btrSourceAccount) {
        this.btrSourceAccount = btrSourceAccount;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBtrReference() {
        return btrReference;
    }

    public void setBtrReference(String btrReference) {
        this.btrReference = btrReference;
    }

    public String getBtrDestAccount() {
        return btrDestAccount;
    }

    public void setBtrDestAccount(String btrDestAccount) {
        this.btrDestAccount = btrDestAccount;
    }

    public String getBtrBankOrigen() {
        return btrBankOrigen;
    }

    public void setBtrBankOrigen(String btrBankOrigen) {
        this.btrBankOrigen = btrBankOrigen;
    }
}
