package co.com.itau.dto;

import java.io.Serializable;
import java.math.BigDecimal;


public class ReconciliationTransactionResult implements Serializable {


    private String swiftId = "";
    private String swiftReference = "";
    private BigDecimal swiftAmount = BigDecimal.ZERO;
    private String swiftSourceAccount = "";
    private String swiftDestinationAccount = "";

    private String jpatReference = "";
    private BigDecimal jpatAmount = BigDecimal.ZERO;
    private String jpatSourceAccount = "";
    private String jpatDestinationAccount = "";

    private String status = "";

    public String getSwiftId() {
        return swiftId;
    }

    public void setSwiftId(String swiftId) {
        this.swiftId = swiftId;
    }

    public String getSwiftReference() {
        return swiftReference;
    }

    public void setSwiftReference(String swiftReference) {
        this.swiftReference = swiftReference;
    }

    public BigDecimal getSwiftAmount() {
        return swiftAmount;
    }

    public void setSwiftAmount(BigDecimal swiftAmount) {
        this.swiftAmount = swiftAmount;
    }

    public String getSwiftSourceAccount() {
        return swiftSourceAccount;
    }

    public void setSwiftSourceAccount(String swiftSourceAccount) {
        this.swiftSourceAccount = swiftSourceAccount;
    }

    public String getSwiftDestinationAccount() {
        return swiftDestinationAccount;
    }

    public void setSwiftDestinationAccount(String swiftDestinationAccount) {
        this.swiftDestinationAccount = swiftDestinationAccount;
    }

    public String getJpatReference() {
        return jpatReference;
    }

    public void setJpatReference(String jpatReference) {
        this.jpatReference = jpatReference;
    }

    public BigDecimal getJpatAmount() {
        return jpatAmount;
    }

    public void setJpatAmount(BigDecimal jpatAmount) {
        this.jpatAmount = jpatAmount;
    }

    public String getJpatSourceAccount() {
        return jpatSourceAccount;
    }

    public void setJpatSourceAccount(String jpatSourceAccount) {
        this.jpatSourceAccount = jpatSourceAccount;
    }

    public String getJpatDestinationAccount() {
        return jpatDestinationAccount;
    }

    public void setJpatDestinationAccount(String jpatDestinationAccount) {
        this.jpatDestinationAccount = jpatDestinationAccount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
