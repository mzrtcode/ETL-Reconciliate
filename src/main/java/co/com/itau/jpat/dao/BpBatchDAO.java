package co.com.itau.jpat.dao;

import co.com.itau.jpat.dto.BpBatchDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BpBatchDAO {

    Optional<List<BpBatchDTO>> findAllBatchesByCustomerAndCreationDateAfterAndReference(String customer, LocalDateTime dateTime, String reference);

}
