package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.others.ErrorException;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ImportExcelUtilLessFour;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinxk
 * @date 2018/10/30 11:45
 */
@Slf4j
public class ExcelTransform {

    private static String pathStr = Constants.TAKE_ACCOUNT_PATH;

    private static String fileStr = "/2018-10-18-业务推算和实际业务-凭证调整数据-加说明";

    private List<SthousingAccount> ssDkye;

    Collection<Map> importExcel;

    public ExcelTransform() {
        importExcel = Common.xlsToList(Constants.BASE_ACCOUNT_INFORMATION);
        Collection<Map> maps = Common.xlsToList(Constants.BASE_ACCOUNT_INFORMATION_SSDKYE);
        List<SthousingAccount> ssDkye = new ArrayList<>();
        for (Map map : maps) {
            SthousingAccount account = new SthousingAccount();
            account.setDkzh(map.get("dkzh").toString());
            account.setDkye(new BigDecimal(map.get("dkye").toString()));
            ssDkye.add(account);
        }
        this.ssDkye = ssDkye;
    }

    private SthousingAccount getAccountByDkzh(String dkzh) {
        return null;
    }

    public void workFormDiretory() {
        File diretory = new File(pathStr);
        if (!diretory.isDirectory()) {
            throw new RuntimeException("not directory");
        }
        File[] files = diretory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                doTransform(file.getPath(), file.getName());
            }
        }
        log.info("分类转换excel运行结束!");
    }

    public void workFormFile() {
        doTransform(pathStr + fileStr + Constants.XLS, fileStr + Constants.XLS);
        log.info("分类转换excel运行结束!");
    }

    public static void main(String[] args) {
        ExcelTransform excelTransform = new ExcelTransform();

        excelTransform.workFormDiretory();
//        excelTransform.workFormFile();

    }

    private Map getMapFromImportExcelByDkzh(String dkzh) {
        Objects.requireNonNull(dkzh);
        for (Map map : importExcel) {
            String dkzh1 = map.get("dkzh").toString();
            if (dkzh.equals(dkzh1)) {
                return map;
            }
        }
        return new HashMap();
    }


    public void doTransform(String pathFileName, String fileName) {
        List<Map<String, Object>> list = ImportExcelUtilLessFour.read(pathFileName, 1, false, false);
        List<Map<String, Object>> firstMes = new ArrayList<>();
        List<Map<String, Object>> secondMes = new ArrayList<>();
        Iterator<Map<String, Object>> iterator = list.iterator();
        int secondTag = 0;
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            Object 序号 = next.get("序号");
            if (序号 instanceof Double) {
                firstMes.add(next);
                secondTag = 0;
            }
            if (secondTag == 3) {
                secondMes.add(next);
            }
            secondTag++;

        }
        Iterator<Map<String, Object>> firstMsgIte = firstMes.iterator();
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        String regexCsye = "初始贷款余额：([\\s\\S]*)\n期初逾期金额";
        String regexTzhye = "(调整后余额|调整后本金余额)([\\s\\S]*)";

        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternCsye = Pattern.compile(regexCsye);
        Pattern patternTzhye = Pattern.compile(regexTzhye);


        int notMatchNumber = 0;
        if (firstMes.size() != secondMes.size())
            throw new ErrorException("两个列表的长度不相等,说明匹配的信息出现问题");
        Iterator<Map<String, Object>> secondMsgIte = secondMes.iterator();
        while (firstMsgIte.hasNext()) {
            Map<String, Object> next = firstMsgIte.next();
            Map<String, Object> secondNext = secondMsgIte.next();
            String 行号 = (String) next.get("行号");
            String nullKey0 = Optional.ofNullable(secondNext.get("nullKey0")).map(Object::toString).orElse("");
            Matcher matcherDkzh = patternDkzh.matcher(行号);
            Matcher matcherCsye = patternCsye.matcher(行号);
            Matcher matcherTzhye = patternTzhye.matcher(nullKey0);
            if (matcherDkzh.find() && matcherCsye.find()) {
                String groupDkzh = matcherDkzh.group(1);
                String groupCsye = matcherCsye.group(1);
                String groupTzhye = "";
                if (matcherTzhye.find()) {
                    groupTzhye = matcherTzhye.group();
                }
                if (StringUtils.isBlank(groupDkzh)) {
                    firstMsgIte.remove();
                    secondMsgIte.remove();
                    continue;
                }
                next.put("tscontent", secondNext.get("nullKey0"));
                next.put("tzhye", groupTzhye);

                Map mapFromImportExcelByDkzh = getMapFromImportExcelByDkzh(groupDkzh);
                String csye = mapFromImportExcelByDkzh.get("csye").toString();
                if (!groupCsye.equals(csye)) {
                    throw new ErrorException("初始逾期本金经查询验证不相等 : " + groupDkzh);
                }

                String csyqbj = mapFromImportExcelByDkzh.get("csyqbj").toString();
                next.put("csyqbj", csyqbj);

                log.info("匹配得到的贷款账号: {} , 匹配得到的初始余额: {} \n ", groupDkzh, groupCsye);
                next.put("dkzh", groupDkzh);
                next.put("csye", groupCsye);
                next.put("xzdkye", new BigDecimal(groupCsye).subtract(new BigDecimal(next.get("本金合计").toString())));
            } else {
                log.info("存在没有匹配" + ++notMatchNumber);
                throw new RuntimeException("存在没有匹配");
            }
        }


        Map<String, String> keyMap = new LinkedHashMap<>();

        keyMap.put("序号", "xh");
        keyMap.put("dkzh", "dkzh");
        keyMap.put("csye", "csye");
        keyMap.put("本金合计", "tsbjhj");
        keyMap.put("xzdkye", "xzdkye");// 修正贷款余额(程序计算)
        keyMap.put("csyqbj", "csyqbj");// 初始逾期本金
        keyMap.put("ssdkye", "ssdkye");// 实时贷款余额
        keyMap.put("dkyesfgx", "dkyesfgx");// 贷款余额是否更新
        keyMap.put("fsxd", "fsxd"); // 推算修正后余额是否与 凭证推算内容中的余额相等
        keyMap.put("发生额差额合计", "fsecehj");
        keyMap.put("本金差额合计", "bjcehj");
        keyMap.put("利息差额合计", "lxcehj");
        keyMap.put("备注", "bz");
        keyMap.put("说明", "sm");
        keyMap.put("行号", "hh");
        keyMap.put("tzhye", "tzhye");// 凭证中的 修正后余额(一截内容,不能精确匹配到数字余额)
        //keyMap.put("tscontent", "tscontent");// 推算凭证的内容


        String[] split = fileName.split("\\.");


        Workbook wb = new HSSFWorkbook();
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/" + split[0] + "-转换版" + Constants.XLS))) {
            //ExcelUtil.exportExcel(keyMap, list, fileOutputStream);
            filterType(firstMes, keyMap, wb);
            wb.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        log.info(pathFileName + "=====转换完成=====");
    }

    private void filterType(List<Map<String, Object>> list, Map<String, String> keyMap, Workbook wb) {

        Sheet sheet1 = wb.createSheet("全部");
        Sheet sheet2 = wb.createSheet("本息颠倒");
        Sheet sheet3 = wb.createSheet("多扣 负正负");
        Sheet sheet6 = wb.createSheet("多扣 负负负 | 负负正 | 负负零");
        Sheet sheet4 = wb.createSheet("少扣");
        Sheet sheet5 = wb.createSheet("其他");
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);
        // 本息颠倒
        List<Map<String, Object>> list2 = new ArrayList<>();
        /**
         * 多扣
         */
        List<Map<String, Object>> list3 = new ArrayList<>();
        List<Map<String, Object>> list6 = new ArrayList<>();
        /**
         * 少扣
         */
        List<Map<String, Object>> list4 = new ArrayList<>();
        /**
         * 其他
         */
        List<Map<String, Object>> list5 = new ArrayList<>();
        Row row1 = sheet1.createRow(0);
        Row row2 = sheet2.createRow(0);
        Row row3 = sheet3.createRow(0);
        Row row4 = sheet4.createRow(0);
        Row row5 = sheet5.createRow(0);
        Row row6 = sheet6.createRow(0);


        Set<Map.Entry<String, String>> keyEntry = keyMap.entrySet();
        Iterator<Map.Entry<String, String>> keyIte = keyEntry.iterator();
        int k = 0;
        while (keyIte.hasNext()) {
            Map.Entry<String, String> key = keyIte.next();
            Cell cell1 = row1.createCell(k);
            Cell cell2 = row2.createCell(k);
            Cell cell3 = row3.createCell(k);
            Cell cell4 = row4.createCell(k);
            Cell cell5 = row5.createCell(k);
            Cell cell6 = row6.createCell(k);

            k++;

            cell1.setCellValue(key.getValue());
            cell2.setCellValue(key.getValue());
            cell3.setCellValue(key.getValue());
            cell4.setCellValue(key.getValue());
            cell5.setCellValue(key.getValue());
            cell6.setCellValue(key.getValue());
        }
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> contentMap = list.get(i);
            String 备注 = Optional.ofNullable(contentMap.get("备注")).map(Object::toString).orElse("");
            BigDecimal xzdkye = (BigDecimal) contentMap.get("xzdkye");

            if (备注.contains("多扣")) {
                BigDecimal 发生额差额合计 = Optional.ofNullable(contentMap.get("发生额差额合计")).map(Object::toString).map(BigDecimal::new).orElse(BigDecimal.ZERO);
                BigDecimal 本金差额合计 = Optional.ofNullable(contentMap.get("本金差额合计")).map(Object::toString).map(BigDecimal::new).orElse(BigDecimal.ZERO);
                BigDecimal 利息差额合计 = Optional.ofNullable(contentMap.get("利息差额合计")).map(Object::toString).map(BigDecimal::new).orElse(BigDecimal.ZERO);
                String moreTagStr = "";
                if (发生额差额合计.compareTo(BigDecimal.ZERO) < 0 &&
                        本金差额合计.compareTo(BigDecimal.ZERO) > 0 &&
                        利息差额合计.compareTo(BigDecimal.ZERO) < 0) {
                    list3.add(contentMap);
                    contentMap.put("xzdkye", xzdkye.subtract(发生额差额合计.abs()));
                    moreTagStr = "负正负";
                } else if (发生额差额合计.compareTo(BigDecimal.ZERO) < 0 &&
                        本金差额合计.compareTo(BigDecimal.ZERO) < 0) {
                    list6.add(contentMap);
                    contentMap.put("xzdkye", xzdkye.subtract(发生额差额合计.abs()));
                    moreTagStr = "负负负 | 负负正 | 负负零";
                } else {
                    list5.add(contentMap);
                    moreTagStr = "其他";
                }

                log.info("多扣类型分类信息: {}, {}, {},  ====> {} \n", 发生额差额合计, 本金差额合计, 利息差额合计, moreTagStr);

            } else if (备注.contains("少扣")) {
                list4.add(contentMap);
            } else if (备注.contains("颠倒")) {
                list2.add(contentMap);
            } else {
                list5.add(contentMap);
            }
            xzdkye = (BigDecimal) contentMap.get("xzdkye");
            //修正贷款余额是否在调整后余额中存在
            String tzhye = Optional.ofNullable(contentMap.get("tzhye")).map(Object::toString).orElse("");
            if (tzhye.contains(xzdkye.stripTrailingZeros().toPlainString())) {
                contentMap.put("fsxd", "是");
            } else {
                contentMap.put("fsxd", "不相等");
            }

            BigDecimal ssdkye = getAccountByDkzh(contentMap.get("dkzh").toString()).getDkye();
            if (xzdkye.subtract(ssdkye).abs().compareTo(Common.ERROR_RANGE) < 0) {
                contentMap.put("dkyesfgx", "不需要更新");
            } else {
                contentMap.put("dkyesfgx", "是");
            }
            contentMap.put("ssdkye", ssdkye);

        }
        creatRowAndCell(list, keyMap, sheet1, cellStyle);
        creatRowAndCell(list2, keyMap, sheet2, cellStyle);
        creatRowAndCell(list3, keyMap, sheet3, cellStyle);
        creatRowAndCell(list6, keyMap, sheet6, cellStyle);
        creatRowAndCell(list4, keyMap, sheet4, cellStyle);
        creatRowAndCell(list5, keyMap, sheet5, cellStyle);


    }


    private void creatRowAndCell(List<Map<String, Object>> list, Map<String, String> keyMap, Sheet sheet, CellStyle cellStyle) {
        for (int i = 0; i < list.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, Object> contentMap = list.get(i);
            Iterator<Map.Entry<String, String>> keyIte = keyMap.entrySet().iterator();
            int j = 0;
            while (keyIte.hasNext()) {
                Map.Entry<String, String> key = keyIte.next();
                if (contentMap.containsKey(key.getKey())) {
                    sheet.autoSizeColumn(j);
                    Cell cell = row.createCell(j++);
                    //cell.setCellStyle(cellStyle);
                    String content = Optional.ofNullable(contentMap.get(key.getKey())).map(Object::toString).orElse("");
                    cell.setCellValue(content);
                } else {
                    throw new RuntimeException("not have the key : " + key.getKey());
                }

            }
        }
    }
}
