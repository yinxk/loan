package top.yinxiaokang.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/10/29 21:00
 */
@Slf4j
public class SimpleExcelUtilLessFour {
    private static File init(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resource = classLoader.getResource(fileName);
        String fileNa = null;
        try {
            assert resource != null;
            fileNa = java.net.URLDecoder.decode(resource.getPath(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert fileNa != null;
        return new File(fileNa);
    }

    public static void writeToExcel(String fileName, Map<String, String> keyMap, List<Map<String, Object>> contents) {
        Workbook wb;
        if (fileName.endsWith(".xls")) {
            wb = new HSSFWorkbook();
        } else if (fileName.endsWith(".xlsx")) {
            wb = new XSSFWorkbook();
        } else {
            throw new RuntimeException("不支持的文件类型");
        }
        log.info("新创建了workbook : {}", wb);
        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            Sheet sheet = wb.createSheet();
            Row title = sheet.createRow(0);
            sheet.createFreezePane(0, 1, 0, 1);
            int k = 0;
            for (Map.Entry<String, String> titleEntry : keyMap.entrySet()) {
                Cell cell = title.createCell(k++);
                cell.setCellValue(titleEntry.getValue());
            }
            int i = 1;
            for (Map<String, Object> content : contents) {
                int j = 0;
                Row row = sheet.createRow(i++);
                for (Map.Entry<String, String> keyEntry : keyMap.entrySet()) {
                    Cell cell = row.createCell(j++);
                    if (content.containsKey(keyEntry.getKey())) {
                        sheet.autoSizeColumn(j - 1);
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(content.get(keyEntry.getKey()).toString());
                    }
                }
            }
            wb.write(outputStream);
            log.info("写出 {} 完成", fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> read(String filename, Integer sheetAtIndex, boolean isClassPath) {
        return read(filename, sheetAtIndex, isClassPath, false);
    }

    public static List<Map<String, Object>> read(String fileName, Integer sheetAtIndex, boolean isClassPath, boolean isFilterAllNullRow) {
        List<Map<String, Object>> read;
        try (InputStream inputStream = new FileInputStream(isClassPath ? init(fileName) : new File(fileName))) {
            read = read(inputStream, sheetAtIndex, isFilterAllNullRow);
            log.info("读取 {} 完成", fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return read;
    }

    private static List<Map<String, Object>> read(InputStream inputStream, Integer sheetAtIndex, boolean isFilterAllNullRow) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<Integer, String> keyMap = new LinkedHashMap<>();
        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            sheetAtIndex = Optional.ofNullable(sheetAtIndex).orElse(0);
            Sheet sheetAt = wb.getSheetAt(sheetAtIndex);
            Iterator<Row> rowIterator = sheetAt.rowIterator();
            boolean isFirst = true;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                if (isFirst) {
                    isFirst = false;
                    int nullKeyNum = 0;
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        cell.setCellType(CellType.STRING);
                        String cellContent = cell.getStringCellValue();
                        Integer cellColumnIndex = cell.getColumnIndex();
                        if (StringUtils.isBlank(cellContent)) {
                            keyMap.put(cellColumnIndex, "nullKey" + nullKeyNum++);
                        } else {
                            keyMap.put(cellColumnIndex, cellContent);
                        }
                    }
                } else {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    boolean isAllCellNull = true;
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        cell.setCellType(CellType.STRING);
                        Integer cellColumnIndex = cell.getColumnIndex();
                        String cellContent = cell.getStringCellValue();
                        if (StringUtils.isNotBlank(cellContent)) isAllCellNull = false;
                        rowMap.put(keyMap.get(cellColumnIndex), cellContent);
                    }
                    if (isFilterAllNullRow) {
                        if (!isAllCellNull) {
                            list.add(rowMap);
                        } else {
                            rowMap = null;
                        }
                    } else {
                        list.add(rowMap);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return list;
    }

}
