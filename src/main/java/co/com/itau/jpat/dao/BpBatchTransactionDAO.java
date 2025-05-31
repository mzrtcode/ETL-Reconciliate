package co.com.itau.jpat.dao;

import co.com.itau.jpat.dto.BpBatchTransactionDTO;

import java.util.List;

public interface BpBatchTransactionDAO {

    List<BpBatchTransactionDTO> findTransactionByBatchUUID(String batchUUID);
}
