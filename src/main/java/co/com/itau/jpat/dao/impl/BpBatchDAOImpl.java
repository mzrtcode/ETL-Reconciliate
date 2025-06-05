package co.com.itau.jpat.dao.impl;

import co.com.itau.jpat.dao.BpBatchDAO;
import co.com.itau.jpat.dto.BpBatchDTO;
import co.com.itau.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public class BpBatchDAOImpl implements BpBatchDAO {

    private static final int BATSTATUS_NO_REPAIRABLE_ERROR = 4;
    private static final int BATSTATUS_REPAIRABLE_ERROR = 8;
    private static final int BATSTATUS_DELETED = 1024;
    private static final int BATSTATUS_EXPIRED = 2048;

    private static final int BATLOADTYPE_SWIFT = 3;

    @Qualifier(Constants.BEAN_JDBC_TEMPLATE_JPAT)
    private final JdbcTemplate jdbcTemplate;

    Logger log = LoggerFactory.getLogger(BpBatchDAOImpl.class);

    private static final String QUERY_FIND_BATCHES = """
            SELECT 
                b.UUID, 
                b.BATNAME,
                SUM(t_all.BTRAMOUNT) AS TOTALAMOUNT
            FROM BP_BATCHTRANSACTION t_ref
            INNER JOIN BP_BATCH b ON t_ref.BATCH = b.UUID
            INNER JOIN BP_BATCHTRANSACTION t_all ON t_all.BATCH = b.UUID
            WHERE b.CUSTOMER = ?
              AND b.BATCREATIONDATE >= ?
              AND t_ref.BTRREFERENCE = ?
              AND BATLOADTYPE = ?
              AND BATSTATUS NOT IN (?, ?, ?, ?)
            GROUP BY b.UUID, b.BATNAME
    """;

    public BpBatchDAOImpl(@Qualifier(Constants.BEAN_JDBC_TEMPLATE_JPAT) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<List<BpBatchDTO>> findAllBatchesByCustomerAndCreationDateAfterAndReference(String customer, LocalDateTime creationDate, String reference) {

        List<BpBatchDTO> results = jdbcTemplate.query(
                QUERY_FIND_BATCHES,
                (rs, rowNum) -> mapToBpBatchDTO(rs),
                customer,
                creationDate,
                reference,
                BATLOADTYPE_SWIFT,
                BATSTATUS_NO_REPAIRABLE_ERROR,
                BATSTATUS_REPAIRABLE_ERROR,
                BATSTATUS_DELETED,
                BATSTATUS_EXPIRED
        );

        return results.isEmpty() ? Optional.empty() : Optional.of(results);
    }

    public BpBatchDTO mapToBpBatchDTO(ResultSet rs) throws SQLException {
        BpBatchDTO bpBatchDTO = new BpBatchDTO();


        try{
            bpBatchDTO.setUuid(rs.getString(Constants.COLUMN_UUID));
            bpBatchDTO.setTotalAmount(rs.getBigDecimal(Constants.COLUMN_TOTALAMOUNT));
            bpBatchDTO.setBatName(rs.getString(Constants.COLUMN_BATNAME));
        }catch (Exception e){
            log.error("Error mapping ResultSet to DTO due to null values");

        }

        return bpBatchDTO;
    }
}
