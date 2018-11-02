package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
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
        return null;
    }

    private void addExplainToExcel(String inFileName, String outFileName) {
        String regexNumber = "^\\d+$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);
        ExcelUtil.copyExcelAndUpdate(inFileName, 1, false, outFileName,
                (row, keyMap, proIndexMapColIndex) -> {
                    System.out.println(row);
                    System.out.println(keyMap);
                    System.out.println(proIndexMapColIndex);
                    Matcher matcher = patternNumber.matcher(ExcelUtil.getStringCellContent(row.getCell(proIndexMapColIndex.get(0))));
                    if (matcher.find()) {
                        Matcher matcherDkzh = patternDkzh.matcher(ExcelUtil.getStringCellContent(row.getCell(proIndexMapColIndex.get(1))));
                        if (matcherDkzh.find()) {
                            String dkzh = matcherDkzh.group(1);
                            Map jkrxmByDkzh = getJkrxmByDkzh(dkzh);
                            Cell cell10 = row.getCell(proIndexMapColIndex.get(10));
                            String cellValue = ExcelUtil.getStringCellContent(cell10);
                            String prefix = "用于调整 %s %s %s";
                            cellValue = String.format(prefix, jkrxmByDkzh.get("dkzh"), jkrxmByDkzh.get("jkrxm"), cellValue);
                            cell10.setCellValue(cellValue);
                            log.info("写入" + dkzh + "的值为: " + cellValue);
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
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                addExplainToExcel(file.getPath(), null);
            }
        }
    }

    private void workFromFile() {
        addExplainToExcel(Constants.TAKE_ACCOUNT_SHOULD_FILLING_IN_DATA_PATH + "/2018-10-15-业务推算和实际业务-凭证调整数据-加说明.xls", null);
    }

    public void work() {
        workFromFile();
    }

    public static void main(String[] args) {

        new AddExplainToExcel().work();

    }
}
