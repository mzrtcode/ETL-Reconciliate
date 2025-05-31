package co.com.itau.swift.dao.impl;

import co.com.itau.swift.dao.AsMonitoringPaymentsDAO;
import co.com.itau.swift.dto.AsMonitoringPaymentDTO;
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
public class AsMonitoringPaymentsDAOImpl implements AsMonitoringPaymentsDAO {

    @Qualifier(Constants.BEAN_JDBC_TEMPLATE_SWIFT)
    private final JdbcTemplate swiftRepository;

    Logger log = LoggerFactory.getLogger(AsMonitoringPaymentsDAOImpl.class);

    public AsMonitoringPaymentsDAOImpl(JdbcTemplate jdbcTemplate) {
        this.swiftRepository = jdbcTemplate;
    }


    @Override
    public List<AsMonitoringPaymentDTO> findAllPaymentsByMmgSequence(String mmgSequence) {
        return swiftRepository.query(QUERY_MONITORING_PAYMENTS, (rs, rowNum) -> mapToAsMonitoringPaymentDTO(rs), mmgSequence);
    }

    public static final String QUERY_MONITORING_PAYMENTS = """
    SELECT 
        mpy_instrid       AS %s,
        mpy_amount        AS %s,
        mpy_payeraccount  AS %s,
        mpy_benefaccount  AS %s,
        mpy_payerid       AS %s
    FROM PAYMENTS
    WHERE 
        MMG_SEQUENCE = ?
        AND MPY_STATUS = 'LOADED'
    """.formatted(
            Constants.COLUMN_REFERENCE,
            Constants.COLUMN_AMOUNT,
            Constants.COLUMN_SOURCE_ACCOUNT,
            Constants.COLUMN_DEST_ACCOUNT ,
            Constants.COLUMN_CUSTOMER
    );


    private AsMonitoringPaymentDTO mapToAsMonitoringPaymentDTO(ResultSet rs) throws SQLException {
        AsMonitoringPaymentDTO asMonitoringPaymentDTO = new AsMonitoringPaymentDTO();

        try{
            asMonitoringPaymentDTO.setReference(rs.getString(Constants.COLUMN_REFERENCE));
            asMonitoringPaymentDTO.setAmount(rs.getBigDecimal(Constants.COLUMN_AMOUNT));
            asMonitoringPaymentDTO.setPayerAccount(rs.getString(Constants.COLUMN_SOURCE_ACCOUNT));
            asMonitoringPaymentDTO.setBeneficiaryAccount(rs.getString(Constants.COLUMN_DEST_ACCOUNT));
            asMonitoringPaymentDTO.setClient(rs.getString(Constants.COLUMN_CUSTOMER));
        }catch(Exception e){
            log.error("Error mapping ResultSet to DTO due to null values");
        }

        return asMonitoringPaymentDTO;
    }
}
