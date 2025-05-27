package com.itau.swift.dao;

import com.itau.swift.dto.AsMonitoringPaymentDTO;

import java.util.List;

public interface AsMonitoringPaymentsDAO {

    List<AsMonitoringPaymentDTO> findAllPaymentsByMmgSequence(String mmgSequence);
}
