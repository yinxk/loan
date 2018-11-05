package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinxk
 * @date 2018/11/3 19:00
 */
@Slf4j
public class FlagSkyBlueTakeDoneAccounts {

    private List<Map<String, Object>> takeDoneAccounts;

    private TakeDoneAccountToExcel takeDoneAccountToExcel;

    private List<Map<String, String>> theFlagAccounts = new ArrayList<>();

    public FlagSkyBlueTakeDoneAccounts() {
        takeDoneAccountToExcel = new TakeDoneAccountToExcel();
        takeDoneAccounts = takeDoneAccountToExcel.getDoneAccounts();

    }

    private Map<String, Object> searchAccount(String dkzh) {
        for (Map<String, Object> account : takeDoneAccounts) {
            String accountDkzh = account.get("贷款账号").toString();
            if (accountDkzh == null) continue;
            if (accountDkzh.equals(dkzh)) {
                return account;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new FlagSkyBlueTakeDoneAccounts().workFromDirectory();
    }

    private void workFromDirectory() {
        File directory = new File(Constants.TAKE_ACCOUNT_PATH);
        if (!directory.isDirectory()) {
            throw new RuntimeException("not directory");
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isFile() || file.getName().contains("-20-")) {
                //flagSkyBlueToDoneAccount(file.getPath(), Constants.TAKE_ACCOUNT_TAKED_FLAG_SKY_BLUE_ACCOUNTS_DATA_PATH + "/" + file.getName(), file.getName());
                flagSkyBlueToDoneAccount(file.getPath(), null, file.getName());
            }
        }
        log.error("该次总共标记账号数量是: {} ", theFlagAccounts.size());
        for (Map<String, String> flagAccount : theFlagAccounts) {
            log.error("标记的贷款账号: {}  对应文件: {}", flagAccount.get("dkzh"), flagAccount.get("file"));
        }
        log.error("该次总共标记账号数量是: {} ", theFlagAccounts.size());
        takeDoneAccountToExcel.doToExcel();
    }

    private void flagSkyBlueToDoneAccount(String inFileName, String outFileName, String fName) {
        String regexNumber = "^\\d+\\.?\\d?$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");
        ExcelUtil.copyExcelAndUpdate(inFileName, 1, false, outFileName,
                (wb, row, keyMap, contentMapColIndex) -> {
                    String xh = Optional.ofNullable(ExcelUtil.getCellContent(row.getCell(contentMapColIndex.get("序号")))).map(Object::toString).orElse("");
                    Matcher matcher = patternNumber.matcher(xh);
                    if (matcher.find()) {
                        Cell hhCell = row.getCell(contentMapColIndex.get("行号"));
                        if (hhCell == null) {
                            return;
                        }
                        String hhStr = Optional.ofNullable(ExcelUtil.getCellContent(row.getCell(contentMapColIndex.get("行号")))).map(Object::toString).orElse("");
                        Matcher matcherDkzh = patternDkzh.matcher(hhStr);
                        if (matcherDkzh.find()) {
                            String dkzh = matcherDkzh.group(1);
                            if (StringUtils.isBlank(dkzh)) {
                                return;
                            }

                            Map<String, Object> doneAccount = searchAccount(dkzh);
                            if (doneAccount == null) {
                                log.error(" 未匹配到已处理贷款账号存在 : {}", dkzh);
                                return;
                            }
                            // 过滤已标记的
                            String flag = doneAccount.get("是否已标记").toString();
                            if ("是".equals(flag)) {
                                log.debug("贷款账号 {} 已标记  标记记号: {} ", dkzh, flag);
                                return;
                            }

                            log.debug("匹配的贷款账号: {}, 已处理的贷款账号: {}", dkzh, doneAccount.get("贷款账号"));

                            CellStyle cellStyle = wb.createCellStyle();
                            cellStyle.cloneStyleFrom(hhCell.getCellStyle());
                            // 根据前面得出的标记颜色, 天蓝色 sky_blue = 40
                            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                            cellStyle.setFillBackgroundColor(IndexedColors.SKY_BLUE.getIndex());
                            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            hhCell.setCellStyle(cellStyle);
                            doneAccount.put("颜色值", IndexedColors.SKY_BLUE.name() + "");
                            doneAccount.put("Excel对应颜色", IndexedColors.SKY_BLUE.name());
                            doneAccount.put("时间", dateTimeFormatter.format(LocalDateTime.now()));
                            doneAccount.put("是否已标记", "是");
                            doneAccount.put("账号对应文件", fName);
                            log.info("标记了贷款账号为: {} 的单元格", dkzh);
                            Map<String, String> theFlag = new HashMap<>();
                            theFlag.put("dkzh", dkzh);
                            theFlag.put("file", fName);
                            theFlagAccounts.add(theFlag);
                        } else {
                            throw new RuntimeException("匹配出错");
                        }

                    }

                });
    }
}
