package com.itau.jpat.dao;

import com.itau.jpat.dto.BpBatchDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface BpBatchDAO {

    List<BpBatchDTO> findAllBatchesByCustomerAndCreationDateAfterAndReference(String customer, LocalDateTime dateTime, String reference);

}
