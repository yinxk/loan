package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
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

    private Map getJkrxmByDkzh(String dkzh) {
        Objects.requireNonNull(dkzh);
        if (maps == null) {
            maps = Common.xlsToList(Constants.BASE_ACCOUNT_INFORMATION_SSDKYE);
        }
        for (Map map : maps) {
            if (dkzh.equals(map.get("dkzh").toString())) {
                return map;
            }
        }
        return new HashMap();
    }

    private void addExplainToExcel(String inFileName, String outFileName) {
        String regexNumber = "^\\d+$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);
        ExcelUtil.copyExcelAndUpdate(inFileName, 1, false, outFileName,
                (row, keyMap, proIndexMapColIndex) -> {
                    Matcher matcher = patternNumber.matcher(ExcelUtil.getStringCellContent(row.getCell(proIndexMapColIndex.get(0))));
                    if (matcher.find()) {
                        Matcher matcherDkzh = patternDkzh.matcher(ExcelUtil.getStringCellContent(row.getCell(proIndexMapColIndex.get(1))));
                        if (matcherDkzh.find()) {
                            String dkzh = matcherDkzh.group(1);
                            Map jkrxmByDkzh = getJkrxmByDkzh(dkzh);
                            Cell cell10 = row.getCell(proIndexMapColIndex.get(10));
                            if (cell10 == null) return;
                            String cellValue = ExcelUtil.getStringCellContent(cell10);
                            String prefix = "用于调整 %s %s %s";
                            cellValue = String.format(prefix, jkrxmByDkzh.get("dkzh"),
                                    jkrxmByDkzh.get("jkrxm"), cellValue);
                            cell10.setCellValue(cellValue);
                            log.info("写入 {} 的值为: {}", dkzh, cellValue);
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
            if (file.isFile()) {
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
