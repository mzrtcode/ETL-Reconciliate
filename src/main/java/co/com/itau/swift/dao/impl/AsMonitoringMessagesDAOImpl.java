package co.com.itau.swift.dao.impl;

import co.com.itau.swift.dao.AsMonitoringMessagesDAO;
import co.com.itau.swift.dto.AsMonitoringMessageDTO;
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

import static co.com.itau.utils.Constants.COLUMN_CUSTOMER_ID;

@Repository
public class AsMonitoringMessagesDAOImpl implements AsMonitoringMessagesDAO {

    @Qualifier(Constants.BEAN_JDBC_TEMPLATE_SWIFT)
    private final JdbcTemplate swiftRepository;

    Logger log = LoggerFactory.getLogger(AsMonitoringMessagesDAOImpl.class);

    public AsMonitoringMessagesDAOImpl(JdbcTemplate jdbcTemplate) {
        this.swiftRepository = jdbcTemplate;
    }

    public static final String QUERY_MONITORING_MESSAGES = """
    SELECT 
        m.mmg_sequence                 AS %s,
        p.mpy_payerid                  AS %s,
        m.mmg_loadingtime              AS %s,
        p.mpy_paymentdate              AS %s,
        SUM(p.mpy_amount)              AS %s
    FROM MENSAJES m
    INNER JOIN PAYMENTS p ON m.mmg_sequence = p.mmg_sequence
    WHERE 
        m.MMG_LOADINGTIME >= ? 
        AND m.MMG_MSGTYPE = 'MT101' 
        AND m.MMG_STATUS = 'LOADED'
    GROUP BY 
        m.mmg_sequence, 
        p.mpy_payerid, 
        m.mmg_loadingtime, 
        p.mpy_paymentdate
    """.formatted(
            Constants.COLUMN_MESSAGE_ID,
            Constants.COLUMN_CUSTOMER_ID,
            Constants.COLUMN_FECHA_CARGUE,
            Constants.COLUMN_FECHA_APLICACION,
            Constants.COLUMN_AMOUNT
    );



    @Override
    public List<AsMonitoringMessageDTO> findAllLoadedMessagesSince(LocalDateTime fromDate) {

        return swiftRepository.query(QUERY_MONITORING_MESSAGES, (rs, rowNum) -> mapToAsMonitoringMessageDTO(rs), fromDate);

    }

    private AsMonitoringMessageDTO mapToAsMonitoringMessageDTO(ResultSet rs) throws SQLException {
        AsMonitoringMessageDTO monitoringMessageDTO = new AsMonitoringMessageDTO();
     try{
         monitoringMessageDTO.setMessageId(rs.getString(Constants.COLUMN_MESSAGE_ID));
         monitoringMessageDTO.setCustomerId(rs.getString(COLUMN_CUSTOMER_ID));
         monitoringMessageDTO.setAmount(rs.getBigDecimal(Constants.COLUMN_AMOUNT));
         monitoringMessageDTO.setFechaAplicacion(rs.getDate(Constants.COLUMN_FECHA_APLICACION).toLocalDate());
         monitoringMessageDTO.setFechaCargue(rs.getDate(Constants.COLUMN_FECHA_CARGUE).toLocalDate());
     }catch (Exception e){
         log.error("Error mapping ResultSet to DTO due to null values");
     }

        return monitoringMessageDTO;
    }
}
