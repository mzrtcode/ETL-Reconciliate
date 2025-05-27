package com.itau.swift.dao;

import com.itau.swift.dto.AsMonitoringMessageDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AsMonitoringMessagesDAO {

    List<AsMonitoringMessageDTO> findAllLoadedMessagesSince(LocalDateTime fromDate);

}
