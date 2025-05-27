package com.itau.jpat.dao;

import com.itau.jpat.dto.BpBatchTransactionDTO;

import java.util.List;

public interface BpBatchTransactionDAO {

    List<BpBatchTransactionDTO> findTransactionByBatchUUID(String batchUUID);
}
