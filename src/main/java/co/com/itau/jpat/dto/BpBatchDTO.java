package co.com.itau.jpat.dto;

import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

//TODO borrar to string
@ToString
public class BpBatchDTO implements Serializable {

    private String uuid;
    private BigDecimal totalAmount;
    private String batName;
    private List<BpBatchTransactionDTO> transactions;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBatName() {
        return batName;
    }

    public void setBatName(String batName) {
        this.batName = batName;
    }

    public List<BpBatchTransactionDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<BpBatchTransactionDTO> transactions) {
        this.transactions = transactions;
    }


}
