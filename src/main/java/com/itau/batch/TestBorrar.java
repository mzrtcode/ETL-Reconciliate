package com.itau.batch;

import com.itau.batch.dto.ReconciliationBatchResult;
import com.itau.batch.dto.ReconciliationTransactionResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class TestBorrar {

    private static final String SHEET_LOTES = "LOTES";
    private static final String SHEET_DETALLE = "DETALLE";
    private static final String FILE_NAME = "reconciliation_report.xlsx";
    private static final String RESOURCE_DIR = "src/main/resources";
    private static final int FILTER_ICON_MARGIN = 1000; // Margen adicional para el icono de filtro (en unidades de 1/256 de carácter)

    private static final String[] HEADERS_LOTES = {
            "ID Principal", "Cliente", "Nombre Archivo", "Fecha de Cargue",
            "Fecha de Aplicación", "Monto SWIFT", "Monto JPAT", "Estado"
    };

    private static final String[] HEADERS_DETALLE = {
            "ID Principal", "Referencia Swift", "Valor Swift", "Cuenta Origen Swift",
            "Cuenta Destino Swift", "Referencia JPAT", "Valor JPAT",
            "Cuenta Origen JPAT", "Cuenta Destino JPAT", "Estado"
    };

    public static void generarExcel(List<ReconciliationBatchResult> lotes, List<ReconciliationTransactionResult> transacciones) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheetLotes = workbook.createSheet(SHEET_LOTES);
            XSSFSheet sheetDetalle = workbook.createSheet(SHEET_DETALLE);

            // Crear estilos para las celdas de estado
            CellStyle styleOk = workbook.createCellStyle();
            styleOk.setFillForegroundColor(new XSSFColor(new Color(204, 255, 204), null)); // #CCFFCC
            styleOk.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle styleNotOk = workbook.createCellStyle();
            styleNotOk.setFillForegroundColor(new XSSFColor(new Color(255, 204, 204), null)); // #FFCCCC
            styleNotOk.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            crearEncabezado(sheetLotes, HEADERS_LOTES, headerStyle);
            crearEncabezado(sheetDetalle, HEADERS_DETALLE, headerStyle);

            llenarLotes(sheetLotes, lotes, styleOk, styleNotOk);
            llenarTransacciones(sheetDetalle, transacciones, styleOk, styleNotOk);

            // Ajustar ancho de columnas después de llenar datos
            ajustarAnchoColumnas(sheetLotes, HEADERS_LOTES.length);
            ajustarAnchoColumnas(sheetDetalle, HEADERS_DETALLE.length);

            Path dir = Paths.get(RESOURCE_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path filePath = dir.resolve(FILE_NAME);
            try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
                workbook.write(out);
                System.out.println("Archivo guardado en: " + filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
        }
    }

    private static void crearEncabezado(Sheet sheet, String[] encabezados, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < encabezados.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(encabezados[i]);
            cell.setCellStyle(headerStyle);
        }
        // Habilitar filtro en los encabezados
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, encabezados.length - 1));
    }

    private static void llenarLotes(Sheet sheet, List<ReconciliationBatchResult> lotes, CellStyle styleOk, CellStyle styleNotOk) {
        int rowNum = 1;
        for (ReconciliationBatchResult lote : lotes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(safe(lote.getSwiftId()));
            row.createCell(1).setCellValue(safe(lote.getCustomerNit()));
            row.createCell(2).setCellValue(safe(lote.getFileName()));
            row.createCell(3).setCellValue(safe(lote.getLoadingTime()));
            row.createCell(4).setCellValue(safe(lote.getApplicationDate()));
            row.createCell(5).setCellValue(safe(lote.getAmountSwift()));
            row.createCell(6).setCellValue(safe(lote.getAmountJpat()));
            Cell statusCell = row.createCell(7);
            statusCell.setCellValue(safe(lote.getStatus()));
            statusCell.setCellStyle("OK".equals(safe(lote.getStatus())) ? styleOk : styleNotOk);
        }
    }

    private static void llenarTransacciones(Sheet sheet, List<ReconciliationTransactionResult> transacciones, CellStyle styleOk, CellStyle styleNotOk) {
        int rowNum = 1;
        for (ReconciliationTransactionResult t : transacciones) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(safe(t.getSwiftId()));
            row.createCell(1).setCellValue(safe(t.getSwiftReference()));
            row.createCell(2).setCellValue(safe(t.getSwiftAmount()));
            row.createCell(3).setCellValue(safe(t.getSwiftSourceAccount()));
            row.createCell(4).setCellValue(safe(t.getSwiftDestinationAccount()));
            row.createCell(5).setCellValue(safe(t.getJpatReference()));
            row.createCell(6).setCellValue(safe(t.getJpatAmount()));
            row.createCell(7).setCellValue(safe(t.getJpatSourceAccount()));
            row.createCell(8).setCellValue(safe(t.getJpatDestinationAccount()));
            Cell statusCell = row.createCell(9);
            statusCell.setCellValue(safe(t.getStatus()));
            statusCell.setCellStyle("OK".equals(safe(t.getStatus())) ? styleOk : styleNotOk);
        }
    }

    private static void ajustarAnchoColumnas(Sheet sheet, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            sheet.autoSizeColumn(i); // Ajustar al contenido
            // Añadir margen adicional para el icono de filtro
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + FILTER_ICON_MARGIN);
        }
    }

    private static String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private static double safe(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}