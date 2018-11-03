package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinxk
 * @date 2018/11/3 19:00
 */
@Slf4j
public class FlagTakeDoneAccounts {

    private List<SthousingAccount> takeDoneAccounts;

    public FlagTakeDoneAccounts() {
        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, 0, false, false);
        List<Map<String, Object>> content = excelReadReturn.getContent();
        takeDoneAccounts = new ArrayList<>();
        for (Map<String, Object> objectMap : content) {
            SthousingAccount sthousingAccount = new SthousingAccount();
            sthousingAccount.setDkzh(objectMap.get("贷款账号").toString());
            takeDoneAccounts.add(sthousingAccount);
        }
    }

    private SthousingAccount searchAccount(String dkzh) {
        for (SthousingAccount account : takeDoneAccounts) {
            String accountDkzh = account.getDkzh();
            if (accountDkzh == null) continue;
            if (accountDkzh.equals(dkzh)) {
                return account;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new FlagTakeDoneAccounts().workFromDirectory();
    }

    private void workFromDirectory() {
        File directory = new File(Constants.TAKE_ACCOUNT_PATH);
        if (!directory.isDirectory()) {
            throw new RuntimeException("not directory");
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                flagSkyBlueToDoneAccount(file.getPath(), Constants.TAKE_ACCOUNT_TAKED_FLAG_SKY_BLUE_ACCOUNTS_DATA_PATH + "/" + file.getName());
            }
        }
    }

    private void flagSkyBlueToDoneAccount(String inFileName, String outFileName) {
        String regexNumber = "^\\d+$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);
        ExcelUtil.copyExcelAndUpdate(inFileName, 1, false, outFileName,
                (wb, row, keyMap, contentMapColIndex) -> {
                    Matcher matcher = patternNumber.matcher(ExcelUtil.getStringCellContent(row.getCell(contentMapColIndex.get("序号"))));
                    if (matcher.find()) {
                        Cell hh = row.getCell(contentMapColIndex.get("行号"));
                        if (hh == null) return;
                        Matcher matcherDkzh = patternDkzh.matcher(ExcelUtil.getStringCellContent(hh));
                        if (matcherDkzh.find()) {
                            String dkzh = matcherDkzh.group(1);
                            if (StringUtils.isBlank(dkzh)) return;
                            SthousingAccount sthousingAccount = searchAccount(dkzh);
                            log.debug("匹配的贷款账号: {}, 已处理的贷款账号: {}", dkzh, sthousingAccount == null ? "" : sthousingAccount.getDkzh());
                            if (sthousingAccount == null) return;
                            CellStyle cellStyle = wb.createCellStyle();
                            // 根据前面得出的标记颜色, 天蓝色 sky_blue = 40
                            cellStyle.setFillForegroundColor((short) 40);
                            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            hh.setCellStyle(cellStyle);
                            log.info("标记了贷款账号为: {} 的单元格", dkzh);
                        } else {
                            throw new RuntimeException("匹配出错");
                        }

                    }

                });
    }
}
