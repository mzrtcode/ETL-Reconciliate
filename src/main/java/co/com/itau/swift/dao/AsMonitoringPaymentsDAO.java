package co.com.itau.swift.dao;

import co.com.itau.swift.dto.AsMonitoringPaymentDTO;

import java.util.List;

public interface AsMonitoringPaymentsDAO {

    List<AsMonitoringPaymentDTO> findAllPaymentsByMmgSequence(String mmgSequence);
}
