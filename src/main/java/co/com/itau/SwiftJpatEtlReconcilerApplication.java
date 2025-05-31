package co.com.itau;

import co.com.itau.jpat.dao.BpBatchDAO;
import co.com.itau.jpat.dao.BpBatchTransactionDAO;
import co.com.itau.swift.dao.AsMonitoringMessagesDAO;
import co.com.itau.swift.dao.AsMonitoringPaymentsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class SwiftJpatEtlReconcilerApplication {

	@Autowired
	private BpBatchDAO batchDAO;

	@Autowired
	private BpBatchTransactionDAO batchTransactionDAO;

	@Autowired
	private AsMonitoringMessagesDAO asMonitoringMessagesDAO;

	@Autowired
	private AsMonitoringPaymentsDAO asMonitoringPaymentsDAO;

	public static void main(String[] args) {
		SpringApplication.run(SwiftJpatEtlReconcilerApplication.class, args);

	}

	@Bean
	public CommandLineRunner execute() {
		return args -> {
			LocalDateTime fechaConHoraCero = LocalDateTime.of(2020, 1, 23, 0, 0);

		//	List<BpBatchDTO> dtoList = batchDAO.findAllBatchesByCustomerAndCreationDateAfterAndReference("9011543914", fechaConHoraCero, "CO012520000148");
			//System.out.println(dtoList);

			System.out.println("Transacciones");
			//batchTransactionDAO.findTransactionByBatchUUID("7496178547940842553921").forEach(System.out::println);

			System.out.println("Mensajes");
			//asMonitoringMessagesDAO.findAllLoadedMessagesSince(LocalDateTime.of(2020, 1, 23, 0, 0)).forEach(System.out::println);

			System.out.println("Payments");
			//asMonitoringPaymentsDAO.findAllPaymentsByMmgSequence("20250422101806045056").forEach(System.out::println);


		};
	}

}
