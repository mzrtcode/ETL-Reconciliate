package co.com.itau.jpat.dao.impl;

import co.com.itau.jpat.dao.BpBatchTransactionDAO;
import co.com.itau.jpat.dto.BpBatchTransactionDTO;
import co.com.itau.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class BpBatchTransactionDAOImpl implements BpBatchTransactionDAO {

    @Qualifier(Constants.BEAN_JDBC_TEMPLATE_JPAT)
    private final JdbcTemplate jdbcTemplate;

    Logger log = LoggerFactory.getLogger(BpBatchTransactionDAOImpl.class);

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

        try{
            bpBatchTransactionDTO.setUuid(rs.getString(Constants.COLUMN_UUID));
            bpBatchTransactionDTO.setBtrAmount(rs.getBigDecimal(Constants.COLUMN_BTRAMOUNT));
            bpBatchTransactionDTO.setBank(rs.getString(Constants.COLUMN_BANK));
            bpBatchTransactionDTO.setBtrReference(rs.getString(Constants.COLUMN_BTRREFERENCE));
            bpBatchTransactionDTO.setBtrDestAccount(rs.getString(Constants.COLUMN_BTRDESTACCOUNT));
            bpBatchTransactionDTO.setBtrSourceAccount(rs.getString(Constants.COLUMN_BTRSOURCEACCOUNT));
            bpBatchTransactionDTO.setBtrBankOrigen(rs.getString(Constants.COLUMN_BTRBANKORIGEN));

        }catch (Exception e){
            log.error("Error mapping ResultSet to DTO due to null values");
        }

        return bpBatchTransactionDTO;
    }
}
