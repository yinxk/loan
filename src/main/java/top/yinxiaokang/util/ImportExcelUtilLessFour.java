package top.yinxiaokang.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/10/29 21:00
 */
public class ImportExcelUtilLessFour {
    private static File init(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resource = classLoader.getResource(fileName);
        String fileNa = null;
        try {
            fileNa = java.net.URLDecoder.decode(resource.getPath(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File file = new File(fileNa);
        return file;
    }

    private static Object getCellContent(Cell cell) {
        if (cell == null
                || (cell.getCellTypeEnum() == CellType.STRING && StringUtils.isBlank(cell
                .getStringCellValue()))) {
            return null;
        }
        Object object = null;

        switch (cell.getCellTypeEnum()) {
            case STRING:
                object = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    object = cell.getDateCellValue();
                } else {
                    object = cell.getNumericCellValue();
                }
                break;
            case BOOLEAN:
                object = cell.getBooleanCellValue();
                break;
            case FORMULA:
                try {
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        object = cell.getDateCellValue();
                    } else {
                        object = cell.getNumericCellValue();
                    }

                } catch (IllegalStateException e) {
                    object = cell.getRichStringCellValue();
                }
                break;
            case BLANK:
                return null;
        }
        return object;
    }

    public static List<Map<String, Object>> read(String filename, Integer sheetAtIndex, boolean isClassPath) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<Integer, String> keyMap = new LinkedHashMap<>();
        try (Workbook wb = WorkbookFactory.create(isClassPath ? init(filename) : new File(filename))) {
            sheetAtIndex = Optional.ofNullable(sheetAtIndex).orElse(0);
            Sheet sheetAt = wb.getSheetAt(sheetAtIndex);
            Iterator<Row> rowIterator = sheetAt.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                if (row.getRowNum() == 0) {
                    int nullKeyNum = 0;
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
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
                        Integer cellColumnIndex = cell.getColumnIndex();
                        Object cellContent = getCellContent(cell);
                        if (cellContent != null) isAllCellNull = false;
                        rowMap.put(keyMap.get(cellColumnIndex), cellContent);
                    }
                    if (!isAllCellNull) {
                        list.add(rowMap);
                    } else {
                        rowMap = null;
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
