package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import top.yinxiaokang.original.dto.CellStyleAndContent;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/11/3 15:36
 */
@SuppressWarnings("Duplicates")
@Slf4j
public class InitTakedAccounts {

    ExcelTransform excelTransform = new ExcelTransform();
    private static String pathStr = Constants.TAKE_ACCOUNT_PATH;
    Map<Integer, Integer> colorMap = new HashMap<>();
    List<Map<String, Object>> notDoneList = new ArrayList<>();
    List<Map<String, Object>> moreOnePrePayment;
    List<Map<String, Object>> moreOnePrePaymentMessage = new ArrayList<>();
    List<Map<String, CellStyleAndContent>> all = new ArrayList<>();
    List<String> overTimeStringList = new ArrayList<>();

    public InitTakedAccounts() {
        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel(Constants.BASE_PATH + "/提前还款或者结清2次及以上问题账号.xlsx", 0, false, false);
        moreOnePrePayment = excelReadReturn.getContent();
    }

    private boolean isInMoreOnePrePayment(String dkzh) {
        for (Map<String, Object> map : moreOnePrePayment) {
            if (map.get("dkzh").equals(dkzh)) {
                return true;
            }

        }
        return false;
    }

    public void init() {
        initTakeDoneAccounts(true);
    }

    public void onlyListMessage() {
        initTakeDoneAccounts(false);
        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, 0, false, false);
        List<Map<String, Object>> content = excelReadReturn.getContent();
        String theLog = "dkzh:%-30s  fse:%-10s  bjje:%-10s  lxje:%-10s  bz:%-20s  file:%-20s";
        for (Map<String, Object> contentMap : notDoneList) {
            log.error(String.format(theLog, contentMap.get("dkzh"), contentMap.get("fse"), contentMap.get("bjje"),
                    contentMap.get("lxje"), contentMap.get("bz").toString().trim(), contentMap.get("file")));
        }
        log.error("总共匹配到的账号数量为 : {}", all.size());
        log.error("还没有处理的账号总数为 : {}", notDoneList.size());
        log.error("已处理贷款账号数量: {}  根据文件匹配到的已处理账号数量: {}", colorMap.get(40), content.size());

        for (Map<String, Object> contentMap : moreOnePrePaymentMessage) {
            log.error(String.format(theLog, contentMap.get("dkzh"), contentMap.get("fse"), contentMap.get("bjje"),
                    contentMap.get("lxje"), contentMap.get("bz").toString().trim(), contentMap.get("file")));
        }
        log.error("匹配到的两次以及以上提前还款或者结清的账号数量: {} ", moreOnePrePaymentMessage.size());

