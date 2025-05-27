package com.itau.swift.dao.impl;

import com.itau.swift.dao.AsMonitoringMessagesDAO;
import com.itau.swift.dto.AsMonitoringMessageDTO;
import com.itau.utils.Constants;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.itau.utils.Constants.COLUMN_CUSTOMER_ID;

@Repository
public class AsMonitoringMessagesDAOImpl implements AsMonitoringMessagesDAO {

    private final JdbcTemplate jdbcTemplate;

    public AsMonitoringMessagesDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        return jdbcTemplate.query(QUERY_MONITORING_MESSAGES, (rs, rowNum) -> mapToAsMonitoringMessageDTO(rs), fromDate);

    }

    private AsMonitoringMessageDTO mapToAsMonitoringMessageDTO(ResultSet rs) throws SQLException {
        AsMonitoringMessageDTO monitoringMessageDTO = new AsMonitoringMessageDTO();
        monitoringMessageDTO.setMessageId(rs.getString(Constants.COLUMN_MESSAGE_ID));
        monitoringMessageDTO.setCustomerId(rs.getString(COLUMN_CUSTOMER_ID));
        monitoringMessageDTO.setAmount(rs.getBigDecimal(Constants.COLUMN_AMOUNT));
        monitoringMessageDTO.setFechaAplicacion(rs.getDate(Constants.COLUMN_FECHA_APLICACION).toLocalDate());
        monitoringMessageDTO.setFechaCargue(rs.getDate(Constants.COLUMN_FECHA_CARGUE).toLocalDate());

        return monitoringMessageDTO;
    }
}
