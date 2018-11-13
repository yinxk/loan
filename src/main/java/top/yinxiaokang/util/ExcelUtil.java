package top.yinxiaokang.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import top.yinxiaokang.original.dto.CellStyleAndContent;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.original.interfaces.BaseExcelReader;
import top.yinxiaokang.original.interfaces.BaseExcelWriter;
import top.yinxiaokang.original.interfaces.TitleCreatedExcelWriter;
import top.yinxiaokang.original.interfaces.TitleLoadedRowReader;
import top.yinxiaokang.others.ErrorException;

import java.io.*;
import java.net.URL;
import java.util.*;

@SuppressWarnings({"Duplicates", "WeakerAccess", "SpellCheckingInspection"})
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
                                          String outFileName, TitleLoadedRowReader titleLoadedRowReader) {
        if (StringUtils.isBlank(outFileName)) {
            outFileName = inFilename;
        }
        try {
            excelReaderAndWriter(inFilename, updateSheetAt, isClassPath, outFileName, titleLoadedRowReader);
        } catch (Exception e) {
            log.info("写出文件失败: {}", e);
        }
    }

    /**
     * 读取excel,并且根据原来的excel做写出
     *
     * @param inFilename  输入文件名
     * @param sheetAt     需要读写的sheet序号
     * @param isClassPath 文件是否在类路径下
     */
    private static void excelReaderAndWriter(String inFilename, Integer sheetAt, boolean isClassPath, String outFileName, TitleLoadedRowReader titleLoadedRowReader) {
        read(inFilename, isClassPath, (workbook) -> {
            loadFirstRow(sheetAt, titleLoadedRowReader, workbook, false);
            File file = getOutFileExcelName(outFileName);
            try {
                writeToExcel(workbook, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public static <T> void writeToExcelByAll(String outFileName, String sheetName, Map<String, String> keyMap,
                                             List<Map<String, T>> contents) {
        try {
            writeToExcelAllThrows(outFileName, sheetName, keyMap, contents);
        } catch (IOException e) {
            log.info("写出excel发生异常: ", e);
        }
    }

    public static <T> void writeToExcelAllThrows(String outFileName, String sheetName, Map<String, String> keyMap,
                                                 List<Map<String, T>> contents) throws IOException {
        writeToExcelByCreatedTitle(outFileName, sheetName, keyMap, contents, (sheet, keyMap1, contents1) -> {
            int i = 1;
            for (Map<String, T> content : contents1) {
                Row row = sheet.createRow(i++);
                int j = 0;
                for (Map.Entry<String, String> key : keyMap1.entrySet()) {
                    Cell cell = row.createCell(j++);
                    if (content.containsKey(key.getKey())) {
                        Object value = content.get(key.getKey());
                        setCellValue(cell, value);
                    }
                }
            }
            for (i = 0; i < keyMap1.size(); i++) {
                sheet.autoSizeColumn(i);
            }
        });
    }

    public static <T> void writeToExcelByCreatedTitle(String outFileName, String sheetName, Map<String, String> keyMap,
                                                      List<Map<String, T>> contents, TitleCreatedExcelWriter<T> titleCreatedExcelWriter) throws IOException {
        writeToExcel(outFileName, keyMap, contents, (workbook, keyMap1, contents1) -> {
            Sheet sheet;
            if (StringUtils.isBlank(sheetName)) {
                sheet = workbook.createSheet();
            } else {
                sheet = workbook.createSheet(sheetName);
            }
            sheet.createFreezePane(0, 1, 0, 1);
            Row row = sheet.createRow(0);
            int k = 0;
            for (Map.Entry<String, String> title : keyMap1.entrySet()) {
                Cell cell = row.createCell(k++);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(title.getValue());
            }
            titleCreatedExcelWriter.process(sheet, keyMap1, contents1);
        });

    }

    /**
     * 写出为excel
     *
     * @param outFileName     文件名(含路径)
     * @param keyMap          属性名-->显示第一行的列名
     * @param contents        内容
     * @param baseExcelWriter
     * @throws IOException
     */
    public static <T> void writeToExcel(String outFileName, Map<String, String> keyMap,
                                        List<Map<String, T>> contents, BaseExcelWriter<T> baseExcelWriter) throws IOException {
        File outFile = getOutFileExcelName(outFileName);
        Workbook wb = createWorkBookByExtension(outFileName);
        baseExcelWriter.process(wb, keyMap, contents);
        writeToExcel(wb, outFile);
    }

    public static void writeToExcel(Workbook wb, File outFile) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            wb.write(outputStream);
            log.info("写出 {} 完成", outFile.getPath());
        }
        wb.close();
    }


    public static List<Map<String, CellStyleAndContent>> readExcelCellStyleAndContent(String inFilename, Integer sheetAt, Boolean isClassPath, Boolean isFilterAllNullRow) {
        List<Map<String, CellStyleAndContent>> result = new ArrayList<>();
        readExcel(inFilename, sheetAt, isClassPath, isFilterAllNullRow, (wb, row, keyMap, keyMapReverse) -> {
            Map<String, CellStyleAndContent> rowContent = new LinkedHashMap<>();
            for (Cell cell : row) {
                CellStyleAndContent cellStyleAndContent = new CellStyleAndContent();
                rowContent.put(keyMap.get(cell.getColumnIndex()), cellStyleAndContent);
                cellStyleAndContent.setContent(getCellContent(cell));
                cellStyleAndContent.setCellStyle(cell.getCellStyle());
            }
            result.add(rowContent);
        });
        return result;
    }

    public static ExcelReadReturn readExcel(String inFilename, Integer sheetAt, Boolean isClassPath, Boolean isFilterAllNullRow) {
        ExcelReadReturn excelReadReturn = new ExcelReadReturn();
        List<Map<String, Object>> content = new ArrayList<>();
        excelReadReturn.setContent(content);
        readExcel(inFilename, sheetAt, isClassPath, isFilterAllNullRow, (wb, row, keyMap, keyMapReverse) -> {
            excelReadReturn.setColIndexMapContent(keyMap);
            excelReadReturn.setContentMapColIndex(keyMapReverse);
            Map<String, Object> rowContent = new LinkedHashMap<>();
            for (Cell cell : row) {
                rowContent.put(keyMap.get(cell.getColumnIndex()), getCellContent(cell));
            }
            content.add(rowContent);
        });
        return excelReadReturn;
    }

    public static List<Map<String, String>> readStringExcel(String inFilename, Integer sheetAt, boolean isClassPath, boolean isFilterAllNullRow) {
        List<Map<String, String>> content = new ArrayList<>();
        readExcel(inFilename, sheetAt, isClassPath, isFilterAllNullRow, (wb, row, keyMap, keyMapReverse) -> {
            Map<String, String> rowContent = new LinkedHashMap<>();
            for (Cell cell : row) {
                cell.setCellType(CellType.STRING);
                rowContent.put(keyMap.get(cell.getColumnIndex()), getStringCellContent(cell));
            }
            content.add(rowContent);
        });
        return content;
    }

    /**
     * 读取excel
     *
     * @param inFilename  输入文件名
     * @param sheetAt     需要读取的sheet序号
     * @param isClassPath 文件是否在类路径下
     */
    public static void readExcel(String inFilename, Integer sheetAt, Boolean isClassPath, Boolean isFilterAllNullRow, TitleLoadedRowReader titleLoadedRowReader) {
        read(inFilename, isClassPath, (workbook) -> {
            loadFirstRow(sheetAt, titleLoadedRowReader, workbook, isFilterAllNullRow);
        });
    }

    public static void loadFirstRow(Integer sheetAt, TitleLoadedRowReader titleLoadedRowReader, Workbook wb, Boolean isFilterAllNullRow) {
        sheetAt = Optional.ofNullable(sheetAt).orElse(0);
        Sheet sheet = wb.getSheetAt(sheetAt);
        boolean isFirst = true;
        if (isFilterAllNullRow == null) isFilterAllNullRow = true;
        Map<Integer, String> keyMap = new LinkedHashMap<>();
        Map<String, Integer> contentMapCol = new LinkedHashMap<>();
        boolean isAllNull = true;
        for (Row row : sheet) {
            if (isFirst) {
                isFirst = false;
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
            sheet.createFreezePane(0, 1, 0, 1);
            titleLoadedRowReader.process(wb, row, keyMap, contentMapCol);
        }
    }

    public static void read(String inFilename, Boolean isClassPath, BaseExcelReader baseExcelReader) {
        Objects.requireNonNull(inFilename);
        if (isClassPath == null) isClassPath = false;
        try (InputStream inputStream = new FileInputStream(isClassPath ? init(inFilename) : new File(inFilename))) {
            try (Workbook wb = WorkbookFactory.create(inputStream)) {
                log.info("读取 {} 完成", inFilename);
                baseExcelReader.process(wb);
            }
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 将单元格类型设置为String类型,并获取值(会破坏原来的属性)
     *
     * @param cell
     * @return
     */
    public static String getStringCellContent(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) return;
        if (value instanceof String) {
            String sValue = (String) value;
            cell.setCellValue(sValue);
        } else if (value instanceof Integer) {
            int intValue = (Integer) value;
            cell.setCellValue(intValue);
        } else if (value instanceof Float) {
            float fValue = (Float) value;
            cell.setCellValue(fValue);
        } else if (value instanceof Double) {
            double dValue = (Double) value;
            cell.setCellValue(dValue);
        } else if (value instanceof Long) {
            long longValue = (Long) value;
            cell.setCellValue(longValue);
        } else if (value instanceof Boolean) {
            boolean bValue = (Boolean) value;
            cell.setCellValue(bValue);
        } else if (value instanceof Date) {
            Date date = (Date) value;
            cell.setCellValue(date);
        } else if (value instanceof RichTextString) {
            RichTextString rtsValue = (RichTextString) value;
            cell.setCellValue(rtsValue);
        } else {
            try {
                RichTextString richTextString = new XSSFRichTextString(value.toString());
                cell.setCellValue(richTextString);
            } catch (Exception e) {
                RichTextString richTextString = new HSSFRichTextString(value.toString());
                cell.setCellValue(richTextString);
            }
        }
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

    private static Workbook createWorkBookByExtension(String outFileName) {
        Workbook wb;
        String extension = outFileName.substring(outFileName.lastIndexOf("."));
        if (".xls".equalsIgnoreCase(extension)) {
            wb = new HSSFWorkbook();
            log.info("新创建了 xls 文件类型对象: {}", wb);
        } else {
            wb = new XSSFWorkbook();
            log.info("新创建了 xlsx 文件类型对象: {}", wb);
        }
        return wb;
    }

    public static File getOutFileExcelName(String outFileName) {
        if (StringUtils.isBlank(outFileName)) {
            throw new RuntimeException("输出文件为空");
        }
        File file = new File(outFileName);
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            if (!mkdirs) {
                log.info("创建文件夹失败");
            }
            log.info("路径不存在  已创建路径 : {}", parentFile.getPath());
        }
        String extension = outFileName.substring(outFileName.lastIndexOf("."));
        if (!extension.equalsIgnoreCase(".xls") && !extension.equalsIgnoreCase(".xlsx")) {
            throw new RuntimeException("只支持xls或xlsx文件类型");
        }
        return file;
    }

}
