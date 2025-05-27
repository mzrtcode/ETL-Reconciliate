package com.itau.jpat.dao.impl;

import com.itau.jpat.dao.BpBatchTransactionDAO;
import com.itau.jpat.dto.BpBatchTransactionDTO;
import com.itau.utils.Constants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class BpBatchTransactionDAOImpl implements BpBatchTransactionDAO {

    private final JdbcTemplate jdbcTemplate;

    public BpBatchTransactionDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private static final String QUERY_FIND_TRANSACTIONS = """
    SELECT UUID,
           BTRAMOUNT,
           BTRBANKORIGEN,
           BANK,
           BTRDESTACCOUNT,
           BTRSOURCEACCOUNT,
           BTRREFERENCE
    FROM BP_BATCHTRANSACTION
    WHERE BATCH = ?
    """;


    @Override
    public List<BpBatchTransactionDTO> findTransactionByBatchUUID(String batchUUID) {
        return jdbcTemplate.query(QUERY_FIND_TRANSACTIONS, (rs, rowNum) -> mapToBpBatchTransactionDTO(rs), batchUUID);
    }


    private BpBatchTransactionDTO mapToBpBatchTransactionDTO(ResultSet rs) throws SQLException {
        BpBatchTransactionDTO bpBatchTransactionDTO = new BpBatchTransactionDTO();

        bpBatchTransactionDTO.setUuid(rs.getString(Constants.COLUMN_UUID));
        bpBatchTransactionDTO.setBtrAmount(rs.getBigDecimal(Constants.COLUMN_BTRAMOUNT));
        bpBatchTransactionDTO.setBank(rs.getString(Constants.COLUMN_BANK));
        bpBatchTransactionDTO.setBtrReference(rs.getString(Constants.COLUMN_BTRREFERENCE));
        bpBatchTransactionDTO.setBtrDestAccount(rs.getString(Constants.COLUMN_BTRDESTACCOUNT));
        bpBatchTransactionDTO.setBtrSourceAccount(rs.getString(Constants.COLUMN_BTRSOURCEACCOUNT));
        bpBatchTransactionDTO.setBtrBankOrigen(rs.getString(Constants.COLUMN_BTRBANKORIGEN));

        return bpBatchTransactionDTO;
    }
}
