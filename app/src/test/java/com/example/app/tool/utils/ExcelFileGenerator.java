package com.example.app.tool.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelFileGenerator {

    public static byte[] generateExcelFile(String sheetName) {
        try (Workbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Create a new sheet
            Sheet sheet = workbook.createSheet("Sample Sheet");

            // Create a row
            Row row = sheet.createRow(0);

            // Create cells and add values
            Cell cell1 = row.createCell(0);
            cell1.setCellValue("Name");

            Cell cell2 = row.createCell(1);
            cell2.setCellValue("Age");

            // Create another row
            Row row2 = sheet.createRow(1);

            Cell cell3 = row2.createCell(0);
            cell3.setCellValue("John");

            Cell cell4 = row2.createCell(1);
            cell4.setCellValue(30);

            // Write the workbook content to the ByteArrayOutputStream
            workbook.write(outputStream);
            // Convert ByteArrayOutputStream to byte array
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File generateExcelFile() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Hello");

            File tempFile = File.createTempFile("testExcelFile", ".xls");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
            return tempFile;
        }
    }
}
