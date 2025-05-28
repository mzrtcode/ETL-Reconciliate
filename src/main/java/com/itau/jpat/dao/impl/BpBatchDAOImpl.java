package com.itau.jpat.dao.impl;

import com.itau.jpat.dao.BpBatchDAO;
import com.itau.jpat.dto.BpBatchDTO;
import com.itau.jpat.dto.BpBatchTransactionDTO;
import com.itau.utils.Constants;
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


    private final JdbcTemplate jdbcTemplate;

    private static final String QUERY_FIND_BATCHES = """
                SELECT  b.UUID, SUM(t.BTRAMOUNT) AS TOTALAMOUNT, b.BATNAME
                FROM BP_BATCH b
                INNER JOIN BP_BATCHTRANSACTION t  ON b.UUID = t.BATCH
                WHERE b.CUSTOMER = ?
                AND b.BATCREATIONDATE >= ?
                AND t.BTRREFERENCE = ?
                GROUP BY b.UUID, b.BATNAME
            """;

    public BpBatchDAOImpl(@Qualifier(Constants.BEAN_JDBC_TEMPLATE_JPAT) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("JdbcTemplate inyectado con qualifier: " + jdbcTemplate);
        // o si usas logger:
        // log.info("JdbcTemplate inyectado con qualifier: {}", jdbcTemplate);
    }

    @Override
    public Optional<List<BpBatchDTO>> findAllBatchesByCustomerAndCreationDateAfterAndReference(String customer, LocalDateTime creationDate, String reference) {

        List<BpBatchDTO> results = jdbcTemplate.query(QUERY_FIND_BATCHES, (rs, rowNum) -> mapToBpBatchDTO(rs), customer, creationDate, reference);

        return results.isEmpty() ? Optional.empty() : Optional.of(results);
    }

    public BpBatchDTO mapToBpBatchDTO(ResultSet rs) throws SQLException {
        BpBatchDTO bpBatchDTO = new BpBatchDTO();

        //TODO: manejar los posbiles null
        //.uuid(Optional.ofNullable(rs.getString(BpBatchColumns.UUID)).orElse(""))
        bpBatchDTO.setUuid(rs.getString(Constants.COLUMN_UUID));
        bpBatchDTO.setTotalAmount(rs.getBigDecimal(Constants.COLUMN_TOTALAMOUNT));
        bpBatchDTO.setBatName(rs.getString(Constants.COLUMN_BATNAME));

        return bpBatchDTO;
    }
}
