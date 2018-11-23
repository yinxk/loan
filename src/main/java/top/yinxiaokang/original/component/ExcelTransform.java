package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import top.yinxiaokang.original.dto.CellStyleAndContent;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.enums.ExcelFilterType;
import top.yinxiaokang.others.ErrorException;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExcelTransform {

    private static String pathStr = Constants.TAKE_ACCOUNT_PATH;

    private static String fileStr = "/2018-10-19-业务推算和实际业务-凭证调整数据-修账-多扣";

    private List<SthousingAccount> ssDkye;

    Collection<Map> importExcel;

    /**
     * 分类映射
     */
    private Map<ExcelFilterType, List<Map<String, CellStyleAndContent>>> filterNotDoneAlltypeMap = new LinkedHashMap<>();

    private Map<ExcelFilterType, List<Map<String, CellStyleAndContent>>> allMap = new LinkedHashMap<>();

    Map<String, String> keyMap = new LinkedHashMap<>();


    public static void main(String[] args) {
        ExcelTransform excelTransform = new ExcelTransform();
        excelTransform.workFormDiretory();
        //excelTransform.workFormFile();

    }

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
        /**
         * 创建映射
         */
        ExcelFilterType[] excelFilterTypes = ExcelFilterType.values();
        for (ExcelFilterType type : excelFilterTypes) {
            List<Map<String, CellStyleAndContent>> typeList = new ArrayList<>();
            List<Map<String, CellStyleAndContent>> allTypeList = new ArrayList<>();
            filterNotDoneAlltypeMap.put(type, typeList);
            allMap.put(type, allTypeList);
        }

        keyMap.put("序号", "xh");
        keyMap.put("dkzh", "dkzh");
        keyMap.put("csye", "csye");
        keyMap.put("本金合计", "tsbjhj");
        keyMap.put("xzdkye", "xzdkye");// 修正贷款余额(程序计算)
        keyMap.put("csyqbj", "csyqbj");// 初始逾期本金
        keyMap.put("ssdkye", "ssdkye");// 实时贷款余额
        keyMap.put("ssdkye-xzdkye", "ssdkye-xzdkye");// 实时贷款余额-修账贷款余额
        keyMap.put("dkyesfgx", "dkyesfgx");// 贷款余额是否更新
        keyMap.put("fsxd", "fsxd"); // 推算修正后余额是否与 凭证推算内容中的余额相等
        keyMap.put("发生额差额合计", "fsecehj");
        keyMap.put("本金差额合计", "bjcehj");
        keyMap.put("利息差额合计", "lxcehj");
        keyMap.put("备注", "bz");
        keyMap.put("说明", "sm");
        keyMap.put("行号", "hh");
        keyMap.put("tzhye", "tzhye");// 凭证中的 修正后余额(一截内容,不能精确匹配到数字余额)
        keyMap.put("fileName", "fileName");// 存在于的文件名, 主要在全部待处理中有意义
        //keyMap.put("tscontent", "tscontent");// 推算凭证的内容
    }

    private SthousingAccount getAccountByDkzh(String dkzh) {
        Objects.requireNonNull(dkzh);
        for (SthousingAccount account : ssDkye) {
            if (account.getDkzh().equals(dkzh)) {
                return account;
            }
        }
        return null;
    }

    private void workFormDiretory() {
        File diretory = new File(pathStr);
        if (!diretory.isDirectory()) {
            throw new RuntimeException("not directory");
        }
        File[] files = diretory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                doTransform(file.getPath(), file.getName());
            }
        }
        allDone();
        all();
        log.info("分类转换excel运行结束!");
    }

    public void workFormFile() {
        doTransform(pathStr + fileStr + Constants.XLS, fileStr + Constants.XLS);
        log.info("分类转换excel运行结束!");
    }

    private void allDone() {
        log.info("准备写出 {} 不要关闭", Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/所有文件待处理" + Constants.XLS);
        Workbook wb = new HSSFWorkbook();
        File file = ExcelUtil.getOutFileExcelName(Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/所有文件待处理" + Constants.XLS);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            for (Map.Entry<ExcelFilterType, List<Map<String, CellStyleAndContent>>> entry : this.filterNotDoneAlltypeMap.entrySet()) {
                createSheet(wb, entry.getKey().getTypeMessage(), entry.getValue(), keyMap);
            }
            wb.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("写出 {} 完成", Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/所有文件待处理" + Constants.XLS);
    }

    private void all() {
        log.info("准备写出 {} 不要关闭", Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/所有文件" + Constants.XLS);
        Workbook wb = new HSSFWorkbook();
        File file = ExcelUtil.getOutFileExcelName(Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/所有文件" + Constants.XLS);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            for (Map.Entry<ExcelFilterType, List<Map<String, CellStyleAndContent>>> entry : this.allMap.entrySet()) {
                createSheet(wb, entry.getKey().getTypeMessage(), entry.getValue(), keyMap);
            }
            wb.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("写出 {} 完成", Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/所有文件" + Constants.XLS);
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

    public List<Map<String, CellStyleAndContent>> listExcelAccounts(String pathFileName) {
        List<Map<String, CellStyleAndContent>> list = ExcelUtil.readExcelCellStyleAndContent(pathFileName, 1, false, false);
        List<Map<String, CellStyleAndContent>> firstMes = new ArrayList<>();
        List<Map<String, CellStyleAndContent>> secondMes = new ArrayList<>();
        Iterator<Map<String, CellStyleAndContent>> iterator = list.iterator();
        int secondTag = Integer.MIN_VALUE;
        String regexNumber = "^\\d+\\.?\\d?$";
        Pattern patternNumber = Pattern.compile(regexNumber);
        while (iterator.hasNext()) {
            Map<String, CellStyleAndContent> next = iterator.next();
            String xuhao = Optional.ofNullable(next.get("序号")).map(CellStyleAndContent::getContent).map(Object::toString).orElse("");

            Matcher matcher = patternNumber.matcher(xuhao);
            if (matcher.find()) {
                firstMes.add(next);
                secondTag = 0;
            }
            //if (序号 instanceof Double) {
            //
            //}
            if (secondTag == 3) {
                secondMes.add(next);
            }
            secondTag++;

        }
        Iterator<Map<String, CellStyleAndContent>> firstMsgIte = firstMes.iterator();
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        String regexCsye = "初始贷款余额：([\\s\\S]*)\n期初逾期金额";
        String regexTzhye = "(调整后余额|调整后本金余额)([\\s\\S]*)";

        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternCsye = Pattern.compile(regexCsye);
        Pattern patternTzhye = Pattern.compile(regexTzhye);


        int notMatchNumber = 0;
        if (firstMes.size() != secondMes.size())
            throw new ErrorException("两个列表的长度不相等,说明匹配的信息出现问题");
        Iterator<Map<String, CellStyleAndContent>> secondMsgIte = secondMes.iterator();
        while (firstMsgIte.hasNext()) {
            Map<String, CellStyleAndContent> next = firstMsgIte.next();
            Map<String, CellStyleAndContent> secondNext = secondMsgIte.next();
            String 行号 = next.get("行号").getContent().toString();
            String nullKey0 = Optional.ofNullable(secondNext.get("财务推算")).map(CellStyleAndContent::getContent).map(Object::toString).orElse("");
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
                next.put("tscontent", secondNext.get("财务推算"));
                next.put("tzhye", new CellStyleAndContent(groupTzhye, null));

                Map mapFromImportExcelByDkzh = getMapFromImportExcelByDkzh(groupDkzh);
                String csye = mapFromImportExcelByDkzh.get("csye").toString();
                if (!groupCsye.equals(csye)) {
                    throw new ErrorException("初始逾期本金经查询验证不相等 : " + groupDkzh);
                }

                String csyqbj = mapFromImportExcelByDkzh.get("csyqbj").toString();
                next.put("csyqbj", new CellStyleAndContent(csyqbj, null));

                log.debug("匹配得到的贷款账号: {} , 匹配得到的初始余额: {}  ", groupDkzh, groupCsye);
                next.put("dkzh", new CellStyleAndContent(groupDkzh, null));
                next.put("csye", new CellStyleAndContent(groupCsye, null));
                next.put("xzdkye", new CellStyleAndContent(new BigDecimal(groupCsye).subtract(new BigDecimal(next.get("本金合计").getContent().toString())), null));
            } else {
                log.info("存在没有匹配" + ++notMatchNumber);
                throw new RuntimeException("存在没有匹配");
            }
        }
        return firstMes;
    }


    private void doTransform(String pathFileName, String fileName) {
        List<Map<String, CellStyleAndContent>> list = listExcelAccounts(pathFileName);


        String[] split = fileName.split("\\.");

        Workbook wb = new HSSFWorkbook();
        File file = ExcelUtil.getOutFileExcelName(Constants.TAKE_ACCOUNT_TRANSFORM_PATH + "/" + split[0] + "-转换版" + Constants.XLS);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            filterType(list, keyMap, wb, fileName);
            wb.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        log.info(pathFileName + "=====转换完成=====");
    }

    private void filterType(List<Map<String, CellStyleAndContent>> list, Map<String, String> keyMap, Workbook wb, String fileName) {
        /**
         * 分类映射
         */
        Map<ExcelFilterType, List<Map<String, CellStyleAndContent>>> typeMap = new LinkedHashMap<>();

        /**
         * 创建映射
         */
        ExcelFilterType[] excelFilterTypes = ExcelFilterType.values();
        for (ExcelFilterType type : excelFilterTypes) {
            List<Map<String, CellStyleAndContent>> typeList = new ArrayList<>();
            typeMap.put(type, typeList);
        }
        for (Map<String, CellStyleAndContent> contentMap : list) {
            String 备注 = Optional.ofNullable(contentMap.get("备注")).map(CellStyleAndContent::getContent).map(Object::toString).orElse("");
            BigDecimal xzdkye = (BigDecimal) contentMap.get("xzdkye").getContent();
            BigDecimal ssdkye = getAccountByDkzh(contentMap.get("dkzh").getContent().toString()).getDkye();
            // 全部co
            typeMap.get(ExcelFilterType.ALL).add(contentMap);
            allMap.get(ExcelFilterType.ALL).add(contentMap);
            if (isDone(contentMap)) {
                this.filterNotDoneAlltypeMap.get(ExcelFilterType.ALL).add(contentMap);
            }

            // 多扣类型
            BigDecimal 发生额差额合计 = Optional.ofNullable(contentMap.get("发生额差额合计")).map(CellStyleAndContent::getContent).map(Object::toString).map(BigDecimal::new).orElse(null);
            if (发生额差额合计.compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal 本金差额合计 = Optional.ofNullable(contentMap.get("本金差额合计")).map(CellStyleAndContent::getContent).map(Object::toString).map(BigDecimal::new).orElse(null);
                BigDecimal 利息差额合计 = Optional.ofNullable(contentMap.get("利息差额合计")).map(CellStyleAndContent::getContent).map(Object::toString).map(BigDecimal::new).orElse(null);
                if (发生额差额合计 == null || 本金差额合计 == null || 利息差额合计 == null) {
                    throw new RuntimeException("发生额差额合计,利息差额合计,本金差额合计三个中存在null");
                }
                String moreTagStr;
                // 多扣类型 未结清的
                if (ssdkye.compareTo(BigDecimal.ZERO) > 0) {
                    // 多扣 未结清 负正负
                    if (发生额差额合计.compareTo(BigDecimal.ZERO) < 0 &&
                            本金差额合计.compareTo(BigDecimal.ZERO) > 0 &&
                            利息差额合计.compareTo(BigDecimal.ZERO) < 0) {
                        typeMap.get(ExcelFilterType.MANY_FZF).add(contentMap);
                        allMap.get(ExcelFilterType.MANY_FZF).add(contentMap);
                        if (isDone(contentMap)) {
                            this.filterNotDoneAlltypeMap.get(ExcelFilterType.MANY_FZF).add(contentMap);
                        }
                        contentMap.put("xzdkye", new CellStyleAndContent(xzdkye.subtract(发生额差额合计.abs()), null));
                        moreTagStr = ExcelFilterType.MANY_FZF.getTypeMessage();
                    }
                    // 多扣 未结清 负负负 | 负负正 | 负负零
                    else if (发生额差额合计.compareTo(BigDecimal.ZERO) < 0 &&
                            本金差额合计.compareTo(BigDecimal.ZERO) < 0) {
                        typeMap.get(ExcelFilterType.MANY_FFF_FFZ_FFL).add(contentMap);
                        allMap.get(ExcelFilterType.MANY_FFF_FFZ_FFL).add(contentMap);
                        if (isDone(contentMap)) {
                            this.filterNotDoneAlltypeMap.get(ExcelFilterType.MANY_FFF_FFZ_FFL).add(contentMap);
                        }
                        contentMap.put("xzdkye", new CellStyleAndContent(xzdkye.subtract(发生额差额合计.abs()), null));
                        moreTagStr = ExcelFilterType.MANY_FFF_FFZ_FFL.getTypeMessage();
                    } else {
                        typeMap.get(ExcelFilterType.OTHER).add(contentMap);
                        allMap.get(ExcelFilterType.OTHER).add(contentMap);
                        if (isDone(contentMap)) {
                            this.filterNotDoneAlltypeMap.get(ExcelFilterType.OTHER).add(contentMap);
                        }
                        moreTagStr = ExcelFilterType.OTHER.getTypeMessage();
                    }
                }
                // 多扣类型 已结清的
                else {
                    typeMap.get(ExcelFilterType.MANY_OUTSTANDING_FFF_FFZ_FFL).add(contentMap);
                    allMap.get(ExcelFilterType.MANY_OUTSTANDING_FFF_FFZ_FFL).add(contentMap);
                    if (isDone(contentMap)) {
                        this.filterNotDoneAlltypeMap.get(ExcelFilterType.MANY_OUTSTANDING_FFF_FFZ_FFL).add(contentMap);
                    }
                    moreTagStr = ExcelFilterType.MANY_OUTSTANDING_FFF_FFZ_FFL.getTypeMessage();
                }
                log.info("多扣类型分类信息: {}, {}, {},  ====> {} ", 发生额差额合计, 本金差额合计, 利息差额合计, moreTagStr);
            }
            // 已结清的 对应类型已经不适合
            else if (ssdkye.compareTo(BigDecimal.ONE) <= 0) {
                typeMap.get(ExcelFilterType.CLOSED_ACCOUNT).add(contentMap);
                allMap.get(ExcelFilterType.CLOSED_ACCOUNT).add(contentMap);
                if (isDone(contentMap)) {
                    this.filterNotDoneAlltypeMap.get(ExcelFilterType.CLOSED_ACCOUNT).add(contentMap);
                }
            }
            // 少扣
            else if (发生额差额合计.compareTo(Common.ERROR_RANGE) > 0) {
                typeMap.get(ExcelFilterType.LESS).add(contentMap);
                allMap.get(ExcelFilterType.LESS).add(contentMap);
                if (isDone(contentMap)) {
                    this.filterNotDoneAlltypeMap.get(ExcelFilterType.LESS).add(contentMap);
                }
            }
            // 本息颠倒
            else if (发生额差额合计.compareTo(Common.ERROR_RANGE.negate()) > 0 && 发生额差额合计.compareTo(Common.ERROR_RANGE) < 0/* && 备注.contains("颠倒")*/) {
                typeMap.get(ExcelFilterType.BX_REVERSE).add(contentMap);
                allMap.get(ExcelFilterType.BX_REVERSE).add(contentMap);
                if (isDone(contentMap)) {
                    this.filterNotDoneAlltypeMap.get(ExcelFilterType.BX_REVERSE).add(contentMap);
                }
            }
            // 其他
            else {
                typeMap.get(ExcelFilterType.OTHER).add(contentMap);
                allMap.get(ExcelFilterType.OTHER).add(contentMap);
                if (isDone(contentMap)) {
                    this.filterNotDoneAlltypeMap.get(ExcelFilterType.OTHER).add(contentMap);
                }
            }
            xzdkye = (BigDecimal) contentMap.get("xzdkye").getContent();
            //修正贷款余额是否在调整后余额中存在
            String tzhye = Optional.ofNullable(contentMap.get("tzhye")).map(CellStyleAndContent::getContent).map(Object::toString).orElse("");
            if (tzhye.contains(xzdkye.stripTrailingZeros().toPlainString())) {
                contentMap.put("fsxd", new CellStyleAndContent("是", null));
            } else {
                contentMap.put("fsxd", new CellStyleAndContent("不相等", null));
            }


            if (xzdkye.subtract(ssdkye).abs().compareTo(Common.ERROR_RANGE) < 0) {
                contentMap.put("dkyesfgx", new CellStyleAndContent("不需要更新", null));
            } else {
                contentMap.put("dkyesfgx", new CellStyleAndContent("是", null));
            }
            contentMap.put("ssdkye", new CellStyleAndContent(ssdkye, null));
            contentMap.put("fileName", new CellStyleAndContent(fileName, null));
            contentMap.put("ssdkye-xzdkye", new CellStyleAndContent(ssdkye.subtract(xzdkye), null));

        }

        for (Map.Entry<ExcelFilterType, List<Map<String, CellStyleAndContent>>> entry : typeMap.entrySet()) {
            createSheet(wb, entry.getKey().getTypeMessage(), entry.getValue(), keyMap);
        }
    }

    private boolean isDone(Map<String, CellStyleAndContent> contentMap) {
        CellStyleAndContent cellStyleAndContent = contentMap.get("行号");
        CellStyle cellStyle = cellStyleAndContent.getCellStyle();
        short fillBackgroundColor = cellStyle.getFillBackgroundColor();
        short fillForegroundColor = cellStyle.getFillForegroundColor();
        if (fillBackgroundColor == 40 || fillBackgroundColor == 30 ||
                fillForegroundColor == 40 || fillForegroundColor == 30) {
            return false;
        }
        return true;
    }

    private void createSheet(Workbook wb, String sheetName, List<Map<String, CellStyleAndContent>> list, Map<String, String> keyMap) {
        Objects.requireNonNull(sheetName);
        creatRowAndCell(list, keyMap, wb.createSheet(sheetName), wb);
    }

    private void createTitleRow(Map<String, String> keyMap, Sheet sheet) {
        Set<Map.Entry<String, String>> keyEntry = keyMap.entrySet();
        int k = 0;
        Row row = sheet.createRow(0);
        for (Map.Entry<String, String> entry : keyEntry) {
            Cell cell = row.createCell(k++);
            cell.setCellValue(entry.getValue());
        }
        sheet.createFreezePane(0, 1, 0, 1);
    }


    private void creatRowAndCell(List<Map<String, CellStyleAndContent>> list, Map<String, String> keyMap, Sheet sheet, Workbook wb) {
        createTitleRow(keyMap, sheet);
        for (Map<String, CellStyleAndContent> map : list) {
            map.put("dkzh", new CellStyleAndContent(map.get("dkzh").getContent(), map.get("行号").getCellStyle()));
        }

        Map<Integer, CellStyle> cellStyleMap = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, CellStyleAndContent> contentMap = list.get(i);
            Iterator<Map.Entry<String, String>> keyIte = keyMap.entrySet().iterator();
            int j = 0;
            while (keyIte.hasNext()) {
                Map.Entry<String, String> key = keyIte.next();
                if (contentMap.containsKey(key.getKey())) {
                    CellStyle cellStyle1 = Optional.ofNullable(contentMap.get(key.getKey())).map(CellStyleAndContent::getCellStyle).orElse(null);
                    Cell cell = row.createCell(j++);
                    if ((key.getKey().equals("dkzh") || key.getKey().equals("行号") || key.getKey().equals("说明")) && cellStyle1 != null) {
                        if (cellStyleMap.containsKey((int) cellStyle1.getFillForegroundColor())) {
                            cell.setCellStyle(cellStyleMap.get((int) cellStyle1.getFillForegroundColor()));
                        } else {
                            CellStyle style = wb.createCellStyle();
                            style.setFillForegroundColor(cellStyle1.getFillForegroundColor());
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            cell.setCellStyle(style);
                            cellStyleMap.put((int) cellStyle1.getFillForegroundColor(), style);
                        }
                    }
                    String content = Optional.ofNullable(contentMap.get(key.getKey())).map(CellStyleAndContent::getContent).map(Object::toString).orElse("");
                    cell.setCellValue(content);
                } else {
                    throw new RuntimeException("not have the key : " + key.getKey());
                }

            }
        }
        for (int i = 0; i < keyMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
