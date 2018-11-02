package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UpdateExcelExplain {
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

    public void doUpdateExcel() {
        String regexNumber = "^\\d+$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);

        ExcelUtil.copyExcelAndUpdate("C:\\修账相关数据\\修账\\2018-10-20-业务推算和实际业务-凭证调整数据-加说明.xls", 1, false, null, row -> {
            Cell cell = row.getCell(0);
            cell.setCellType(CellType.STRING);
            String value = cell.getStringCellValue();
            Matcher matcher = patternNumber.matcher(value);
            if (matcher.find()) {
                Cell cell1 = row.getCell(1);
                cell1.setCellType(CellType.STRING);

                Matcher matcherDkzh = patternDkzh.matcher(cell1.getStringCellValue());
                if (matcherDkzh.find()) {
                    String dkzh = matcherDkzh.group(1);
                    Map jkrxmByDkzh = getJkrxmByDkzh(dkzh);
                    Cell cell10 = row.getCell(10);
                    cell10.setCellType(CellType.STRING);
                    String cellValue = cell10.getStringCellValue();
                    cellValue = "用于调整 " + jkrxmByDkzh.get("dkzh") + " " + jkrxmByDkzh.get("jkrxm") + cellValue;
                    cell10.setCellValue(cellValue);
                    log.info("写入" + dkzh + "的值为: " + cellValue);
                } else {
                    throw new RuntimeException("匹配出错");
                }

            }

        });
    }

    public static void main(String[] args) {

        new UpdateExcelExplain().doUpdateExcel();

    }
}
