package co.com.itau.service;


import co.com.itau.dto.ReconciliationBatchResult;
import co.com.itau.dto.ReconciliationTransactionResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ExcelReportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelReportService.class);

    private static final String SHEET_LOTES = "LOTES";
    private static final String SHEET_DETALLE = "DETALLE";


    private static final String[] HEADERS_LOTES = {
            "ID Principal", "Cliente", "Nombre Archivo", "Fecha de Cargue",
            "Fecha de Aplicación", "Monto SWIFT", "Monto JPAT", "Estado"
    };

    private static final String[] HEADERS_DETALLE = {
            "ID Principal", "Referencia Swift", "Valor Swift", "Cuenta Origen Swift",
            "Cuenta Destino Swift", "Referencia JPAT", "Valor JPAT",
            "Cuenta Origen JPAT", "Cuenta Destino JPAT", "Estado"
    };


    private static final Color STATUS_OK_COLOR = new Color(204, 255, 204);    // Verde
    private static final Color STATUS_NOT_OK_COLOR = new Color(255, 204, 204); // Rojo

    private static final String STATUS_OK = "OK";

    private static final String DEFAULT_STRING = "";
    private static final double DEFAULT_DOUBLE = 0.0;
    private static final int FIRST_ROW = 0;
    private static final int START_ROW_DATA = 1;
    private static final int FILTER_ICON_MARGIN = 1000;

    public byte[] generarExcel(List<ReconciliationBatchResult> lotes, List<ReconciliationTransactionResult> transacciones) {


        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheetLotes = workbook.createSheet(SHEET_LOTES);
            XSSFSheet sheetDetalle = workbook.createSheet(SHEET_DETALLE);

            CellStyle styleOk = crearCeldaEstado(workbook, STATUS_OK_COLOR);
            CellStyle styleNotOk = crearCeldaEstado(workbook, STATUS_NOT_OK_COLOR);
            CellStyle headerStyle = createCeldaCabecera(workbook);

            crearEncabezado(sheetLotes, HEADERS_LOTES, headerStyle);
            crearEncabezado(sheetDetalle, HEADERS_DETALLE, headerStyle);

            llenarLotes(sheetLotes, lotes, styleOk, styleNotOk);
            llenarTransacciones(sheetDetalle, transacciones, styleOk, styleNotOk);

            ajustarAnchoColumnas(sheetLotes, HEADERS_LOTES.length);
            ajustarAnchoColumnas(sheetDetalle, HEADERS_DETALLE.length);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                log.info("Reporte Excel generado exitosamente");
                return out.toByteArray();
            }
        } catch (IOException e) {
            log.error("Error al generar el reporte Excel", e);
            return null;
        }
    }

    private CellStyle crearCeldaEstado(XSSFWorkbook workbook, Color color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(color, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createCeldaCabecera(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void crearEncabezado(Sheet sheet, String[] encabezados, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(FIRST_ROW);
        for (int i = 0; i < encabezados.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(encabezados[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.setAutoFilter(new CellRangeAddress(FIRST_ROW, FIRST_ROW, 0, encabezados.length - 1));
    }

    private void llenarLotes(Sheet sheet, List<ReconciliationBatchResult> lotes, CellStyle styleOk, CellStyle styleNotOk) {
        if (lotes == null) return;

        int rowNum = START_ROW_DATA;
        for (ReconciliationBatchResult lote : lotes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(valorSeguro(lote.getSwiftId()));
            row.createCell(1).setCellValue(valorSeguro(lote.getCustomerNit()));
            row.createCell(2).setCellValue(valorSeguro(lote.getFileName()));
            row.createCell(3).setCellValue(valorSeguro(lote.getLoadingTime()));
            row.createCell(4).setCellValue(valorSeguro(lote.getApplicationDate()));
            row.createCell(5).setCellValue(valorSeguro(lote.getAmountSwift()));
            row.createCell(6).setCellValue(valorSeguro(lote.getAmountJpat()));
            Cell statusCell = row.createCell(7);
            statusCell.setCellValue(valorSeguro(lote.getStatus()));
            statusCell.setCellStyle(STATUS_OK.equals(valorSeguro(lote.getStatus())) ? styleOk : styleNotOk);
        }
    }

    private void llenarTransacciones(Sheet sheet, List<ReconciliationTransactionResult> transacciones, CellStyle styleOk, CellStyle styleNotOk) {
        if (transacciones == null) return;

        int rowNum = START_ROW_DATA;
        for (ReconciliationTransactionResult t : transacciones) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(valorSeguro(t.getSwiftId()));
            row.createCell(1).setCellValue(valorSeguro(t.getSwiftReference()));
            row.createCell(2).setCellValue(valorSeguro(t.getSwiftAmount()));
            row.createCell(3).setCellValue(valorSeguro(t.getSwiftSourceAccount()));
            row.createCell(4).setCellValue(valorSeguro(t.getSwiftDestinationAccount()));
            row.createCell(5).setCellValue(valorSeguro(t.getJpatReference()));
            row.createCell(6).setCellValue(valorSeguro(t.getJpatAmount()));
            row.createCell(7).setCellValue(valorSeguro(t.getJpatSourceAccount()));
            row.createCell(8).setCellValue(valorSeguro(t.getJpatDestinationAccount()));
            Cell statusCell = row.createCell(9);
            statusCell.setCellValue(valorSeguro(t.getStatus()));
            statusCell.setCellStyle(STATUS_OK.equals(valorSeguro(t.getStatus())) ? styleOk : styleNotOk);
        }
    }

    private void ajustarAnchoColumnas(Sheet sheet, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + FILTER_ICON_MARGIN);
        }
    }

    private String valorSeguro(Object value) {
        return value == null ? DEFAULT_STRING : value.toString();
    }

    private double valorSeguro(BigDecimal value) {
        return value == null ? DEFAULT_DOUBLE : value.doubleValue();
    }
}

