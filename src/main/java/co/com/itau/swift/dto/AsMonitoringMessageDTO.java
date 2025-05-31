package co.com.itau.swift.dto;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


//TODO QUITAR TOSTRING
@ToString
public class AsMonitoringMessageDTO implements Serializable {


    private String messageId;
    private String customerId;
    private LocalDate fechaCargue;
    private LocalDate fechaAplicacion;
    private BigDecimal amount;
    private List<AsMonitoringPaymentDTO> payments;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDate getFechaCargue() {
        return fechaCargue;
    }

    public void setFechaCargue(LocalDate fechaCargue) {
        this.fechaCargue = fechaCargue;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<AsMonitoringPaymentDTO> getPayments() {
        return payments;
    }

    public void setPayments(List<AsMonitoringPaymentDTO> payments) {
        this.payments = payments;
    }
}
