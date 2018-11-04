package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AddExplainToExcel {
    Collection<Map> maps;

    {
        maps = Common.xlsToList(Constants.BASE_ACCOUNT_INFORMATION_SSDKYE);
    }

    private Map getJkrxmByDkzh(String dkzh) {
        Objects.requireNonNull(dkzh);
        for (Map map : maps) {
            if (dkzh.equals(map.get("dkzh").toString())) {
                return map;
            }
        }
        return new HashMap();
    }

    private void addExplainToExcel(String inFileName, String outFileName) {
        String regexNumber = "^\\d+\\.?\\d?$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);
        ExcelUtil.copyExcelAndUpdate(inFileName, 1, false, outFileName,
                (wb, row, keyMap, contentMapColIndex) -> {
                    String xh = Optional.ofNullable(ExcelUtil.getCellContent(row.getCell(contentMapColIndex.get("序号")))).map(Object::toString).orElse("");
                    Matcher matcher = patternNumber.matcher(xh);
                    if (matcher.find()) {
                        String hh = Optional.ofNullable(ExcelUtil.getCellContent(row.getCell(contentMapColIndex.get("行号")))).map(Object::toString).orElse("");
                        Matcher matcherDkzh = patternDkzh.matcher(hh);
                        if (matcherDkzh.find()) {
                            String dkzh = matcherDkzh.group(1);
                            if (StringUtils.isBlank(dkzh)) return;
                            Map jkrxmByDkzh = getJkrxmByDkzh(dkzh);
                            Cell sm = row.getCell(contentMapColIndex.get("说明"));
                            if (sm == null) return;
                            CellStyle cellStyle = wb.createCellStyle();
                            Font font = wb.createFont();
                            cellStyle.cloneStyleFrom(sm.getCellStyle());
                            cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
                            cellStyle.setAlignment(HorizontalAlignment.LEFT);
                            cellStyle.setWrapText(true);
                            cellStyle.setFont(font);
                            font.setFontName("宋体");
                            font.setFontHeightInPoints((short)11);
                            String cellValue = ExcelUtil.getStringCellContent(sm);
                            String prefix = "用于调整 %s %s %s";
                            cellValue = String.format(prefix, jkrxmByDkzh.get("dkzh"),
                                    jkrxmByDkzh.get("jkrxm"), cellValue);
                            sm.setCellValue(cellValue);
                            sm.setCellStyle(cellStyle);
                            log.debug("写入 {} 的说明 : {}", dkzh, cellValue);
                        } else {
                            throw new RuntimeException("匹配出错");
                        }

                    }

                });
    }

    private void workFromDirectory() {
        File directory = new File(Constants.TAKE_ACCOUNT_SHOULD_FILLING_IN_DATA_PATH);
        if (!directory.isDirectory()) {
            throw new RuntimeException("not directory");
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isFile() || file.getName().contains("-18")) {
                addExplainToExcel(file.getPath(), Constants.TAKE_ACCOUNT_FILLED_DATA_PATH + "/" + file.getName());
            }
        }
    }

    private void workFromFile() {
        String fName = "/2018-10-15-业务推算和实际业务-凭证调整数据-加说明.xls";
        addExplainToExcel(Constants.TAKE_ACCOUNT_SHOULD_FILLING_IN_DATA_PATH + fName, Constants.TAKE_ACCOUNT_FILLED_DATA_PATH + fName);
    }

    public void work() {
        workFromDirectory();
        //workFromFile();
    }

    public static void main(String[] args) {
        new AddExplainToExcel().work();
    }
}
