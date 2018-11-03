package top.yinxiaokang.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import top.yinxiaokang.original.dto.CellStyleAndContent;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.original.interfaces.RowAndCellProcess;
import top.yinxiaokang.others.ErrorException;

import java.io.*;
import java.net.URL;
import java.util.*;

@SuppressWarnings("Duplicates")
@Slf4j
public class ExcelUtil {

    private static File init(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resource = classLoader.getResource(fileName);
        Objects.requireNonNull(resource);
        String fileNameTransform = null;
        try {
            fileNameTransform = java.net.URLDecoder.decode(resource.getPath(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert fileNameTransform != null;
        return new File(fileNameTransform);
    }


    /**
     * 复制excel, 更新其中一些值写出
     *
     * @param inFilename    输入文件名
     * @param updateSheetAt 需要更新的sheet序号
     * @param isClassPath   文件是否在类路径下
     * @param outFileName   输出文件名
     */
    public static void copyExcelAndUpdate(String inFilename, Integer updateSheetAt, boolean isClassPath,
                                          String outFileName, RowAndCellProcess rowAndCellProcess) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isBlank(outFileName)) {
            String[] split = inFilename.split("\\.");
            for (int i = 0; i < split.length - 1; i++) {
                sb.append(split[i]);
            }
            sb.append("-更新版");
            sb.append(".");
            sb.append(split[split.length - 1]);
            outFileName = sb.toString();
        }
        excelReaderAndWriter(inFilename, updateSheetAt, isClassPath, outFileName, rowAndCellProcess);
    }

    public static ExcelReadReturn readExcel(String inFilename, Integer sheetAt, Boolean isClassPath,Boolean isFilterAllNullRow) {
        ExcelReadReturn excelReadReturn = new ExcelReadReturn();
        List<Map<String, Object>> content = new ArrayList<>();
        excelReadReturn.setContent(content);
        readExcel(inFilename, sheetAt, isClassPath, isFilterAllNullRow,(row, colIndexMapContent, contentMapColIndex, proIndexMapColIndex) -> {
            excelReadReturn.setColIndexMapContent(colIndexMapContent);
            excelReadReturn.setContentMapColIndex(contentMapColIndex);
            excelReadReturn.setProIndexMapColIndex(proIndexMapColIndex);
            Map<String, Object> rowContent = new LinkedHashMap<>();
            for (Cell cell : row) {
                rowContent.put(colIndexMapContent.get(cell.getColumnIndex()), getCellContent(cell));
            }
            content.add(rowContent);
        });
        return excelReadReturn;
    }

    public static List<Map<String, CellStyleAndContent>> readExcelCellStyleAndContent(String inFilename, Integer sheetAt, Boolean isClassPath,Boolean isFilterAllNullRow) {
        List<Map<String, CellStyleAndContent>> result = new ArrayList<>();
        readExcel(inFilename, sheetAt, isClassPath,isFilterAllNullRow, (row, colIndexMapContent, contentMapColIndex, proIndexMapColIndex) -> {
            Map<String, CellStyleAndContent> rowContent = new LinkedHashMap<>();
            for (Cell cell : row) {
                CellStyleAndContent cellStyleAndContent = new CellStyleAndContent();
                rowContent.put(colIndexMapContent.get(cell.getColumnIndex()), cellStyleAndContent);
                cellStyleAndContent.setContent(getCellContent(cell));
                cellStyleAndContent.setCellStyle(cell.getCellStyle());
            }
            result.add(rowContent);
        });
        return result;
    }


    /**
     * 读取excel,并且根据原来的excel做写出
     *
     * @param inFilename  输入文件名
     * @param sheetAt     需要读写的sheet序号
     * @param isClassPath 文件是否在类路径下
     */
    private static void excelReaderAndWriter(String inFilename, Integer sheetAt, boolean isClassPath, String outFileName, RowAndCellProcess rowAndCellProcess) {
        Objects.requireNonNull(inFilename);
        try (Workbook wb = WorkbookFactory.create(isClassPath ? init(inFilename) : new File(inFilename))) {
            log.info("读取 {} 完成", inFilename);
            read(sheetAt, rowAndCellProcess, wb,false);
            File file = getOutFileExcelName(outFileName);
            writeToExcel(wb, file);
            log.info("写出 {} 完成", outFileName);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 读取excel
     *
     * @param inFilename  输入文件名
     * @param sheetAt     需要读取的sheet序号
     * @param isClassPath 文件是否在类路径下
     */
    private static void readExcel(String inFilename, Integer sheetAt, Boolean isClassPath,Boolean isFilterAllNullRow, RowAndCellProcess rowAndCellProcess) {
        Objects.requireNonNull(inFilename);
        if (sheetAt == null) sheetAt = 0;
        if (isClassPath == null) isClassPath = false;
        try (Workbook wb = WorkbookFactory.create(isClassPath ? init(inFilename) : new File(inFilename))) {
            log.info("读取 {} 完成", inFilename);
            read(sheetAt, rowAndCellProcess, wb,isFilterAllNullRow);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }


    private static void read(Integer sheetAt, RowAndCellProcess rowAndCellProcess, Workbook wb,Boolean isFilterAllNullRow) {
        sheetAt = Optional.ofNullable(sheetAt).orElse(0);
        Sheet sheet = wb.getSheetAt(sheetAt);
        boolean isFirst = true;
        if (isFilterAllNullRow == null) isFilterAllNullRow = true;
        Map<Integer, String> keyMap = new LinkedHashMap<>();
        Map<String, Integer> contentMapCol = new LinkedHashMap<>();
        Map<Integer, Integer> proMapColumn = new LinkedHashMap<>();
        boolean isAllNull = true;
        for (Row row : sheet) {
            if (isFirst) {
                isFirst = false;
                int propertyIndex = 0;
                int nullKeyNum = 0;
                for (Cell cell : row) {
                    cell.setCellType(CellType.STRING);
                    String cellContent = cell.getStringCellValue();
                    Integer cellColumnIndex = cell.getColumnIndex();
                    if (StringUtils.isBlank(cellContent)) {
                        contentMapCol.put("nullKey" + nullKeyNum, cellColumnIndex);
                        keyMap.put(cellColumnIndex, "nullKey" + nullKeyNum++);
                    } else {
                        if (null != contentMapCol.get(cellContent)) {
                            throw new ErrorException("首列名作为键值重复 : " + cellContent);
                        }
                        contentMapCol.put(cellContent, cellColumnIndex);
                        keyMap.put(cellColumnIndex, cellContent);
                    }
                    proMapColumn.put(propertyIndex++, cellColumnIndex);
                }
                continue;
            }
            if (isFilterAllNullRow) {
                for (Cell cell : row) {
                    if (getCellContent(cell) != null) {
                        isAllNull = false;
                    }
                }
                if (isAllNull) continue;
            }
            rowAndCellProcess.process(row, keyMap, contentMapCol, proMapColumn);
        }
    }


    public static String getStringCellContent(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    /**
     * 获取可能的content类型
     *
     * @param cell 单元格
     * @return
     */
    public static Object getCellContent(Cell cell) {
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

    private static void writeToExcel(Workbook wb, File outFile) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            wb.write(outputStream);
        }
    }

    private static File getOutFileExcelName(String outFileName) {
        if (StringUtils.isBlank(outFileName)) {
            throw new RuntimeException("输出文件为空");
        }
        File file = new File(outFileName);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            if (!mkdirs) {
                log.info("创建文件夹失败");
            }
        }
        String[] split = outFileName.split("\\.");
        String extension = split[split.length - 1];
        if (!extension.equalsIgnoreCase("xls") && !extension.equalsIgnoreCase("xlsx")) {
            throw new RuntimeException("只支持xls或xlsx文件类型");
        }
        return file;
    }
}
