package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

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

    private void flagSkyBlueToDoneAccount(String inFileName, String outFileName) {
        String regexNumber = "^\\d+$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);
        ExcelUtil.copyExcelAndUpdate(inFileName, 1, false, outFileName,
                (row, keyMap, contentMapColIndex, proIndexMapColIndex) -> {
                    Matcher matcher = patternNumber.matcher(ExcelUtil.getStringCellContent(row.getCell(contentMapColIndex.get("序号"))));
                    if (matcher.find()) {
                        Cell hh = row.getCell(contentMapColIndex.get("行号"));
                        if (hh == null) return;
                        Matcher matcherDkzh = patternDkzh.matcher(ExcelUtil.getStringCellContent(hh));
                        if (matcherDkzh.find()) {
                            String dkzh = matcherDkzh.group(1);
                            if (StringUtils.isBlank(dkzh)) return;
                            SthousingAccount sthousingAccount = searchAccount(dkzh);
                            if (sthousingAccount== null) return;

                        } else {
                            throw new RuntimeException("匹配出错");
                        }

                    }

                });
    }
}
