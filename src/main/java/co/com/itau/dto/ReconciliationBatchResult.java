package co.com.itau.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

public class ReconciliationBatchResult {

    private String swiftId;
    private String customerNit;
    private String fileName;
    private LocalDate loadingTime;
    private LocalDate applicationDate;
    private BigDecimal amountSwift;
    private BigDecimal amountJpat;
    private String status;

    public String getSwiftId() {
        return swiftId;
    }

    public void setSwiftId(String swiftId) {
        this.swiftId = swiftId;
    }

    public String getCustomerNit() {
        return customerNit;
    }

    public void setCustomerNit(String customerNit) {
        this.customerNit = customerNit;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDate getLoadingTime() {
        return loadingTime;
    }

    public void setLoadingTime(LocalDate loadingTime) {
        this.loadingTime = loadingTime;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public BigDecimal getAmountSwift() {
        return amountSwift;
    }

    public void setAmountSwift(BigDecimal amountSwift) {
        this.amountSwift = amountSwift;
    }

    public BigDecimal getAmountJpat() {
        return amountJpat;
    }

    public void setAmountJpat(BigDecimal amountJpat) {
        this.amountJpat = amountJpat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
