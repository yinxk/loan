package top.yinxiaokang.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/10/29 21:00
 */
@Slf4j
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

    public static List<Map<String, Object>> read(String filename, Integer sheetAtIndex, boolean isClassPath) {
        return read(filename, sheetAtIndex, isClassPath, false);
    }

    public static List<Map<String, Object>> read(String filename, Integer sheetAtIndex, boolean isClassPath, boolean isFilterAllNullRow) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<Integer, String> keyMap = new LinkedHashMap<>();
        try (Workbook wb = WorkbookFactory.create(isClassPath ? init(filename) : new File(filename))) {
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
        log.info("读取 " + filename + "结束!");
        return list;
    }

}