        for (String s : overTimeStringList) {
            log.error(s);
        }
        log.error("超时账号数量: {} ", overTimeStringList.size());


    }

    public void initTakeDoneAccounts(boolean isWrite) {
        File diretory = new File(pathStr);
        if (!diretory.isDirectory()) {
            throw new RuntimeException("not directory");
        }
        File[] files = diretory.listFiles();
        Map<String, String> dkzhsKey = new HashMap<>();
        Map<String, CellStyle> dkzhsAndCellStyle = new HashMap<>();
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                List<Map<String, CellStyleAndContent>> excelAccounts = excelTransform.listExcelAccounts(file.getPath());
                for (Map<String, CellStyleAndContent> excelAccount : excelAccounts) {
                    String dkzh = excelAccount.get("dkzh").getContent().toString();
                    if (dkzhsKey.containsKey(dkzh)) {
                        String msg = "%s 和 %s 存在相同贷款账号: %s \n";
                        String formatMsg = String.format(msg, dkzhsKey.get(dkzh), file.getName(), dkzh);
                        CellStyle cellStyle = dkzhsAndCellStyle.get(dkzh);
                        IndexedColors indexedColors = IndexedColors.fromInt(cellStyle.getFillForegroundColor());
                        // 紫罗兰色 表示超时的
                        if (indexedColors == IndexedColors.VIOLET || IndexedColors.fromInt(excelAccount.get("行号").getCellStyle().getFillForegroundColor()) == IndexedColors.VIOLET) {
                            String overTimeStr = "%s 和 %s 存在相同贷款账号: %s 其中已超时:%s ";
                            overTimeStringList.add(String.format(overTimeStr, dkzhsKey.get(dkzh), file.getName(), dkzh, dkzhsKey.get(dkzh)));
                            //log.error("{} 和 {} 存在相同贷款账号: {} 其中已超时:{} ",dkzhsKey.get(dkzh),file.getName(),dkzh,dkzhsKey.get(dkzh));
                            //all.removeIf(next -> dkzh.equals(next.get("dkzh").toString()));
                        } else {
                            throw new RuntimeException(formatMsg);
                        }
                    }
                    dkzhsKey.put(dkzh, file.getName());
                    dkzhsAndCellStyle.put(dkzh, excelAccount.get("行号").getCellStyle());
                    all.add(excelAccount);
                }

            }
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");
        // 发现颜色为40 30的是已标记为处理过的数据
        List<Map<String, Object>> doneAccounts = new ArrayList<>();
        for (Map<String, CellStyleAndContent> contentMap : all) {
            CellStyleAndContent content = contentMap.get("dkzh");
            CellStyleAndContent style = contentMap.get("行号");
            String dkzh = content.getContent().toString();

            short color = style.getCellStyle().getFillForegroundColor();
            log.debug("贷款账号: {}  颜色是: {}  对应颜色名: {}  文件: {} ", dkzh, color, IndexedColors.fromInt(color).name(), dkzhsKey.get(dkzh));
            Integer integer = colorMap.get((int) color);
            integer = integer == null ? 1 : integer + 1;
            colorMap.put((int) color, integer);
            if (color == 40 || color == 30) {
                Map<String, Object> data = new HashMap<>();
                data.put("dkzh", dkzh);
                data.put("colorValue", color + "");
                data.put("color", IndexedColors.fromInt(color).name());
                data.put("time", dateTimeFormatter.format(LocalDateTime.now()));
                data.put("isFlag", "是");
                data.put("zhMapFile", dkzhsKey.get(dkzh));
                doneAccounts.add(data);
            } else {
                Map<String, Object> notDone = new HashMap<>();
                notDone.put("dkzh", dkzh);
                notDone.put("fse", contentMap.get("发生额差额合计").getContent());
                notDone.put("bjje", contentMap.get("本金差额合计").getContent());
                notDone.put("lxje", contentMap.get("利息差额合计").getContent());
                notDone.put("bz", contentMap.get("备注").getContent());
                notDone.put("file", dkzhsKey.get(dkzh));
                notDoneList.add(notDone);
            }
            if (isInMoreOnePrePayment(dkzh)) {
                Map<String, Object> notDone = new HashMap<>();
                notDone.put("dkzh", dkzh);
                notDone.put("fse", contentMap.get("发生额差额合计").getContent());
                notDone.put("bjje", contentMap.get("本金差额合计").getContent());
                notDone.put("lxje", contentMap.get("利息差额合计").getContent());
                notDone.put("bz", contentMap.get("备注").getContent());
                notDone.put("file", dkzhsKey.get(dkzh));
                moreOnePrePaymentMessage.add(notDone);
            }
        }
        log.error("不同颜色对应的记录数量: {}", colorMap);
        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "贷款账号");
        keyMap.put("colorValue", "颜色值");
        keyMap.put("color", "Excel对应颜色");
        keyMap.put("isFlag", "是否已标记");
        keyMap.put("time", "时间");
        keyMap.put("zhMapFile", "账号对应文件");
        if (isWrite) {
            ExcelUtil.writeToExcelByAll(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, null, keyMap, doneAccounts);
        }
    }

    public static void main(String[] args) {
        new InitTakedAccounts().onlyListMessage();
        //new InitTakedAccounts().init();
    }
}
